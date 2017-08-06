package com.covart.streaming_prototype.Image;

import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.Viewport;

/**
 * Created by lctseng on 2017/8/6.
 * For NCP project at COVART, NTU
 */

public class EyeWrapper {
    private Eye eye;

    public EyeWrapper(){
    }

    public Eye getEye() {
        return eye;
    }

    public EyeWrapper setEye(Eye eye) {
        if(this.eye != eye){
            onChangeEye();
        }
        this.eye = eye;
        return this;
    }

    private void onChangeEye(){

    }

    // Forwarding methods
    public float[] getEyeView() {
        return this.eye.getEyeView();
    }

    public float[] getPerspective(float zNear, float zFar) {
        return this.eye.getPerspective(zNear, zFar);
    }

    public Viewport getViewport() {
        return this.eye.getViewport();
    }

    public int getType() {
        return this.eye.getType();
    }
}
