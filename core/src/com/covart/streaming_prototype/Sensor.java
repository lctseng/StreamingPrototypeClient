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

    // current status
    private Vector3 crossDir;
    private Vector3 directon;
    private Quaternion rotation;

    // temp data for computation
    private Matrix4 tempMatrix;
    private Quaternion tempQuaternion;

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
        initDirection = new Vector3(0,0,1);
        initUp = Vector3.Y;
        crossDir = new Vector3(1,0,0);
        directon = new Vector3(initDirection);
        rotation = new Quaternion();
        tempMatrix = new Matrix4();
        tempQuaternion = new Quaternion();
        serialNumber = 0;
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

    public void setInitialDirection(float vx, float vy, float vz){
        lock.lock();
        initDirection.set(vx, vy, vz);
        defaultPosReady.signal();
        lock.unlock();
    }

    boolean touchDragged (int screenX, int screenY, int pointer){
        this.screenX = clamp(screenX, 0, Gdx.graphics.getWidth());
        this.screenY = clamp(screenY, 0, Gdx.graphics.getHeight());
        return true;
    }

    public void updateSensorData(){
        if(USE_FAKE_INPUT){
            // apply horz rotation
            float angleHorz = screenX / (float)(Gdx.graphics.getWidth()) * 350 - 175;
            tempQuaternion.set(Vector3.Y, angleHorz);
            directon.set(initDirection);
            tempQuaternion.transform(directon);
            // apply vert rotation
            float angleVert = screenY / (float)(Gdx.graphics.getHeight()) * 170 - 85;
            crossDir.set(directon);
            crossDir.crs(initUp);
            tempQuaternion.set(crossDir, angleVert);
            tempQuaternion.transform(directon);
        }
        else{
            // TODO: For debug use
            float accelX = Gdx.input.getAccelerometerX();
            float accelY = Gdx.input.getAccelerometerY();
            float accelZ = Gdx.input.getAccelerometerZ();
            StringPool.addField("Accel:", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = % 6.4f", accelX, accelY, accelZ));
            // generate the current rotation matrix
            Gdx.input.getRotationMatrix(tempMatrix.val);
            tempQuaternion.setFromMatrix(true, tempMatrix);
            directon.set(initDirection);
            tempQuaternion.transform(directon);
        }
    }


    private void sendSensorData(){

        updateSensorData();

        rotation.setFromCross(initDirection, directon);

        StringPool.addField("Rotation:", String.format(Locale.TAIWAN, "Yaw = %6.4f, Pitch = %6.4f, Roll = % 6.4f", rotation.getYaw(), rotation.getPitch(), rotation.getRoll()));
        StringPool.addField("Direction:", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = % 6.4f", directon.x, directon.y, directon.z));

        for (SensorDataListener listener : listeners) {
            listener.onSensorDataReady(directon, rotation);
        }
    }

    public Vector3 getInitDirection(){
        return initDirection;
    }

    public int getSerialNumber(){
        return serialNumber;
    }


}
