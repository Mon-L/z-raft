package cn.zcn.zraft.log;

import cn.zcn.zraft.protocol.LogEntry;

/**
 * @author zicung
 */
public interface LogStorage {
    void start();

    void append(LogEntry logEntry);

    void getLogEntry(long index);

    void commit(long term, long index);

    void stop();
}
