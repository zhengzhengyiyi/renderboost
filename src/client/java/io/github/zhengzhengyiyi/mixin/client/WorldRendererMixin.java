package io.github.zhengzhengyiyi.mixin.client;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.zhengzhengyiyi.event.RenderEvent;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
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
}
