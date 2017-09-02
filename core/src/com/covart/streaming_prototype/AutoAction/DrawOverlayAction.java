package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public class DrawOverlayAction extends OnetimeAction {

    private boolean operation;

    public DrawOverlayAction(boolean operation){
        super();
        this.operation = operation;
    }

    @Override
    protected void act(float deltaTime) {
        ConfigManager.getApp().display.setDrawOverlay(operation);
    }
}
