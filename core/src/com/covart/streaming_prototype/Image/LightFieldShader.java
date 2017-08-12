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
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.Utils.Vector4;
import com.covart.streaming_prototype.StringPool;

import java.util.Locale;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.round;

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

    private Matrix4 matrixRkToRf;

    private Matrix4 tmpMatrix;
    private Vector3 tmpVector;
    private Vector3 tmpVector2;

    // for UV compute
    Vector4 cursor_rk;
    Vector4 cursor_rf;
    Vector4 cursor_rd;


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

        matrixRkToRf = new Matrix4();

        cursor_rk = new Vector4();
        cursor_rf = new Vector4();
        cursor_rd = new Vector4();

        tmpMatrix = new Matrix4();
        tmpVector = new Vector3();
        tmpVector2 = new Vector3();
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
        bindProjections();
        bindTexture();
        if(ConfigManager.isMainEye(display.currentEye())) {
            StringPool.addField("Camera Position", String.format(Locale.TAIWAN, "X: %4f, Y: %4f, Z: %4f", camera.position.x, camera.position.y, camera.position.z));
            visualizeLightFieldStatus();
            computeCursorUV();
        }
        super.render(renderable, combinedAttributes);
    }

    private void bindConfiguration(){
        program.setUniformi("u_screenWidth", display.eyeWrapper.getViewport().width);
        program.setUniformi("u_screenHeight", display.eyeWrapper.getViewport().height);
        program.setUniformi("u_screenOffsetX", 0);
        program.setUniformi("u_screenOffsetX", display.eyeWrapper.getViewport().x);
        program.setUniformi("u_screenOffsetY", display.eyeWrapper.getViewport().y);
        program.setUniformi("u_cols",ConfigManager.getNumOfLFs());
        program.setUniformi("u_rows",ConfigManager.getNumOfSubLFImgs());
        program.setUniformf("u_columnPositionRatio",ConfigManager.getColumnPositionRatio());
        program.setUniformf("u_apertureSize",ConfigManager.getApertureSize());
        program.setUniformf("u_cameraStep",ConfigManager.getCameraStep());

        program.setUniformf("u_editingScreenX", display.editingScreenPosition.x);
        program.setUniformf("u_editingScreenY", display.editingScreenPosition.y);

        program.setUniformf("u_stPlaneRadius", ConfigManager.getStPlaneRadius());
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

        // TODO: these value needs new meaning
        // for columns
        // find first index that fall into aperture
        for(int i=0;i<cols;i++){
            float cameraX = (initCameraX + i * spanX) * cameraStep;
            float dx = cameraX - getEyePosition().x;
            float dist = dx * dx; // assume dy is zero
            if(dist < ConfigManager.getApertureSize()){
                startIndex = i;
                break;
            }
        }
        // find last index that fall into aperture
        for(int i=cols - 1;i>= 0;i--){
            float cameraX = (initCameraX + i * spanX) * cameraStep;
            float dx = cameraX - getEyePosition().x;
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
            float dy = cameraY - getEyePosition().y;
            float dist = dy * dy; // assume dx is zero
            if(dist < ConfigManager.getApertureSize()){
                startRow = i;
                break;
            }
        }
        // find last index that fall into aperture
        for(int i=rows - 1;i>= 0;i--){
            float cameraY = (initCameraY + i * spanY) * cameraStep;
            float dy = cameraY - getEyePosition().y;
            float dist = dy * dy; // assume dy is zero
            if(dist < ConfigManager.getApertureSize()){
                endRow = i;
                break;
            }
        }

        // FIXME: these values should have new meanings
        startIndex = 0;
        endIndex = cols-1;
        startRow = 0;
        endRow = rows - 1;

        program.setUniformi("u_colTextureOffset", startIndex);

        program.setUniformi("u_colStart",startIndex);
        program.setUniformi("u_colEnd",endIndex);
        program.setUniformi("u_rowStart",startRow);
        program.setUniformi("u_rowEnd",endRow);


        StringPool.addField("Columns", "" + startIndex + "-" + endIndex);
        StringPool.addField("Rows", "" + startRow + "-" + endRow);

    }

    private Vector3 getEyePosition(){
        return display.eyeWrapper.getLastEyePosition();
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
        float[] perspective = display.eyeWrapper.getPerspective(0.01f, 3.0f + ratio * ratio);
        tmpMatrix.set(perspective);
        ((CardboardCamera)camera).setEyeProjection(tmpMatrix);
        camera.update();
        matrixRkToRf.set(camera.invProjectionView);
        program.setUniformMatrix("u_rk_to_rf",matrixRkToRf);
    }

    private void bindRfRdProjections(){
        if(startIndex >= 0) {
            dataCamera.fieldOfView = ConfigManager.getDataCameraFOV();
            dataCamera.update();
            program.setUniformMatrix("u_rf_to_rd_center", dataCamera.combined);
        }
    }

    private void visualizeLightFieldStatus(){
        //visualizeVisibleColumn();
        visualizeCameraPosition();
        if(display != null){
            display.getTextureManager().visualizeColumnStatus();
        }
    }

    // TODO: #34 this is debug only, should be remove in the future

    private void visualizeCameraPosition(){
        int index = clamp(round((getEyePosition().x / 4f + 0.5f) * ConfigManager.getNumOfLFs()), 0, ConfigManager.getNumOfLFs() - 1);
        String status = "";
        for(int i=0;i<index;i++){
            status += "=";
        }
        status += "+";
        for(int i=index+1;i<ConfigManager.getNumOfLFs();i++){
            status += "=";
        }
        StringPool.addField("Visible Status V", status);
    }

    // FIXME: #31 wait for new model
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

    private void computeCursorUV(){
        // compute cursor UV
        // cursor projection
        float cursor_screen_x = display.editingScreenPosition.x / display.eyeWrapper.getViewport().width;
        float cursor_screen_y = display.editingScreenPosition.y / display.eyeWrapper.getViewport().height;
        // map to [-1, 1]
        cursor_screen_x = cursor_screen_x * 2.0f - 1.0f;
        cursor_screen_y = cursor_screen_y * 2.0f - 1.0f;

        cursor_rk.set(cursor_screen_x, cursor_screen_y, 1.0f, 1.0f);

        // compute RF(s,t)
        cursor_rf.set(cursor_rk);
        cursor_rf.mul(matrixRkToRf);

        // compute RD(s,t)
        // prepare matrix from rf to rd
        cursor_rd.set(cursor_rf);
        cursor_rd.mul(dataCamera.combined);

        // RF(s,t) -> RD(s,t): Given
        // sample texture with RD(s,t)
        // RD is in clip space
        // Map RD into NDC(-1,1)
        // Divided by w
        cursor_rd.scl(1.0f / cursor_rd.w);



        // map [-1,1] to [0,1] for image position calculation

        float cursor_UV_x = clamp(cursor_rd.x / 2.0f + 0.5f, 0f, 1f);
        float cursor_UV_y = clamp(cursor_rd.y / 2.0f + 0.5f, 0f, 1f);

        //StringPool.addField("Cursor UV", "X:" + cursor_UV_x + ", Y: " + cursor_UV_y);
        //StringPool.addField("Image Pos", "X:" + cursor_UV_x * ConfigManager.getImageWidth() + ", Y: " + cursor_UV_y * ConfigManager.getImageHeight());


        // TODO: convert to server coordinate (to fit the origin of server)

        display.editingImagePosition.set(cursor_UV_x * ConfigManager.getImageWidth(), cursor_UV_y * ConfigManager.getImageHeight());
    }
}