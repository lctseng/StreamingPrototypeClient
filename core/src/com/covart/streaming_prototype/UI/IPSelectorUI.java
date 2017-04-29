package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.covart.streaming_prototype.ConfigManager;

/**
 * Created by lctseng on 2017/4/10.
 * NTU COV-ART Lab, for NCP project
 */

public class IPSelectorUI extends UIComponent {

    private SelectBox<String> ipSelectBox;
    private BitmapFont selectFont;
    private BitmapFont labelFont;

    private TextButton button;

    public IPSelectorUI(){
        // create skin data
        Skin skin = new Skin();
        TextureAtlas buttonAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
        skin.addRegions(buttonAtlas);

        // create fonts
        selectFont = new BitmapFont();
        selectFont.getData().setScale(3f);

        labelFont = new BitmapFont();
        labelFont.getData().setScale(3f);

        // create button
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = labelFont;
        textButtonStyle.up = skin.getDrawable("default-round");
        textButtonStyle.down = skin.getDrawable("default-round-down");
        textButtonStyle.checked = skin.getDrawable("default-select-selection");
        button = new TextButton(ConfigManager.getSelectedIP(), textButtonStyle);
        button.setX((Gdx.graphics.getWidth() - button.getWidth())/2);
        button.setY(Gdx.graphics.getHeight()- button.getHeight());

        // create select box
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle();
        style.font = selectFont;
        style.scrollStyle = new ScrollPane.ScrollPaneStyle();
        style.scrollStyle.background = skin.getDrawable("default-select");
        style.listStyle = new List.ListStyle();
        style.listStyle.font = selectFont;
        style.listStyle.selection = skin.getDrawable("default-select-selection");
        ipSelectBox = new SelectBox<String>(style);
        ipSelectBox.setX((Gdx.graphics.getWidth() - button.getWidth())/2);
        ipSelectBox.setY(Gdx.graphics.getHeight() - button.getHeight() * 2);


        // add button listener
        button.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                ipSelectBox.showList();
            }
        });


        // add select list listener
        ipSelectBox.setItems(ConfigManager.getServerList());
        ipSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("IP Select", ipSelectBox.getSelected());
                button.setText(ipSelectBox.getSelected());
                ConfigManager.setSelectedIP(ipSelectBox.getSelected());
            }
        });

    }

    @Override
    public void dispose() {
        labelFont.dispose();
        selectFont.dispose();
    }

    @Override
    public void registerActors(Stage stage) {
        stage.addActor(button);
        stage.addActor(ipSelectBox);
    }
}
