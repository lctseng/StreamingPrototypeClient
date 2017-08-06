package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.StringPool;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.Viewport;

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

    public EyeWrapper(){
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
            tmpMatrix1.set(this.getEyeView());
            //Gdx.app.log("Eye View", tmpMatrix1.toString());
            Vector3 translation = new Vector3();
            tmpMatrix1.getTranslation(translation);
            //Gdx.app.log("Eye Translation", translation.toString());

            Quaternion rotation = new Quaternion();
            tmpMatrix1.getRotation(rotation);
            //Gdx.app.log("Eye Rotation", rotation.toString());
            //Gdx.app.log("Eye Rotation", "Yaw:" + rotation.getYaw() + ", Pitch:" + rotation.getPitch() + ", Roll:" + rotation.getRoll());

            //tmpMatrix1.getRotation(rotation, true);
            //Gdx.app.log("Eye Rotation Normalized", rotation.toString());
            //tmpMatrix1.set(this.getPerspective(0.01f, 100f));
            //Gdx.app.log("Eye Project", tmpMatrix1.toString());
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

    private void updateEyeView(){
        // apply limitation

        // FIXME: current limitation does not work well when "roll" is too large
        tmpMatrix1.set(this.eye.getEyeView());
        tmpMatrix1.getRotation(tmpQuaternion1);
        float yaw = tmpQuaternion1.getYaw();
        float pitch = tmpQuaternion1.getPitch();
        float roll = tmpQuaternion1.getRoll();
        if(ConfigManager.isEyeWrapperEnableAngleLimit()){
            yaw = clamp(yaw,-ConfigManager.getEyeWrapperYawLimit(), ConfigManager.getEyeWrapperYawLimit());
            pitch = clamp(pitch,-ConfigManager.getEyeWrapperPitchLimit(), ConfigManager.getEyeWrapperPitchLimit());
        }
        eyeView.setFromEulerAngles(yaw, pitch, roll);

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
