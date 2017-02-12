package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Locale;

import StreamingFormat.Message;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public class Sensor implements Runnable, Component {

    private Thread worker = null;
    private SensorDataListener listener;

    private Vector3 initDirection;

    private Matrix4 tempMatrix;
    private Quaternion tempQuaternion;

    private int serialNumber;

    Sensor(SensorDataListener listener){
        this.listener = listener;
        initDirection = new Vector3(0,0,1);
        tempMatrix = new Matrix4();
        tempQuaternion = new Quaternion();
        serialNumber = 0;
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
            Message.StreamingMessage msg = makeSensorPacket();
            if(msg != null){
                listener.onSensorMessageReady(msg);
            }
        }
    }

    public void setInitialDirection(float vx, float vy, float vz){
        initDirection.set(vx, vy, vz);
    }


    private Message.StreamingMessage makeSensorPacket() {
        // TODO: For debug use
        float accelX = Gdx.input.getAccelerometerX();
        float accelY = Gdx.input.getAccelerometerY();
        float accelZ = Gdx.input.getAccelerometerZ();
        StringPool.addField("Accel:", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = % 6.4f", accelX, accelY, accelZ));

        // generate the current rotation matrix
        Gdx.input.getRotationMatrix(tempMatrix.val);
        tempQuaternion.setFromMatrix(true, tempMatrix);
        Vector3 rotated = tempQuaternion.transform(initDirection);
        StringPool.addField("Direction:", String.format(Locale.TAIWAN, "X = %6.4f, Y = %6.4f, Z = % 6.4f", rotated.x, rotated.y, rotated.z));

        // crafting packet
        Message.StreamingMessage msg = Message.StreamingMessage.newBuilder()
                .setType(Message.MessageType.MsgCameraInfo)
                .setCameraMsg(
                        Message.Camera.newBuilder()
                                .setDeltaVx(rotated.x - initDirection.x)
                                .setDeltaVy(rotated.y - initDirection.y)
                                .setDeltaVz(rotated.y - initDirection.z)
                                .setSerialNumber(serialNumber)
                                .build()

                ).build();
        return msg;
    }
}
