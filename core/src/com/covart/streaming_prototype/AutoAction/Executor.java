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

    private boolean running;

    private List<Action> allActions;
    private Set<Action> activeActions;

    private float accumulateTime;
    private float executionTime;
    private float timeFactor;
    private int actionIndex;


    public Executor(){
        reset();
    }

    public void reset(){
        running = false;
        allActions = new ArrayList<Action>();
        activeActions = new HashSet<Action>();

        accumulateTime = 0.0f;
        executionTime = 0.0f;
        timeFactor = 1.0f;
        actionIndex = 0;
    }

    public void start(){
        running = true;
    }

    public void addAction(Action action){
        action.startTime = accumulateTime;
        allActions.add(action);
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
                running = false;
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


    public boolean isUpdateEnded(){
        return actionIndex >= allActions.size() && activeActions.isEmpty();
    }

    public void setTimeFactor(float timeFactor) {
        this.timeFactor = timeFactor;
    }
}
