package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Locale;

import static com.badlogic.gdx.math.MathUtils.clamp;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public class Sensor implements Component {

    public enum MoveType {
        REAL, MANUAL, AUTO, NONE
    }

    // init settings
    private Vector3 initDirection;
    private Vector3 initUp;
    private Vector3 initPosition;
    private Vector3 initRightVector;

    private Quaternion initRotation;

    // current status
    private Vector3 positionDelta;
    private Vector3 rightVector;
    private Vector3 directon;
    private Quaternion rotation;
    private float translationMagnitudeHorz;
    private float translationMagnitudeVert;


    // temp data for computation
    private Matrix4 tempMatrix;
    private Quaternion tempQuaternion;

    private int serialNumber;

    // for fake data generation

    private float screenX;
    private float screenY;

    private boolean initDataReady;


    private float infoPrintTimeMax = 1f;
    private float infoPrintTimeCurrent;

    private float autoMoveScreenX = 0.0f;
    private boolean autoMoveForward;
    private float autoMovePausingTime;

    private boolean flipHorzAndVert = false;

    Sensor(){

        initDirection = Vector3.Z;
        initUp = Vector3.Y;
        initRightVector = new Vector3();
        initRotation = new Quaternion();
        updateInitRightVector();

        initPosition = new Vector3(0,0,0);
        positionDelta = new Vector3(0,0,0);
        rightVector = new Vector3(1,0,0);
        directon = new Vector3(initDirection);
        rotation = new Quaternion();
        translationMagnitudeHorz = 0.f;
        translationMagnitudeVert = 0.f;

        tempMatrix = new Matrix4();
        tempQuaternion = new Quaternion();

        serialNumber = 0;
        infoPrintTimeCurrent = 0;

        setInitDataReady(false);
        onMoveTypeChanged();
    }

    public boolean isInitDataReady() {
        return initDataReady;
    }

    public void setInitDataReady(boolean initDataReady) {
        this.initDataReady = initDataReady;
    }

    private void updateInitRightVector(){
        initRightVector.set(initDirection);
        initRightVector.crs(initUp);
        Gdx.app.log("Sensor", String.format(Locale.TAIWAN, "Init right: X = %6.4f, Y = %6.4f, Z = %6.4f", initRightVector.x, initRightVector.y, initRightVector.z));
    }

    @Override
    public void start() {
        setInitDataReady(false);
    }

    @Override
    public void stop() {
        setInitDataReady(false);
    }

    public void setInitDirection(float vx, float vy, float vz){
        initDirection.set(vx, vy, vz);
        Gdx.app.log("Sensor", String.format(Locale.TAIWAN, "Init direction: X = %6.4f, Y = %6.4f, Z = %6.4f", initDirection.x, initDirection.y, initDirection.z));
        updateInitRightVector();
        Gdx.input.getRotationMatrix(tempMatrix.val);
        RecenterRotation();
        setInitDataReady(true);
    }

    public void RecenterRotation(){
        initRotation.setFromMatrix(true, tempMatrix);
        //initRotation.conjugate();
    }

    public void setInitPosition(float x, float y, float z){
        initPosition.set(x, y, z);
    }

    boolean touchDragged (int screenX, int screenY, int pointer){
        this.screenX = clamp(screenX, 0, Gdx.graphics.getWidth());
        this.screenY = clamp(screenY, 0, Gdx.graphics.getHeight()-100);
        return true;
    }

    private void updateRightVector(){
        rightVector.set(directon);
        rightVector.crs(initUp);
        rightVector.nor();
    }

    public void onMoveTypeChanged(){
        switch(ConfigManager.getSensorMoveType()){
            case AUTO:
                // reset auto move
                autoMoveScreenX = 0.0f;
                autoMoveForward = true;
                autoMovePausingTime = 1.0f;
                flipHorzAndVert = false;
                break;
            case MANUAL:
                flipHorzAndVert = false;
                break;
            case REAL:
                flipHorzAndVert = true;
                break;
        }
    }

    private void updateAutoMoveScreenX(){
        if(autoMovePausingTime > 0.0f){
            autoMovePausingTime -= Gdx.graphics.getDeltaTime();
        }
        else {
            if (autoMoveForward) {
                autoMoveScreenX += Gdx.graphics.getDeltaTime() * ConfigManager.getSensorAutoMoveSpeed();
                if (autoMoveScreenX >= Gdx.graphics.getWidth() - 10) {
                    autoMoveScreenX = Gdx.graphics.getWidth() - 10;
                    autoMoveForward = false;
                    autoMovePausingTime = 0.5f;
                }
            } else {
                autoMoveScreenX -= Gdx.graphics.getDeltaTime() * ConfigManager.getSensorAutoMoveSpeed();
                if (autoMoveScreenX <= 10) {
                    autoMoveScreenX = 10;
                    autoMoveForward = true;
                    autoMovePausingTime = 0.5f;
                }
            }
        }
    }

    private void computeFakeDirectionUsingScreenXY(float screenX, float screenY){
        // apply horz rotation
        float angleHorz = screenX / (float)(Gdx.graphics.getWidth()) * 120 - 60;
        tempQuaternion.set(Vector3.Y, angleHorz);
        directon.set(initDirection);
        tempQuaternion.transform(directon);
        // compute right-vector
        updateRightVector();

        // apply vert rotation
        float angleVert;
        angleVert = screenY / (float)(Gdx.graphics.getHeight()) * 60 - 30;
        tempQuaternion.set(rightVector, angleVert);
        tempQuaternion.transform(directon);

        // compute rotation
        rotation.setFromCross(initDirection, directon);
    }

    private void computeRealDirection(){
        // FIXME: clean code
        tempQuaternion.set(initRotation);
        tempQuaternion.conjugate();
        Gdx.input.getRotationMatrix(tempMatrix.val);
        rotation.setFromMatrix(true, tempMatrix);
        rotation.mul(tempQuaternion);
        directon.set(initDirection);
        rotation.transform(directon);
        updateRightVector();
    }

    public void updateSensorData(){
        // Goal: compute rotation and direction
        switch(ConfigManager.getSensorMoveType()){
            case AUTO:
                updateAutoMoveScreenX();
                computeFakeDirectionUsingScreenXY(autoMoveScreenX, screenY);
                break;
            case MANUAL:
                computeFakeDirectionUsingScreenXY(screenX, screenY);
                break;
            case REAL:
                computeRealDirection();
                break;
        }
        // compute translation from rotation
        computeTranslation();

    }

    private void computeTranslation(){
        float angleHorz, angleVert;
        if(flipHorzAndVert){
            angleHorz = rotation.getAngleAroundRad(rightVector) ;
            angleVert = rotation.getAngleAroundRad(initUp) ;
        }
        else{
            angleVert = rotation.getAngleAroundRad(rightVector) ;
            angleHorz = rotation.getAngleAroundRad(initUp) ;

        }
        // compute vertical translation
        // scale the angle
        // use the scaled angle to compute magnitude
        if(angleVert > (Math.PI / 6) && angleVert <= Math.PI) angleVert = ((float)Math.PI/6);
        if(angleVert >= Math.PI && angleVert < 2*Math.PI) angleVert -= 2*Math.PI ;
        if(angleVert < -(Math.PI/6)) angleVert = -((float)Math.PI/6);
        translationMagnitudeVert = translationMagnitudeVert * ConfigManager.getTranslationAverageFactor() +  (float)Math.sin(angleVert)  * (1 - ConfigManager.getTranslationAverageFactor());

        // compute horizontal  translation
        // scale the angle

        if(angleHorz > (Math.PI / 3) && angleHorz <= Math.PI) angleHorz = ((float)Math.PI/3);
        if(angleHorz >= Math.PI && angleHorz < 2*Math.PI) angleHorz -= 2*Math.PI ;
        if(angleHorz < -(Math.PI/3)) angleHorz = -((float)Math.PI/3);
        translationMagnitudeHorz =  translationMagnitudeHorz * ConfigManager.getTranslationAverageFactor() + (angleHorz / ((float)Math.PI/3) / 2) * (1f - ConfigManager.getTranslationAverageFactor()) * -1;
        // apply on position delta
        positionDelta.set(initRightVector.x * translationMagnitudeHorz, initRightVector.y * translationMagnitudeHorz, initRightVector.z * translationMagnitudeHorz);


        infoPrintTimeCurrent += Gdx.graphics.getDeltaTime();
        if(infoPrintTimeCurrent > infoPrintTimeMax) {
            infoPrintTimeCurrent = 0f;
            StringPool.addField("SensorAngle", "Vert angle:" + angleVert * 57.2957795 + ", Horz: " + angleHorz * 57.2957795);
            StringPool.addField("Rotation", String.format(Locale.TAIWAN, "Yaw = %6.4f, Pitch = %6.4f, Roll = %6.4f", rotation.getYaw(), rotation.getPitch(), rotation.getRoll()));
            StringPool.addField("Direction", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = %6.4f", directon.x, directon.y, directon.z));
            StringPool.addField("Translation", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = %6.4f (Mag = %6.4f)", positionDelta.x, positionDelta.y, positionDelta.z, translationMagnitudeHorz));
        }

    }

    public Vector3 getInitDirection(){
        return initDirection;
    }

    public Vector3 getDirecton(){
        return directon;
    }

    public Quaternion getRotation(){
        return rotation;
    }

    public int getSerialNumber(){
        return serialNumber;
    }

    public float getTranslationMagnitudeHorz(){
        return translationMagnitudeHorz;
    }


    public float getTranslationMagnitudeVert() {
        return translationMagnitudeVert;
    }


    public float getScreenX() {
        return screenX;
    }


    public float getScreenY() {
        return screenY;
    }

}
