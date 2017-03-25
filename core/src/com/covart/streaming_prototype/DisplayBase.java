package com.covart.streaming_prototype;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
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

    @Override
    public void onSensorDataReady(Vector3 direction, Quaternion rotation){
        //Gdx.app.log("DisplayBase", "Sensor data received");
    }
}
