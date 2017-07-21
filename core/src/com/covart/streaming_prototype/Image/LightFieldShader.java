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

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
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
        initDataCameras();

    }

    private void initDataCameras() {
        int totalCols = ConfigManager.getNumOfLFs();
        int totalRows = ConfigManager.getNumOfSubLFImgs();

        dataCamera = new PerspectiveCamera(ConfigManager.getDataCameraFOV(), ConfigManager.getImageWidth(), ConfigManager.getImageHeight());
        dataCamera.near = 0.1f;
        dataCamera.far = 100f;
        dataCamera.position.set(0, 0, 0);
        dataCamera.lookAt(0, 0, -1);
    }

    @Override
    public void render(Renderable renderable, Attributes combinedAttributes) {
        updateColumnIndex();
        bindConfiguration();
        bindPosition();
        bindProjections();
        bindTexture();

        StringPool.addField("Camera Position", String.format(Locale.TAIWAN, "X: %4f, Y: %4f, Z: %4f",camera.position.x,camera.position.y,camera.position.z));
        visualizeLightFieldStatus();
        super.render(renderable, combinedAttributes);
    }

    private void bindPosition(){
        program.setUniformf("u_cameraPositionX", camera.position.x);
        program.setUniformf("u_cameraPositionY", camera.position.y);
    }

    private void bindConfiguration(){
        //program.setUniformi("u_screenWidth", display.getScreenWidth());
        //program.setUniformi("u_screenHeight", display.getScreenHeight());
        //program.setUniformi("u_screenOffsetX", display.getScreenOffsetX());
        //program.setUniformi("u_screenOffsetY", display.getScreenOffsetY());
        program.setUniformi("u_cols",ConfigManager.getNumOfLFs());
        program.setUniformi("u_rows",ConfigManager.getNumOfSubLFImgs());
        program.setUniformf("u_columnPositionRatio",ConfigManager.getColumnPositionRatio());
        program.setUniformf("u_apertureSize",ConfigManager.getApertureSize());
        program.setUniformf("u_cameraStep",ConfigManager.getCameraStep());
    }

    private void updateColumnIndex(){
        // compute valid column index range
        int cols = ConfigManager.getNumOfLFs();
        float columnRatio = ConfigManager.getColumnPositionRatio();

        float spanX = 2f * columnRatio /cols;

        float initCameraX = -1.0f * columnRatio + 0.5f * spanX;

        float cameraStep = ConfigManager.getCameraStep();

        startIndex = -1;
        endIndex = -1;
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

        program.setUniformi("u_colTextureOffset", startIndex);
        program.setUniformi("u_colStart",startIndex);
        program.setUniformi("u_colEnd",endIndex);

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
        Matrix4 inverseProj = camera.invProjectionView;
        program.setUniformMatrix("u_rk_to_rf",inverseProj);
    }

    private void bindRfRdProjections(){
        if(startIndex >= 0) {
            dataCamera.fieldOfView = ConfigManager.getDataCameraFOV();


            float aspect = display.currentEye.getViewport().width / display.currentEye.getViewport().height;

            dataCamera.viewportWidth = display.currentEye.getViewport().width;
            dataCamera.viewportHeight = display.currentEye.getViewport().height;

            //dataCamera.viewportWidth = aspect * dataCamera.viewportHeight;
            //dataCamera.viewportHeight = dataCamera.viewportWidth / aspect;

            dataCamera.update();

            Matrix4 tmpMatrix = new Matrix4();
            Matrix4 combined = new Matrix4();

            Matrix4 eyeMatrix = new Matrix4(display.currentEye.getEyeView());


            tmpMatrix.set(eyeMatrix);
            Matrix4.mul(tmpMatrix.val, dataCamera.view.val);
            combined.set(dataCamera.projection);
            Matrix4.mul(combined.val, tmpMatrix.val);

            //program.setUniformMatrix("u_rf_to_rd_center", dataCamera.combined);
            program.setUniformMatrix("u_rf_to_rd_center", combined);



            //program.setUniformMatrix("u_rf_to_rd_center", camera.combined);
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