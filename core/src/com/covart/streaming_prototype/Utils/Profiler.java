package com.covart.streaming_prototype.Utils;

import com.badlogic.gdx.Gdx;
import com.covart.streaming_prototype.StringPool;

import java.util.Locale;

/**
 * Created by lctseng on 2017/2/6.
 * NTU COV-ART Lab, for NCP project
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

    private long displayTimestamp;
    private long displayCurrent;
    private long displayTotal;
    private long displayCount;

    private static Profiler instance = new Profiler();

    public static void reset(){
        instance._reset();
    }

    public static void reportOnDisplay(){
        long now = System.nanoTime();
        instance.displayCurrent = now -instance.displayTimestamp;
        instance.displayTimestamp = now;
        instance.displayTotal += instance.displayCurrent;
        instance.displayCount += 1;
    }

    public static void reportOnRecvStart(){
        instance.recvTimestamp = System.nanoTime();
    }

    public static void reportOnRecvEnd(){
        if(instance.recvTimestamp > 0){
            instance.recvCurrent = System.nanoTime() - instance.recvTimestamp;
            instance.recvTotal += instance.recvCurrent;
            instance.recvCount += 1;
            instance.recvTimestamp = -1;
        }
        else{
            Gdx.app.error("Profiler", "Should call reportOnRecvStart before reportOnRecvEnd");
        }
    }

    public static void reportOnProcStart(){
        instance.procTimestamp = System.nanoTime();
    }

    public static void reportOnProcEnd(){
        if(instance.procTimestamp > 0){
            instance.procCurrent = System.nanoTime() - instance.procTimestamp;
            instance.procTotal += instance.procCurrent;
            instance.procCount += 1;
            instance.procTimestamp = -1;
        }
        else{
            Gdx.app.error("Profiler", "Should call reportOnProcStart before reportOnProcEnd");
        }
    }

    public static void generateProfilingStrings(){
        double totalRecv = instance.recvTotal * 0.000001;
        double totalProc = instance.procTotal * 0.000001;
        double totalDisplay = instance.displayTotal * 0.000001;
        StringPool.addField("Time for receive", String.format(Locale.TAIWAN,"%6.4f ms, total: %6.1f ms, avg: %6.4f ms", instance.recvCurrent * 0.000001, totalRecv, totalRecv / instance.recvCount));
        StringPool.addField("Time for process", String.format(Locale.TAIWAN,"%6.4f ms, total: %6.1f ms, avg: %6.4f ms", instance.procCurrent * 0.000001, totalProc, totalProc / instance.procCount));
        StringPool.addField("Time from last display", String.format(Locale.TAIWAN,"%6.4f ms, total: %6.1f ms, avg: %6.4f ms", instance.displayCurrent * 0.000001, totalDisplay, totalDisplay / instance.displayCount));

        /* // Display FPS is not used now
        long fpsCurrent = 0;
        long fpsAverage = 0;
        if(instance.displayCount > 0){
            fpsCurrent = Math.round(1.0 / (instance.displayCurrent * 0.000000001));
            fpsAverage = Math.round(1.0 / ((instance.displayTotal / instance.displayCount) * 0.000000001));
        }
        StringPool.addField("Display FPS", String.format(Locale.TAIWAN,"Current: %d, Average: %d", fpsCurrent, fpsAverage));
        */
    }

    private Profiler() {
        _reset();
    }

    public void _reset(){
        recvTotal = 0;
        recvCount = 0;
        recvTimestamp = -1;
        recvCurrent = 0;

        procTotal = 0;
        procCount = 0;
        procTimestamp = -1;
        procCurrent = 0;

        displayTimestamp = System.nanoTime();
        displayCurrent = 0;
        displayTotal = 0;
        displayCount = 0;
    }
}
