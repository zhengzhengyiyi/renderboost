package io.github.zhengzhengyiyi.config;

import com.google.gson.annotations.Expose;

public class RenderBoostConfig {
    @Expose
    public boolean enableChunkCulling = true;
    
    @Expose
    public boolean enableEntityCulling = true;
    
    @Expose
    public boolean enableParticleOptimization = true;
    
    @Expose
    public int maxParticleCount = 500;
    
    @Expose
    public int entityRenderDistance = 32;
    
    @Expose
    public int chunkRenderDistance = 8;
    
    @Expose
    public boolean enableMemoryCleaner = true;
    
    @Expose
    public int memoryCleanupInterval = 30000;
    
    @Expose
    public boolean enableFastRender = true;
    
    @Expose
    public boolean reduceDistantParticles = true;
}
