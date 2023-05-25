package cn.zcn.zraft.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zicung
 */
public enum ResponseCode {
    UNKNOWN(404),
    SUCCESS(200),
    ILLEGAL_PEER(100),
    EXPIRED_TERM(101),
    MULTI_LEADER(102),
    NETWORK_ERROR(103),

    VOTE_GRANTED(201),
    VOTE_REJECT(202)
    ;

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
