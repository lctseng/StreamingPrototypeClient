package com.covart.streaming_prototype.UI;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by lctseng on 2017/4/29.
 * NTU COV-ART Lab, for NCP project
 */

public abstract class UIComponent implements Disposable {

    protected Skin skin;

    protected BitmapFont largeFont;
    protected Label.LabelStyle largeLabelStyle;

    UIComponent(){
        // resources
        largeFont = new BitmapFont();
        largeFont.getData().setScale(1.5f);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        largeFont = new BitmapFont();
        largeFont.getData().setScale(1.5f);

        largeLabelStyle = new Label.LabelStyle(largeFont, Color.YELLOW);
    }

    void setStage(Stage stage){

    }

    void start(){

    }

    void onAppStateChanged(){

    }

    @Override
    public void dispose() {
        largeFont.dispose();
    }
}
