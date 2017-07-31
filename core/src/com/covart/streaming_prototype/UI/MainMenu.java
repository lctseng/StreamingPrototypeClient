package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Image.Display;
import com.covart.streaming_prototype.StreamingPrototype.State;

import java.util.Locale;


/**
 * Created by lctseng on 2017/4/29.
 * NTU COV-ART Lab, for NCP project
 */

public class MainMenu extends UIComponent {


    private Table canvas;

    private int tableColumnSpan = 2;
    private int buttonWidth = 250;

    private float commonRowHeight = 70f;
    private float slideBarSize = 50f;

    private Label startStopLabel;
    private TextButton startStopButton;

    private TextButton editingModeButton;

    private TextButton canvasControlButton;

    private float fadeTime = 0.2f;

    public MainMenu(){


        // canvas
        createCanvas();

        // canvas control button
        createCanvasControlButton();

        show();
    }

    private void createCanvas(){
        canvas = new Table();
        canvas.setX(0);
        canvas.setWidth(Gdx.graphics.getWidth());
        canvas.setHeight(1000);
        canvas.setDebug(false);
        canvas.setVisible(false);
        canvas.top();
        canvas.row().height(commonRowHeight);
        addComponents();
        canvas.setY(Gdx.graphics.getHeight() - canvas.getHeight() - 150);
    }

