
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
uniform sampler2D u_custom_texture8;
uniform sampler2D u_custom_texture9;
uniform sampler2D u_custom_texture10;
uniform sampler2D u_custom_texture11;
uniform sampler2D u_custom_texture12;
uniform sampler2D u_custom_texture13;
uniform sampler2D u_custom_texture14;
uniform sampler2D u_custom_texture15;


uniform int rows;
uniform int cols;
uniform float cameraPositionX;
uniform float cameraPositionY;
uniform float focusPoint;
uniform float apertureSize;
uniform int col_start;
uniform int col_end;

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
		for (int j = col_start; j < col_end; j++) {
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
					validPixelCount++;
					float global_y = (float(i) + py)*spanY;
					
					int tex_index = j - col_start;
					float remainX = px;
					vec2 V;
					V.x = remainX;
					V.y = global_y;
					if(tex_index == 0){
						color = color + texture2D(u_custom_texture0, V);
					}
					else if(tex_index == 1){
						color = color + texture2D(u_custom_texture1, V);
					}
					else if(tex_index == 2){
						color = color + texture2D(u_custom_texture2, V);
					}
					else if(tex_index == 3){
						color = color + texture2D(u_custom_texture3, V);
					}
					else if(tex_index == 4){
						color = color + texture2D(u_custom_texture4, V);
					}
					else if(tex_index == 5){
						color = color + texture2D(u_custom_texture5, V);
					}
					else if(tex_index == 6){
						color = color + texture2D(u_custom_texture6, V);
					}
					else if(tex_index == 7){
						color = color + texture2D(u_custom_texture7, V);
					}
					else if(tex_index == 8){
						color = color + texture2D(u_custom_texture8, V);
					}
					else if(tex_index == 9){
						color = color + texture2D(u_custom_texture9, V);
					}
					else if(tex_index == 10){
						color = color + texture2D(u_custom_texture10, V);
					}
					else if(tex_index == 11){
						color = color + texture2D(u_custom_texture11, V);
					}
					else if(tex_index == 12){
						color = color + texture2D(u_custom_texture12, V);
					}
					else if(tex_index == 13){
						color = color + texture2D(u_custom_texture13, V);
					}
					else if(tex_index == 14){
						color = color + texture2D(u_custom_texture14, V);
					}
					else if(tex_index == 15){
						color = color + texture2D(u_custom_texture15, V);
					}
				}
			}
		}
	}	
	if(validPixelCount == 0){
		gl_FragColor = vec4(0.5, 0.0, 0.0, 0.0);
	}
	else{
		gl_FragColor = color / float(validPixelCount);
	}
	
}
