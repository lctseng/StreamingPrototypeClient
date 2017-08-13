package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.covart.streaming_prototype.ConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lctseng on 2017/8/13.
 * For NCP project at COVART, NTU
 */

public class EditingPanel extends UIComponent {
    private Stage stage;
    private ScrollPane pane;
    private Table canvas;
    private float fadeTime = 0.2f;

    private boolean visible;

    private float commonRowHeight = 70f;
    private int buttonWidth = 200;

    private ArrayList<ModelButton> modelButtons;

    private boolean needRefreshList = false;

    public EditingPanel(){
        modelButtons = new ArrayList<ModelButton>();
    }

    @Override
    void start() {
        super.start();
        refreshModelList();
    }

    private void createPane(){
        pane = new ScrollPane(canvas);
        pane.setX(0);
        pane.setY(0);
        pane.setWidth(Gdx.graphics.getWidth()/2);
        pane.setDebug(false);
        pane.setVisible(visible);
    }

    private void createCanvas() {
        canvas = new Table();
        canvas.setX(0);
        canvas.setY(0);
        canvas.setHeight(commonRowHeight);
        canvas.setDebug(false);
        canvas.top();
        canvas.row().height(commonRowHeight);
        addComponents();
    }

    @Override
    void setStage(Stage stage) {
        this.stage = stage;
    }

    public void show(){
        visible = true;
        if(pane != null) {
            pane.addAction(Actions.sequence(Actions.alpha(0), Actions.show(), Actions.fadeIn(fadeTime)));
        }
    }

    public void hide(){
        visible = false;
        if(pane != null) {
            pane.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
        }
    }


    public void checkRefreshList(){
        if(needRefreshList){
            needRefreshList = false;
            refreshModelList();
        }
    }

    private void refreshModelList(){
        if(pane != null){
            pane.remove();
        }
        modelButtons.clear();
        createCanvas();
        createPane();
        this.stage.addActor(pane);
    }

    private void addComponents(){
        List<Integer> idList = ConfigManager.getEditingModelIdList();
        if(idList != null && !idList.isEmpty()) {
            for (Integer modelId : idList) {
                Cell<ModelButton> cell = addModelItemButton(modelId).width(buttonWidth);
                modelButtons.add(cell.getActor());
            }
        }
        else{
            Label alert = new Label("Loading model lists from server...", largeLabelStyle);
            canvas.add(alert);
        }
    }

    private Cell<ModelButton> addModelItemButton(final int modelId){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if( ConfigManager.getEditingCurrentModelId() == modelId){
                    // click on same model id, toggle it
                    ConfigManager.setEditingCurrentModelId(-1);
                }
                else{
                    ConfigManager.setEditingCurrentModelId(modelId);
                }
                onModelChanged();
            }
        };
        ModelButton button = new ModelButton(modelId, skin);
        button.getStyle().font = largeFont;
        button.addListener(listener);
        return canvas.add(button);
    }


    private Cell<TextButton> addButton(String buttonText, EventListener listener){
        TextButton button = new TextButton(buttonText, skin);
        button.getStyle().font = largeFont;
        button.setStyle(button.getStyle());
        button.addListener(listener);
        return canvas.add(button);
    }

    private void onModelChanged(){
        for(ModelButton button : modelButtons){
            button.onModelChanged();
        }
        ConfigManager.getApp().onEditingModelChanged();
    }

    public boolean isNeedRefreshList() {
        return needRefreshList;
    }

    public void setNeedRefreshList(boolean needRefreshList) {
        this.needRefreshList = needRefreshList;
    }
}
