package cn.zcn.zraft.protocol;

/**
 * @author zicung
 */
public class RequestVoteResponse {
    private long term;
    private ResponseCode code;

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
