package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

    private float commonRowHeight = 200f;
    private int buttonWidth = 200;

    private ArrayList<ModelButton> newModelButtons;
    private ArrayList<ModelButton> currentModelButtons;

    private Label addCancelLabel;
    private Texture addCancelTexture;
    private Label addConfirmLabel;


    private boolean needRefreshCurrentList = false;
    private boolean needRefreshNewList = false;

    public EditingPanel(){
        newModelButtons = new ArrayList<ModelButton>();
        currentModelButtons = new ArrayList<ModelButton>();
        setupCancelLabel();
        setupConfirmLabel();

    }

    private void setupCancelLabel(){
        int labelWidth = 256, labelHeight = 256;

        // style
        Label.LabelStyle style = new Label.LabelStyle(largeFont, Color.WHITE);

        // label
        addCancelLabel = new Label("", style);
        addCancelLabel.setX(Gdx.graphics.getWidth() - labelWidth);
        addCancelLabel.setY(Gdx.graphics.getHeight() - labelHeight);
        addCancelLabel.setWidth(labelWidth);
        addCancelLabel.setHeight(labelHeight);
        addCancelTexture = new Texture(Gdx.files.internal("cancel.png"));
        addCancelLabel.getStyle().background = new Image(addCancelTexture).getDrawable();
        addCancelLabel.setVisible(false);
    }

    private void setupConfirmLabel(){
        addConfirmLabel = new Label("Waiting for server to confirm the change...", largeLabelStyle);
        addConfirmLabel.setX(600);
        addConfirmLabel.setY(0);
        addConfirmLabel.setVisible(false);
    }

    @Override
    void start() {
        super.start();
        this.stage.addActor(addCancelLabel);
        this.stage.addActor(addConfirmLabel);
        refreshModelList();
    }

    private ScrollPane createPane(Table canvas){
        ScrollPane pane = new ScrollPane(canvas);
        pane.setX(600);
        pane.setY(0);
        pane.setWidth(Gdx.graphics.getWidth()/2);
        pane.setHeight(commonRowHeight);
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
        if(needRefreshCurrentList){
            needRefreshCurrentList = false;
            refreshCurrentModelList();
        }
        if(needRefreshNewList){
            needRefreshNewList = false;
            refreshNewModelList();
        }
    }

    private void refreshModelList(){
        refreshCurrentModelList();
        refreshNewModelList();
    }

    private void refreshCurrentModelList(){
        if(currentButtonPane != null){
            currentButtonPane.remove();
        }

        disposeCurrentModelButtons();

        currentsButtonCanvas = createCanvas();
        currentButtonPane = createPane(currentsButtonCanvas);
        addCurrentModelComponents();
        if(ConfigManager.getEditingState() == ConfigManager.EditingState.SelectOperation || ConfigManager.getEditingState() == ConfigManager.EditingState.MovingModel ){
            currentsButtonCanvas.setVisible(true);
            currentButtonPane.setY(0);
        }
        else{
            currentsButtonCanvas.setVisible(false);
            currentButtonPane.setY(-1000);
        }


        this.stage.addActor(currentButtonPane);
    }

    private void refreshNewModelList(){
        if(newButtonPane != null){
            newButtonPane.remove();
        }

        disposeNewModelButtons();

        newButtonCanvas = createCanvas();
        newButtonPane = createPane(newButtonCanvas);
        addNewModelComponents();
        if(ConfigManager.getEditingState() == ConfigManager.EditingState.SelectAddingModel || ConfigManager.getEditingState() == ConfigManager.EditingState.SelectAddingPosition ){
            newButtonCanvas.setVisible(true);
            newButtonPane.setY(0);
        }
        else{
            newButtonCanvas.setVisible(false);
            newButtonPane.setY(-1000);
        }

        this.stage.addActor(newButtonPane);
    }

    private void addNewModelComponents(){
        List<Integer> idList = ConfigManager.getEditingNewModelIdList();
        if(idList != null && !idList.isEmpty()) {
            // title
            Label title = new Label("Adding New model", largeLabelStyle);
            newButtonCanvas.add(title);
            // Add cancel button
            addCancelAddButton();
            // buttons
            for (Integer modelId : idList) {
                Cell<ModelButton> cell = addNewModelItemButton(modelId).width(buttonWidth);
                newModelButtons.add(cell.getActor());
            }


        }
        else{
            Label alert = new Label("Loading new model lists from server...", largeLabelStyle);
            newButtonCanvas.add(alert);
        }
    }

    private void addCurrentModelComponents(){
        List<Integer> idList = ConfigManager.getEditingCurrentModelIdList();
        if(idList != null && !idList.isEmpty()) {
            // title
            Label title = new Label("Moving existing model", largeLabelStyle);
            currentsButtonCanvas.add(title);
            // Add New Model button
            addNewModelButton();
            // buttons
            for (Integer modelId : idList) {
                Cell<ModelButton> cell = addCurrentModelItemButton(modelId).width(buttonWidth);
                currentModelButtons.add(cell.getActor());
            }
        }
        else{
            Label alert = new Label("Loading current model lists from server...", largeLabelStyle);
            currentsButtonCanvas.add(alert);
        }
    }

    public void showAddingCancel(){
        addCancelLabel.addAction(Actions.sequence(Actions.alpha(0), Actions.show(), Actions.fadeIn(fadeTime)));
    }

    public void hideAddingCancel(){
        addCancelLabel.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
    }

    public void hideAddingModels(){
        // hide new buttons
        //newButtonCanvas.setVisible(false);
        newButtonCanvas.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
        newButtonPane.setY(-1000);
    }

    public void showAddingModels(){
        //newButtonCanvas.setVisible(true);
        newButtonCanvas.addAction(Actions.sequence(Actions.alpha(0), Actions.show(), Actions.fadeIn(fadeTime)));
        newButtonPane.setY(0);
    }

    public void showCurrentModels(){
        // show current buttons
        //currentsButtonCanvas.setVisible(true);
        currentsButtonCanvas.addAction(Actions.sequence(Actions.alpha(0), Actions.show(), Actions.fadeIn(fadeTime)));
        currentButtonPane.setY(0);
    }

    public void hideCurrentModels(){
        // hide current buttons
        //currentsButtonCanvas.setVisible(false);
        currentsButtonCanvas.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
        currentButtonPane.setY(-1000);
    }

    public void goToAddingMode(){
        hideCurrentModels();
        showAddingModels();

        clearAllIndex();
    }

    public void goToSelectOperationMode(){
        showCurrentModels();
        hideAddingModels();

        clearAllIndex();
    }

    public void goToConfirmAddingMode(){
        hideAddingModels();
        addConfirmLabel.setVisible(true);

        clearAllIndex();
    }

    public void finishConfirmAddingMode(){
        addConfirmLabel.setVisible(false);
        needRefreshCurrentList = true;
        goToSelectOperationMode();
    }

    private void addNewModelButton(){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEditingState(ConfigManager.EditingState.SelectAddingModel);
                goToAddingMode();
            }
        };
        addButton(currentsButtonCanvas,"[Add New Model]", "add.png", listener).width(buttonWidth);;
    }

    public void clearAllIndex(){
        ConfigManager.setEditingCurrentModelIndex(-1);
        ConfigManager.setEditingNewModelIndex(-1);
        for(ModelButton button : currentModelButtons){
            button.onModelChanged(-1);
        }
        for(ModelButton button : newModelButtons){
            button.onModelChanged(-1);
        }
    }

    private void addCancelAddButton(){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEditingState(ConfigManager.EditingState.SelectOperation);
                ConfigManager.setEditingNewModelId(-1);
                goToSelectOperationMode();
            }
        };
        addButton(newButtonCanvas, "[Back to select]", "back.png", listener).width(buttonWidth);;
    }

    private Cell<ModelButton> addCurrentModelItemButton(final int modelId){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(ConfigManager.getEditingState() == ConfigManager.EditingState.SelectOperation || ConfigManager.getEditingState() == ConfigManager.EditingState.MovingModel) {
                    int lastIndex = ConfigManager.getEditingCurrentModelIndex();
                    if (ConfigManager.getEditingCurrentModelId() == modelId) {
                        // click on same model id, toggle it
                        ConfigManager.setEditingCurrentModelId(-1);
                    } else {
                        ConfigManager.setEditingCurrentModelId(modelId);
                    }
                    onCurrentModelChanged(lastIndex);
                }
                else{
                    Gdx.app.log("Editing", "Current model button is no allowed in this state: " + ConfigManager.getEditingState());
                }
            }
        };
        ModelButton button = new CurrentModelButton(modelId);
        button.addListener(listener);
        return currentsButtonCanvas.add(button);
    }

    private Cell<ModelButton> addNewModelItemButton(final int modelId){
        EventListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(ConfigManager.getEditingState() == ConfigManager.EditingState.SelectAddingModel || ConfigManager.getEditingState() == ConfigManager.EditingState.SelectAddingPosition ) {
                    int lastIndex = ConfigManager.getEditingNewModelIndex();
                    if (ConfigManager.getEditingNewModelId() == modelId) {
                        // click on same model id, toggle it
                        ConfigManager.setEditingNewModelId(-1);
                    } else {
                        ConfigManager.setEditingNewModelId(modelId);
                    }
                    onNewModelChanged(lastIndex);
                }
                else{
                    Gdx.app.log("Editing", "New model button is no allowed in this state: " + ConfigManager.getEditingState());
                }
            }
        };
        ModelButton button = new NewModelButton(modelId);
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

    private Cell<VerticalImageTextButton> addButton(Table canvas, String buttonText, String filename , EventListener listener){
        VerticalImageTextButton button = new VerticalImageTextButton(buttonText, filename);
        button.getStyle().font = largeFont;
        button.setStyle(button.getStyle());
        button.addListener(listener);
        return canvas.add(button);
    }

    private void onCurrentModelChanged(int lastIndex){
        int currentId = ConfigManager.getEditingCurrentModelId();
        for(ModelButton button : currentModelButtons){
            button.onModelChanged(currentId);
        }
        ConfigManager.getApp().onEditingCurrentModelChanged(lastIndex);
    }

    private void onNewModelChanged(int lastIndex){
        int currentId = ConfigManager.getEditingNewModelId();
        for(ModelButton button : newModelButtons){
            button.onModelChanged(currentId);
        }
        ConfigManager.getApp().onEditingNewModelChanged(lastIndex);
    }

    public boolean isNeedRefreshCurrentList() {
        return needRefreshCurrentList;
    }

    public void setNeedRefreshCurrentList(boolean needRefreshCurrentList) {
        this.needRefreshCurrentList = needRefreshCurrentList;
    }

    public boolean isNeedRefreshNewList() {
        return needRefreshNewList;
    }

    public void setNeedRefreshNewList(boolean needRefreshNewList) {
        this.needRefreshNewList = needRefreshNewList;
    }

    @Override
    public void dispose() {
        addCancelTexture.dispose();
        disposeNewModelButtons();
        disposeCurrentModelButtons();
        super.dispose();
    }

    private void disposeNewModelButtons(){
        for(ModelButton btn : newModelButtons){
            btn.dispose();
        }
        newModelButtons.clear();
    }

    private void disposeCurrentModelButtons(){
        for(ModelButton btn : currentModelButtons){
            btn.dispose();
        }
        currentModelButtons.clear();
    }


}
