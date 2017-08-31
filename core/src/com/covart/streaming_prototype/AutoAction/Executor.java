package com.covart.streaming_prototype.AutoAction;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by lctseng on 2017/8/30.
 * For NCP project at COVART, NTU
 */

public class Executor {

    private ExecutorEventListener listener;
    private boolean running;

    private List<Action> allActions;
    private Set<Action> activeActions;

    private float accumulateTime;
    private float executionTime;
    private float timeFactor;
    private int actionIndex;

    private boolean waitByDefault;

    public Executor(ExecutorEventListener listener)
    {
        this.listener = listener;
        reset();
    }

    public Executor(){
        reset();
    }

    public void reset(){
        running = false;
        allActions = new ArrayList<Action>();
        activeActions = new HashSet<Action>();

        waitByDefault = true;
        accumulateTime = 0.0f;
        timeFactor = 1.0f;
    }

    public void start(){
        running = true;
        actionIndex = 0;
        activeActions.clear();
        executionTime = 0.0f;
        if(listener != null){
            listener.onExecutorStart();
        }
    }

    public void stop(){
        running = false;
        if(listener != null){
            listener.onExecutorStop();
        }
    }

    public void addAction(Action action, boolean waitForAction) {
        action.startTime = accumulateTime;
        allActions.add(action);
        if(waitForAction){
            addWait(action.getWaitTime());
        }
    }

    public void addAction(Action action){
        addAction(action, waitByDefault);
    }

    public void addWait(float time){
        accumulateTime += time;
    }

    public void update(){
        if(running) {
            float deltaTime = timeFactor * Gdx.graphics.getDeltaTime();
            executionTime += deltaTime;
            startActions();
            updateActions(deltaTime);
            if(isUpdateEnded()){
                if(listener != null){
                    listener.onExecutorUpdateEnded();
                }
                stop();
            }
        }

    }

    private void startActions(){
        while(actionIndex < allActions.size() && allActions.get(actionIndex).startTime < executionTime){
            // start this action
            Action action = allActions.get(actionIndex);
            action.start();
            activeActions.add(action);
            actionIndex += 1;
        }
    }

    private void updateActions(float deltaTime){
        for (Iterator<Action> i = activeActions.iterator(); i.hasNext();) {
            Action action = i.next();
            if(action.update(deltaTime)){
                // finish!
                i.remove();
            }
        }
    }


    private boolean isUpdateEnded(){
        return actionIndex >= allActions.size() && activeActions.isEmpty();
    }

    public boolean isRunning(){
        return running;
    }

    public void setTimeFactor(float timeFactor) {
        this.timeFactor = timeFactor;
    }

    public boolean isWaitByDefault() {
        return waitByDefault;
    }

    public void setWaitByDefault(boolean waitByDefault) {
        this.waitByDefault = waitByDefault;
    }
}
