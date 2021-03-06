package com.covart.streaming_prototype.AutoAction;

import com.badlogic.gdx.Gdx;
import com.covart.streaming_prototype.Utils.Http;

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

    private float deltaTime;

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

    public void clearActions(){
        allActions.clear();
        activeActions.clear();
        accumulateTime = 0f;
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
            deltaTime = timeFactor * Gdx.graphics.getDeltaTime();
            executionTime += deltaTime;
            updateActions();
            startActions();
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
            action.requestStart();
            activeActions.add(action);
            actionIndex += 1;
            if(action.updateOnStart()){
                action.update(deltaTime);
            }
        }
    }

    private void updateActions(){
        for (Iterator<Action> i = activeActions.iterator(); i.hasNext();) {
            Action action = i.next();
            if(action.update(deltaTime)){
                // finish!
                i.remove();
            }
        }
    }


    private boolean isUpdateEnded(){
        return executionTime >= accumulateTime && actionIndex >= allActions.size() && activeActions.isEmpty();
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


    public boolean loadActionFromURL(String url) {
        String text = null;
        try {
            text = Http.getHTML(url);
        } catch (Exception e) {
            Gdx.app.error("AutoAction", "Cannot load action from " + url);
            return false;
        }
        if (text != null) {
            try {
                loadActionText(text);
            } catch (Exception e) {
                Gdx.app.error("AutoAction", "Cannot parse action from" + text);
                return false;
            }
        }
        return true;
    }

    public  void loadActionText(String actionText){
        ActionParser parser = new ActionParser(this);
        parser.loadActionText(actionText);
    }
}
