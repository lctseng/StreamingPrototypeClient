
#ifdef GL_ES
precision highp float;
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

uniform int u_texture_valid0;
uniform int u_texture_valid1;
uniform int u_texture_valid2;
uniform int u_texture_valid3;
uniform int u_texture_valid4;
uniform int u_texture_valid5;
uniform int u_texture_valid6;
uniform int u_texture_valid7;
uniform int u_texture_valid8;
uniform int u_texture_valid9;
uniform int u_texture_valid10;
uniform int u_texture_valid11;
uniform int u_texture_valid12;
uniform int u_texture_valid13;
uniform int u_texture_valid14;
uniform int u_texture_valid15;

uniform int rows;
uniform int cols;
uniform float cameraPositionX;
uniform float cameraPositionY;
uniform float focusPointX;
uniform float focusPointY;
uniform float apertureSize;
uniform int col_start;
uniform int col_end;
uniform int interop_span;

uniform int enable_distortion_correction;
uniform float lensFactorX;
uniform float lensFactorY;

void main(void) {
	float spanX = 1.0 / float(cols);
	float spanY = 1.0 / float(rows);
	float cameraIndexX = cameraPositionX * float(cols - 1);
	float cameraIndexY = cameraPositionY * float(rows - 1);
	
	int cameraIndexXRounded = int(floor(cameraIndexX + 0.5));
	int cameraIndexYRounded = int(floor(cameraIndexY + 0.5));
	
	float gapRatio = 8.0;

	float cameraGapX = gapRatio / float(cols - 1);
	float cameraGapY = gapRatio / float(rows - 1);
	float initCameraX = -gapRatio * 0.5;
	float initCameraY = -gapRatio * 0.5;
	float focusRatio = 10.0 * gapRatio;

	float centerCameraX = initCameraX + cameraIndexX * cameraGapX;
	float centerCameraY = initCameraY + cameraIndexY * cameraGapY;

	vec4 color = vec4(0.0, 0.0, 0.0, 0.0);
	int  validPixelCount = 0;
	
	
	for (int i = 0; i < rows; i++) {
		for (int j = col_start; j < col_end; j++) {
			float cameraX = initCameraX + float(j) * cameraGapX;
			float cameraY = initCameraY + float(i) * cameraGapY;
			float dx = cameraX - centerCameraX;
			float dy = cameraY - centerCameraY;
			int xDiff = cameraIndexXRounded - j;
			if(xDiff < 0){
				xDiff *= -1;
			}
			int yDiff = cameraIndexYRounded - i;
			if(yDiff < 0){
				yDiff *= -1;
			}
			if (dx * dx + dy * dy < apertureSize && xDiff <= interop_span && yDiff <= interop_span) {
				
				// map texture coordinate from [0,1] to NDC [-1, 1]
				float projX   = 2.0 * textureCoords.s - 1.0;
				float projY   = 2.0 * textureCoords.t - 1.0;

				if(enable_distortion_correction != 0){
					// distortion
					// ref: http://paulbourke.net/miscellaneous/lenscorrection/
					
					float r2 = projX * projX + projY * projY;
					float rScaleX = 1.0 - lensFactorX*r2;
					float rScaleY = 1.0 - lensFactorY*r2;
					
					vec2 P;
					P.x = projX / rScaleX;
					P.y = projY / rScaleY;
					
					
					float rr2 = dot(P, P);
					projX = projX / ( 1.0 - (lensFactorX * rr2 )  );
					projY = projY / ( 1.0 - (lensFactorY * rr2 )  );
				}
				

				if(projX >= -1.0 && projY >= -1.0 && projX < 1.0 && projY < 1.0) {
					
					// apply focus shift
					float pixelX = projX - dx * focusPointX;
					float pixelY = projY + dy * focusPointY;
					
					
					// convert back to [0, 1]
					float px = 0.5 * pixelX + 0.5;
					float py = 0.5 * pixelY + 0.5;
					if(px >= 0.0 && py >= 0.0 && px < 1.0 && py < 1.0) {
					
						float global_y = (float(i) + py)*spanY;
						
						int tex_index = j - col_start;
						float remainX = px;
						vec2 V;
						V.x = remainX;
						V.y = global_y;
						
						
						if(tex_index == 0 && u_texture_valid0 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture0, V);
						}
						else if(tex_index == 1 && u_texture_valid1 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture1, V);
						}
						else if(tex_index == 2 && u_texture_valid2 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture2, V);
						}
						else if(tex_index == 3 && u_texture_valid3 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture3, V);
						}
						else if(tex_index == 4 && u_texture_valid4 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture4, V);
						}
						else if(tex_index == 5 && u_texture_valid5 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture5, V);
						}
						else if(tex_index == 6 && u_texture_valid6 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture6, V);
						}
						else if(tex_index == 7 && u_texture_valid7 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture7, V);
						}
						else if(tex_index == 8 && u_texture_valid8 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture8, V);
						}
						else if(tex_index == 9 && u_texture_valid9 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture9, V);
						}
						else if(tex_index == 10 && u_texture_valid10 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture10, V);
						}
						else if(tex_index == 11 && u_texture_valid11 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture11, V);
						}
						else if(tex_index == 12 && u_texture_valid12 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture12, V);
						}
						else if(tex_index == 13 && u_texture_valid13 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture13, V);
						}
						else if(tex_index == 14 && u_texture_valid14 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture14, V);
						}
						else if(tex_index == 15 && u_texture_valid15 > 0){
							validPixelCount++;
							color = color + texture2D(u_custom_texture15, V);
						}
					}
				}
			}
		}
	}	
	if(validPixelCount == 0){
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	}
	else{
		gl_FragColor = color / float(validPixelCount);
	}
	
}
