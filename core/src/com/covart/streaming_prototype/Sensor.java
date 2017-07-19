package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Locale;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public class Sensor implements Component {

    // TODO: Support AUTO mode
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

    private boolean initDataReady;


    private float infoPrintTimeMax = 1f;
    private float infoPrintTimeCurrent;


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
        lastHorzRotate = angleHorz = 0f;
        lastVertRotate = angleVert = 0f;
        horzRotateDiff = 0f;
        vertRotateDiff = 0f;
        direction.set(initDirection);
        rightVector.set(initRightVector);
        upVector.set(initUp);
    }

    public void setInitPosition(float x, float y, float z){
        initPosition.set(x, y, z);
    }


    private void updateRightAndUpVector(){
        // update right vector
        rightVector.set(direction);
        rightVector.crs(upVector);
        rightVector.nor();
        // update up vector
        upVector.set(rightVector);
        upVector.crs(direction);
        upVector.nor();
    }

    public void onMoveTypeChanged(){
    }

    private void computeRotation(){
        tempQuaternion.set(initRotation);
        tempQuaternion.conjugate();
        Gdx.input.getRotationMatrix(tempMatrix.val);
        rotation.setFromMatrix(false, tempMatrix);
        rotation.mul(tempQuaternion);
        direction.set(initDirection);
        rotation.transform(direction);

        updateRightAndUpVector();
    }

    public void updateSensorData(){
        if(ConfigManager.getSensorMoveType() == MoveType.REAL) {
            computeRotation();
            computeAngles();
        }

    }

    private void computeAngles(){
        Quaternion relativeRotation = new Quaternion();
        relativeRotation.setFromCross(initDirection, direction);
        float prevRatio = ConfigManager.getTranslationAverageFactor();
        float currentRatio = 1f - prevRatio;

        float currentHorz = relativeRotation.getAngleAround(initRightVector);
        if(currentHorz > 180f){
            currentHorz -= 360f;
        }
        if(currentHorz < -180f){
            currentHorz += 360f;
        }
        float currentVert = relativeRotation.getAngleAround(initUp);
        if(currentVert > 180f){
            currentVert -= 360f;
        }
        if(currentVert < -180f){
            currentVert += 360f;
        }

        angleVert = currentVert * currentRatio + angleVert * prevRatio;
        angleHorz = currentHorz * currentRatio + angleHorz * prevRatio;

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
