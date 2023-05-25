package cn.zcn.zraft;

import java.util.Objects;

/**
 * @author zicung
 */
public class PeerId {

    private String id;
    private String ip;
    private int port;

    private PeerId() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeerId)) return false;

        PeerId peerId = (PeerId) o;

        if (port != peerId.port) return false;
        if (!Objects.equals(id, peerId.id)) return false;
        return Objects.equals(ip, peerId.ip);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    /**
     * ip:port:id
     */
    public static PeerId parse(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        String[] values = str.split(":");
        if (values.length < 3) {
            return null;
        }

        PeerId peerId = new PeerId();
        peerId.ip = values[0];
        peerId.port = Integer.parseInt(values[1]);
        peerId.id = values[2];
        return peerId;
    }

    @Override
    public String toString() {
        return ip + ":" + port + ":" + id;
    }
}
