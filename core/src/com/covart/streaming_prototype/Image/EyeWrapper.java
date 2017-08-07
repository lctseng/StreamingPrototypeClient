package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.StringPool;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.Viewport;

import java.util.Locale;

import static com.badlogic.gdx.math.MathUtils.clamp;

/**
 * Created by lctseng on 2017/8/6.
 * For NCP project at COVART, NTU
 */

public class EyeWrapper {
    private Eye eye;


    private Matrix4 eyeView;

    // tmps
    private Matrix4 tmpMatrix1;
    private Quaternion tmpQuaternion1;

    private float inspectCount = 0f;
    private boolean needUpdate = false;


    private AutoEyeViewGenerator autoEyeViewGenerator;

    private float yaw = 0f;
    private float pitch = 0f;
    private float roll  = 0f;

    public EyeWrapper(){
        autoEyeViewGenerator = new AutoEyeViewGenerator(this);

        tmpMatrix1  = new Matrix4();
        tmpQuaternion1 = new Quaternion();
        eyeView = new Matrix4();
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

    public void onNewFrame(){
        needUpdate = true;
        autoEyeViewGenerator.update();
    }

    private void onChangeEye(){
        needUpdate = true;
    }

    public void requestUpdate(){
        needUpdate = true;
    }

    public void inspect(){
        inspectCount += Gdx.graphics.getDeltaTime();
        if(inspectCount > 0.5f) {
            inspectCount = 0f;
            StringPool.addField("Eye angles", String.format(Locale.TAIWAN, "Yaw: %2f, Pitch: %2f, Roll: %2f", yaw, pitch, roll));
        }
    }

    // Forwarding methods

    public Matrix4 getEyeView()
    {
        if(needUpdate){
            needUpdate = false;
            updateEyeView();
        }
        return this.eyeView;
    }

    // Setup eyeView and eyeViewLimited
    private void updateEyeView(){
        // get the real eye view
        if(ConfigManager.isAutoRotateEnabled()){
            // fetch yaw, pitch and roll from generator
            yaw = (float)autoEyeViewGenerator.getYaw();
            pitch = (float)autoEyeViewGenerator.getPitch();
            roll = (float)autoEyeViewGenerator.getRoll();
            eyeView.setFromEulerAngles(yaw, pitch, roll);
        }
        else{
            eyeView.set(this.eye.getEyeView());
        }
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

    public void resetAutoRotate(){
        autoEyeViewGenerator.reset();
    }
}
