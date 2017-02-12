package com.covart.streaming_prototype;

import StreamingFormat.Message;

/**
 * Created by lctseng on 2017/2/12.
 * NTU COV-ART Lab, for NCP project
 */

public interface SensorDataListener {
    public void onSensorMessageReady(Message.StreamingMessage msg);
}
