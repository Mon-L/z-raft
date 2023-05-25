package cn.zcn.zraft.test;

import cn.zcn.zraft.RaftNode;

/**
 * @author zicung
 */
public class Raft01 {

    public static void main(String[] args) {
        RaftNode raftNode = new RaftNode(TestingConfigs.CONFIGS[0]);
        raftNode.start();
    }
}
