package cn.zcn.zraft.log;

/**
 * @author zicung
 */
public class Term {
    private long id;
    private String voteFor;

    public Term(long id, String voteFor) {
        this.id = id;
        this.voteFor = voteFor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVoteFor() {
        return voteFor;
    }

    public void setVoteFor(String voteFor) {
        this.voteFor = voteFor;
    }
}
