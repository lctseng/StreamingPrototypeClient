package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Created by lctseng on 2017/8/13.
 * For NCP project at COVART, NTU
 */

public class ModelButton extends VerticalImageTextButton {
    private int modelId;

    private static FileHandle openModelFileWithFallback(String filename){
        FileHandle file = Gdx.files.internal(filename);
        if(file.exists()){
            return file;
        }
        else{
            return Gdx.files.internal("objects.png");
        }
    }

    public ModelButton(int modelId, String filename) {
        super("Model " + modelId, openModelFileWithFallback(filename));
        this.modelId = modelId;
    }

    public ModelButton(int modelId) {
        super("Model " + modelId, "base.png");
        this.modelId = modelId;
    }


    public int getModelId() {
        return modelId;
    }

    public void onModelChanged(int currentId){
        //Color color = modelId == currentId ? Color.GREEN : Color.WHITE;
        //getStyle().fontColor = color;
    }
}