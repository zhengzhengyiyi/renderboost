package io.github.zhengzhengyiyi.mixin.client;

import java.util.ArrayList;
//import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.zhengzhengyiyi.event.RenderEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.profiler.ScopedProfiler;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    public ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks;
    
    @Shadow
    public Frustum frustum;
    
    @Inject(at=@At("HEAD"), cancellable = true, method = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V")
	public void render(ObjectAllocator allocator,
			 RenderTickCounter tickCounter,
			 boolean renderBlockOutline,
			 Camera camera,
			 GameRenderer gameRenderer,
			 Matrix4f positionMatrix,
			 Matrix4f projectionMatrix,
			 CallbackInfo ci) {
		RenderEvent.BEFORE_RENDER.invoker().callback(allocator, tickCounter, renderBlockOutline, camera, gameRenderer, positionMatrix, projectionMatrix);
	}

    @Inject(at = @At("HEAD"), cancellable = true, method = "renderLayer")
    private void renderLayer(RenderLayer renderLayer, double x, double y, double z, Matrix4f viewMatrix, Matrix4f positionMatrix, CallbackInfo ci) {
        ci.cancel();
        
        RenderSystem.assertOnRenderThread();
        ScopedProfiler scopedProfiler = Profilers.get().scoped("render_" + renderLayer.getName());
        Objects.requireNonNull(renderLayer);
        scopedProfiler.addLabel(renderLayer::toString);
        boolean bl = (renderLayer != RenderLayer.getTranslucent());
        ObjectListIterator<ChunkBuilder.BuiltChunk> objectListIterator = this.builtChunks.listIterator(bl ? 0 : this.builtChunks.size());
        renderLayer.startDrawing();
        RenderPipeline renderPipeline = renderLayer.getPipeline();
        ArrayList<RenderPass.RenderObject> arrayList = new ArrayList<>();
        RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(renderLayer.getDrawMode());
        int i = 0;
        
        List<ChunkBuilder.BuiltChunk> visibleChunks = new ArrayList<>();
        while (bl ? objectListIterator.hasNext() : objectListIterator.hasPrevious()) {
            ChunkBuilder.BuiltChunk builtChunk = bl ? objectListIterator.next() : objectListIterator.previous();
            if (isChunkVisible(builtChunk, x, y, z)) {
                visibleChunks.add(builtChunk);
            }
        }
        
//        Collections.sort(visibleChunks, (chunk1, chunk2) -> {
//            BlockPos pos1 = chunk1.getOrigin();
//            BlockPos pos2 = chunk2.getOrigin();
//            double dist1 = pos1.getSquaredDistance(x, y, z);
//            double dist2 = pos2.getSquaredDistance(x, y, z);
//            return Double.compare(dist1, dist2);
//        });
        
        GpuBuffer gpuBuffer;
        VertexFormat.IndexType indexType;
        
        for (ChunkBuilder.BuiltChunk builtChunk : visibleChunks) {
            ChunkBuilder.Buffers buffers = builtChunk.getBuffers(renderLayer);
            
            gpuBuffer = null;
            indexType = null;
            
            if (builtChunk.getData().isEmpty(renderLayer) || buffers == null)
                continue;
            if (buffers.getIndexBuffer() == null) {
                if (buffers.getIndexCount() > i)
                    i = buffers.getIndexCount(); 
            } else {
                gpuBuffer = buffers.getIndexBuffer();
                indexType = buffers.getIndexType();
            } 
            BlockPos blockPos = builtChunk.getOrigin();
            arrayList.add(new RenderPass.RenderObject(0, buffers.getVertexBuffer(), gpuBuffer, indexType, 0, buffers.getIndexCount(), uniformUploader -> uniformUploader.upload("ModelOffset", new float[] { (float)(blockPos.getX() - x), (float)(blockPos.getY() - y), (float)(blockPos.getZ() - z) })));
        }
        
        GpuBuffer gpuBuffer2 = (i == 0) ? null : shapeIndexBuffer.getIndexBuffer(i);
        VertexFormat.IndexType indexType2 = (i == 0) ? null : shapeIndexBuffer.getIndexType();
        RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(renderLayer.getTarget().getColorAttachment(), OptionalInt.empty(), renderLayer.getTarget().getDepthAttachment(), OptionalDouble.empty());
        try {
            renderPass.setPipeline(renderPipeline);
            for (int j = 0; j < 12; j++) {
                GpuTexture gpuTexture = RenderSystem.getShaderTexture(j);
                if (gpuTexture != null)
                    renderPass.bindSampler("Sampler" + j, gpuTexture); 
            } 
            renderPass.drawMultipleIndexed(arrayList, gpuBuffer2, indexType2);
            if (renderPass != null)
                renderPass.close(); 
        } catch (Throwable throwable) {
            if (renderPass != null)
                try {
                    renderPass.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }  
            throw throwable;
        }
        scopedProfiler.close();
        renderLayer.endDrawing();
    }

    private boolean isChunkVisible(ChunkBuilder.BuiltChunk chunk, double cameraX, double cameraY, double cameraZ) {
        BlockPos origin = chunk.getOrigin();
//        double chunkX = origin.getX() - cameraX;
//        double chunkY = origin.getY() - cameraY; 
//        double chunkZ = origin.getZ() - cameraZ;
//        double distance = Math.sqrt(chunkX * chunkX + chunkY * chunkY + chunkZ * chunkZ);
//        if (distance > 256.0) return false;
        return this.frustum.isVisible(new Box(origin.getX(), origin.getY(), origin.getZ(), 
                                         origin.getX() + 16, origin.getY() + 16, origin.getZ() + 16));
    }
}
