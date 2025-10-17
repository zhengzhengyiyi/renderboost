package io.github.zhengzhengyiyi.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.opengl.GlStateManager;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.ObjectAllocator;

import static org.lwjgl.opengl.GL46.*;

public class ModRenderer {
    private static int shaderProgram;
    private static int vao;
    private static int vbo;
    
    public static void init() {
        setupShaders();
        setupTriangle();
    }
    
    public static void render(ObjectAllocator allocator,
                             RenderTickCounter tickCounter,
                             boolean renderBlockOutline,
                             Camera camera,
                             GameRenderer gameRenderer,
                             Matrix4f positionMatrix,
                             Matrix4f projectionMatrix) {
        
        GlStateManager._enableBlend();
//        GlStateManager._blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glUseProgram(shaderProgram);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
        glUseProgram(0);
        
        GlStateManager._disableBlend();
    }
    
    private static void setupShaders() {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, """
            #version 460 core
            layout (location = 0) in vec2 aPos;
            void main() {
                gl_Position = vec4(aPos, 0.0, 1.0);
            }
            """);
        glCompileShader(vertexShader);
        
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, """
            #version 460 core
            out vec4 FragColor;
            void main() {
                FragColor = vec4(1.0, 0.5, 0.2, 1.0);
            }
            """);
        glCompileShader(fragmentShader);
        
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }
    
    private static void setupTriangle() {
        float[] vertices = {
            0.0f,  0.5f,
           -0.5f, -0.5f,
            0.5f, -0.5f
        };
        
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
}
