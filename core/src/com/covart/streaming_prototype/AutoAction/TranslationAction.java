package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.UI.PositionController.Direction;
import com.covart.streaming_prototype.Utils.Easing.EasingBase;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public class TranslationAction extends ContinuousAction {

    private Direction direction;

    public TranslationAction(Direction direction, float distance, float duration) {
        super(0f, distance, duration);
        this.direction = direction;
    }

    public TranslationAction(Direction direction, float distance, float duration, EasingBase easing) {
        super(0f, distance, duration, easing);
        this.direction = direction;
    }

    @Override
    protected void act(float stepValue) {
        ConfigManager.getApp().display.moveCamera(direction, stepValue);
    }
}
