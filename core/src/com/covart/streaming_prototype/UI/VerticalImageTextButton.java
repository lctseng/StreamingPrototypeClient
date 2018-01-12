package com.covart.streaming_prototype.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by lctseng on 2018/1/11.
 * For NCP project at COVART, NTU
 */

public class VerticalImageTextButton extends ImageTextButton implements Disposable {

    protected Texture textureUp;
    protected Texture textureDown;

    public VerticalImageTextButton(String text, String filename) {
        super(text, createImageTextButtonStyle(filename));
        setup();
    }

    public VerticalImageTextButton(String text, FileHandle file) {
        super(text, createImageTextButtonStyle(file));
        setup();
    }

    private void setup(){
        makeVertical();
        textureUp = ((TextureRegionDrawable)getStyle().imageUp).getRegion().getTexture();
        textureDown = ((TextureRegionDrawable)getStyle().imageDown).getRegion().getTexture();
    }

    private void makeVertical(){
        clearChildren();
        add(getImage()).row();
        add(getLabel());
    }

    protected static ImageTextButton.ImageTextButtonStyle createImageTextButtonStyle(String filename){
        return createImageTextButtonStyle(Gdx.files.internal(filename));
    }

    protected static ImageTextButton.ImageTextButtonStyle createImageTextButtonStyle(FileHandle file){
        ImageTextButton.ImageTextButtonStyle style = new ImageTextButton.ImageTextButtonStyle();

        // font
        style.font = new BitmapFont();;

        // image up
        style.imageUp = extractTextureDrawable(new Texture(file));

        // image down
        Pixmap imgDown = new Pixmap(file);
        Pixmap.Blending oldBlending = Pixmap.getBlending();
        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        imgDown.setColor(0.5f,0,0,0.5f);
        imgDown.fillRectangle(0, 0, imgDown.getWidth(), imgDown.getHeight());
        style.imageDown = extractTextureDrawable(new Texture(imgDown));
        imgDown.dispose();
        Pixmap.setBlending(oldBlending);

        return style;
    }

    protected static Drawable extractTextureDrawable(Texture texture){
        TextureRegion textureRegion = new TextureRegion(texture);
        return new TextureRegionDrawable(textureRegion);
    }

    @Override
    public void dispose() {
        getStyle().font.dispose();
        textureUp.dispose();
        textureDown.dispose();
    }
}
