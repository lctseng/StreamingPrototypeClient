package com.covart.streaming_prototype.AutoAction;

/**
 * Created by lctseng on 2017/9/1.
 * For NCP project at COVART, NTU
 */

public class InternalState {
    // these need to stored to internal state because they need to be read from outside world
    public boolean rotationLocked = false;
    public float yaw = 0f;
    public float pitch = 0f;
    public float roll = 0f;

    public void clearRotation(){
        yaw = pitch = roll = 0f;
    }

}
