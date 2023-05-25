package cn.zcn.zraft.role;

import cn.zcn.zraft.Config;
import cn.zcn.zraft.PeerId;
import cn.zcn.zraft.log.LogStorage;
import cn.zcn.zraft.log.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zicung
 */
public class NodeState {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeState.class);

    private volatile long term;
    private volatile PeerId self;
    private volatile String leaderId;
    private volatile String voteFor;
    private volatile Role role;
    private final Config config;
    private final LogStorage logStorage;
    public Map<String, PeerId> peerIds = new HashMap<>();

    public NodeState(Config config, LogStorage logStorage) {
        this.config = config;
        this.logStorage = logStorage;
    }

    public synchronized void init() {
        this.term = logStorage.getTerm().getId();
        this.voteFor = logStorage.getTerm().getVoteFor();
        this.role = Role.FOLLOWER;

        for (String server : config.getServers()) {
            PeerId peerId = PeerId.parse(server);
            peerIds.put(peerId.getId(), peerId);

            if (peerId.getId().equals(config.getId())) {
                self = peerId;
            }
        }
    }

    public synchronized void changeToLeader() {
        LOGGER.info("Change to leader.Term:{}, NodeId:{}", term, self.getId());
        this.role = Role.LEADER;
        this.leaderId = self.getId();
    }

    public synchronized void changeToFollower(long term, String leaderId) {
        if (term >= this.term) {
            LOGGER.info("Change to follower. Term:{}, NodeId:{}, LeaderId:{}", term, self.getId(), leaderId);
            this.role = Role.FOLLOWER;
            this.leaderId = leaderId;
            updateTerm(term);
        }
    }

    public synchronized void changeToCandidate() {
        LOGGER.info("Change to candidate. Term:{}, NodeId:{}", term, self.getId());
        this.role = Role.CANDIDATE;
        this.leaderId = null;
    }

    public synchronized long nextTerm() {
        updateTerm(this.term + 1);
        return this.term;
    }

    public synchronized void updateTerm(long newTerm) {
        if (newTerm <= term) {
            return;
        }
        this.term = newTerm;
        this.voteFor = null;
        persistTerm();
    }

    private synchronized void persistTerm() {
        Term newTerm = new Term(this.term, this.voteFor);
        logStorage.saveTerm(newTerm);
    }

    public synchronized void setVoteFor(String candidateId) {
        this.voteFor = candidateId;
        persistTerm();
    }

    public String getVoteFor() {
        return this.voteFor;
    }

    public long getTerm() {
        return this.term;
    }

    public long getLogLastTerm() {
        return logStorage.getLastLogTerm();
    }

    public long getLogLastIndex() {
        return logStorage.getLastLogIndex();
    }

    public Role getRole() {
        return this.role;
    }

    public PeerId getSelf() {
        return self;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public List<PeerId> getPeerIds() {
        return new ArrayList<>(peerIds.values());
    }

    public boolean isPeer(String peerId) {
        return peerIds.containsKey(peerId);
    }

    public boolean isQuorum(int num) {
        return num >= ((peerIds.size() / 2) + 1);
    }

    public boolean isLeader() {
        return this.role == Role.LEADER;
    }

    public boolean isCandidate() {
        return this.role == Role.CANDIDATE;
    }

    public boolean isFollower() {
        return this.role == Role.FOLLOWER;
    }
}
