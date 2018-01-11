package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by lctseng on 2018/1/11.
 * For NCP project at COVART, NTU
 */

public class VerticalImageTextButton extends ImageTextButton implements Disposable {
    public VerticalImageTextButton(String text, String filename) {
        super(text, createImageTextButtonStyle(filename));
        makeVertical();
    }

    private void makeVertical(){
        clearChildren();
        add(getImage()).row();
        add(getLabel());
    }

    protected static ImageTextButton.ImageTextButtonStyle createImageTextButtonStyle(String filename){
        ImageTextButton.ImageTextButtonStyle style = new ImageTextButton.ImageTextButtonStyle();

        // font
        BitmapFont font = new BitmapFont();

        // bg
        Texture texture = new Texture(Gdx.files.internal(filename));
        TextureRegion textureRegion = new TextureRegion(texture);
        style.imageUp = new TextureRegionDrawable(textureRegion);
        style.font = font;

        return style;
    }

    @Override
    public void dispose() {
        getStyle().font.dispose();
        ((TextureRegionDrawable)getStyle().imageUp).getRegion().getTexture().dispose();
    }
}
