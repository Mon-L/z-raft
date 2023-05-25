package cn.zcn.zraft.role;

import cn.zcn.zraft.Config;
import cn.zcn.zraft.PeerId;
import cn.zcn.zraft.RaftProtocolService;
import cn.zcn.zraft.protocol.*;
import cn.zcn.zraft.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author zicung
 */
public class LeaderElector {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderElector.class);

    private static final int ELECTION_TIMEOUT_RANDOM_FIXED = 150; // ms

    private final Config config;
    private final NodeState nodeState;
    private final RaftProtocolService raftProtocolService;
    private volatile boolean running = false;
    private volatile long lastLeaderHeartbeat = -1;
    private volatile long nextRequestVoteTime = -1;
    private final Random random = new Random();

    public LeaderElector(Config config, NodeState nodeState, RaftProtocolService raftProtocolService) {
        this.config = config;
        this.nodeState = nodeState;
        this.raftProtocolService = raftProtocolService;
    }

    public synchronized void start() {
        this.running = true;
        new RoleMaintainTask().start();
    }

    private void maintainRole() throws Exception {
        switch (nodeState.getRole()) {
            case LEADER:
                maintainLeader();
                break;
            case FOLLOWER:
                maintainFollower();
                break;
            case CANDIDATE:
                maintainCandidate();
                break;
        }
    }

    public CompletableFuture<HeartbeatResponse> handleHeartbeat(HeartbeatRequest heartbeatRequest) {
        if (heartbeatRequest.getTerm() < nodeState.getTerm()) {
            // ignored expired term
            return CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.EXPIRED_TERM));
        }

        if (!nodeState.isPeer(heartbeatRequest.getLeaderId())) {
            LOGGER.info("Illegal cluster peer. PeerId:{}", heartbeatRequest.getLeaderId());

            return CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.ILLEGAL_PEER));
        }

        if (nodeState.getLeaderId() != null && !nodeState.getLeaderId().equals(heartbeatRequest.getLeaderId())) {
            LOGGER.info("Unexpected leader heartbeat request. CurrLeader:{}, RequestLeader:{}", nodeState.getLeaderId(),
                    heartbeatRequest.getLeaderId());

            return CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.ILLEGAL_PEER));
        }

        synchronized (nodeState) {
            if (heartbeatRequest.getTerm() < nodeState.getTerm()) {
                // ignored expired term
                return CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.EXPIRED_TERM));
            } else if (heartbeatRequest.getTerm() == nodeState.getTerm()) {
                if (nodeState.getLeaderId() == null) {
                    // first leader heartbeat
                    this.lastLeaderHeartbeat = TimeUtils.now();
                    nodeState.changeToFollower(heartbeatRequest.getTerm(), heartbeatRequest.getLeaderId());

                    return CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.SUCCESS));
                } else if (heartbeatRequest.getLeaderId().equals(nodeState.getLeaderId())) {
                    // leader heartbeat
                    this.lastLeaderHeartbeat = TimeUtils.now();
                    return CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.SUCCESS));
                } else {
                    // multi leader
                    return CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.MULTI_LEADER));
                }
            } else {
                // apply new term, change to follower
                nodeState.changeToFollower(heartbeatRequest.getTerm(), null);
                return CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.SUCCESS));
            }
        }
    }

    public CompletableFuture<RequestVoteResponse> handleRequestVote(RequestVoteRequest requestVoteRequest) {
        if (!nodeState.isPeer(requestVoteRequest.getCandidateId())) {
            LOGGER.info("Illegal cluster peer. PeerId:{}", requestVoteRequest.getCandidateId());
            return CompletableFuture.completedFuture(
                    new RequestVoteResponse(nodeState.getTerm(), ResponseCode.VOTE_REJECT));
        }

        synchronized (nodeState) {
            if (nodeState.getTerm() < requestVoteRequest.getTerm()) {
                // greater term, apply greater term and change to follower.
                LOGGER.info("Apply new term. PeerId:{}, CurTerm:{}, newTerm:{}", nodeState.getSelf().getId(),
                        nodeState.getTerm(), requestVoteRequest.getTerm());

                lastLeaderHeartbeat = TimeUtils.now();
                nodeState.changeToFollower(requestVoteRequest.getTerm(), null);
            }

            //check log
            if (nodeState.getLogLastTerm() > requestVoteRequest.getLastLogTerm() ||
                    (nodeState.getLogLastTerm() == requestVoteRequest.getLastLogTerm() &&
                            nodeState.getLogLastIndex() > requestVoteRequest.getLastLogIndex())) {
                return CompletableFuture.completedFuture(
                        new RequestVoteResponse(nodeState.getTerm(), ResponseCode.VOTE_REJECT));
            }

            //check term
            if (nodeState.getTerm() > requestVoteRequest.getTerm()) {
                // expired term
                return CompletableFuture.completedFuture(
                        new RequestVoteResponse(nodeState.getTerm(), ResponseCode.VOTE_REJECT));
            } else {
                String leader = nodeState.getLeaderId();
                String voteFor = nodeState.getVoteFor();

                if (leader != null) {
                    if (leader.equals(requestVoteRequest.getCandidateId())) {
                        //vote repeatedly
                        return CompletableFuture.completedFuture(
                                new RequestVoteResponse(requestVoteRequest.getTerm(), ResponseCode.VOTE_GRANTED));
                    } else {
                        //already has leader
                        return CompletableFuture.completedFuture(
                                new RequestVoteResponse(requestVoteRequest.getTerm(), ResponseCode.VOTE_REJECT));
                    }
                }

                if (voteFor != null) {
                    if (!voteFor.equals(requestVoteRequest.getCandidateId())) {
                        //already voted
                        return CompletableFuture.completedFuture(
                                new RequestVoteResponse(requestVoteRequest.getTerm(), ResponseCode.VOTE_REJECT));
                    } else {
                        //vote repeatedly
                        return CompletableFuture.completedFuture(
                                new RequestVoteResponse(requestVoteRequest.getTerm(), ResponseCode.VOTE_GRANTED));
                    }
                }

                nodeState.setVoteFor(requestVoteRequest.getCandidateId());
                return CompletableFuture.completedFuture(
                        new RequestVoteResponse(requestVoteRequest.getTerm(), ResponseCode.VOTE_GRANTED));
            }
        }
    }

    private void maintainLeader() throws Exception {
        if (nodeState.getRole() == Role.LEADER &&
                TimeUtils.reachIntervalTime(lastLeaderHeartbeat, config.getHeartbeatIntervalMs() - 50)) {
            long term;
            String leaderId;
            synchronized (nodeState) {
                if (nodeState.getRole() != Role.LEADER) {
                    return;
                }

                term = nodeState.getTerm();
                leaderId = nodeState.getSelf().getId();
            }

            sendHeartbeats(term, leaderId);
        }
    }

    private void maintainFollower() {
        if (lastLeaderHeartbeat + config.getHeartbeatIntervalMs() < TimeUtils.now()) {
            //election timeout
            LOGGER.info("Leader heartbeat timeout. LeaderId:{}, NodeId:{}, LastLeaderHeartbeat:{}, HeartbeatIntervalMs:{}",
                    nodeState.getLeaderId(), nodeState.getSelf().getId(), lastLeaderHeartbeat, config.getHeartbeatIntervalMs());
            nodeState.changeToCandidate();
        }
    }

    @SuppressWarnings("unchecked")
    private void maintainCandidate() throws InterruptedException {
        if (nodeState.getRole() != Role.CANDIDATE || TimeUtils.now() < this.nextRequestVoteTime) {
            return;
        }

        long term, logLastTerm, logLastIndex;
        synchronized (nodeState) {
            if (nodeState.getRole() != Role.CANDIDATE) {
                return;
            }

            term = nodeState.nextTerm();
            logLastTerm = nodeState.getLogLastTerm();
            logLastIndex = nodeState.getLogLastIndex();
            nodeState.setVoteFor(nodeState.getSelf().getId());
        }

        this.nextRequestVoteTime = getNextRequestVoteTime();

        long startTime = TimeUtils.now();

        RequestVoteRequest requestVoteRequest = new RequestVoteRequest();
        requestVoteRequest.setCandidateId(nodeState.getSelf().getId());
        requestVoteRequest.setTerm(term);
        requestVoteRequest.setLastLogTerm(logLastTerm);
        requestVoteRequest.setLastLogIndex(logLastIndex);

        List<PeerId> peerIds = nodeState.getPeerIds();
        CompletableFuture<RequestVoteResponse>[] futures =
                (CompletableFuture<RequestVoteResponse>[]) new CompletableFuture<?>[peerIds.size()];

        for (int i = 0; i < peerIds.size(); i++) {
            PeerId peerId = peerIds.get(i);
            if (peerId.equals(nodeState.getSelf())) {
                futures[i] = CompletableFuture.completedFuture(new RequestVoteResponse(nodeState.getTerm(), ResponseCode.VOTE_GRANTED));
                continue;
            }

            futures[i] = raftProtocolService.sendRequestVote(peerIds.get(i), requestVoteRequest);
        }

        try {
            CompletableFuture.allOf(futures).get(config.getElectionTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException | TimeoutException ignored) {
        }

        int allNum = peerIds.size(), grantedNum = 0, rejectedNum = 0, timeoutNum = 0, errorNum = 0;
        long maxTerm = term;

        for (int i = 0; i < allNum; i++) {
            PeerId peerId = peerIds.get(i);

            CompletableFuture<RequestVoteResponse> future = futures[i];
            boolean isCancelled = !future.isDone() && future.cancel(true);
            if (isCancelled) {
                timeoutNum++;
                continue;
            }

            RequestVoteResponse response;
            try {
                response = future.get();
            } catch (ExecutionException e) {
                errorNum++;
                LOGGER.error("Failed to get request vote response. RemotingPeerId:{}", peerId, e);
                continue;
            }

            switch (response.getCode()) {
                case VOTE_GRANTED:
                    grantedNum++;
                    break;
                case VOTE_REJECT:
                    rejectedNum++;
                    maxTerm = Math.max(maxTerm, response.getTerm());
                    break;
            }
        }

        long endTime = TimeUtils.now(), costTime = TimeUtils.elapsed(startTime, endTime);

        Role nextRole = Role.CANDIDATE;
        if (maxTerm > term) {
            nextRole = Role.FOLLOWER;
            nodeState.changeToFollower(maxTerm, null);
        } else if (nodeState.isQuorum(grantedNum)) {
            nextRole = Role.LEADER;
            nodeState.changeToLeader();
        }

        LOGGER.info("Election finish. Result:{}, NodeId:{}, StartTime:{}, EndTime:{}, CostTime:{}. " +
                        "Term: {}, KnownMaxTerm:{}. " +
                        "PeersCount:{}, GrantedPeers:{}, RejectedPeers:{}, TimeoutPeers:{}, ErrorPeers:{}.",
                nextRole.name(), nodeState.getSelf().getId(), startTime, endTime, costTime,
                term, maxTerm,
                allNum, grantedNum, rejectedNum, timeoutNum, errorNum);
    }

    @SuppressWarnings("unchecked")
    private void sendHeartbeats(long term, String leaderId) throws InterruptedException {
        List<PeerId> peerIds = nodeState.getPeerIds();
        CompletableFuture<HeartbeatResponse>[] futures =
                (CompletableFuture<HeartbeatResponse>[]) new CompletableFuture<?>[peerIds.size()];

        this.lastLeaderHeartbeat = TimeUtils.now();
        long startTime = TimeUtils.now();
        HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
        heartbeatRequest.setTerm(term);
        heartbeatRequest.setLeaderId(leaderId);

        for (int i = 0; i < peerIds.size(); i++) {
            PeerId peerId = peerIds.get(i);
            if (peerId.equals(nodeState.getSelf())) {
                futures[i] = CompletableFuture.completedFuture(new HeartbeatResponse(nodeState.getTerm(), ResponseCode.SUCCESS));
                continue;
            }

            futures[i] = raftProtocolService.sendHeartbeat(peerId, heartbeatRequest);
        }

        try {
            CompletableFuture.allOf(futures).get(config.getHeartbeatIntervalMs() - 30, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | TimeoutException ignored) {
        }

        int successNum = 0, timeoutNum = 0, errorNum = 0;
        long maxTerm = term;
        boolean haveMultiLeader = false;
        for (int i = 0; i < peerIds.size(); i++) {
            PeerId peerId = peerIds.get(i);
            CompletableFuture<HeartbeatResponse> future = futures[i];
            boolean isCancelled = !future.isDone() && future.cancel(true);
            if (isCancelled) {
                timeoutNum++;
                continue;
            }

            HeartbeatResponse response = null;
            try {
                response = future.get();
            } catch (ExecutionException e) {
                errorNum++;
                LOGGER.error("Failed to get heartbeat response. RemotingPeerId:{}", peerId, e);
            }

            if (response != null) {
                switch (response.getCode()) {
                    case SUCCESS:
                        successNum++;
                        break;
                    case EXPIRED_TERM:
                        maxTerm = Math.max(maxTerm, response.getTerm());
                        break;
                    case MULTI_LEADER:
                        haveMultiLeader = true;
                        break;
                }
            }
        }

        long endTime = TimeUtils.now(), costTime = TimeUtils.elapsed(startTime, endTime);
        Role nextRole;
        if (maxTerm > term || haveMultiLeader) {
            nextRole = Role.FOLLOWER;
            nodeState.changeToFollower(maxTerm, null);
        } else {
            if (nodeState.isQuorum(successNum)) {
                nextRole = Role.LEADER;
            } else {
                nextRole = Role.FOLLOWER;
                nodeState.changeToFollower(maxTerm, null);
            }
        }

        LOGGER.info("Leader heartbeat finish. Result:{}, NodeId:{}, StartTime: {}, EndTime:{}, CostTime:{}. " +
                        "Term:{}, KnownMaxTerm:{}. " +
                        "PeersCount:{}, SuccessPeers:{}, TimeoutPeers:{}, ErrorPeers:{}.",
                nextRole.name(), nodeState.getSelf().getId(), startTime, endTime, costTime,
                term, maxTerm,
                peerIds.size(), successNum, timeoutNum, errorNum);
    }

    private long getNextRequestVoteTime() {
        return TimeUtils.now() + config.getElectionTimeoutMs() + random.nextInt(ELECTION_TIMEOUT_RANDOM_FIXED);
    }

    public synchronized void stop() {
        this.running = false;
    }

    private class RoleMaintainTask extends Thread {

        @Override
        public void run() {
            while (running) {
                try {
                    maintainRole();
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (Throwable t) {
                    LOGGER.error("Error occur when maintain role.", t);
                }
            }
        }
    }
}
