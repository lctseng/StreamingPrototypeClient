package com.covart.streaming_prototype.AutoAction;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public interface ExecutorEventListener {
    void onExecutorStart();
    void onExecutorStop();
    void onExecutorUpdateEnded();
}
