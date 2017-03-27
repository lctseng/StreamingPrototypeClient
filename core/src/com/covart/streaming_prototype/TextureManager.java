package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;

/**
 * Created by lctseng on 2017/3/27.
 * NTU COV-ART Lab, for NCP project
 */

public class TextureManager implements Disposable {



    private int nSlots = 0;
    private Texture[] textures;

    private Pixmap slotImage;
    private ByteBuffer slotImageBuf;

    private DisplayLightField display;

    private int lastColumnIndex;

    TextureManager(DisplayLightField display){
        this.display = display;
        slotImage = new Pixmap(DisplayLightField.DIMENSION, DisplayLightField.DIMENSION * DisplayLightField.ROW_WIDTH, Pixmap.Format.RGB888);
        slotImageBuf = slotImage.getPixels();
    }

    public void createTextureSlots(int nSlots){
        disposeExistingTextures();
        this.nSlots = nSlots;
        textures = new Texture[nSlots];
    }

    public void addImage(Buffer buffer, int rowIndex){
        // save last column index if row index == 0
        if(rowIndex == 0){
            lastColumnIndex = buffer.index;
        }
        else
        {
            // check index should same as last
            if(lastColumnIndex != buffer.index){
                Gdx.app.error("TextureManager","Buffer index not same! Expect: " + lastColumnIndex + " , got: " + buffer.index);
            }
        }
        // check boundary
        if(buffer.index >= nSlots){
            Gdx.app.error("TextureManager","Buffer index out of bound: " + buffer.index);
            return;
        }
        // just concat all images
        slotImageBuf.put(buffer.data, 0, buffer.size);
        // if last row, rewind the buffer and submit the texture
        if(rowIndex == DisplayLightField.ROW_WIDTH - 1){
            slotImageBuf.rewind();
            textures[buffer.index] = new Texture(slotImage);
            slotImageBuf.rewind();
            Gdx.app.log("TextureManager", "Enf of column: " + buffer.index);
        }
    }


    @Override
    public void dispose() {
        disposeExistingTextures();
    }

    public void disposeExistingTextures(){
        if(textures != null){
            for(Texture tex : textures){
                if(tex != null) {
                    tex.dispose();
                }
            }
            textures = null;
            nSlots = 0;
        }
    }

    public Texture[] getTextures(){
        return textures;
    }
}
