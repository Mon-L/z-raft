package cn.zcn.zraft;

import cn.zcn.zraft.protocol.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author zicung
 */
public interface RaftProtocolService {
    CompletableFuture<AppendEntryResponse> handleAppendEntry(AppendEntryRequest appendEntryRequest);

    CompletableFuture<RequestVoteResponse> handleRequestVote(RequestVoteRequest requestVoteRequest);

    CompletableFuture<HeartbeatResponse> handleHeartbeat(HeartbeatRequest heartbeatRequest);

    CompletableFuture<RequestVoteResponse> sendRequestVote(PeerId peerId, RequestVoteRequest requestVoteRequest);

    CompletableFuture<HeartbeatResponse> sendHeartbeat(PeerId peerId, HeartbeatRequest heartbeatRequest);
}
