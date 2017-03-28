package com.covart.streaming_prototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;

/**
 * Created by lctseng on 2017/3/27.
 * NTU COV-ART Lab, for NCP project
 */

public class TextureManager implements Disposable {



    private int nSlots = 0;
    private Texture[] textures;
    private float slotSpan = 0.f;

    private Pixmap slotImage;
    private ByteBuffer slotImageBuf;

    private DisplayLightField display;

    private int lastColumnIndex = -1;
    private int rowIndex;

    private float deltaHorz = 0.f;
    private float deltaVert = 0.f;

    private float cameraPositionX = 0.5f;
    private float cameraPositionY = 0.5f;

    private int centerIndex = 0;
    private int columnStart = 0;
    private int columnEnd = 0;

    TextureManager(DisplayLightField display){
        this.display = display;
        slotImage = new Pixmap(DisplayLightField.DIMENSION, DisplayLightField.DIMENSION * DisplayLightField.ROW_WIDTH, Pixmap.Format.RGB888);
        slotImageBuf = slotImage.getPixels();

    }

    public void createTextureSlots(int nSlots){
        disposeExistingTextures();
        this.nSlots = nSlots;
        textures = new Texture[nSlots];
        slotSpan = 1.f / nSlots;
    }

    public void addImage(Buffer buffer){
        // if lastColumn != current column, then reset rowIndex
        // means a new column is coming!
        if(lastColumnIndex != buffer.index){
            Gdx.app.log("TextureManager", "Set new column: " + buffer.index);
            rowIndex = 0;
            lastColumnIndex = buffer.index;
            // reset image buffer
            slotImageBuf.rewind();
        }
        // check boundary
        if(buffer.index >= nSlots){
            Gdx.app.error("TextureManager","Buffer index out of bound: " + buffer.index);
            return;
        }
        // just concat all images
        slotImageBuf.put(buffer.data, 0, buffer.size);
        // if last row, rewind the buffer and submit the texture
        rowIndex += 1;
        if(rowIndex == DisplayLightField.ROW_WIDTH){
            slotImageBuf.rewind();
            if(textures[buffer.index] != null){
                textures[buffer.index].dispose();
            }
            textures[buffer.index] = new Texture(slotImage);
            slotImageBuf.rewind();
            Gdx.app.log("TextureManager", "End of column: " + buffer.index);
            freeUnusedTextures();
        }
    }

    private void freeUnusedTextures(){
        for(int i=0;i<nSlots;i++){
            if(textures[i] != null) {
                if (Math.abs(lastColumnIndex - i) > 9) {
                    // i-th texture is too far
                    textures[i].dispose();
                    textures[i] = null;
                }
            }
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

    public void updateDelta(float dh, float dv){
        deltaHorz = dh;
        deltaVert = dv;
        // compute center index
        // TODO: should apply function H(delta) = INDEX
        // Here center index computed by (x+ 0.5)*nSlots
        centerIndex = (int)((dh + 0.5) * nSlots);
        if(centerIndex < 0) centerIndex = 0;
        else if (centerIndex >= nSlots) centerIndex = nSlots;
        // compute the span
        columnStart = centerIndex - DisplayLightField.HALF_COL_SPAN;
        columnEnd = centerIndex + DisplayLightField.HALF_COL_SPAN + 1;

        if(columnStart < 0) columnStart = 0;
        else if(columnStart >= nSlots) columnStart = nSlots - 1;

        if(columnEnd >nSlots) columnEnd = nSlots ;
        //Gdx.app.log("LightField Display", "Effective col: " + columnStart + "-" + (columnEnd-1));
        // prepare camera XY
        // TODO: cameraX should map deltaMin ~ deltaMax to 0 ~ 1
        cameraPositionX = dh + 0.5f;
        cameraPositionY = dv + 0.5f;
        visualizeColumnStatus();
    }

    public void bindTextures(ShaderProgram shaderProgram){
        for(int i=columnStart;i<columnEnd;i++){
            int textureIndex = i - columnStart;
            if(textures[i] != null) {
                textures[i].bind(textureIndex);
                shaderProgram.setUniformi("u_custom_texture" + textureIndex, textureIndex);
                shaderProgram.setUniformi("u_texture_valid" + textureIndex, 1);
            }
            else{
                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + textureIndex);
                Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
                shaderProgram.setUniformi("u_texture_valid" + textureIndex, 0);
            }
        }
    }

    public void visualizeColumnStatus(){
        visualizeVisibleColumn();
        visualizeTextureStatus();
    }

    public void visualizeTextureStatus(){
        String status = "";
        for(int i=0;i<nSlots;i++){
            if(textures[i] == null){
                status += "=";
            }
            else{
                status += "+";
            }
        }
        StringPool.addField("Visible Status T", status);
    }

    public void visualizeVisibleColumn(){
        String status = "";
        for(int i=0;i<columnStart;i++){
            status += "=";
        }
        for(int i=columnStart;i<columnEnd;i++){
            status += "+";
        }
        for(int i=columnEnd;i<nSlots;i++){
            status += "=";
        }
        StringPool.addField("Visible Status V", status);
    }

    public float getCameraPositionX(){
        return cameraPositionX;
    }

    public float getCameraPositionY(){
        return cameraPositionY;
    }

    public int getColumnStart(){
        return columnStart;
    }

    public int getColumnEnd(){
        return columnEnd;
    }
}
