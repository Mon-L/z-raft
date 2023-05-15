package cn.zcn.zraft.role;

import java.util.List;

/**
 * @author zicung
 */
public class RaftState {
    public enum Role {
        LEADER,
        FOLLOWER,
        CANDIDATE;
    }

    private long term;
    private Role role;
    private List<RolePolicy> rolePolicies;

    public void start() {

    }

    public synchronized void changeToLeader() {

    }

    public synchronized void changeToFollower() {

    }

    private synchronized void changeToCandidate() {

    }

    public void stop() {

    }

    public long getTerm() {
        return term;
    }

    public Role getRole() {
        return role;
    }
}
