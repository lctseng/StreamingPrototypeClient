package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class ApertureAction extends DirectAndIncrementalAction {

    public ApertureAction(float changeValue, float duration) {
        super(changeValue, duration);
    }

    public ApertureAction(float targetValue) {
        super(targetValue);
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
