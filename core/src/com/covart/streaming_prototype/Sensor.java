package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.badlogic.gdx.math.MathUtils.clamp;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public class Sensor implements Runnable, Component {

    private Thread worker = null;
    private ArrayList<SensorDataListener> listeners;

    // init settings
    private Vector3 initDirection;
    private Vector3 initUp;
    private Vector3 initPosition;
    private Vector3 initRightVector;

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
    private Vector3 tempVector3;

    // wait for init direction
    private final Lock lock = new ReentrantLock();
    private final Condition defaultPosReady = lock.newCondition();

    private int serialNumber;

    // for fake data generation
    public static final boolean USE_FAKE_INPUT = true;
    private float screenX;
    private float screenY;


    Sensor(){
        listeners = new ArrayList<SensorDataListener>();

        initDirection = Vector3.Z;
        initUp = Vector3.Y;
        initRightVector = new Vector3();
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
        tempVector3 = new Vector3();

        serialNumber = 0;
    }

    private void updateInitRightVector(){
        initRightVector.set(initDirection);
        initRightVector.crs(initUp);
        Gdx.app.log("Sensor", String.format(Locale.TAIWAN, "Init right: X = %6.4f, Y = %6.4f, Z = %6.4f", initRightVector.x, initRightVector.y, initRightVector.z));
    }

    public void addListener(SensorDataListener listener){
        listeners.add(listener);
    }

    @Override
    public void start() {
        stop();
        Gdx.app.log("Sensor","starting");
        worker = new Thread(this);
        worker.start();
    }

    @Override
    public void stop() {
        if(worker != null){
            Gdx.app.log("Sensor","stopping");
            worker.interrupt();
            if(Thread.currentThread() != worker){
                try {
                    worker.join();
                    Gdx.app.log("Sensor","Worker stopped");
                } catch (InterruptedException e) {
                    Gdx.app.error("Sensor", "Cannot join worker: interrupted");
                    e.printStackTrace();
                }
            }
            worker = null;
        }
    }

    @Override
    public void run() {
        // wait for default position
        Gdx.app.log("Sensor Worker", "Start waiting for default position...");
        lock.lock();
        try {
            defaultPosReady.await();
        } catch (InterruptedException e) {
            Gdx.app.error("Sensor Worker", "Waiting for default position is interrupted. Worker terminated");
            return;
        } finally {
            lock.unlock();
        }
        // start sending
        serialNumber = 0;
        while(true){
            if(Thread.currentThread().isInterrupted()){
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            serialNumber += 1;
            sendSensorData();
        }
    }

    public void setInitDirection(float vx, float vy, float vz){
        lock.lock();
        initDirection.set(vx, vy, vz);
        updateInitRightVector();
        defaultPosReady.signal();
        lock.unlock();
    }

    public void setInitPosition(float x, float y, float z){
        initPosition.set(x, y, z);
    }

    boolean touchDragged (int screenX, int screenY, int pointer){
        this.screenX = clamp(screenX, 0, Gdx.graphics.getWidth());
        this.screenY = clamp(screenY, 0, Gdx.graphics.getHeight());
        return true;
    }

    private void updateRightVector(){
        rightVector.set(directon);
        rightVector.crs(initUp);
        rightVector.nor();
    }

    public void updateSensorData(){
        if(USE_FAKE_INPUT){
            // apply horz rotation
            float angleHorz = screenX / (float)(Gdx.graphics.getWidth()) * 350 - 175;
            tempQuaternion.set(Vector3.Y, angleHorz);
            directon.set(initDirection);
            tempQuaternion.transform(directon);
            // compute right-vector
            updateRightVector();
            // apply vert rotation
            float angleVert = screenY / (float)(Gdx.graphics.getHeight()) * 170 - 85;
            tempQuaternion.set(rightVector, angleVert);
            tempQuaternion.transform(directon);

        }
        else{
            // generate the current rotation matrix
            Gdx.input.getRotationMatrix(tempMatrix.val);
            tempQuaternion.setFromMatrix(true, tempMatrix);
            directon.set(initDirection);
            tempQuaternion.transform(directon);
            // compute right-vector
            updateRightVector();
        }
        // set rotation
        rotation.setFromCross(initDirection, directon);

        // compute translation from direction
        computeTranslation();

    }

    private void computeTranslation(){
        float angleHorz, angleVert;
        // compute projection on rotation plane
        tempVector3.set(initUp);
        tempVector3.crs(rightVector);

        // compute vertical translation
        // compute rotation between projection and current direction
        tempQuaternion.setFromCross(directon, tempVector3);
        // scale the angle
        angleVert = tempQuaternion.getAngleRad() / 3;
        // fix neg
        if(directon.dot(initUp) > 0){
            angleVert *= -1;
        }
        // use the scaled angle to compute magnitude
        translationMagnitudeVert = (float)Math.sin(angleVert);

        // compute horizontal translation
        // compute rotation between projection and init direction
        tempQuaternion.setFromCross(initDirection, tempVector3);
        // scale the angle
        angleHorz = tempQuaternion.getAngleRad() / 6;
        // fix neg
        if(initRightVector.dot(tempVector3) < 0){
            angleHorz *= -1;
        }
        // use the scaled angle to compute magnitude
        translationMagnitudeHorz = (float)Math.sin(angleHorz);
        // apply on position delta
        positionDelta.set(initRightVector.x * translationMagnitudeHorz, initRightVector.y * translationMagnitudeHorz, initRightVector.z * translationMagnitudeHorz);


        Gdx.app.log("Sensor", "Vert angle:" + angleVert * 57.2957795 + ", Horz: " + angleHorz * 57.2957795);
    }


    private void sendSensorData(){

        updateSensorData();



        StringPool.addField("Rotation:", String.format(Locale.TAIWAN, "Yaw = %6.4f, Pitch = %6.4f, Roll = %6.4f", rotation.getYaw(), rotation.getPitch(), rotation.getRoll()));
        StringPool.addField("Direction:", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = %6.4f", directon.x, directon.y, directon.z));
        StringPool.addField("Translation:", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = %6.4f (Mag = %6.4f)", positionDelta.x, positionDelta.y, positionDelta.z, translationMagnitudeHorz));


        for (SensorDataListener listener : listeners) {
            listener.onSensorDataReady(this);
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
}
