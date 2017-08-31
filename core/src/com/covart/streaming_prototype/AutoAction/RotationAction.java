package com.covart.streaming_prototype.AutoAction;

import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public class RotationAction extends ContinuousAction {

    public enum Type {
        YAW, PITCH, ROLL
    }

    private Type rotationType;

    public RotationAction(Type rotationType, float change, float duration) {
        super(0, change, duration);
        this.rotationType = rotationType;
    }

    @Override
    protected void act(float stepValue) {
        setCurrentValueByType(this.rotationType, getCurrentValueByType(rotationType) + stepValue);
    }

    private static float getCurrentValueByType(Type rotationType){
        switch (rotationType){
            case YAW:
                return ConfigManager.getAutoActionState().yaw;
            case PITCH:
                return ConfigManager.getAutoActionState().pitch;
            case ROLL:
                return ConfigManager.getAutoActionState().roll;
            default:
                return 0f;
        }
    }

    private static void setCurrentValueByType(Type rotationType, float value){
        switch (rotationType){
            case YAW:
                ConfigManager.getAutoActionState().yaw = value;
                break;
            case PITCH:
                ConfigManager.getAutoActionState().pitch = value;
                break;
            case ROLL:
                ConfigManager.getAutoActionState().roll = value;
                break;
        }
    }
}
