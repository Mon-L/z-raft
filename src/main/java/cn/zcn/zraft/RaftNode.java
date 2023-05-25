package cn.zcn.zraft;

import cn.zcn.zraft.log.LogStorage;
import cn.zcn.zraft.log.MemoryLogStorage;
import cn.zcn.zraft.protocol.*;
import cn.zcn.zraft.role.LeaderElector;
import cn.zcn.zraft.role.NodeState;
import cn.zcn.zraft.rpc.RaftRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;


/**
 * @author zicung
 */
public class RaftNode implements RaftProtocolService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftNode.class);

    private volatile boolean running = false;
    private final Config config;
    private NodeState nodeState;
    private LogStorage logStorage;
    private LeaderElector leaderElector;
    private RaftRpcService raftRpcService;

    public RaftNode(Config config) {
        this.config = config;
    }

    public synchronized void start() {
        this.running = true;

        if (config.getLogStorageType().equals("memory")) {
            this.logStorage = new MemoryLogStorage();
        }
        this.logStorage.start();

        this.nodeState = new NodeState(config, logStorage);
        this.nodeState.init();

        this.raftRpcService = new RaftRpcService(this, nodeState);
        this.raftRpcService.start();

        this.leaderElector = new LeaderElector(config, nodeState, this);
        this.leaderElector.start();

        LOGGER.info("Raft node started. NodeId:{}", nodeState.getSelf().getId());
    }

    public synchronized void stop() {
        if (!this.running) {
            return;
        }

        this.raftRpcService.stop();
        this.leaderElector.stop();
        this.logStorage.stop();

        this.running = false;
        LOGGER.info("Raft node stopped. NodeId:{}", nodeState.getSelf().getId());
    }

    public NodeState getNodeState() {
        return nodeState;
    }

    @Override
    public CompletableFuture<AppendEntryResponse> handleAppendEntry(AppendEntryRequest appendEntryRequest) {
        return null;
    }

    @Override
    public CompletableFuture<RequestVoteResponse> handleRequestVote(RequestVoteRequest requestVoteRequest) {
        return leaderElector.handleRequestVote(requestVoteRequest);
    }

    @Override
    public CompletableFuture<HeartbeatResponse> handleHeartbeat(HeartbeatRequest heartbeatRequest) {
        return leaderElector.handleHeartbeat(heartbeatRequest);
    }

    @Override
    public CompletableFuture<RequestVoteResponse> sendRequestVote(PeerId peerId, RequestVoteRequest requestVoteRequest) {
        return raftRpcService.sendRequestVote(peerId, requestVoteRequest);
    }

    @Override
    public CompletableFuture<HeartbeatResponse> sendHeartbeat(PeerId peerId, HeartbeatRequest heartbeatRequest) {
        return raftRpcService.sendHeartbeat(peerId, heartbeatRequest);
    }
}
