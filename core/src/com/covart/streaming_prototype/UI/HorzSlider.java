package com.covart.streaming_prototype.UI;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

/**
 * Created by lctseng on 2017/4/29.
 * NTU COV-ART Lab, for NCP project
 */

public class HorzSlider extends Slider {

    private float customWidth;

    public float getCustomWidth() {
        return customWidth;
    }

    public void setCustomWidth(float customWidth) {
        this.customWidth = customWidth;
    }

    public HorzSlider(float min, float max, float stepSize, boolean vertical, Skin skin) {
        super(min, max, stepSize, vertical, skin);
        setCustomWidth(super.getPrefWidth());
    }

    @Override
    public float getPrefWidth() {
        return getCustomWidth();
    }
}
