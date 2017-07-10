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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.covart.streaming_prototype.ConfigManager;
import com.covart.streaming_prototype.StringPool;

import java.util.Locale;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 * @author Xoppa
 */
public class LightFieldShader extends DefaultShader{

    private TextureManager textureManager;

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public void setTextureManager(TextureManager textureManager) {
        this.textureManager = textureManager;
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
    public void render(Renderable renderable, Attributes combinedAttributes) {
        bindProjections();
        bindTexture();
        program.setUniformi("u_cols",ConfigManager.getNumOfLFs());
        program.setUniformi("u_rows",ConfigManager.getNumOfSubLFImgs());
        program.setUniformi("colLimit", ConfigManager.getNumOfMaxInterpolatedLFRadius());
        program.setUniformf("u_apertureSize",ConfigManager.getApertureSize() / 50.0f);
        program.setUniformf("u_cameraPositionX", -camera.position.x);
        program.setUniformf("u_cameraPositionY", -camera.position.y);
        program.setUniformf("u_positionFactor",ConfigManager.getCameraStepY() * 1);

        StringPool.addField("Camera Position", String.format(Locale.TAIWAN, "X: %4f, Y: %4f, Z: %4f",camera.position.x,camera.position.y,camera.position.z));
        super.render(renderable, combinedAttributes);
    }

    private void bindTexture(){
        if(textureManager != null) {
            textureManager.bindTextures(program);
        }
    }

    private void bindProjections(){
        bindRkRfProjection();
        bindRfRdProjections();
    }

    private void bindRkRfProjection(){
        Matrix4 inverseProj = camera.invProjectionView;
        float screen_x = Display.screenX / Gdx.graphics.getWidth();//(Display.screenX - ((Gdx.graphics.getWidth() - Gdx.graphics.getHeight()) / 2))/ Gdx.graphics.getHeight();
        float screen_y = Display.screenY / Gdx.graphics.getHeight();
        float ndc_x = 2.0f*(screen_x) - 1.0f;
        float ndc_y = -2.0f*(screen_y) + 1.0f;
        Vector3 posScreen = new Vector3(ndc_x,ndc_y,0);
        posScreen.prj(inverseProj);
        //Gdx.app.log("Shader", "NDC X:" + ndc_x + ", NDC Y: " + ndc_y + ", Converted: (" + posScreen.x + "," + posScreen.y + ", " + posScreen.z + ")");
        float[] data = {posScreen.x, posScreen.y, posScreen.z};
        program.setUniform3fv("u_test_position",data, 0, 3);
        program.setUniformMatrix("u_rk_to_rf",inverseProj);
        program.setUniformMatrix("u_myProjView",camera.combined);
    }

    private void bindRfRdProjections(){
        int totalCols = ConfigManager.getNumOfLFs();
        int totalRows = ConfigManager.getNumOfSubLFImgs();

        float spanX = 2.0f / totalCols;
        float spanY = 2.0f / totalRows;



        float initCameraX = -1.0f + 0.5f * spanX;
        float initCameraY = -1.0f + 0.5f * spanY;
        for(int i=0;i< totalCols;++i){
            for(int j=0;j<totalRows;++j){

                PerspectiveCamera cam = new PerspectiveCamera(67, ConfigManager.getImageWidth(), ConfigManager.getImageHeight());

                float camera_x = ( initCameraX + i * spanX ) * ConfigManager.getCameraStepY() * 1;
                float camera_y = ( initCameraY + j * spanY ) * ConfigManager.getCameraStepY() * 1;
                cam.position.set( camera_x, camera_y, 0f);
                cam.lookAt(camera_x,camera_y,-1);
                cam.near = 0.1f;
                cam.far = 100f;
                /*
                PerspectiveCamera cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                cam.position.set(0f, 0f, 1f);
                cam.lookAt(0,0,-1);
                cam.near = 1f;
                cam.far = 3f;
                */


                cam.update();
                String name = "u_rf_to_rd" + i + "_" + j;
                program.setUniformMatrix(name, cam.combined);


            }
        }
    }
}