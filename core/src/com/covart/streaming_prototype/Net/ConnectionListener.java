package com.covart.streaming_prototype.Net;

/**
 * Created by lctseng on 2017/2/10.
 * NTU COV-ART Lab, for NCP project
 */

public interface ConnectionListener {
    public void onConnectionReady();

    public void onConnectionClose();

    public void onConnectionStarted();
}
