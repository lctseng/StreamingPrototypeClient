package com.covart.streaming_prototype.UI;

/**
 * Created by lctseng on 2017/8/13.
 * For NCP project at COVART, NTU
 */

public class ModelButton extends VerticalImageTextButton {
    private int modelId;



    public ModelButton(int modelId, String filename) {
        super("Model " + modelId, filename);
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
