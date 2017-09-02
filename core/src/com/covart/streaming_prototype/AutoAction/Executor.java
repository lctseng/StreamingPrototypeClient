package com.covart.streaming_prototype.AutoAction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.FloatAction;
import com.covart.streaming_prototype.UI.PositionController;
import com.covart.streaming_prototype.Utils.Easing.EasingBase;
import com.covart.streaming_prototype.Utils.Easing.EasingLinear;
import com.covart.streaming_prototype.Utils.Easing.EasingQuadInOut;
import com.covart.streaming_prototype.Utils.Easing.Http;

import java.util.ArrayList;
import java.util.Arrays;
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

    private class ActionControl {
        public boolean needWait = false;
    }

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
            action.start();
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
        String[] lines = actionText.split("\n");
        for(String line : lines){
            if(!line.startsWith("#") && line.length() > 0){
                parseActionLineWithControl(line);
            }
        }
    }

    private  ActionControl parseControlLine(String controlLine){
        ActionControl control = getDefaultActionControl();
        String[] controlWords = controlLine.split(",");
        for(String controlWord : controlWords){
            if(controlLine.equals("wait")){
                control.needWait = true;
            }
            else if(controlLine.equals("nowait")){
                control.needWait = false;
            }
            else{
                // TODO: unknown control word
            }
        }
        return control;
    }

    private EasingBase parseEasingStringFromParams(ArrayList<String> params){
        EasingBase easing = null;
        if(params.size() > 0){
            String easingString = params.remove(0);
            if(easingString.equals("Linear")){
                easing = new EasingLinear();
            }
            else if(easingString.equals("QuadInOut")){
                easing = new EasingQuadInOut();
            }
            else{
                // TODO: unknown easing
            }
        }
        return easing;
    }

    private  RotationAction parseRotationAction(ArrayList<String> params){
        // type
        RotationAction.Type type;
        String typeString = params.remove(0);
        if(typeString.equals("YAW")){
            type = RotationAction.Type.YAW;
        }
        else if(typeString.equals("PITCH")){
            type = RotationAction.Type.PITCH;
        }
        else if(typeString.equals("ROLL")){
            type = RotationAction.Type.ROLL;
        }
        else{
            // TODO: unknown type, default to yaw
            type = RotationAction.Type.YAW;
        }
        // change and duration
        float change = Float.parseFloat(params.remove(0));
        float duration = Float.parseFloat(params.remove(0));
        // easing
        EasingBase easing = parseEasingStringFromParams(params);

        return new RotationAction(type, change, duration, easing);
    }

    private  TranslationAction parseTranslationAction(ArrayList<String> params){
        // direction
        PositionController.Direction direction;
        String directionString = params.remove(0);
        if(directionString.equals("FORWARD")){
            direction = PositionController.Direction.FORWARD;
        }
        else if(directionString.equals("BACKWARD")){
            direction = PositionController.Direction.BACKWARD;
        }
        else if(directionString.equals("UP")){
            direction = PositionController.Direction.UP;
        }
        else if(directionString.equals("DOWN")){
            direction = PositionController.Direction.DOWN;
        }
        else if(directionString.equals("LEFT")){
            direction = PositionController.Direction.LEFT;
        }
        else if(directionString.equals("RIGHT")){
            direction = PositionController.Direction.RIGHT;
        }
        else{
            // TODO: unknown direction, default to right
            direction = PositionController.Direction.RIGHT;
        }
        // change and duration
        float change = Float.parseFloat(params.remove(0));
        float duration = Float.parseFloat(params.remove(0));
        // easing
        EasingBase easing = parseEasingStringFromParams(params);

        return new TranslationAction(direction, change, duration, easing);
    }

    private ApertureAction parseApertureAction(ArrayList<String> params){
        if(params.size() == 1){
            // set value
            return new ApertureAction(Float.parseFloat(params.remove(0)));
        }
        else{
            // TODO: check param size
            // incremental
            // change and duration
            float change = Float.parseFloat(params.remove(0));
            float duration = Float.parseFloat(params.remove(0));
            // easing
            EasingBase easing = parseEasingStringFromParams(params);
            return new ApertureAction(change, duration, easing);
        }
    }

    private  FocusAction parseFocusAction(ArrayList<String> params){
        if(params.size() == 1){
            // set value
            return new FocusAction(Float.parseFloat(params.remove(0)));
        }
        else{
            // TODO: check param size
            // incremental
            // change and duration
            float change = Float.parseFloat(params.remove(0));
            float duration = Float.parseFloat(params.remove(0));
            // easing
            EasingBase easing = parseEasingStringFromParams(params);
            return new FocusAction(change, duration, easing);
        }
    }

    private void parseConfig(ArrayList<String> params){
        String type = params.remove(0);
        if(type.equals("TimeFactor")){
            setTimeFactor(Float.parseFloat(params.remove(0)));
        }
        else if((type.equals("WaitByDefault"))){
            setWaitByDefault(params.remove(0).equals("true"));
        }
        else{
            // TODO" unknown config
        }
    }

    private void parseActionLine(String actionLine, ActionControl control){
        ArrayList<String> actionWords = new ArrayList<String>(Arrays.asList(actionLine.split(",")));
        // Action or Wait?

        String type = actionWords.remove(0);
        if(type.equals("Action")){
            String actionName = actionWords.remove(0);
            Action action = null;
            if(actionName.equals("Aperture")){
                action = parseApertureAction(actionWords);
            }
            else if(actionName.equals("Focus")){
                action = parseFocusAction(actionWords);
            }
            else if(actionName.equals("Rotation")){
                action = parseRotationAction(actionWords);
            }
            else if(actionName.equals("Translation")){
                action = parseTranslationAction(actionWords);
            }
            else{
                // TODO: unknown action
            }
            // apply control
            if(action != null){
                addAction(action, control.needWait);
            }
        }
        else if(type.equals("Wait")){
            addWait(Float.parseFloat(actionWords.remove(0)));
        }
        else if(type.equals("Config")){
            parseConfig(actionWords);
        }
        else{
            // TODO: unknown type
        }
    }

    private ActionControl getDefaultActionControl(){
        ActionControl control = new ActionControl();
        control.needWait = waitByDefault;
        return control;
    }


    private  void parseActionLineWithControl(String actionLine){
        //System.out.println(actionLine);
        String[] majorParts = actionLine.split("@");
        ActionControl control = null;
        if(majorParts.length > 1){
            control = parseControlLine(majorParts[1]);
        }
        if(control == null){
            control = getDefaultActionControl();
        }
        parseActionLine(majorParts[0], control);
    }

}
