package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.covart.streaming_prototype.ConfigManager;

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

    public MainMenu(){
        canvas = new Table();
        canvas.setX(0);
        canvas.setY(300);
        canvas.setWidth(Gdx.graphics.getWidth());
        canvas.setHeight(Gdx.graphics.getWidth());

        canvas.setDebug(true);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        largeFont = new BitmapFont();
        largeFont.getData().setScale(2.5f);

        largeLabelStyle = new Label.LabelStyle(largeFont, Color.YELLOW);

        addComponents();
    }

    private void addComponents(){
        addIPSelectUI();
        addChangeSceneUI();
        addFakeDirectionUI();
        addFocusChangeUI();
        addButtons();

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
        canvas.row();


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
        canvas.row();
    }

    private void addFakeDirectionUI(){
        // label
        Label name = new Label("Use fake direction:", largeLabelStyle);

        // checkbox
        final CheckBox box = new CheckBox("",skin);
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
        canvas.row();
    }

    private void addFocusChangeUI(){
        // label
        final Label name = new Label(getFocusRatioText(), largeLabelStyle);

        // slider
        final HorzSlider slider = new HorzSlider(0.1f, 2.0f, 0.001f, false, skin);
        slider.setCustomWidth(300);
        slider.setDebug(canvas.getDebug());
        slider.getStyle().background.setMinHeight(35);
        slider.getStyle().knob.setMinWidth(35);
        slider.getStyle().knob.setMinHeight(35);
        slider.setStyle(slider.getStyle());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigManager.setFocusChangeRatio(slider.getValue());
                name.setText(getFocusRatioText());
            }
        });


        canvas.add(name);
        canvas.add(slider).colspan(tableColumnSpan - 1);
        canvas.row();

    }

    private String getFocusRatioText(){
        return String.format(Locale.TAIWAN,"Focus ratio: %.3f",ConfigManager.getFocusChangeRatio());
    }

    private void addButtons(){
        addRecenterButton().width(250);
        addSaveFrameButton().width(250);
        canvas.row();
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

    private void enlargeCheckBoxFont(CheckBox box){
        box.getStyle().font = largeFont;
        box.setStyle(box.getStyle());
        box.getImage().setScale(2.5f);
        box.getImage().setDebug(canvas.getDebug());
        box.getImage().setOriginX(30);
        box.getImage().setOriginY(7);
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
    }

    @Override
    public void dispose() {
        largeFont.dispose();
    }
}
