package com.covart.streaming_prototype;

/**
 * Created by lctseng on 2017/2/10.
 */

public interface ConnectionListener {
    public void onConnectionReady();

    public void onConnectionClose();
}
