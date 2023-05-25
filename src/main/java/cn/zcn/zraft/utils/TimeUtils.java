package cn.zcn.zraft.utils;

/**
 * @author zicung
 */
public class TimeUtils {

    public static boolean reachIntervalTime(long startMs, long intervalMs) {
        return System.currentTimeMillis() - startMs >= intervalMs;
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long elapsed(long startMs, long endMs) {
        return endMs - startMs;
    }
}
