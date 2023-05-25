package cn.zcn.zraft.rpc;

import cn.zcn.zraft.PeerId;
import cn.zcn.zraft.RaftNode;
import cn.zcn.zraft.protocol.*;
import cn.zcn.zraft.role.NodeState;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author zicung
 */
public class RaftRpcService extends AsyncUserProcessor<RpcCommand> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftRpcService.class);

    private final RaftNode raftNode;
    private final NodeState nodeState;

    private RpcServer rpcServer;
    private RpcClient rpcClient;

    public RaftRpcService(RaftNode raftNode, NodeState nodeState) {
        this.raftNode = raftNode;
        this.nodeState = nodeState;
    }

    public synchronized void start() {
        PeerId self = this.nodeState.getSelf();
        this.rpcServer = new RpcServer(self.getIp(), self.getPort(), false);
        this.rpcServer.registerUserProcessor(this);
        this.rpcServer.start();

        this.rpcClient = new RpcClient();
        rpcClient.init();
    }

    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, RpcCommand request) {
        int code = request.getCode();
        switch (code) {
            case RpcCommand.HEARTBEAT:
                handleHeartbeat(asyncContext, request);
                break;
            case RpcCommand.APPEND_ENTRY:
                handleAppendEntry(asyncContext, request);
                break;
            case RpcCommand.REQUEST_VOTE:
                handleRequestVote(asyncContext, request);
                break;
            default:
                LOGGER.warn("Unknown command code:" + code);
                break;
        }
    }

    private void handleHeartbeat(AsyncContext asyncContext, RpcCommand request) {
        HeartbeatRequest heartbeatRequest = (HeartbeatRequest) request.getBody();
        raftNode.handleHeartbeat(heartbeatRequest).whenComplete((ret, t) -> {
            RpcCommand response = RpcCommand.create(RpcCommand.HEARTBEAT, ret);
            asyncContext.sendResponse(response);
        });
    }

    private void handleAppendEntry(AsyncContext asyncContext, RpcCommand request) {
        AppendEntryRequest appendEntryRequest = (AppendEntryRequest) request.getBody();
        raftNode.handleAppendEntry(appendEntryRequest).whenComplete((ret, t) -> {
            RpcCommand response = RpcCommand.create(RpcCommand.APPEND_ENTRY, ret);
            asyncContext.sendResponse(response);
        });
    }

    private void handleRequestVote(AsyncContext asyncContext, RpcCommand request) {
        RequestVoteRequest requestVoteRequest = (RequestVoteRequest) request.getBody();
        raftNode.handleRequestVote(requestVoteRequest).whenComplete((ret, t) -> {
            RpcCommand response = RpcCommand.create(RpcCommand.REQUEST_VOTE, ret);
            asyncContext.sendResponse(response);
        });
    }

    public CompletableFuture<RequestVoteResponse> sendRequestVote(PeerId peerId, RequestVoteRequest requestVoteRequest) {
        CompletableFuture<RequestVoteResponse> future = new CompletableFuture<>();
        try {
            rpcClient.invokeWithCallback(
                    createUrl(peerId),
                    RpcCommand.create(RpcCommand.REQUEST_VOTE, requestVoteRequest),
                    new InvokeCallback() {
                        @Override
                        public void onResponse(Object o) {
                            RpcCommand rpcCommand = (RpcCommand) o;
                            RequestVoteResponse response = (RequestVoteResponse) rpcCommand.getBody();
                            future.complete(response);
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            future.completeExceptionally(throwable);
                        }

                        @Override
                        public Executor getExecutor() {
                            return null;
                        }
                    },
                    1500);
        } catch (RemotingException | InterruptedException e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<HeartbeatResponse> sendHeartbeat(PeerId peerId, HeartbeatRequest heartbeatRequest) {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();
        try {
            rpcClient.invokeWithCallback(
                    createUrl(peerId),
                    RpcCommand.create(RpcCommand.HEARTBEAT, heartbeatRequest),
                    new InvokeCallback() {
                        @Override
                        public void onResponse(Object o) {
                            RpcCommand rpcCommand = (RpcCommand) o;
                            HeartbeatResponse response = (HeartbeatResponse) rpcCommand.getBody();
                            future.complete(response);
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            future.completeExceptionally(throwable);
                        }

                        @Override
                        public Executor getExecutor() {
                            return null;
                        }
                    },
                    1500);
        } catch (RemotingException | InterruptedException e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private Url createUrl(PeerId peerId) {
        Url url = new Url(peerId.getIp(), peerId.getPort());
        url.setConnNum(1);
        url.setProtocol(RpcProtocolV2.PROTOCOL_VERSION_1);
        return url;
    }

    @Override
    public String interest() {
        return RpcCommand.class.getName();
    }

    public synchronized void stop() {
        this.rpcServer.stop();
        this.rpcClient.shutdown();
    }
}
