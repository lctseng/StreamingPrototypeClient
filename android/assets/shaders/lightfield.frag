
#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 position;
varying vec2 textureCoords;

uniform sampler2D u_texture;
uniform sampler2D u_custom_texture0;
uniform sampler2D u_custom_texture1;
uniform sampler2D u_custom_texture2;
uniform sampler2D u_custom_texture3;
uniform sampler2D u_custom_texture4;
uniform sampler2D u_custom_texture5;
uniform sampler2D u_custom_texture6;
uniform sampler2D u_custom_texture7;


uniform int rows;
uniform int cols;
uniform float cameraPositionX;
uniform float cameraPositionY;
uniform float focusPoint;
uniform float apertureSize;

void main(void) {
		float spanX = 1.0 / float(cols);
		float spanY = 1.0 / float(rows);
		float cameraIndexX = cameraPositionX * float(cols - 1);
		float cameraIndexY = cameraPositionY * float(rows - 1);
		float gapRatio = 8.0;

		float cameraGapX = gapRatio / float(cols - 1);
		float cameraGapY = gapRatio / float(rows - 1);
		float initCameraX = -cameraGapX * float(cols - 1) * 0.5;
		float initCameraY = -cameraGapY * float(rows - 1) * 0.5;
		float focusRatio = 10.0 * gapRatio;

		float centerCameraX = initCameraX + cameraIndexX * cameraGapX;
		float centerCameraY = initCameraY + cameraIndexY * cameraGapY;
		float focusPointRatio = 1.0 + focusPoint / focusRatio;

		vec4 color = vec4(0.0, 0.0, 0.0, 0.0);
		int  validPixelCount = 0;
		
		for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
						float cameraX = initCameraX + float(j) * cameraGapX;
						float cameraY = initCameraY + float(i) * cameraGapY;
						float dx = cameraX - centerCameraX;
						float dy = cameraY - centerCameraY;
						if (dx * dx + dy * dy < apertureSize) {
								float projX   = 2.0 * textureCoords.s - 1.0;
								float projY   = 2.0 * textureCoords.t - 1.0;
								float pixelX = cameraX + (projX - cameraX) * focusPointRatio;
								float pixelY = cameraY + (projY - cameraY) * focusPointRatio;
								float px = 0.5 * pixelX + 0.5;
								float py = 0.5 * pixelY + 0.5;
								if(px >= 0.0 && py >= 0.0 && px < 1.0 && py < 1.0) {
										vec2 V;
										V.x = float(j) * spanX + px * spanX;
										V.y = float(i) * spanY + py * spanY;
										//color = color + texture2D(u_texture, V);
										validPixelCount++;
								}
					  }
				}
		}
		int tex_index = int(textureCoords.s / spanX);
		float remainX = (textureCoords.s - spanX * float(tex_index)) / spanX;
		vec2 V;
		V.x = remainX;
		V.y = textureCoords.t;
		if(tex_index == 0){
			gl_FragColor = texture2D(u_custom_texture0, V);
		}
		else if(tex_index == 1){
			gl_FragColor = texture2D(u_custom_texture1, V);
		}
		else if(tex_index == 2){
			gl_FragColor = texture2D(u_custom_texture2, V);
		}
		else if(tex_index == 3){
			gl_FragColor = texture2D(u_custom_texture3, V);
		}
		else{
			gl_FragColor = vec4(0.5, 0.0, 0.0, 0.0);
		}
		/*
		if(validPixelCount == 0){
			gl_FragColor = vec4(0.5, 0.0, 0.0, 0.0);
		}
		else{
			gl_FragColor = color / float(validPixelCount);
		}
		*/
}