    private void createCanvasControlButton(){
        canvasControlButton = new TextButton("Hide Menu", skin);
        canvasControlButton.setWidth(200);
        canvasControlButton.setHeight(100);
        canvasControlButton.setX((Gdx.graphics.getWidth() - canvasControlButton.getWidth())/2);
        canvasControlButton.setY(Gdx.graphics.getHeight() - canvasControlButton.getHeight());
        canvasControlButton.getStyle().font = largeFont;
        canvasControlButton.setStyle(canvasControlButton.getStyle());
        canvasControlButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(canvas.isVisible()){
                    hide();
                    canvasControlButton.setText("Open Menu");
                }
                else{
                    show();
                    canvasControlButton.setText("Hide Menu");
                }

            }
        });
    }

    private void show(){
        canvas.addAction(Actions.sequence(Actions.alpha(0), Actions.show(),Actions.fadeIn(fadeTime)));
    }

    private void hide(){
        canvas.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
    }

    @Override
    void onAppStateChanged() {
        updateStartStopText();
    }

    private void addComponents(){
        addStartStopUI();
        canvas.row().height(commonRowHeight);

        addIPSelectUI();
        addChangeSceneUI();
        canvas.row().height(commonRowHeight);

        //addDisplayModeSelectUI();
        addDisplayModeToggleUI();
        canvas.row().height(commonRowHeight);

        addStopOnDisconnectedUI();
        canvas.row().height(commonRowHeight);

        addFocusChangeUI();
        addStepChangeUI();
        addApertureSizeUI();
        canvas.row().height(commonRowHeight);

        addDataCameraFOVUI();
        addFreeUnusedTextureControlUI();
        addEyeDisparityFactorUI();
        canvas.row().height(commonRowHeight);

        addSensorReportIntervalUI();
        canvas.row().height(commonRowHeight);

        addEditingReportIntervalUI();
        addEyeRotationToTranslationRatioUI();
        addManualMoveUI();
        canvas.row().height(commonRowHeight);

        addButtons();

    }

    private void addStartStopUI(){
        // create label
        startStopLabel = new Label(getStartStopLabelText(), largeLabelStyle);

        startStopButton = new TextButton(getStartStopButtonText(), skin);
        startStopButton.getStyle().font = largeFont;
        startStopButton.setStyle(startStopButton.getStyle());
        startStopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                switch (getAppState()){
                    case Stopped:
                        ConfigManager.getApp().onStartCalled();
                        break;
                    case Running:
                        ConfigManager.getApp().onStopCalled();
                        break;
                }
                updateStartStopText();

            }
        });
        canvas.add(startStopLabel);
        canvas.add(startStopButton).width(buttonWidth);
    }

    private void updateStartStopText(){
        startStopLabel.setText(getStartStopLabelText());
        startStopButton.setText(getStartStopButtonText());
    }

    private String getStartStopButtonText(){
        switch (getAppState()){
            case Running:
                return "Stop";
            case ShuttingDown:
                return "Stopping";
            case Stopped:
                return "Start";
            default:
                return "Unknown";
        }
    }

    private String getStartStopLabelText(){
        switch (getAppState()){
            case Running:
                return "Running";
            case ShuttingDown:
                return "Shutting Down";
            case Stopped:
                return "Stopped";
            default:
                return "Unknown";
        }
    }

    private State getAppState(){
        return ConfigManager.getApp().getState();
    }


    private void addIPSelectUI(){
        // create label
        Label label = new Label("Server IP:", largeLabelStyle);

        // create select box
        final SelectBox<String> selectBox = new SelectBox<String>(skin);
        selectBox.getStyle().font = largeFont;
        selectBox.getStyle().listStyle.font = largeFont;

        // add select list listener
        selectBox.setItems(ConfigManager.getServerList());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setSelectedIP(selectBox.getSelected());
            }
        });


        canvas.add(label);
        canvas.add(selectBox).colspan(tableColumnSpan - 1);


    }

    private void addChangeSceneUI(){
        // label
        Label name = new Label("Scene index:", largeLabelStyle);

        // create select box
        final SelectBox<Integer> selectBox = new SelectBox<Integer>(skin);
        selectBox.getStyle().font = largeFont;
        selectBox.getStyle().listStyle.font = largeFont;

        // add select list listener
        selectBox.setItems(ConfigManager.getSceneList());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setSceneIndex(selectBox.getSelected());
                ConfigManager.getApp().sceneChanged = true;
            }
        });

        canvas.add(name);
        canvas.add(selectBox).colspan(tableColumnSpan - 1);
    }


    private void addDisplayModeSelectUI(){
        // label
        Label name = new Label("Display mode:", largeLabelStyle);

        // create select box
        final SelectBox<Display.Mode> selectBox = new SelectBox<Display.Mode>(skin);
        selectBox.getStyle().font = largeFont;
        selectBox.getStyle().listStyle.font = largeFont;

        // add select list listener
        selectBox.setItems(ConfigManager.getDisplayModeList());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setDisplayMode(selectBox.getSelected());
            }
        });

        canvas.add(name);
        canvas.add(selectBox).colspan(tableColumnSpan - 1);
    }

    private void addStopOnDisconnectedUI(){
        // label
        Label name = new Label("Stop on disconnected:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
        box.setChecked(ConfigManager.isStopOnDisconnected());
        updateCheckBoxText(box);
        enlargeCheckBoxFont(box);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setStopOnDisconnected(box.isChecked());
                updateCheckBoxText(box);
            }
        });


        canvas.add(name);
        canvas.add(box).colspan(tableColumnSpan - 1);
    }

    private void addDisplayModeToggleUI(){
        // label
        Label name = new Label("VR Mode enabled:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
        box.setChecked(ConfigManager.getDisplayMode() == Display.Mode.VR);
        updateCheckBoxText(box);
        enlargeCheckBoxFont(box);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setDisplayMode(box.isChecked() ? Display.Mode.VR : Display.Mode.NORMAL);
                updateCheckBoxText(box);
            }
        });


        canvas.add(name);
        canvas.add(box).colspan(tableColumnSpan - 1);
    }


    private void addFocusChangeUI(){
        // label
        final Label name = new Label(getFocusRatioText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.100f, 6.0f, 0.001f, false, skin);
        slider.setValue(ConfigManager.getFocusChangeRatio());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setFocusChangeRatio(slider.getValue());
                name.setText(getFocusRatioText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getFocusRatioText(){
        return String.format(Locale.TAIWAN,"Focus ratio: %.3f",ConfigManager.getFocusChangeRatio());
    }


    private void addEyeDisparityFactorUI(){
        // label
        final Label name = new Label(getEyeDisparityFactorText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(1.000f, 50.0f, 0.01f, false, skin);
        slider.setValue(ConfigManager.getEyeDisparityFactor());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEyeDisparityFactor(slider.getValue());
                name.setText(getEyeDisparityFactorText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getEyeDisparityFactorText(){
        return String.format(Locale.TAIWAN,"Eye disparity: %.3f",ConfigManager.getEyeDisparityFactor());
    }

    private void addEyeRotationToTranslationRatioUI(){
        // label
        final Label name = new Label(getEyeRotationToTranslationRatioText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.00f, 2.0f, 0.01f, false, skin);
        slider.setValue(ConfigManager.getEyeRotationToTranslationRatio());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEyeRotationToTranslationRatio(slider.getValue());
                name.setText(getEyeRotationToTranslationRatioText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getEyeRotationToTranslationRatioText(){
        return String.format(Locale.TAIWAN,"Rotation to translation: %.3f",ConfigManager.getEyeRotationToTranslationRatio());
    }

    private void addStepChangeUI(){
        // label
        final Label name = new Label(getStepChangeText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.1f, 6.0f, 0.1f, false, skin);
        slider.setValue(ConfigManager.getCameraStep());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setCameraStep(slider.getValue());
                name.setText(getStepChangeText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getStepChangeText(){
        return String.format(Locale.TAIWAN,"Step ratio: %.5f",ConfigManager.getCameraStep());
    }

    private void addFreeUnusedTextureControlUI(){
        // label
        final Label name = new Label(getFreeUnusedTextureControlText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0, ConfigManager.getNumOfLFs(), 1, false, skin);
        slider.setValue(ConfigManager.getFreeUnusedTextureThreshold());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setFreeUnusedTextureThreshold(((int)slider.getValue()));
                name.setText(getFreeUnusedTextureControlText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
    }

    private String getFreeUnusedTextureControlText(){
        return String.format(Locale.TAIWAN,"Free texture threshold: %d", ConfigManager.getFreeUnusedTextureThreshold());
    }


    private void addSensorReportIntervalUI(){
        // label
        final Label name = new Label(getSensorReportIntervalText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.01f, 2f, 0.001f, false, skin);
        slider.setValue(ConfigManager.getSensorReportInterval());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setSensorReportInterval(slider.getValue());
                name.setText(getSensorReportIntervalText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
    }

    private String getSensorReportIntervalText(){
        return String.format(Locale.TAIWAN,"Sensor interval: %.3f", ConfigManager.getSensorReportInterval());
    }

    private void addApertureSizeUI(){
        // label
        final Label name = new Label(getApertureSizeText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.0f, 0.5f, 0.0001f, false, skin);
        slider.setValue(ConfigManager.getApertureSize());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setApertureSize(slider.getValue());
                name.setText(getApertureSizeText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
    }

    private String getApertureSizeText(){
        return String.format(Locale.TAIWAN,"Aperture size: %.3f", ConfigManager.getApertureSize());
    }


    private void addDataCameraFOVUI(){
        // label
        final Label name = new Label(getDataCameraFOVText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(5.0f, 180.0f, 1f, false, skin);
        slider.setValue(ConfigManager.getDataCameraFOV());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setDataCameraFOV(slider.getValue());
                name.setText(getDataCameraFOVText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
    }

    private String getDataCameraFOVText(){
        return String.format(Locale.TAIWAN,"Data FOV: %.3f", ConfigManager.getDataCameraFOV());
    }

    private void addEditingReportIntervalUI(){
        // label
        final Label name = new Label(getEditingReportIntervalText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.01f, 2.0f, 0.01f, false, skin);
        slider.setValue(ConfigManager.getEditingReportInterval());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEditingReportInterval(slider.getValue());
                name.setText(getEditingReportIntervalText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
    }

    private String getEditingReportIntervalText(){
        return String.format(Locale.TAIWAN,"Editing report interval: %.3f", ConfigManager.getEditingReportInterval());
    }

    private void addManualMoveUI(){
        // label
        Label name = new Label("Manually Move:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
        box.setChecked(ConfigManager.isEnableManuallyMove());
        updateCheckBoxText(box);
        enlargeCheckBoxFont(box);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEnableManuallyMove(box.isChecked());
                if(box.isChecked()){
                    ConfigManager.getApp().positionController.show();
                }
                else{
                    ConfigManager.getApp().positionController.hide();
                }

                updateCheckBoxText(box);
            }
        });


        canvas.add(name);
        canvas.add(box).colspan(tableColumnSpan - 1);
    }



    private void addButtons(){
        addRecenterButton().width(buttonWidth);
        addSaveFrameButton().width(buttonWidth);
        addEditingModeButton().width(buttonWidth);
    }

    private Cell<TextButton> addButton(String buttonText, EventListener listener){
        TextButton button = new TextButton(buttonText, skin);
        button.getStyle().font = largeFont;
        button.setStyle(button.getStyle());
        button.addListener(listener);
        return canvas.add(button);
    }

    private Cell<TextButton> addRecenterButton(){
        return addButton("Re-center", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.getApp().recenter();
            }
        });
    }

    private Cell<TextButton> addSaveFrameButton(){
        return addButton("Save frame", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.getApp().setSaveFrameRequested(true);
            }
        });
    }

    private Cell<TextButton> addEditingModeButton(){
        // create label
        editingModeButton = new TextButton(getEditingModeButtonText(), skin);
        editingModeButton.getStyle().font = largeFont;
        editingModeButton.setStyle(editingModeButton.getStyle());
        editingModeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.toggleEditingModeEnabled();
                if(ConfigManager.isEditingModeEnabled()){
                    // enable editing
                    ConfigManager.getApp().startEditingMode();
                }
                else{
                    // disable editing
                    ConfigManager.getApp().finishEditingMode();
                }

                updateEditingModeText();

            }
        });
        return canvas.add(editingModeButton);
    }

    private void updateEditingModeText(){
        editingModeButton.setText(getEditingModeButtonText());
    }

    private String getEditingModeButtonText(){
        if(ConfigManager.isEditingModeEnabled()){
            return "Finish Editing";
        }
        else{
            return "Start Editing";
        }
    }

    private void enlargeSlider(HorzSlider slider){
        slider.setCustomWidth(buttonWidth);
        slider.setDebug(false);
        slider.getStyle().background.setMinHeight(slideBarSize);
        slider.getStyle().knob.setMinWidth(slideBarSize);
        slider.getStyle().knob.setMinHeight(slideBarSize);
        slider.setStyle(slider.getStyle());
    }

    private void enlargeCheckBoxFont(CheckBox box){
        box.getStyle().font = largeFont;
        box.setStyle(box.getStyle());
        box.getImage().setDebug(false);
        box.getImage().setOriginX(20);
        box.getImage().setOriginY(7);
        box.getImage().setScale(4f);
    }

    private void updateCheckBoxText(CheckBox box){
        if(box.isChecked()){
            box.setText("Enabled");
        }
        else{
            box.setText("Disabled");
        }
    }

    @Override
    void registerActors(Stage stage) {
        stage.addActor(canvas);
        stage.addActor(canvasControlButton);
    }
}
