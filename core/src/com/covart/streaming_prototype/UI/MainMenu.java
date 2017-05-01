package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.StreamingPrototype.State;

import java.util.Locale;


/**
 * Created by lctseng on 2017/4/29.
 * NTU COV-ART Lab, for NCP project
 */

public class MainMenu extends UIComponent {
    private Table canvas;

    private Skin skin;

    private BitmapFont largeFont;
    private Label.LabelStyle largeLabelStyle;

    private int tableColumnSpan = 3;
    private int buttonWidth = 250;

    private float commonRowHeight = 100f;
    private float slideBarSize = 70f;

    private Label startStopLabel;
    private TextButton startStopButton;

    private TextButton canvasControlButton;

    private float fadeTime = 0.2f;

    public MainMenu(){
        // resources
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        largeFont = new BitmapFont();
        largeFont.getData().setScale(2.5f);

        largeLabelStyle = new Label.LabelStyle(largeFont, Color.YELLOW);

        // canvas
        createCanvas();

        // canvas control button
        createCanvasControlButton();

        showMenu();

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
                    hideMenu();
                    canvasControlButton.setText("Open Menu");
                }
                else{
                    showMenu();
                    canvasControlButton.setText("Hide Menu");
                }

            }
        });
    }

    private void showMenu(){
        canvas.addAction(Actions.sequence(Actions.alpha(0), Actions.show(),Actions.fadeIn(fadeTime)));
    }

    private void hideMenu(){
        canvas.addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.hide()));
    }

    @Override
    void onAppStateChanged() {
        updateStartStopText();
    }

    private void addComponents(){
        addStartStopUI();
        addIPSelectUI();
        addChangeSceneUI();
        addFakeDirectionUI();
        addStopOnDisconnectedUI();
        addFocusChangeUI();
        addApertureSizeUI();
        addInterpolateChangeUI();
        addSensorUpdateDisplayUI();
        addSensorReportIntervalUI();
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
                switch (gettAppState()){
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
        canvas.row().height(commonRowHeight);
    }

    private void updateStartStopText(){
        startStopLabel.setText(getStartStopLabelText());
        startStopButton.setText(getStartStopButtonText());
    }

    private String getStartStopButtonText(){
        switch (gettAppState()){
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
        switch (gettAppState()){
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

    private State gettAppState(){
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
        canvas.row().height(commonRowHeight);


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
        canvas.row().height(commonRowHeight);
    }

    private void addFakeDirectionUI(){
        // label
        Label name = new Label("Use fake direction:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
        box.setChecked(ConfigManager.isUseFakeDirection());
        updateCheckBoxText(box);
        enlargeCheckBoxFont(box);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setUseFakeDirection(box.isChecked());
                updateCheckBoxText(box);
            }
        });


        canvas.add(name);
        canvas.add(box).colspan(tableColumnSpan - 1);
        canvas.row().height(commonRowHeight);
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
        canvas.row().height(commonRowHeight);
    }

    private void addFocusChangeUI(){
        // label
        final Label name = new Label(getFocusRatioText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.0f, 2.0f, 0.001f, false, skin);
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
        canvas.row().height(commonRowHeight);

    }

    private String getFocusRatioText(){
        return String.format(Locale.TAIWAN,"Focus ratio: %.3f",ConfigManager.getFocusChangeRatio());
    }

    private void addInterpolateChangeUI(){
        // label
        final Label name = new Label(getInterpolateRadiusText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0, 5, 1, false, skin);
        slider.setValue(ConfigManager.getNumOfMaxInterpolatedLFRadius());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setNumOfMaxInterpolatedLFRadius(((int)slider.getValue()));
                name.setText(getInterpolateRadiusText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
        canvas.row().height(commonRowHeight);
    }

    private String getInterpolateRadiusText(){
        return String.format(Locale.TAIWAN,"Interpolate span: %d", ConfigManager.getNumOfMaxInterpolatedLFRadius());
    }

    private void addSensorUpdateDisplayUI(){
        // label
        final Label name = new Label(getSensorUpdateDisplayText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(1f/60f, 0.2f, 0.001f, false, skin);
        slider.setValue(ConfigManager.getSensorUpdateDisplayTime());
        enlargeSlider(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setSensorUpdateDisplayTime(slider.getValue());
                name.setText(getSensorUpdateDisplayText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
        canvas.row().height(commonRowHeight);
    }

    private String getSensorUpdateDisplayText(){
        return String.format(Locale.TAIWAN,"Sensor update display: %.3f", ConfigManager.getSensorUpdateDisplayTime());
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
        canvas.row().height(commonRowHeight);
    }

    private String getSensorReportIntervalText(){
        return String.format(Locale.TAIWAN,"Sensor interval: %.3f", ConfigManager.getSensorReportInterval());
    }

    private void addApertureSizeUI(){
        // label
        final Label name = new Label(getApertureSizeText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.0f, 80.0f, 1.0f, false, skin);
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
        canvas.row().height(commonRowHeight);
    }

    private String getApertureSizeText(){
        return String.format(Locale.TAIWAN,"Aperture size: %.3f", ConfigManager.getApertureSize());
    }

    private void addButtons(){
        addRecenterButton().width(buttonWidth);
        addSaveFrameButton().width(buttonWidth);
        canvas.row().height(commonRowHeight);
    }

    private Cell<TextButton> addRecenterButton(){
        TextButton button = new TextButton("Re-center", skin);
        button.getStyle().font = largeFont;
        button.setStyle(button.getStyle());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.getApp().recenterSensor();
            }
        });
        return canvas.add(button);
    }

    private Cell<TextButton> addSaveFrameButton(){
        TextButton button = new TextButton("Save frame", skin);
        button.getStyle().font = largeFont;
        button.setStyle(button.getStyle());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.getApp().setSaveFrameRequested(true);
            }
        });
        return canvas.add(button);
    }

    private void enlargeSlider(HorzSlider slider){
        slider.setCustomWidth(300);
        slider.setDebug(canvas.getDebug());
        slider.getStyle().background.setMinHeight(slideBarSize);
        slider.getStyle().knob.setMinWidth(slideBarSize);
        slider.getStyle().knob.setMinHeight(slideBarSize);
        slider.setStyle(slider.getStyle());
    }

    private void enlargeCheckBoxFont(CheckBox box){
        box.getStyle().font = largeFont;
        box.setStyle(box.getStyle());
        box.getImage().setDebug(canvas.getDebug());
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

    @Override
    public void dispose() {
        largeFont.dispose();
    }
}
