package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/8/6.
 * For NCP project at COVART, NTU
 */

public class AutoEyeViewGenerator {
    private EyeWrapper eyeWrapper;



    private double yaw = 0f;
    private double pitch = 0f;
    private double roll  = 0f;

    private double angle = 0f;
    private double angleSpeed = Math.PI/4.0;

    public AutoEyeViewGenerator(EyeWrapper eyeWrapper){
        this.eyeWrapper = eyeWrapper;
    }

    public void update(){
        angle += angleSpeed * Gdx.graphics.getDeltaTime();
        if(angle >= Math.PI*2.0){
            angle = 0.0;
        }
        yaw = ConfigManager.getEyeWrapperYawLimit() * Math.cos(angle);
        pitch = ConfigManager.getEyeWrapperPitchLimit() * Math.sin(angle);
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
