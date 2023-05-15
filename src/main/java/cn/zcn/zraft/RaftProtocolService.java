package cn.zcn.zraft;

import cn.zcn.zraft.protocol.AppendEntryRequest;
import cn.zcn.zraft.protocol.AppendEntryResponse;
import cn.zcn.zraft.protocol.RequestVoteRequest;
import cn.zcn.zraft.protocol.RequestVoteResponse;

/**
 * @author zicung
 */
public interface RaftProtocolService {
    AppendEntryResponse handleAppendEntry(AppendEntryRequest appendEntryRequest);

    RequestVoteResponse handleRequestVote(RequestVoteRequest request);

    RequestVoteResponse sendRequestVote(RequestVoteRequest request);
}
