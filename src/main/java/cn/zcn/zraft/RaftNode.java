package cn.zcn.zraft;

import cn.zcn.zraft.log.LogStorage;
import cn.zcn.zraft.protocol.AppendEntryRequest;
import cn.zcn.zraft.protocol.AppendEntryResponse;
import cn.zcn.zraft.protocol.RequestVoteRequest;
import cn.zcn.zraft.protocol.RequestVoteResponse;
import cn.zcn.zraft.role.RaftState;

/**
 * @author zicung
 */
public class RaftNode implements RaftProtocolService {

    private RaftState raftState;

    public void start() {

    }

    public void stop() {

    }

    @Override
    public AppendEntryResponse handleAppendEntry(AppendEntryRequest appendEntryRequest) {
        return null;
    }

    @Override
    public RequestVoteResponse handleRequestVote(RequestVoteRequest request) {
        return null;
    }

    @Override
    public RequestVoteResponse sendRequestVote(RequestVoteRequest request) {
        return null;
    }
}
