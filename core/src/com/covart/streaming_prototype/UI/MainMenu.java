package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.covart.streaming_prototype.ConfigManager;


/**
 * Created by lctseng on 2017/4/29.
 * NTU COV-ART Lab, for NCP project
 */

public class MainMenu extends UIComponent {
    private Table canvas;

    private Skin skin;

    private BitmapFont largeFont;
    private Label.LabelStyle largeLabelStyle;

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
        canvas.add(selectBox);
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
        canvas.add(selectBox);
        canvas.row();
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
