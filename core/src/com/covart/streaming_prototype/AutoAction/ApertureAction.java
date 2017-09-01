package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Utils.Easing.EasingBase;
import com.covart.streaming_prototype.Utils.Easing.EasingLinear;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class ApertureAction extends DirectAndIncrementalAction {

    public ApertureAction(float changeValue, float duration) {
        super(changeValue, duration);
    }

    public ApertureAction(float changeValue, float duration, EasingBase easing) {
        super(changeValue, duration, easing);
    }

    public ApertureAction(float targetValue) {
        super(targetValue);
    }

    protected EasingBase getDefaultEasing() {
        return new EasingLinear();
    }

    @Override
    protected void setValue(float value) {
        ConfigManager.setApertureSize(value);
    }

    @Override
    protected float getIncrementalStartValue() {
        return ConfigManager.getApertureSize();
    }
}
