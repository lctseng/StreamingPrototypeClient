package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by lctseng on 2017/4/10.
 * NTU COV-ART Lab, for NCP project
 */

public class IPSelectorUI implements Disposable {

    public static String[] IP_LIST = new String[]{"140.112.90.95", "140.112.90.86", "140.112.90.89"};

    private SelectBox<String> ipSelectBox;
    private BitmapFont selectFont;
    private BitmapFont labelFont;

    private Stage stage;

    TextButton button;
    TextButton.TextButtonStyle textButtonStyle;
    Skin skin;
    TextureAtlas buttonAtlas;

    private static IPSelectorUI ourInstance;

    public static IPSelectorUI getInstance() {
        return ourInstance;
    }

    public static void initialize(){
        ourInstance = new IPSelectorUI();
    }

    public static void cleanup(){
        ourInstance.dispose();
    }


    private IPSelectorUI(){
        skin = new Skin();
        buttonAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
        skin.addRegions(buttonAtlas);

        selectFont = new BitmapFont();
        selectFont.getData().setScale(2f);

        labelFont = new BitmapFont();
        labelFont.getData().setScale(2f);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);


        textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = labelFont;
        textButtonStyle.up = skin.getDrawable("default-round");
        textButtonStyle.down = skin.getDrawable("default-round-down");
        textButtonStyle.checked = skin.getDrawable("default-select-selection");
        button = new TextButton(IP_LIST[0], textButtonStyle);
        stage.addActor(button);

        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle();
        style.font = selectFont;
        style.scrollStyle = new ScrollPane.ScrollPaneStyle();
        style.scrollStyle.background = skin.getDrawable("default-select");
        style.listStyle = new List.ListStyle();
        style.listStyle.font = selectFont;
        style.listStyle.selection = skin.getDrawable("default-select-selection");
        ipSelectBox = new SelectBox<String>(style);
        stage.addActor(ipSelectBox);




        ipSelectBox.showList();






        button.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                ipSelectBox.showList();
            }
        });


        ipSelectBox.setItems(IP_LIST);
        ipSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("IP Select", ipSelectBox.getSelected());
                button.setText(ipSelectBox.getSelected());
            }
        });

        button.setX((Gdx.graphics.getWidth() - button.getWidth())/2);
        button.setY(Gdx.graphics.getHeight()- button.getHeight());

        ipSelectBox.setX((Gdx.graphics.getWidth() - button.getWidth())/2);
        ipSelectBox.setY(Gdx.graphics.getHeight() - button.getHeight() * 2);

    }

    @Override
    public void dispose() {
        stage.dispose();
        labelFont.dispose();
        selectFont.dispose();
    }

    public void draw(){
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public InputProcessor getInputProcessor(){
        return stage;
    }

    public String getSelectedIP(){
        return ipSelectBox.getSelected();
    }
}
