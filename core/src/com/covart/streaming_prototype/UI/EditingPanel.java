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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.covart.streaming_prototype.ConfigManager;

import java.util.ArrayList;
import java.util.Locale;

import static com.covart.streaming_prototype.UI.PositionController.Direction.BACKWARD;
import static com.covart.streaming_prototype.UI.PositionController.Direction.DOWN;
import static com.covart.streaming_prototype.UI.PositionController.Direction.FORWARD;
import static com.covart.streaming_prototype.UI.PositionController.Direction.LEFT;
import static com.covart.streaming_prototype.UI.PositionController.Direction.NONE;
import static com.covart.streaming_prototype.UI.PositionController.Direction.RIGHT;
import static com.covart.streaming_prototype.UI.PositionController.Direction.UP;

/**
 * Created by lctseng on 2017/8/13.
 * For NCP project at COVART, NTU
 */

public class EditingPanel extends UIComponent {
    private Stage stage;
    private ScrollPane pane;
    private Table canvas;
    private float fadeTime = 0.2f;

    private float commonRowHeight = 70f;
    private int buttonWidth = 200;
    private float slideBarSize = 50f;

    private ArrayList<ModelButton> modelButtons;

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
        pane.setWidth(Gdx.graphics.getWidth());
        pane.setDebug(true);
        pane.setVisible(true);
    }

    private void createCanvas() {
        canvas = new Table();
        canvas.setX(Gdx.graphics.getWidth() / 2);
        canvas.setY(0);
        canvas.setWidth(Gdx.graphics.getWidth() / 2);
        canvas.setHeight(commonRowHeight);
        canvas.setDebug(true);
        canvas.top();
        canvas.row().height(commonRowHeight);
        addComponents();
    }

    @Override
    void setStage(Stage stage) {
        this.stage = stage;
    }

    public void show(){
        if(pane != null) {
            pane.addAction(Actions.sequence(Actions.alpha(0), Actions.show(), Actions.fadeIn(fadeTime)));
        }
    }

    public void hide(){
        if(pane != null) {
            pane.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
        }
    }

    private void refreshModelList(){
        if(pane != null){
            pane.remove();
        }
        createCanvas();
        createPane();
        this.stage.addActor(pane);
    }

    private void addComponents(){
        for(int modelId : ConfigManager.getEditingModelList()){
            Cell<ModelButton> cell = addModelItemButton(modelId).width(buttonWidth);
            modelButtons.add(cell.getActor());
        }
    }

    private Cell<ModelButton> addModelItemButton(final int modelId){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int currentId = ConfigManager.getEditingCurrentModelId();
                ConfigManager.setEditingCurrentModelId(modelId);
                if( currentId != modelId){
                    onModelChanged();
                }
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
    }
}
