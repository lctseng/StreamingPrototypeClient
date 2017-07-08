package com.covart.streaming_prototype.Image;

/**
 * Created by lctseng on 2017/7/8.
 * NTU COV-ART Lab, for NCP project
 */

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public class LightFieldShaderProvider extends BaseShaderProvider {
    public final DefaultShader.Config config;

    private TextureManager textureManager;

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public void setTextureManager(TextureManager textureManager) {
        this.textureManager = textureManager;
    }


    public LightFieldShaderProvider (final DefaultShader.Config config) {
        this.config = (config == null) ? new DefaultShader.Config() : config;
    }

    public LightFieldShaderProvider (final String vertexShader, final String fragmentShader) {
        this(new DefaultShader.Config(vertexShader, fragmentShader));
    }

    public LightFieldShaderProvider (final FileHandle vertexShader, final FileHandle fragmentShader) {
        this(vertexShader.readString(), fragmentShader.readString());
    }

    public LightFieldShaderProvider () {
        this(null);
    }

    @Override
    protected Shader createShader (final Renderable renderable) {
        LightFieldShader shader = new LightFieldShader(renderable, config);
        shader.setTextureManager(this.textureManager);
        return shader;
    }
}