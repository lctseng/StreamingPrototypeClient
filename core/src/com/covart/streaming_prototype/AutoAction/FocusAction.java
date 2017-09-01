package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class FocusAction extends DirectAndIncrementalAction {


    public FocusAction(float changeValue, float duration) {
        super(changeValue, duration);
    }

    public FocusAction(float targetValue) {
        super(targetValue);
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
