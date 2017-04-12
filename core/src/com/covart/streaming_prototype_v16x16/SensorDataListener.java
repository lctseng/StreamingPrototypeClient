package com.covart.streaming_prototype_v16x16;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public interface SensorDataListener {
    void onSensorDataReady(Sensor sensor);
}
