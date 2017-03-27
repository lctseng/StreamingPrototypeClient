package com.covart.streaming_prototype;

import com.badlogic.gdx.utils.Disposable;

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

    @Override
    public void onSensorDataReady(Sensor sensor){
        //Gdx.app.log("DisplayBase", "Sensor data received");
    }
}
