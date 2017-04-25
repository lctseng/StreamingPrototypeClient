package com.covart.streaming_prototype;

import com.badlogic.gdx.utils.Disposable;

import StreamingFormat.Message;

/**
 * Created by lctseng on 2017/3/11.
 * NTU COV-ART Lab, for NCP project
 */

public abstract class DisplayBase implements Disposable, SensorDataListener {

    abstract void updateStart();
    abstract void updateEnd();
    abstract void injectImageData(byte[] bufData);
    abstract void disposeExistingTexture();
    abstract void start();

    public void attachControlFrameInfo(Message.Control.Builder controlBuilder){

    }
    public boolean checkControlFrameRequired(){
        return false;
    }

    @Override
    public void onSensorDataReady(Sensor sensor){
        //Gdx.app.log("DisplayBase", "Sensor data received");
    }

    public void toggleEnableFocusChange(){
    }
}
