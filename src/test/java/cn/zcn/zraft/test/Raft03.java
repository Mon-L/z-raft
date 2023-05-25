package cn.zcn.zraft.test;

import cn.zcn.zraft.RaftNode;

/**
 * @author zicung
 */
public class Raft03 {

    public static void main(String[] args) {
        RaftNode raftNode = new RaftNode(TestingConfigs.CONFIGS[2]);
        raftNode.start();
    }
}
