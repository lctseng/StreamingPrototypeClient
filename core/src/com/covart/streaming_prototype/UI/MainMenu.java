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
        addIPSelectList();

    }

    private void addIPSelectList(){
        // create button
        Label label = new Label("Server IP:", largeLabelStyle);

        // create select box
        final SelectBox<String> ipSelectBox = new SelectBox<String>(skin);
        ipSelectBox.getStyle().font = largeFont;
        ipSelectBox.getStyle().listStyle.font = largeFont;

        // add select list listener
        ipSelectBox.setItems(ConfigManager.getServerList());
        ipSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("IP Select", ipSelectBox.getSelected());
                ConfigManager.setSelectedIP(ipSelectBox.getSelected());
            }
        });


        canvas.add(label).expandX().width(100);
        canvas.add(ipSelectBox).expandX();
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
