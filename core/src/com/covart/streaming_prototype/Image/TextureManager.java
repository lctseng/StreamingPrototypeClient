package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.covart.streaming_prototype.Buffer;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.StringPool;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import StreamingFormat.Message;

/**
 * Created by lctseng on 2017/3/27.
 * NTU COV-ART Lab, for NCP project
 */

public class TextureManager implements Disposable {



    private int nSlots = 0;
    private Texture[] textures;

    private Pixmap slotImage;
    private ByteBuffer slotImageBuf;

    private Display display;

    private int lastColumnIndex = -1;
    private int lastImageTypeValue = -1;
    private int rowIndex;
    private int rowIndexStep;
    private int rowIndexOffset;

    private float deltaHorz = 0.f;
    private float deltaVert = 0.f;

    private float cameraPositionX = 0.5f;
    private float cameraPositionY = 0.5f;

    private int centerIndex = 0;
    private int columnStart = 0;
    private int columnEnd = 0;

    private final List<Integer> droppedIndex;

    private byte[] paddingZeroBytes;

    TextureManager(Display display){
        this.display = display;
        slotImage = new Pixmap(ConfigManager.getImageWidth(), ConfigManager.getImageHeight()* ConfigManager.getNumOfSubLFImgs(), Pixmap.Format.RGB888);
        slotImageBuf = slotImage.getPixels();
        droppedIndex = new ArrayList<Integer>();
        paddingZeroBytes = new byte[ConfigManager.getImageBufferSize()];

    }

    public void createTextureSlots(int nSlots){
        disposeExistingTextures();
        this.nSlots = nSlots;
        textures = new Texture[nSlots];
    }

    public void addImage(Buffer buffer){
        // if lastColumn != current column, then reset rowIndex
        // means a new column is coming!
        if(lastColumnIndex != buffer.index || lastImageTypeValue != buffer.imageTypeValue){
            //Gdx.app.log("TextureManager", "Set new column: " + buffer.index);
            // setup new offset and step
            switch(buffer.imageTypeValue){
                case Message.ImageType.FULL_INDEX_VALUE:
                    rowIndexOffset = 0;
                    rowIndexStep = 1;
                    break;
                case Message.ImageType.ODD_INDEX_VALUE:
                    rowIndexOffset = 1;
                    rowIndexStep = 2;
                    break;
                case Message.ImageType.EVEN_INDEX_VALUE:
                    rowIndexOffset = 0;
                    rowIndexStep = 2;
                    break;
            }

            rowIndex = rowIndexOffset;
            lastColumnIndex = buffer.index;
            lastImageTypeValue = buffer.imageTypeValue;
            // reset image buffer
            slotImageBuf.rewind();
            // fill init padding
            for(int i=1;i<=rowIndexOffset;i++){
                slotImageBuf.put(paddingZeroBytes);
            }
        }
        // check boundary
        if(buffer.index >= nSlots){
            Gdx.app.error("TextureManager","Buffer index out of bound: " + buffer.index);
            return;
        }
        // just concat all images
        slotImageBuf.put(buffer.data, 0, buffer.size);
        // step padding, without overflow the buffer
        int fake_row_index = rowIndex + 1;
        for(int i=2;i<=rowIndexStep;i++){
            if(fake_row_index < ConfigManager.getNumOfSubLFImgs()) {
                slotImageBuf.put(paddingZeroBytes);
                fake_row_index += 1;
            }
            else{
                break;
            }
        }
        // if last row, rewind the buffer and submit the texture
        rowIndex += rowIndexStep;
        if(rowIndex >= ConfigManager.getNumOfSubLFImgs()){
            slotImageBuf.rewind();
            if(textures[buffer.index] != null){
                textures[buffer.index].dispose();
            }
            textures[buffer.index] = new Texture(slotImage);
            //textures[buffer.index] = new Texture("grid4.jpg");
            slotImageBuf.rewind();
            //Gdx.app.log("TextureManager", "End of column: " + buffer.index);
            rowIndex = 0;
            freeUnusedTextures();

        }
    }

    private void freeUnusedTextures(){
        int threshold = ConfigManager.getFreeUnusedTextureThreshold();
        if(threshold > 0) {
            for (int i = 0; i < nSlots; i++) {
                if (textures[i] != null) {
                    if (Math.abs(lastColumnIndex - i) >= threshold) {
                        // i-th texture is too far
                        textures[i].dispose();
                        textures[i] = null;
                        synchronized (droppedIndex) {
                            droppedIndex.add(i);
                        }
                    }
                }
            }
        }
    }

    public void attachControlFrameInfo(Message.Control.Builder controlBuilder){
        synchronized (droppedIndex) {
            controlBuilder.addAllDropIndex(droppedIndex);
            droppedIndex.clear();
        }
    }

    public boolean checkControlFrameRequired(){
        synchronized (droppedIndex) {
            return !droppedIndex.isEmpty();
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
            synchronized (droppedIndex) {
                droppedIndex.clear();
            }
        }
    }

    public Texture[] getTextures(){
        return textures;
    }

    public void bindTextures(ShaderProgram shaderProgram, int startIndex, int endIndex){
        if(textures == null){
            return;
        }
        for(int i=startIndex;i<=endIndex;i++){
            int textureIndex = i - startIndex;
            if(textures[i] != null) {
                textures[i].bind(textureIndex);
                shaderProgram.setUniformi("u_custom_texture" + textureIndex, textureIndex);
                shaderProgram.setUniformi("u_texture_valid" + textureIndex, 1);
                //Gdx.app.log("Tex", "Binding:" + textureIndex);
            }
            else{
                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + textureIndex);
                Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
                shaderProgram.setUniformi("u_texture_valid" + textureIndex, 0);
            }
        }
    }

    // Leftmost: index = numLightfield - 1
    // Rightmost: index = 0
    public void visualizeColumnStatus(){
        String status = "";
        for(int i=nSlots-1;i>=0;i--){
            if(textures[i] == null){
                status += "=";
            }
            else{
                status += "+";
            }
        }
        StringPool.addField("Visible Status T", status);
    }
}
