package cn.zcn.zraft.rpc;

import java.io.Serializable;

/**
 * @author zicung
 */
public class RpcCommand implements Serializable {
    public static final int HEARTBEAT = 0;
    public static final int APPEND_ENTRY = 1;
    public static final int REQUEST_VOTE = 2;

    private int code;
    private Object body;

    public int getCode() {
        return code;
    }

    public Object getBody() {
        return body;
    }

    public static RpcCommand create(int code, Object obj) {
        RpcCommand rpcCommand = new RpcCommand();
        rpcCommand.code = code;
        rpcCommand.body = obj;
        return rpcCommand;
    }
}
