package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class FocusAction extends ContinuousAction {

    private float changeValue;

    // incremental
    public FocusAction(float changeValue, float duration) {
        super(duration);
        // need to bind the start/end value when started
        this.changeValue = changeValue;
    }

    // set to value
    public FocusAction(float targetValue) {
        super(targetValue,targetValue,0);
    }

    @Override
    protected void prepareStartEndValue() {
        // must be the incremental
        startValue = ConfigManager.getFocusChangeRatio();
        endValue = startValue + changeValue;
    }

    @Override
    protected void act(float stepValue) {
        ConfigManager.setFocusChangeRatio(currentValue);
    }
}
