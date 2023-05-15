package cn.zcn.zraft.protocol;

/**
 * @author zicung
 */
public class AppendEntryRequest {
    private String leaderId;
    private long term;
    private byte[] content;

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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
