package cn.zcn.zraft.log;

import cn.zcn.zraft.protocol.LogEntry;

/**
 * @author zicung
 */
public interface LogStorage {
    void start();

    Term getTerm();

    void saveTerm(Term term);

    long getLastLogIndex();

    long getLastLogTerm();

    void append(LogEntry logEntry);

    LogEntry getLogEntry(long index);

    void commit(long term, long index);

    void stop();
}
