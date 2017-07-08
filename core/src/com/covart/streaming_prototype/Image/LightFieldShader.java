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

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 * @author Xoppa
 */
public class LightFieldShader extends DefaultShader{

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
}