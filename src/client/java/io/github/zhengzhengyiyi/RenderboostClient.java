package io.github.zhengzhengyiyi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import io.github.zhengzhengyiyi.optimizer.*;

public class RenderboostClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupRenderOptimizations();
        Renderboost.LOGGER.info("RenderBoost loaded");
    }

    private void setupRenderOptimizations() {
        WorldRenderEvents.START.register(context -> {
            MemoryCleaner.cleanupFrameResources();
        });
    }
}
