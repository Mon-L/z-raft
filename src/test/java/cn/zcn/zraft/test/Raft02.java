package cn.zcn.zraft.test;

import cn.zcn.zraft.RaftNode;

/**
 * @author zicung
 */
public class Raft02 {

    public static void main(String[] args) {
        RaftNode raftNode = new RaftNode(TestingConfigs.CONFIGS[1]);
        raftNode.start();
    }
}
