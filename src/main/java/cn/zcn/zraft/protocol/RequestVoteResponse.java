package cn.zcn.zraft.protocol;

import java.io.Serializable;

/**
 * @author zicung
 */
public class RequestVoteResponse implements Serializable {
    private long term;
    private ResponseCode code;

    public RequestVoteResponse(long term, ResponseCode code) {
        this.term = term;
        this.code = code;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }
}
