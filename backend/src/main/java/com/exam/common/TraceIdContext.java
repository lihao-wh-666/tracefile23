package com.exam.common;

public class TraceIdContext {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }

    public static String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
