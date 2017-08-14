package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/8/13.
 * For NCP project at COVART, NTU
 */

public class ModelButton extends TextButton{
    private int modelId;

    public ModelButton(int modelId, Skin skin) {
        super("Model " + modelId, new TextButtonStyle(skin.get(TextButtonStyle.class)));
        this.modelId = modelId;
    }

    public int getModelId() {
        return modelId;
    }

    public void onModelChanged(){
        Color color = modelId == ConfigManager.getEditingCurrentModelId() ? Color.GREEN : Color.WHITE;
        getStyle().fontColor = color;
    }
}
