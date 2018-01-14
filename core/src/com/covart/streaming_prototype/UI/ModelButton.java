package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Created by lctseng on 2017/8/13.
 * For NCP project at COVART, NTU
 */

public class ModelButton extends VerticalImageTextButton {
    private EditingModelManager.ModelInfo model;

    private static FileHandle openModelFileWithFallback(String filename){
        FileHandle file = Gdx.files.internal(filename);
        if(file.exists()){
            return file;
        }
        else{
            return Gdx.files.internal("objects.png");
        }
    }

    public ModelButton(EditingModelManager.ModelInfo model, String filename) {
        super("Model " + model.modelId, openModelFileWithFallback(filename));
        this.model = model;
    }

    public ModelButton(EditingModelManager.ModelInfo model) {
        super("Model " + model.modelId, "base.png");
        this.model = model;
    }

    public EditingModelManager.ModelInfo getModel() {
        return model;
    }

    public void onModelChanged(EditingModelManager.ModelInfo currentModel){
        if(model == currentModel){
            getStyle().imageUp = extractTextureDrawable(textureDown);
            getStyle().fontColor = Color.YELLOW;
        }
        else{
            getStyle().imageUp = extractTextureDrawable(textureUp);
            getStyle().fontColor = Color.WHITE;
        }
    }
}