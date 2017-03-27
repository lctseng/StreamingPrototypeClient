package com.covart.streaming_prototype;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public interface SensorDataListener {
    void onSensorDataReady(Sensor sensor);
}
