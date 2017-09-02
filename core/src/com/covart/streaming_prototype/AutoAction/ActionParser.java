package com.covart.streaming_prototype.AutoAction;

import com.badlogic.gdx.Gdx;
import com.covart.streaming_prototype.UI.PositionController;
import com.covart.streaming_prototype.Utils.Easing.EasingBase;
import com.covart.streaming_prototype.Utils.Easing.EasingLinear;
import com.covart.streaming_prototype.Utils.Easing.EasingQuadIn;
import com.covart.streaming_prototype.Utils.Easing.EasingQuadInOut;
import com.covart.streaming_prototype.Utils.Easing.EasingQuadOut;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by lctseng on 2017/9/2.
 * For NCP project at COVART, NTU
 */

public class ActionParser {
    private class ActionControl {
        public boolean needWait = false;
    }


    private Executor executor;
    public ActionParser(Executor executor){
        this.executor = executor;
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
            else if(easingString.equals("QuadIn")){
                easing = new EasingQuadIn();
            }
            else if(easingString.equals("QuadOut")){
                easing = new EasingQuadOut();
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



    private DirectAndIncrementalAction parseDirectAndIncrementalAction(Class klass, ArrayList<String> params){
        if(params.size() == 1){
            // set value
            Constructor con = null;
            try {
                con = klass.getConstructor(float.class);
            } catch (NoSuchMethodException e) {
                Gdx.app.error("ActionParser", "Cannot find method!");
                e.printStackTrace();
                return null;
            }
            try {
                return (DirectAndIncrementalAction)(con.newInstance(Float.parseFloat(params.remove(0))));
            } catch (Exception e) {
                Gdx.app.error("ActionParser", "Cannot create instance!");
                e.printStackTrace();
                return null;
            }
        }
        else if(params.size() >= 2){
            // incremental
            // change and duration
            float change = Float.parseFloat(params.remove(0));
            float duration = Float.parseFloat(params.remove(0));
            // easing
            EasingBase easing = parseEasingStringFromParams(params);
            // create the class
            Constructor con = null;
            try {
                con = klass.getConstructor(float.class, float.class, EasingBase.class);
            } catch (NoSuchMethodException e) {
                Gdx.app.error("ActionParser", "Cannot find method!");
                e.printStackTrace();
                return null;
            }
            try {
                return (DirectAndIncrementalAction)(con.newInstance(change, duration, easing));
            } catch (Exception e) {
                Gdx.app.error("ActionParser", "Cannot create instance!");
                e.printStackTrace();
                return null;
            }
        }
        else{
            Gdx.app.error("ActionParser", "Unknown params size to handle: " + params.size());
            return null;
        }
    }

    private void parseConfig(ArrayList<String> params){
        String type = params.remove(0);
        if(type.equals("TimeFactor")){
            executor.setTimeFactor(Float.parseFloat(params.remove(0)));
        }
        else if((type.equals("WaitByDefault"))){
            executor.setWaitByDefault(params.remove(0).equals("true"));
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
                action = parseDirectAndIncrementalAction(ApertureAction.class,actionWords);
            }
            else if(actionName.equals("Focus")){
                action = parseDirectAndIncrementalAction(FocusAction.class,actionWords);
            }
            else if(actionName.equals("DataFov")){
                action = parseDirectAndIncrementalAction(DataFovAction.class,actionWords);
            }
            else if(actionName.equals("CameraStep")){
                action = parseDirectAndIncrementalAction(CameraStepAction.class,actionWords);
            }
            else if(actionName.equals("StPlaneRadius")){
                action = parseDirectAndIncrementalAction(StPlaneRadiusAction.class,actionWords);
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
                executor.addAction(action, control.needWait);
            }
        }
        else if(type.equals("Wait")){
            executor.addWait(Float.parseFloat(actionWords.remove(0)));
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
        control.needWait = executor.isWaitByDefault();
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
