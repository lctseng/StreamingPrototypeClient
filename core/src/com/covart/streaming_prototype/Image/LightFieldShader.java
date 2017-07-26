/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.covart.streaming_prototype.Image;

import com.badlogic.gdx.backends.android.CardboardCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.StringPool;

import java.util.Locale;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 * @author Xoppa
 */
public class LightFieldShader extends DefaultShader{

    private Display display;
    private PerspectiveCamera dataCamera;
    private int startIndex;
    private int endIndex;

    private int startRow;
    private int endRow;

    private Matrix4 eyeMatrix;
    private Quaternion eyeRotation;
    private Vector3 eyeTranslate;
    private Vector3 eyePosition;

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public LightFieldShader(Renderable renderable) {
        super(renderable);
    }

    public LightFieldShader(Renderable renderable, Config config) {
        super(renderable, config);
    }

    public LightFieldShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix);
    }

    public LightFieldShader(Renderable renderable, Config config, String prefix, String vertexShader, String fragmentShader) {
        super(renderable, config, prefix, vertexShader, fragmentShader);
    }

    public LightFieldShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);
    }

    @Override
    public void init() {
        super.init();
        startIndex = -1;
        endIndex = -1;

        startRow = -1;
        endRow = -1;

        eyeMatrix = new Matrix4();
        eyeRotation = new Quaternion();
        eyePosition = new Vector3();
        eyeTranslate = new Vector3();
        initDataCameras();

    }

    private void initDataCameras() {
        int totalCols = ConfigManager.getNumOfLFs();
        int totalRows = ConfigManager.getNumOfSubLFImgs();

        dataCamera = new PerspectiveCamera(ConfigManager.getDataCameraFOV(), ConfigManager.getImageWidth(), ConfigManager.getImageHeight());
        dataCamera.near = 0.1f;
        dataCamera.far = 10f;
        dataCamera.position.set(0, 0, 0);
        dataCamera.lookAt(0, 0, -1);
    }

    @Override
    public void render(Renderable renderable, Attributes combinedAttributes) {
        updateLFIndex();
        bindConfiguration();
        bindPosition();
        bindProjections();
        bindTexture();

        StringPool.addField("Camera Position", String.format(Locale.TAIWAN, "X: %4f, Y: %4f, Z: %4f",camera.position.x,camera.position.y,camera.position.z));
        visualizeLightFieldStatus();
        super.render(renderable, combinedAttributes);
    }

    private void bindPosition(){
        eyeMatrix.set(display.currentEye.getEyeView());
        eyeMatrix.getRotation(eyeRotation);

        eyeMatrix.getTranslation(eyeTranslate);
        eyeTranslate.scl(ConfigManager.getEyeDisparityFactor());
        eyeRotation.transform(eyeTranslate);

        eyePosition.set(camera.position);
        eyePosition.add(eyeTranslate);

        program.setUniformf("u_cameraPositionX", eyePosition.x);
        program.setUniformf("u_cameraPositionY", eyePosition.y);
    }

    private void bindConfiguration(){
        program.setUniformi("u_screenWidth", display.currentEye.getViewport().width);
        program.setUniformi("u_screenHeight", display.currentEye.getViewport().height);
        program.setUniformi("u_screenOffsetX", 0);
        program.setUniformi("u_screenOffsetX", display.currentEye.getViewport().x);
        program.setUniformi("u_screenOffsetY", display.currentEye.getViewport().y);
        program.setUniformi("u_cols",ConfigManager.getNumOfLFs());
        program.setUniformi("u_rows",ConfigManager.getNumOfSubLFImgs());
        program.setUniformf("u_columnPositionRatio",ConfigManager.getColumnPositionRatio());
        program.setUniformf("u_apertureSize",ConfigManager.getApertureSize() / 20.f);
        program.setUniformf("u_cameraStep",ConfigManager.getCameraStep());
    }

    private void updateLFIndex(){
        // compute valid column index range
        int cols = ConfigManager.getNumOfLFs();
        int rows = ConfigManager.getNumOfSubLFImgs();
        float columnRatio = ConfigManager.getColumnPositionRatio();

        float spanX = 2f * columnRatio /cols;
        float spanY = 2f / rows;

        float initCameraX = -1.0f * columnRatio + 0.5f * spanX;
        float initCameraY = -1.0f + 0.5f * spanY;

        float cameraStep = ConfigManager.getCameraStep();

        startIndex = -1;
        endIndex = -1;

        // for columns
        // find first index that fall into aperture
        for(int i=0;i<cols;i++){
            float cameraX = (initCameraX + i * spanX) * cameraStep;
            float dx = cameraX - camera.position.x;
            float dist = dx * dx; // assume dy is zero
            if(dist < ConfigManager.getApertureSize()){
                startIndex = i;
                break;
            }
        }
        // find last index that fall into aperture
        for(int i=cols - 1;i>= 0;i--){
            float cameraX = (initCameraX + i * spanX) * cameraStep;
            float dx = cameraX - camera.position.x;
            float dist = dx * dx; // assume dy is zero
            if(dist < ConfigManager.getApertureSize()){
                endIndex = i;
                break;
            }
        }

        if(startIndex >= 0){
            // ensure the range falls into numOfMaxLFTextures
            // this assume numOfMaxLFTextures to be a even number
            int middleIndex = (startIndex + endIndex) / 2;
            int radius = ConfigManager.getNumOfMaxLFTextures() / 2;
            if(middleIndex - startIndex >= radius){
                startIndex = middleIndex - radius + 1;
            }
            if(endIndex - middleIndex > radius){
                endIndex = middleIndex + radius ;
            }

        }
        // for rows
        startRow = -1;
        endRow = -1;
        for(int i=0;i<rows;i++){
            float cameraY = (initCameraY + i * spanY) * cameraStep;
            float dy = cameraY - camera.position.y;
            float dist = dy * dy; // assume dx is zero
            if(dist < ConfigManager.getApertureSize()){
                startRow = i;
                break;
            }
        }
        // find last index that fall into aperture
        for(int i=rows - 1;i>= 0;i--){
            float cameraY = (initCameraY + i * spanY) * cameraStep;
            float dy = cameraY - camera.position.y;
            float dist = dy * dy; // assume dy is zero
            if(dist < ConfigManager.getApertureSize()){
                endRow = i;
                break;
            }
        }

        program.setUniformi("u_colTextureOffset", startIndex);

        program.setUniformi("u_colStart",startIndex);
        program.setUniformi("u_colEnd",endIndex);
        program.setUniformi("u_rowStart",startRow);
        program.setUniformi("u_rowEnd",endRow);

    }

    private void bindTexture(){
        if(display != null && startIndex >= 0) {
            display.getTextureManager().bindTextures(program, startIndex, endIndex);
        }
    }

    private void bindProjections(){
        bindRkRfProjection();
        bindRfRdProjections();
    }

    private void bindRkRfProjection(){
        float ratio = ConfigManager.getFocusChangeRatio();
        float[] perspective = display.currentEye.getPerspective(0.01f, 3.0f + ratio * ratio);
        ((CardboardCamera)camera).setEyeProjection(new Matrix4(perspective));
        camera.update();
        Matrix4 inverseProj = camera.invProjectionView;
        program.setUniformMatrix("u_rk_to_rf",inverseProj);
    }

    private void bindRfRdProjections(){
        if(startIndex >= 0) {
            dataCamera.fieldOfView = ConfigManager.getDataCameraFOV();
            dataCamera.update();
            program.setUniformMatrix("u_rf_to_rd_center", dataCamera.combined);
        }
    }

    private void visualizeLightFieldStatus(){
        visualizeVisibleColumn();
        if(display != null){
            display.getTextureManager().visualizeColumnStatus();
        }
    }

    private void visualizeVisibleColumn(){
        String status = "";
        for(int i=0;i<startIndex;i++){
            status += "=";
        }
        for(int i=startIndex;i<=endIndex;i++){
            status += "+";
        }
        for(int i=endIndex+1;i<ConfigManager.getNumOfLFs();i++){
            status += "=";
        }
        StringPool.addField("Visible Status V", status);
    }
}