package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;

import java.util.Locale;

/**
 * Created by lctseng on 2017/2/6.
 */

public class Profiler {

    private long recvTotal;
    private int recvCount;
    private long recvTimestamp;
    private long recvCurrent;

    private long procTotal;
    private int procCount;
    private long procTimestamp;
    private long procCurrent;

    private static Profiler instance = null;
    public static Profiler getInstance(){
        if(instance == null){
            instance = new Profiler();
        }
        return instance;
    }

    Profiler() {
        reset();
    }

    public void reset(){
        recvTotal = 0;
        recvCount = 0;
        recvTimestamp = -1;
        recvCurrent = 0;

        procTotal = 0;
        procCount = 0;
        procTimestamp = -1;
        procCurrent = 0;
    }

    public void reportOnRecvStart(){
        recvTimestamp = System.nanoTime();
    }

    public void reportOnRecvEnd(){
        if(recvTimestamp > 0){
            recvCurrent = System.nanoTime() - recvTimestamp;
            recvTotal += recvCurrent;
            recvCount += 1;
            recvTimestamp = -1;
        }
        else{
            Gdx.app.error("Profiler", "Should call reportOnRecvStart before reportOnRecvEnd");
        }
    }

    public void reportOnProcStart(){
        procTimestamp = System.nanoTime();
    }

    public void reportOnProcEnd(){
        if(procTimestamp > 0){
            procCurrent = System.nanoTime() - procTimestamp;
            procTotal += procCurrent;
            procCount += 1;
            procTimestamp = -1;
        }
        else{
            Gdx.app.error("Profiler", "Should call reportOnProcStart before reportOnProcEnd");
        }
    }

    public String[] generateProfilingStrings(){
        String[] result = new String[2];
        double totalRecv = recvTotal * 0.000001;
        double totalProc = procTotal * 0.000001;
        result[0] = String.format(Locale.TAIWAN,"Time for receive : %6.4f ms, total: %6.4f ms, avg: %6.4f ms", recvCurrent * 0.000001, totalRecv, totalRecv / recvCount);
        result[1] = String.format(Locale.TAIWAN,"Time for process : %6.4f ms, total: %6.4f ms, avg: %6.4f ms", procCurrent * 0.000001, totalProc, totalProc / procCount);
        return result;
    }
}
