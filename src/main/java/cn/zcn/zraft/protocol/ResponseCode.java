package cn.zcn.zraft.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zicung
 */
public enum ResponseCode {
    UNKNOWN(404),
    SUCCESS(200);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    private final static Map<Integer, ResponseCode> CODES = new HashMap<>();

    static {
        for (ResponseCode responseCode : ResponseCode.values()) {
            CODES.put(responseCode.code, responseCode);
        }
    }

    public static ResponseCode valueOf(int code) {
        ResponseCode rsp = CODES.get(code);
        if (rsp != null) {
            return rsp;
        } else {
            return UNKNOWN;
        }
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return String.format("[code=%d,name=%s]", code, name());
    }
}
