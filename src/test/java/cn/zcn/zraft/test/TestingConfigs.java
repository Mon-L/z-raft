package cn.zcn.zraft.test;

import cn.zcn.zraft.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zicung
 */
public class TestingConfigs {
    public static final Config[] CONFIGS = new Config[3];

    static {
        List<String> servers = new ArrayList<>();

        int portStart = 8081;
        for (int i = 0; i < CONFIGS.length; i++) {
            cn.zcn.zraft.Config config = new Config();
            config.setId(i + "");
            config.setLogStorageType("memory");

            int port = portStart + i;
            servers.add("127.0.0.1:" + port + ":" + i);
            config.setServers(servers);
            CONFIGS[i] = config;
        }
    }
}
