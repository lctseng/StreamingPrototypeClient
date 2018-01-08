package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
    private ScrollPane newButtonPane;
    private Table newButtonCanvas;
    private ScrollPane currentButtonPane;
    private Table currentsButtonCanvas;
    private float fadeTime = 0.2f;

    private boolean visible;

    private float commonRowHeight = 100f;
    private int buttonWidth = 200;

    private ArrayList<ModelButton> newModelButtons;
    private ArrayList<ModelButton> currentModelButtons;


    private boolean needRefreshList = false;

    public EditingPanel(){
        newModelButtons = new ArrayList<ModelButton>();
        currentModelButtons = new ArrayList<ModelButton>();
    }

    @Override
    void start() {
        super.start();
        refreshModelList();
    }

    private ScrollPane createPane(Table canvas){
        ScrollPane pane = new ScrollPane(canvas);
        pane.setX(600);
        pane.setY(0);
        pane.setWidth(Gdx.graphics.getWidth()/2);
        pane.setDebug(false);
        pane.setVisible(visible);
        return pane;
    }

    private Table createCanvas() {
        Table canvas = new Table();
        canvas.setX(0);
        canvas.setY(0);
        canvas.setHeight(commonRowHeight);
        canvas.setDebug(false);
        canvas.left();
        canvas.bottom();
        canvas.row().height(commonRowHeight);
        return canvas;

    }

    @Override
    void setStage(Stage stage) {
        this.stage = stage;
    }

    public void show(){
        visible = true;
        if(newButtonPane != null) {
            newButtonPane.addAction(Actions.sequence(Actions.alpha(0), Actions.show(), Actions.fadeIn(fadeTime)));
        }
        if(currentButtonPane != null) {
            currentButtonPane.addAction(Actions.sequence(Actions.alpha(0), Actions.show(), Actions.fadeIn(fadeTime)));
        }
    }

    public void hide(){
        visible = false;
        if(newButtonPane != null) {
            newButtonPane.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
        }
        if(currentButtonPane != null) {
            currentButtonPane.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
        }
    }


    public void checkRefreshList(){
        if(needRefreshList){
            needRefreshList = false;
            refreshModelList();
        }
    }

    private void refreshModelList(){
        if(newButtonPane != null){
            newButtonPane.remove();
        }
        if(currentButtonPane != null){
            currentButtonPane.remove();
        }
        newModelButtons.clear();
        currentModelButtons.clear();

        newButtonCanvas = createCanvas();
        newButtonPane = createPane(newButtonCanvas);
        addNewModelComponents();
        newButtonCanvas.setVisible(false);
        newButtonPane.setY(-1000);

        currentsButtonCanvas = createCanvas();
        currentButtonPane = createPane(currentsButtonCanvas);
        addCurrentModelComponents();
        currentsButtonCanvas.setVisible(true);



        this.stage.addActor(newButtonPane);
        this.stage.addActor(currentButtonPane);
    }

    private void addNewModelComponents(){
        List<Integer> idList = ConfigManager.getEditingNewModelIdList();
        if(idList != null && !idList.isEmpty()) {
            Label title = new Label("Adding New model", largeLabelStyle);
            newButtonCanvas.add(title);
            for (Integer modelId : idList) {
                Cell<ModelButton> cell = addNewModelItemButton(modelId).width(buttonWidth);
                newModelButtons.add(cell.getActor());
            }
            // Add cancel button
            addCancelAddButton();

        }
        else{
            Label alert = new Label("Loading new model lists from server...", largeLabelStyle);
            newButtonCanvas.add(alert);
        }
    }

    private void addCurrentModelComponents(){
        List<Integer> idList = ConfigManager.getEditingCurrentModelIdList();
        if(idList != null && !idList.isEmpty()) {
            Label title = new Label("Moving existing model", largeLabelStyle);
            currentsButtonCanvas.add(title);
            for (Integer modelId : idList) {
                Cell<ModelButton> cell = addCurrentModelItemButton(modelId).width(buttonWidth);
                currentModelButtons.add(cell.getActor());
            }
            // Add New Model button
            addNewModelButton();

        }
        else{
            Label alert = new Label("Loading current model lists from server...", largeLabelStyle);
            currentsButtonCanvas.add(alert);
        }
    }

    private void addNewModelButton(){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEditingState(ConfigManager.EditingState.SelectAddingModel);
                // hide current buttons
                currentsButtonCanvas.setVisible(false);
                currentButtonPane.setY(-1000);
                // show new buttons
                newButtonCanvas.setVisible(true);
                newButtonPane.setY(0);
            }
        };
        addButton(currentsButtonCanvas,"[Add New Model]", listener).width(buttonWidth);;
    }

    private void addCancelAddButton(){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEditingState(ConfigManager.EditingState.SelectOperation);
                // show current buttons
                currentsButtonCanvas.setVisible(true);
                currentButtonPane.setY(0);
                // hide new buttons
                newButtonCanvas.setVisible(false);
                newButtonPane.setY(-1000);
            }
        };
        addButton(newButtonCanvas, "[Back to select]", listener).width(buttonWidth);;
    }

    private Cell<ModelButton> addCurrentModelItemButton(final int modelId){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(ConfigManager.getEditingState() == ConfigManager.EditingState.SelectOperation) {
                    int lastIndex = ConfigManager.getEditingCurrentModelIndex();
                    if (ConfigManager.getEditingCurrentModelId() == modelId) {
                        // click on same model id, toggle it
                        ConfigManager.setEditingCurrentModelId(-1);
                    } else {
                        ConfigManager.setEditingCurrentModelId(modelId);
                    }
                    onCurrentModelChanged(lastIndex);
                }
            }
        };
        ModelButton button = new ModelButton(modelId, skin);
        button.getStyle().font = largeFont;
        button.addListener(listener);
        return currentsButtonCanvas.add(button);
    }

    private Cell<ModelButton> addNewModelItemButton(final int modelId){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(ConfigManager.getEditingState() == ConfigManager.EditingState.SelectAddingModel) {
                    int lastIndex = ConfigManager.getEditingNewModelIndex();
                    if (ConfigManager.getEditingNewModelId() == modelId) {
                        // click on same model id, toggle it
                        ConfigManager.setEditingNewModelId(-1);
                    } else {
                        ConfigManager.setEditingNewModelId(modelId);
                    }
                    onNewModelChanged(lastIndex);
                }
            }
        };
        ModelButton button = new ModelButton(modelId, skin);
        button.getStyle().font = largeFont;
        button.addListener(listener);
        return newButtonCanvas.add(button);
    }


    private Cell<TextButton> addButton(Table canvas, String buttonText, EventListener listener){
        TextButton button = new TextButton(buttonText, skin);
        button.getStyle().font = largeFont;
        button.setStyle(button.getStyle());
        button.addListener(listener);
        return canvas.add(button);
    }

    private void onCurrentModelChanged(int lastIndex){
        for(ModelButton button : newModelButtons){
            button.onModelChanged();
        }
        ConfigManager.getApp().onEditingCurrentModelChanged(lastIndex);
    }

    private void onNewModelChanged(int lastIndex){
        for(ModelButton button : newModelButtons){
            button.onModelChanged();
        }
        ConfigManager.getApp().onEditingNewModelChanged(lastIndex);
    }

    public boolean isNeedRefreshList() {
        return needRefreshList;
    }

    public void setNeedRefreshList(boolean needRefreshList) {
        this.needRefreshList = needRefreshList;
    }
}
