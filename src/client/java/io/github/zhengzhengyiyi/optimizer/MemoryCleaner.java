package io.github.zhengzhengyiyi.optimizer;

import net.minecraft.client.MinecraftClient;

public class MemoryCleaner {
    private static long lastCleanupTime = 0;
    private static final long CLEANUP_INTERVAL = 30000;

    public static void cleanupFrameResources() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL) {
            System.gc();
            lastCleanupTime = currentTime;
        }
    }

    public static void forceCleanup() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.getTextureManager().tick();
        }
        System.gc();
    }
}
