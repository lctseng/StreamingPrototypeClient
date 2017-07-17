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
    private Vector3 rightVector;
    private Vector3 upVector;
    private Vector3 direction;
    private Quaternion rotation;

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

    private float horzRotateDiff = 0f;
    private float vertRotateDiff = 0f;

    private float lastHorzRotate = 0f;
    private float lastVertRotate = 0f;

    private float angleHorz;
    private float angleVert;

    Sensor(){

        initDirection = Vector3.Z;
        initUp = Vector3.Y;
        initRightVector = new Vector3();
        initRotation = new Quaternion();
        updateInitRightVector();

        initPosition = new Vector3(0,0,0);
        rightVector = new Vector3(1,0,0);
        upVector = new Vector3(0,1,0);
        direction = new Vector3(initDirection);
        rotation = new Quaternion();

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
        upVector.set(initUp);
        Gdx.app.log("Sensor", String.format(Locale.TAIWAN, "Init direction: X = %6.4f, Y = %6.4f, Z = %6.4f", initDirection.x, initDirection.y, initDirection.z));
        updateInitRightVector();
        RecenterRotation();
        setInitDataReady(true);
    }

    public void RecenterRotation(){
        Gdx.input.getRotationMatrix(tempMatrix.val);
        initRotation.setFromMatrix(true, tempMatrix);
        //initRotation.conjugate();
        lastHorzRotate = angleHorz;
        lastVertRotate = angleVert;
        horzRotateDiff = 0f;
        vertRotateDiff = 0f;
        direction.set(initDirection);
        rightVector.set(initRightVector);
        upVector.set(initUp);
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
        rightVector.set(direction);
        rightVector.crs(upVector);
        rightVector.nor();
        // update up vector
        upVector.set(rightVector);
        upVector.crs(direction);
        upVector.nor();
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
        direction.set(initDirection);
        tempQuaternion.transform(direction);
        // compute right-vector
        updateRightVector();

        // apply vert rotation
        float angleVert;
        angleVert = screenY / (float)(Gdx.graphics.getHeight()) * 60 - 30;
        tempQuaternion.set(rightVector, angleVert);
        tempQuaternion.transform(direction);

        // compute rotation
        rotation.setFromCross(initDirection, direction);
    }

    private void computeRealDirection(){
        // FIXME: clean code
        tempQuaternion.set(initRotation);
        tempQuaternion.conjugate();
        Gdx.input.getRotationMatrix(tempMatrix.val);
        rotation.setFromMatrix(false, tempMatrix);
        rotation.mul(tempQuaternion);
        direction.set(initDirection);
        rotation.transform(direction);

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
        Quaternion relativeRotation = new Quaternion();
        relativeRotation.setFromCross(initDirection, direction);
        float prevRatio = ConfigManager.getTranslationAverageFactor();
        float currentRatio = 1f - prevRatio;

        float currentHorz = relativeRotation.getAngleAround(initUp);
        if(currentHorz > 180f){
            currentHorz -= 360f;
        }
        if(currentHorz < -180f){
            currentHorz += 360f;
        }
        float currentVert = relativeRotation.getAngleAround(initRightVector);
        if(currentVert > 180f){
            currentVert -= 360f;
        }
        if(currentVert < -180f){
            currentVert += 360f;
        }

        if(flipHorzAndVert){
            angleVert = currentHorz * currentRatio + angleVert * prevRatio;
            angleHorz = currentVert * currentRatio + angleHorz * prevRatio;
        }
        else{
            angleVert = currentVert * currentRatio + angleVert * prevRatio;
            angleHorz = currentHorz * currentRatio + angleHorz * prevRatio;
        }

        infoPrintTimeCurrent += Gdx.graphics.getDeltaTime();
        if(infoPrintTimeCurrent > infoPrintTimeMax) {
            infoPrintTimeCurrent = 0f;
            StringPool.addField("SensorAngle", "Vert angle:" + angleVert + ", Horz: " + angleHorz );
            StringPool.addField("Rotation", String.format(Locale.TAIWAN, "Yaw = %6.4f, Pitch = %6.4f, Roll = %6.4f", rotation.getYaw(), rotation.getPitch(), rotation.getRoll()));
            StringPool.addField("Direction", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = %6.4f", direction.x, direction.y, direction.z));
        }

    }

    public Vector3 getInitDirection(){
        return initDirection;
    }

    public Vector3 getDirection(){
        return direction;
    }

    public Quaternion getRotation(){
        return rotation;
    }

    public int getSerialNumber(){
        return serialNumber;
    }


    public float getHorzRotateDiff() {
        return horzRotateDiff;
    }

    public float getVertRotateDiff() {
        return vertRotateDiff;
    }

    public void makeRotateDiff(){
        // diff is valid only larger than threshold

        float tempHorzRotateDiff = angleHorz - lastHorzRotate;
        float tempVertRotateDiff = angleVert - lastVertRotate;

        float tempHorzRotateDiffAbs = Math.abs(tempHorzRotateDiff);
        float tempVertRotateDiffAbs = Math.abs(tempVertRotateDiff);

        if(tempHorzRotateDiffAbs > 0.9f){
            horzRotateDiff = tempHorzRotateDiff;
            lastHorzRotate = angleHorz;
        }
        else{
            // discard small variance
            if(tempHorzRotateDiffAbs < 0.5f){
                lastHorzRotate = angleHorz;
            }
            horzRotateDiff = 0f;
        }

        if(tempVertRotateDiffAbs > 0.9f){
            vertRotateDiff = tempVertRotateDiff;
            lastVertRotate = angleVert;
        }
        else{
            // discard small variance
            if(tempVertRotateDiffAbs < 0.5f){
                lastVertRotate = angleVert;
            }
            vertRotateDiff = 0f;
        }
    }

}
