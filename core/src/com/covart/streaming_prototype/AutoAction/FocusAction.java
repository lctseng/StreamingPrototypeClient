package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Utils.Easing.EasingBase;
import com.covart.streaming_prototype.Utils.Easing.EasingLinear;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class FocusAction extends DirectAndIncrementalAction {


    public FocusAction(float changeValue, float duration) {
        super(changeValue, duration);
    }

    public FocusAction(float changeValue, float duration, EasingBase easing) {
        super(changeValue, duration, easing);
    }

    public FocusAction(float targetValue) {
        super(targetValue);
    }

    @Override
    protected EasingBase getDefaultEasing() {
        return new EasingLinear();
    }
    
    @Override
    protected void setValue(float value) {
        ConfigManager.setFocusChangeRatio(value);
    }

    @Override
    protected float getIncrementalStartValue() {
        return ConfigManager.getFocusChangeRatio();
    }
}
