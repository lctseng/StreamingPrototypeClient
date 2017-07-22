package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.covart.streaming_prototype.StringPool;

import java.util.ArrayList;

/**
 * Created by lctseng on 2017/4/29.
 * NTU COV-ART Lab, for NCP project
 */

public class UIManager implements Disposable{

    private static UIManager ourInstance;

    public static UIManager getInstance() {
        return ourInstance;
    }

    public static void initialize(){
        ourInstance = new UIManager();
    }

    public static void cleanup(){
        ourInstance.dispose();
    }

    private Stage stage;

    private ArrayList<UIComponent> components;

    private UIManager() {
        stage = new Stage(new ScreenViewport());
        components = new ArrayList<UIComponent>();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void draw(){
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        StringPool.addField("Graphics","W:" + Gdx.graphics.getWidth() + ", H:" + Gdx.graphics.getHeight());
        StringPool.addField("Screen UI","W:" + stage.getWidth());
    }

    public void resetViewport(){


        stage.getViewport().setWorldSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
    }

    public InputProcessor getInputProcessor(){
        return stage;
    }

    public void registerUI(UIComponent component){
        components.add(component);
        component.registerActors(stage);
        component.start();
    }

    public void onAppStateChanged(){
        for(UIComponent ui : components){
            ui.onAppStateChanged();
        }
    }
}
