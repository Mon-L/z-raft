package cn.zcn.zraft;

import java.util.Collections;
import java.util.List;

/**
 * @author zicung
 */
public class Config {

    private String id;
    private List<String> servers = Collections.emptyList();
    private long heartbeatIntervalMs = 3000;
    private long electionTimeoutMs = 500;
    private String logStorageType = "memory";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    public long getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    public void setHeartbeatIntervalMs(long heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }

    public long getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    public void setElectionTimeoutMs(long electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    public String getLogStorageType() {
        return logStorageType;
    }

    public void setLogStorageType(String logStorageType) {
        this.logStorageType = logStorageType;
    }
}
