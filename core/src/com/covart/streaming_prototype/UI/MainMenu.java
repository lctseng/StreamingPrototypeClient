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

    private Label actionExecutorLabel;
    private TextButton actionExecutorButton;

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
        addStopOnDisconnectedUI();
        addDisplayModeToggleUI();
        canvas.row().height(commonRowHeight);

        addIPSelectUI();
        addChangeSceneUI();
        addManualMoveUI();
        canvas.row().height(commonRowHeight);

        addFocusChangeUI();
        addStepChangeUI();
        addApertureSizeUI();
        canvas.row().height(commonRowHeight);

        addDataCameraFOVUI();
        addFreeUnusedTextureControlUI();
        addActionExecutorButtonUI();
        canvas.row().height(commonRowHeight);

        addEyeWrapperPitchLimitUI();
        addEyeWrapperYawLimitUI();
        addStPlaneRadiusUI();
        canvas.row().height(commonRowHeight);

        addAutoRotateEnabledUI();
        addAutoRotateSpeedFactorUI();
        addForceLowQuailityUI();
        canvas.row().height(commonRowHeight);

        addDisplayFPSLimitUI();
        addDrawStPlaneBackgroundUI();
        addForceWeightingFixUI();
        canvas.row().height(commonRowHeight);

        addEditingReportIntervalUI();
        addSensorReportIntervalUI();
        addEyeRotationCenterDistanceUI();
        canvas.row().height(commonRowHeight);


        addDisplayIndexRowUI();
        addDisplayIndexColumnUI();
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




    private void addActionExecutorButtonUI(){
        // create label
        actionExecutorLabel = new Label(getActionExecutorLabelText(), largeLabelStyle);

        actionExecutorButton = new TextButton(getActionExecutorButtonText(), skin);
        actionExecutorButton.getStyle().font = largeFont;
        actionExecutorButton.setStyle(startStopButton.getStyle());
        actionExecutorButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(isActionExecutorRunning()){
                    ConfigManager.getApp().autoActionExecutor.stop();
                }
                else{
                    ConfigManager.getApp().autoActionExecutor.start();

                }
                updateActionExecutorText();

            }
        });
        canvas.add(actionExecutorLabel);
        canvas.add(actionExecutorButton).width(buttonWidth);
    }

    public void updateActionExecutorText(){
        actionExecutorLabel.setText(getActionExecutorLabelText());
        actionExecutorButton.setText(getActionExecutorButtonText());
    }

    private String getActionExecutorButtonText(){
        return isActionExecutorRunning() ? "Stop" : "Start";
    }

    private String getActionExecutorLabelText(){
        return isActionExecutorRunning() ? "Auto action running" : "Auto action stopped";
    }

    private boolean isActionExecutorRunning(){
        return ConfigManager.getApp().autoActionExecutor != null && ConfigManager.getApp().autoActionExecutor.isRunning();
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

    private void addDrawStPlaneBackgroundUI(){
        // label
        Label name = new Label("Draw St Background:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
        box.setChecked(ConfigManager.isDrawStPlaneBackground());
        updateCheckBoxText(box);
        enlargeCheckBoxFont(box);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setDrawStPlaneBackground(box.isChecked());
                updateCheckBoxText(box);
            }
        });


        canvas.add(name);
        canvas.add(box).colspan(tableColumnSpan - 1);
    }

    private void addForceWeightingFixUI(){
        // label
        Label name = new Label("Force weighting fix:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
        box.setChecked(ConfigManager.isForceWeightingFix());
        updateCheckBoxText(box);
        enlargeCheckBoxFont(box);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setForceWeightingFix(box.isChecked());
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
        final HorzSlider slider = new HorzSlider(inputValueForExpSlider(0.1f), inputValueForExpSlider(20f), 0.0001f, false, skin);
        slider.setValue(inputValueForExpSlider(ConfigManager.getFocusChangeRatio()));
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setFocusChangeRatio(outputValueForExpSlider((slider.getValue())));
                name.setText(getFocusRatioText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getFocusRatioText(){
        return String.format(Locale.TAIWAN,"Focus ratio: %.4f",ConfigManager.getFocusChangeRatio());
    }

    private void addDisplayFPSLimitUI(){
        // label
        final Label name = new Label(getDisplayFPSLimitText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(1f, 60f, 1f , false, skin);
        slider.setValue(ConfigManager.getDisplayFpsLimit());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setDisplayFpsLimit(slider.getValue());
                name.setText(getDisplayFPSLimitText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
    }

    private String getDisplayFPSLimitText(){
        return String.format(Locale.TAIWAN,"Display FPS: %.0f",ConfigManager.getDisplayFpsLimit());
    }

    private void addEyeRotationCenterDistanceUI(){
        // label
        final Label name = new Label(getEyeRotationCenterDistanceText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.00f, 5.0f, 0.01f, false, skin);
        slider.setValue(ConfigManager.getEyeRotationCenterDistance());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setEyeRotationCenterDistance(slider.getValue());
                name.setText(getEyeRotationCenterDistanceText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getEyeRotationCenterDistanceText(){
        return String.format(Locale.TAIWAN,"Rotation center: %.3f",ConfigManager.getEyeRotationCenterDistance());
    }

    private void addStepChangeUI(){
        // label
        final Label name = new Label(getStepChangeText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.1f, 12.0f, 0.1f, false, skin);
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
        final HorzSlider slider = new HorzSlider(0.0f, inputValueForExpSlider(9), 0.0001f, false, skin);
        slider.setValue(inputValueForExpSlider(ConfigManager.getApertureSize()));
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setApertureSize(outputValueForExpSlider(slider.getValue()));
                name.setText(getApertureSizeText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
    }

    private String getApertureSizeText(){
        return String.format(Locale.TAIWAN,"Aperture size: %.4f", ConfigManager.getApertureSize());
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



    private void addDisplayIndexRowUI(){
        // label
        final Label name = new Label(getDisplayIndexRowText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(-1f, ConfigManager.getNumOfSubLFImgs() - 1, 1f, false, skin);
        slider.setValue(ConfigManager.getDisplayIndexRow());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setDisplayIndexRow((int)slider.getValue());
                name.setText(getDisplayIndexRowText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getDisplayIndexRowText(){
        return String.format(Locale.TAIWAN,"Display index row: %.0f",ConfigManager.getDisplayIndexRow());
    }

    private void addDisplayIndexColumnUI(){
        // label
        final Label name = new Label(getDisplayIndexColumnText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(-1f, ConfigManager.getNumOfLFs() - 1, 1f, false, skin);
        slider.setValue(ConfigManager.getDisplayIndexColumn());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setDisplayIndexColumn((int)slider.getValue());
                name.setText(getDisplayIndexColumnText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getDisplayIndexColumnText(){
        return String.format(Locale.TAIWAN,"Display index column: %.0f",ConfigManager.getDisplayIndexColumn());
    }


    private void addEyeWrapperPitchLimitUI(){
        // label
        final Label name = new Label(getEyeWrapperPitchLimitText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0f, 45f, 1f, false, skin);
        slider.setValue(ConfigManager.getAutoRotatePitchLimit());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setAutoRotatePitchLimit(slider.getValue());
                name.setText(getEyeWrapperPitchLimitText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getEyeWrapperPitchLimitText(){
        return String.format(Locale.TAIWAN,"Auto rotate pitch limit: %.0f",ConfigManager.getAutoRotatePitchLimit());
    }

    private void addEyeWrapperYawLimitUI(){
        // label
        final Label name = new Label(getEyeWrapperYawLimitText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0f, 45f, 1f, false, skin);
        slider.setValue(ConfigManager.getAutoRotateYawLimit());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setAutoRotateYawLimit(slider.getValue());
                name.setText(getEyeWrapperYawLimitText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getEyeWrapperYawLimitText(){
        return String.format(Locale.TAIWAN,"Auto rotate yaw limit: %.0f",ConfigManager.getAutoRotateYawLimit());
    }

    private void addStPlaneRadiusUI(){
        // label
        final Label name = new Label(getStPlaneRadiusText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.1f, 10f, 0.1f, false, skin);
        slider.setValue(ConfigManager.getStPlaneRadius());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setStPlaneRadius(slider.getValue());
                name.setText(getStPlaneRadiusText());
                ConfigManager.getApp().display.refreshStPlane();
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getStPlaneRadiusText(){
        return String.format(Locale.TAIWAN,"ST plane radius %.1f",ConfigManager.getStPlaneRadius());
    }

    private void addAutoRotateSpeedFactorUI(){
        // label
        final Label name = new Label(getAutoRotateSpeedFactorText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0f, 5f, 0.001f, false, skin);
        slider.setValue(ConfigManager.getAutoRotateSpeedFactor());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setAutoRotateSpeedFactor(slider.getValue());
                name.setText(getAutoRotateSpeedFactorText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);

    }

    private String getAutoRotateSpeedFactorText(){
        return String.format(Locale.TAIWAN,"Auto rotate speed: %.3f",ConfigManager.getAutoRotateSpeedFactor());
    }

    private void addAutoRotateEnabledUI(){
        // label
        Label name = new Label("Enable auto rotate:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
        box.setChecked(ConfigManager.isAutoRotateEnabled());
        updateCheckBoxText(box);
        enlargeCheckBoxFont(box);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setAutoRotateEnabled(box.isChecked());
                updateCheckBoxText(box);
                if(ConfigManager.isAutoRotateEnabled()){
                    ConfigManager.getApp().display.eyeWrapper.resetAutoRotate();
                }
            }
        });


        canvas.add(name);
        canvas.add(box).colspan(tableColumnSpan - 1);
    }

    private void addForceLowQuailityUI(){
        // label
        Label name = new Label("Force low quality:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
        box.setChecked(ConfigManager.isForceLowQuality());
        updateCheckBoxText(box);
        enlargeCheckBoxFont(box);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setForceLowQuality(box.isChecked());
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
    void setStage(Stage stage) {
        stage.addActor(canvas);
        stage.addActor(canvasControlButton);
    }
}
