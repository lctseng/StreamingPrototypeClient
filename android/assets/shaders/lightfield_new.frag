#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOWP
#define HIGH
#endif

uniform mat4 u_rk_to_rf;

uniform float u_cameraPositionX;
uniform float u_cameraPositionY;

uniform float u_cameraStep;

uniform float u_apertureSize;

uniform int u_cols;
uniform int u_rows;

uniform int u_colStart;
uniform int u_colEnd;

uniform int u_rowStart;
uniform int u_rowEnd;

uniform int u_colTextureOffset;
uniform float u_columnPositionRatio;

uniform int u_screenWidth;
uniform int u_screenHeight;

uniform int u_screenOffsetX;
uniform int u_screenOffsetY;

uniform mat4 u_rf_to_rd_center;

uniform sampler2D u_custom_texture0;
uniform int u_texture_valid0;

uniform sampler2D u_custom_texture1;
uniform int u_texture_valid1;

uniform sampler2D u_custom_texture2;
uniform int u_texture_valid2;

uniform sampler2D u_custom_texture3;
uniform int u_texture_valid3;

uniform sampler2D u_custom_texture4;
uniform int u_texture_valid4;

uniform sampler2D u_custom_texture5;
uniform int u_texture_valid5;

uniform sampler2D u_custom_texture6;
uniform int u_texture_valid6;

uniform sampler2D u_custom_texture7;
uniform int u_texture_valid7;

#ifdef diffuseTextureFlag
varying vec2 v_diffuseUV;
uniform sampler2D u_diffuseTexture;
#endif

void main() {

	// project 
	// RK(s,t) -> RF(s,t) , range: [-1,1]
	float screen_x = 2.0 * ((gl_FragCoord.x - float(u_screenOffsetX))/float(u_screenWidth)) - 1.0;
	float screen_y = (2.0 * ((gl_FragCoord.y - float(u_screenOffsetY))/float(u_screenHeight) ) - 1.0) * 1.0;


	float spanX = 2.0 * u_columnPositionRatio / float(u_cols);
	float spanY = 2.0 / float(u_rows);

	if(screen_x >=-1.0 && screen_x <= 1.0 && screen_y >=-1.0 && screen_y <= 1.0){

		if(u_colStart >= 0){
			vec4 rk = vec4(screen_x,screen_y, 1.0, 1.0);

			vec4 rf = u_rk_to_rf * rk;

			vec4 outputColor = vec4(0,0,0,0);
			float accumulateWeight = 0.0;

			float initCameraX = -1.0 * u_columnPositionRatio + 0.5 * spanX;
			float initCameraY = -1.0 + 0.5 * spanY;

			mat4 u_rf_to_rd = u_rf_to_rd_center;
			// for each D(s,t)
			for(int i=u_colStart;i<=u_colEnd;++i){
				for(int j=u_rowStart;j<=u_rowEnd;++j){

					int columnTextureIndex = i - u_colTextureOffset;
					
					float cameraX = (initCameraX + float(i) * spanX) * u_cameraStep;
					float cameraY = (initCameraY + float(j) * spanY) * u_cameraStep;

					float dx = cameraX - u_cameraPositionX;
					float dy = cameraY - u_cameraPositionY;

					float dist = dx * dx +  dy * dy;

					if(dist < u_apertureSize){
						// prepare matrix from rf to rd
						u_rf_to_rd[3][0] = cameraX;
						u_rf_to_rd[3][1] = cameraY;
						// compute RD(s,t)
						vec4 rd = u_rf_to_rd * rf;

						// RF(s,t) -> RD(s,t): Given
						// sample texture with RD(s,t)
						// RD is in clip space
						// Map RD into NDC(-1,1)
						vec3 ndc_pos = rd.xyz / rd.w;

						// need to map [-1,1] to [0,1] for sampling
						vec2 UV;
						UV.s = ndc_pos.s / 2.0 + 0.5;
						UV.t = ndc_pos.t / 2.0 + 0.5;

						if(UV.s >= 0.0 && UV.s <= 1.0 && UV.t >= 0.0 && UV.t <= 1.0){
							// compute weight
							float weight = (u_apertureSize - dist)/u_apertureSize;
							
							
							// UV valid, sample D-ij(s,t)
							// v scaling
							vec2 realUV;
							realUV.s = UV.s;
							realUV.t = 1.0 - (float(j) * spanY + UV.t * spanY) / 2.0;
							
							if(columnTextureIndex == 0 && u_texture_valid0 == 1){
								accumulateWeight += weight;
								outputColor += texture2D(u_custom_texture0, realUV) * weight;
							}
							else if(columnTextureIndex == 1 && u_texture_valid1 == 1){
								accumulateWeight += weight;
								outputColor += texture2D(u_custom_texture1, realUV) * weight;
							}
							else if(columnTextureIndex == 2 && u_texture_valid2 == 1){
								accumulateWeight += weight;
								outputColor += texture2D(u_custom_texture2, realUV) * weight;
							}
							else if(columnTextureIndex == 3 && u_texture_valid3 == 1){
								accumulateWeight += weight;
								outputColor += texture2D(u_custom_texture3, realUV) * weight;
							}
							else if(columnTextureIndex == 4 && u_texture_valid4 == 1){
								accumulateWeight += weight;
								outputColor += texture2D(u_custom_texture4, realUV) * weight;
							}
							else if(columnTextureIndex == 5 && u_texture_valid5 == 1){
								accumulateWeight += weight;
								outputColor += texture2D(u_custom_texture5, realUV) * weight;
							}
							else if(columnTextureIndex == 6 && u_texture_valid6 == 1){
								accumulateWeight += weight;
								outputColor += texture2D(u_custom_texture6, realUV) * weight;
							}
							else if(columnTextureIndex == 7 && u_texture_valid7 == 1){
								accumulateWeight += weight;
								outputColor += texture2D(u_custom_texture7, realUV) * weight;
							}
						}
					}
				}
			}
			
			if(accumulateWeight > 0.0){
				// output color maybe overweight
				outputColor = outputColor / accumulateWeight;
			}
			else{
				outputColor = vec4(0.4,0,0,1);
			}
				
			gl_FragColor.rgb = outputColor.rgb;
			//gl_FragColor.rgb = vec3(rf.x, rf.y, 0);
		}
		else{
			gl_FragColor.rgb = vec3(0.4,0,0);
		}
	}
	else{
		gl_FragColor.rgb = vec3(0,0,1);
	}
}