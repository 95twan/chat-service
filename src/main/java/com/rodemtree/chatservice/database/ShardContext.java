package com.rodemtree.chatservice.database;

public class ShardContext {

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    private ShardContext() {
    }

    public static Long getChannelId() {
        return threadLocal.get();
    }

    public static void setChannelId(Long channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("Channel ID cannot be null");
        }
        threadLocal.set(channelId);
    }

    public static void clear() {
        threadLocal.remove();
    }

    public static final class ShardContextScope implements AutoCloseable {
        public ShardContextScope(Long channelId) {
            setChannelId(channelId);
        }

        @Override
        public void close() {
            clear();
        }
    }
}
