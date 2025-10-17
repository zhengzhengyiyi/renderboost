package io.github.zhengzhengyiyi.event;

import org.joml.Matrix4f;

import net.fabricmc.fabric.api.event.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.ObjectAllocator;

public class RenderEvent {
	public static Event<BeforeRender> BEFORE_RENDER = EventFactory.createArrayBacked(BeforeRender.class,
		(listeners) -> (ObjectAllocator a,
				 RenderTickCounter b,
				 boolean c,
				 Camera d,
				 GameRenderer e,
				 Matrix4f f,
				 Matrix4f g) -> {
            for (BeforeRender listener : listeners) {
                listener.callback(a, b, c, d, e, f, g);
            }
        });
	
	public static interface BeforeRender {
		void callback(ObjectAllocator allocator,
			 RenderTickCounter tickCounter,
			 boolean renderBlockOutline,
			 Camera camera,
			 GameRenderer gameRenderer,
			 Matrix4f positionMatrix,
			 Matrix4f projectionMatrix);
	}
}
