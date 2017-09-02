package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Utils.Easing.EasingBase;
import com.covart.streaming_prototype.Utils.Easing.EasingLinear;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class StPlaneRadiusAction extends DirectAndIncrementalAction {


    public StPlaneRadiusAction(float changeValue, float duration) {
        super(changeValue, duration);
    }

    public StPlaneRadiusAction(float changeValue, float duration, EasingBase easing) {
        super(changeValue, duration, easing);
    }

    public StPlaneRadiusAction(float targetValue) {
        super(targetValue);
    }

    @Override
    protected EasingBase getDefaultEasing() {
        return new EasingLinear();
    }

    @Override
    protected void setValue(float value) {
        ConfigManager.setStPlaneRadius(value);
        ConfigManager.getApp().display.refreshStPlane();
    }

    @Override
    protected float getIncrementalStartValue() {
        return ConfigManager.getStPlaneRadius();
    }
}
