package cn.zcn.zraft.log;

import cn.zcn.zraft.protocol.LogEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zicung
 */
public class MemoryLogStorage implements LogStorage {

    private Term term;
    private long committed;
    private long lastLogTerm;
    private long lastLogIndex;
    private final Map<Long, LogEntry> logEntries = new HashMap<>();

    @Override
    public synchronized void start() {
        term = new Term(0, null);
    }

    @Override
    public Term getTerm() {
        return term;
    }

    @Override
    public void saveTerm(Term term) {
        this.term = term;
    }

    @Override
    public long getLastLogIndex() {
        return lastLogIndex;
    }

    @Override
    public long getLastLogTerm() {
        return lastLogTerm;
    }

    @Override
    public void append(LogEntry log) {
        logEntries.put(log.getIndex(), log);
        this.lastLogTerm = log.getTerm();
        this.lastLogIndex = log.getIndex();
        this.committed = log.getIndex();
    }

    @Override
    public LogEntry getLogEntry(long index) {
        return logEntries.get(index);
    }

    @Override
    public void commit(long term, long index) {

    }

    @Override
    public synchronized void stop() {
        term = null;
        logEntries.clear();
    }
}
