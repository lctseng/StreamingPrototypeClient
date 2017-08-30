package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public class RecenterAction extends OnetimeAction {
    @Override
    protected void act(float deltaTime) {
        ConfigManager.getApp().recenter();
    }
}
