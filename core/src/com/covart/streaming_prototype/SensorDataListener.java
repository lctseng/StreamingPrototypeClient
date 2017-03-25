package com.covart.streaming_prototype;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public interface SensorDataListener {
    void onSensorDataReady(Vector3 direction, Quaternion rotation);
}
