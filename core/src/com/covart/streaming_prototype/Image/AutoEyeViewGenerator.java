package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/8/6.
 * For NCP project at COVART, NTU
 */

public class AutoEyeViewGenerator {

    public static final float PAUSE_BETWEEN_ROUND = 1f;

    private EyeWrapper eyeWrapper;

    private float pauseTime = 0f;

    private double yaw = 0f;
    private double pitch = 0f;
    private double roll  = 0f;

    private double angle = 0f;
    private double angleSpeed = Math.PI/4.0;

    public AutoEyeViewGenerator(EyeWrapper eyeWrapper){
        this.eyeWrapper = eyeWrapper;
    }

    public void reset(){
        angle = 0;
    }

    public void update(){
        if(pauseTime > 0f){
            pauseTime -= Gdx.graphics.getDeltaTime();
        }
        else {
            angle += angleSpeed * Gdx.graphics.getDeltaTime() * ConfigManager.getAutoRotateSpeedFactor();
            if (angle >= Math.PI * 2.0) {
                angle = 0.0;
                pauseTime = PAUSE_BETWEEN_ROUND;
            }
            yaw = ConfigManager.getAutoRotateYawLimit() * Math.cos(angle);
            pitch = ConfigManager.getAutoRotatePitchLimit() * Math.sin(angle);
        }
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public double getRoll() {
        return roll;
    }
}
