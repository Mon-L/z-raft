package cn.zcn.zraft.protocol;

import java.io.Serializable;

/**
 * @author zicung
 */
public class HeartbeatRequest implements Serializable {

    private long term;
    private String leaderId;

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }
}
