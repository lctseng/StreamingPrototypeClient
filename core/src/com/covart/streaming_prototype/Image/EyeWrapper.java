package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
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
    private Camera camera;
    private Eye eye;


    private Matrix4 eyeView;
    private Vector3 lastEyePosition;

    // tmps
    private Matrix4 tmpMatrix1;
    private Matrix4 tmpMatrix2;
    private Quaternion tmpQuaternion1;

    private Vector3 tmpVector1;

    private float inspectCount = 0f;
    private boolean needUpdate = false;


    private AutoEyeViewGenerator autoEyeViewGenerator;

    private float yaw = 0f;
    private float pitch = 0f;
    private float roll  = 0f;

    public EyeWrapper(Camera camera){
        this.camera = camera;
        autoEyeViewGenerator = new AutoEyeViewGenerator(this);

        tmpMatrix1  = new Matrix4();
        tmpMatrix2  = new Matrix4();
        tmpVector1 = new Vector3();
        tmpQuaternion1 = new Quaternion();

        lastEyePosition = new Vector3();
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
            eyeView.getTranslation(tmpVector1);
            tmpVector1.add(camera.position);
            StringPool.addField("Eye angles", String.format(Locale.TAIWAN, "Yaw: %2f, Pitch: %2f, Roll: %2f", yaw, pitch, roll));
            StringPool.addField("Eye position", String.format(Locale.TAIWAN, "X: %2f, Y: %2f, Z: %2f", lastEyePosition.x, lastEyePosition.y, lastEyePosition.z));
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
        // get the eye view without translation
        if(ConfigManager.isAutoRotateEnabled()){
            // fetch yaw, pitch and roll from generator
            yaw = (float)autoEyeViewGenerator.getYaw();
            pitch = (float)autoEyeViewGenerator.getPitch();
            roll = (float)autoEyeViewGenerator.getRoll();
            tmpMatrix1.setFromEulerAngles(yaw, pitch, roll);
        }
        else{
            tmpMatrix1.set(this.eye.getEyeView());
        }
        // apply compute translation for eye view
        // TODO: need optimize variables
        tmpMatrix1.getRotation(tmpQuaternion1);
        Vector3 finalEyePosition = new Vector3(camera.position);
        Vector3 finalEyeTranslation = new Vector3();
        Vector3 rotationCenter = new Vector3(camera.position);
        rotationCenter.z -= ConfigManager.getEyeRotationCenterDistance(); // assume on st plane
        // rotate around the projection on the plane
        // translation(-x, -y, -z) => Rotation => translation(x, y, z)
        // translation(-x, -y, -z)
        Vector3 rotationCenterNeg = new Vector3(rotationCenter);
        rotationCenterNeg.scl(-1);
        tmpMatrix2.setToTranslation(rotationCenterNeg);
        finalEyePosition.mul(tmpMatrix2);

        // rotation
        tmpQuaternion1.transform(finalEyePosition);

        // translation(x, y, z)
        tmpMatrix2.setToTranslation(rotationCenter);
        finalEyePosition.mul(tmpMatrix2);

        finalEyeTranslation.set(finalEyePosition);
        finalEyeTranslation.sub(camera.position);
        tmpMatrix2.setToTranslation(finalEyeTranslation);
        eyeView.set(tmpMatrix1);
        eyeView.mulLeft(tmpMatrix2);


        if(ConfigManager.isMainEye(this.eye)){
            this.lastEyePosition.set(camera.position);
            this.lastEyePosition.add(finalEyeTranslation);
        }




    }

    public float[] getPerspective(float zNear, float zFar) {
        return this.eye.getPerspective(zNear, zFar);
    }

    public Viewport getViewport() {
        return this.eye.getViewport();
    }

    public Vector3 getLastEyePosition() {
        return lastEyePosition;
    }

    public int getType() {
        return this.eye.getType();
    }

    public void resetAutoRotate(){
        autoEyeViewGenerator.reset();
    }
}
