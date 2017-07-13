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

uniform int u_screenWidth;
uniform int u_screenHeight;

uniform int u_screenOffsetX;
uniform int u_screenOffsetY;

uniform int u_enableDistortionCorrection;
uniform float u_lensFactorX;
uniform float u_lensFactorY;

uniform mat4 u_rf_to_rd0_0; 
uniform mat4 u_rf_to_rd0_1; 
uniform mat4 u_rf_to_rd0_2; 
uniform mat4 u_rf_to_rd0_3; 
uniform mat4 u_rf_to_rd0_4; 
uniform mat4 u_rf_to_rd0_5; 
uniform mat4 u_rf_to_rd0_6; 
uniform mat4 u_rf_to_rd0_7; 
uniform mat4 u_rf_to_rd1_0; 
uniform mat4 u_rf_to_rd1_1; 
uniform mat4 u_rf_to_rd1_2; 
uniform mat4 u_rf_to_rd1_3; 
uniform mat4 u_rf_to_rd1_4; 
uniform mat4 u_rf_to_rd1_5; 
uniform mat4 u_rf_to_rd1_6; 
uniform mat4 u_rf_to_rd1_7; 
uniform mat4 u_rf_to_rd2_0; 
uniform mat4 u_rf_to_rd2_1; 
uniform mat4 u_rf_to_rd2_2; 
uniform mat4 u_rf_to_rd2_3; 
uniform mat4 u_rf_to_rd2_4; 
uniform mat4 u_rf_to_rd2_5; 
uniform mat4 u_rf_to_rd2_6; 
uniform mat4 u_rf_to_rd2_7; 
uniform mat4 u_rf_to_rd3_0; 
uniform mat4 u_rf_to_rd3_1; 
uniform mat4 u_rf_to_rd3_2; 
uniform mat4 u_rf_to_rd3_3; 
uniform mat4 u_rf_to_rd3_4; 
uniform mat4 u_rf_to_rd3_5; 
uniform mat4 u_rf_to_rd3_6; 
uniform mat4 u_rf_to_rd3_7; 
uniform mat4 u_rf_to_rd4_0; 
uniform mat4 u_rf_to_rd4_1; 
uniform mat4 u_rf_to_rd4_2; 
uniform mat4 u_rf_to_rd4_3; 
uniform mat4 u_rf_to_rd4_4; 
uniform mat4 u_rf_to_rd4_5; 
uniform mat4 u_rf_to_rd4_6; 
uniform mat4 u_rf_to_rd4_7; 
uniform mat4 u_rf_to_rd5_0; 
uniform mat4 u_rf_to_rd5_1; 
uniform mat4 u_rf_to_rd5_2; 
uniform mat4 u_rf_to_rd5_3; 
uniform mat4 u_rf_to_rd5_4; 
uniform mat4 u_rf_to_rd5_5; 
uniform mat4 u_rf_to_rd5_6; 
uniform mat4 u_rf_to_rd5_7; 
uniform mat4 u_rf_to_rd6_0; 
uniform mat4 u_rf_to_rd6_1; 
uniform mat4 u_rf_to_rd6_2; 
uniform mat4 u_rf_to_rd6_3; 
uniform mat4 u_rf_to_rd6_4; 
uniform mat4 u_rf_to_rd6_5; 
uniform mat4 u_rf_to_rd6_7; 
uniform mat4 u_rf_to_rd6_6; 
uniform mat4 u_rf_to_rd7_0; 
uniform mat4 u_rf_to_rd7_1; 
uniform mat4 u_rf_to_rd7_2; 
uniform mat4 u_rf_to_rd7_3; 
uniform mat4 u_rf_to_rd7_4; 
uniform mat4 u_rf_to_rd7_5; 
uniform mat4 u_rf_to_rd7_6; 
uniform mat4 u_rf_to_rd7_7;  
                               

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


void main() {

	float spanX = 2.0 / float(u_cols);
	float spanY = 2.0 / float(u_rows);

	// project 
	// RK(s,t) -> RF(s,t) , range: [-1,1]
	float screen_x = 2.0 * ((gl_FragCoord.x - u_screenOffsetX)/float(u_screenWidth)) - 1.0;
	float screen_y = (2.0 * ((gl_FragCoord.y - u_screenOffsetY)/float(u_screenHeight) ) - 1.0) * 1.0;


	
	if(u_enableDistortionCorrection != 0){
		// lens distortion correction
		// ref: http://paulbourke.net/miscellaneous/lenscorrection/
		
		float r2 = screen_x * screen_x + screen_y * screen_y;
		float rScaleX = 1.0 - u_lensFactorX*r2;
		float rScaleY = 1.0 - u_lensFactorY*r2;
		
		vec2 P;
		P.x = screen_x / rScaleX;
		P.y = screen_y / rScaleY;
		
		
		float rr2 = dot(P, P);
		screen_x = screen_x / ( 1.0 - (u_lensFactorX * rr2 )  );
		screen_y = screen_y / ( 1.0 - (u_lensFactorY * rr2 )  );
	}
	

	if(screen_x >=-1.0 && screen_x <= 1.0 && screen_y >=-1.0 && screen_y <= 1.0){

		vec4 rk = vec4(screen_x,screen_y, 1, 1.0);

		vec4 rf = u_rk_to_rf * rk;

		vec4 outputColor = vec4(0,0,0,0);
		int valid = 0;
		float accumulateWeight = 0.0;

		float initCameraX = -1.0 + 0.5 * spanX;
		float initCameraY = -1.0 + 0.5 * spanY;

		// for each D(s,t)
		for(int i=0;i<u_cols;++i){
			for(int j=0;j<u_rows;++j){
				
				float cameraX = (initCameraX + i * spanX) * u_cameraStep;
				float cameraY = (initCameraY + j * spanY) * u_cameraStep;

				float dx = cameraX - u_cameraPositionX;
				float dy = cameraY - u_cameraPositionY;

				float dist = dx * dx +  dy * dy;

				if(dist < u_apertureSize){
					// compute RD(s,t)
					vec4 rd;

					if(i == 0 && j == 0){
						rd = u_rf_to_rd0_0 * rf;
					}
					if(i == 0 && j == 1){
						rd = u_rf_to_rd0_1 * rf;
					}
					if(i == 0 && j == 2){
						rd = u_rf_to_rd0_2 * rf;
					}
					if(i == 0 && j == 3){
						rd = u_rf_to_rd0_3 * rf;
					}
					if(i == 0 && j == 4){
						rd = u_rf_to_rd0_4 * rf;
					}
					if(i == 0 && j == 5){
						rd = u_rf_to_rd0_5 * rf;
					}
					if(i == 0 && j == 6){
						rd = u_rf_to_rd0_6 * rf;
					}
					if(i == 0 && j == 7){
						rd = u_rf_to_rd0_7 * rf;
					}
					if(i == 1 && j == 0){
						rd = u_rf_to_rd1_0 * rf;
					}
					if(i == 1 && j == 1){
						rd = u_rf_to_rd1_1 * rf;
					}
					if(i == 1 && j == 2){
						rd = u_rf_to_rd1_2 * rf;
					}
					if(i == 1 && j == 3){
						rd = u_rf_to_rd1_3 * rf;
					}
					if(i == 1 && j == 4){
						rd = u_rf_to_rd1_4 * rf;
					}
					if(i == 1 && j == 5){
						rd = u_rf_to_rd1_5 * rf;
					}
					if(i == 1 && j == 6){
						rd = u_rf_to_rd1_6 * rf;
					}
					if(i == 1 && j == 7){
						rd = u_rf_to_rd1_7 * rf;
					}
					if(i == 2 && j == 0){
						rd = u_rf_to_rd2_0 * rf;
					}
					if(i == 2 && j == 1){
						rd = u_rf_to_rd2_1 * rf;
					}
					if(i == 2 && j == 2){
						rd = u_rf_to_rd2_2 * rf;
					}
					if(i == 2 && j == 3){
						rd = u_rf_to_rd2_3 * rf;
					}
					if(i == 2 && j == 4){
						rd = u_rf_to_rd2_4 * rf;
					}
					if(i == 2 && j == 5){
						rd = u_rf_to_rd2_5 * rf;
					}
					if(i == 2 && j == 6){
						rd = u_rf_to_rd2_6 * rf;
					}
					if(i == 2 && j == 7){
						rd = u_rf_to_rd2_7 * rf;
					}
					if(i == 3 && j == 0){
						rd = u_rf_to_rd3_0 * rf;
					}
					if(i == 3 && j == 1){
						rd = u_rf_to_rd3_1 * rf;
					}
					if(i == 3 && j == 2){
						rd = u_rf_to_rd3_2 * rf;
					}
					if(i == 3 && j == 3){
						rd = u_rf_to_rd3_3 * rf;
					}
					if(i == 3 && j == 4){
						rd = u_rf_to_rd3_4 * rf;
					}
					if(i == 3 && j == 5){
						rd = u_rf_to_rd3_5 * rf;
					}
					if(i == 3 && j == 6){
						rd = u_rf_to_rd3_6 * rf;
					}
					if(i == 3 && j == 7){
						rd = u_rf_to_rd3_7 * rf;
					}
					if(i == 4 && j == 0){
						rd = u_rf_to_rd4_0 * rf;
					}
					if(i == 4 && j == 1){
						rd = u_rf_to_rd4_1 * rf;
					}
					if(i == 4 && j == 2){
						rd = u_rf_to_rd4_2 * rf;
					}
					if(i == 4 && j == 3){
						rd = u_rf_to_rd4_3 * rf;
					}
					if(i == 4 && j == 4){
						rd = u_rf_to_rd4_4 * rf;
					}
					if(i == 4 && j == 5){
						rd = u_rf_to_rd4_5 * rf;
					}
					if(i == 4 && j == 6){
						rd = u_rf_to_rd4_6 * rf;
					}
					if(i == 4 && j == 7){
						rd = u_rf_to_rd4_7 * rf;
					}
					if(i == 5 && j == 0){
						rd = u_rf_to_rd5_0 * rf;
					}
					if(i == 5 && j == 1){
						rd = u_rf_to_rd5_1 * rf;
					}
					if(i == 5 && j == 2){
						rd = u_rf_to_rd5_2 * rf;
					}
					if(i == 5 && j == 3){
						rd = u_rf_to_rd5_3 * rf;
					}
					if(i == 5 && j == 4){
						rd = u_rf_to_rd5_4 * rf;
					}
					if(i == 5 && j == 5){
						rd = u_rf_to_rd5_5 * rf;
					}
					if(i == 5 && j == 6){
						rd = u_rf_to_rd5_6 * rf;
					}
					if(i == 5 && j == 7){
						rd = u_rf_to_rd5_7 * rf;
					}
					if(i == 6 && j == 0){
						rd = u_rf_to_rd6_0 * rf;
					}
					if(i == 6 && j == 1){
						rd = u_rf_to_rd6_1 * rf;
					}
					if(i == 6 && j == 2){
						rd = u_rf_to_rd6_2 * rf;
					}
					if(i == 6 && j == 3){
						rd = u_rf_to_rd6_3 * rf;
					}
					if(i == 6 && j == 4){
						rd = u_rf_to_rd6_4 * rf;
					}
					if(i == 6 && j == 5){
						rd = u_rf_to_rd6_5 * rf;
					}
					if(i == 6 && j == 6){
						rd = u_rf_to_rd6_6 * rf;
					}
					if(i == 6 && j == 7){
						rd = u_rf_to_rd6_7 * rf;
					}
					if(i == 7 && j == 0){
						rd = u_rf_to_rd7_0 * rf;
					}
					if(i == 7 && j == 1){
						rd = u_rf_to_rd7_1 * rf;
					}
					if(i == 7 && j == 2){
						rd = u_rf_to_rd7_2 * rf;
					}
					if(i == 7 && j == 3){
						rd = u_rf_to_rd7_3 * rf;
					}
					if(i == 7 && j == 4){
						rd = u_rf_to_rd7_4 * rf;
					}
					if(i == 7 && j == 5){
						rd = u_rf_to_rd7_5 * rf;
					}
					if(i == 7 && j == 6){
						rd = u_rf_to_rd7_6 * rf;
					}
					if(i == 7 && j == 7){
						rd = u_rf_to_rd7_7 * rf;
					}

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
						accumulateWeight += weight;
						
						// UV valid, sample D-ij(s,t)
						// v scaling
						vec2 realUV;
						realUV.s = UV.s;
						realUV.t = 1.0 - (j * spanY + UV.t * spanY) / 2.0;
						
						if(i == 0 && u_texture_valid0 == 1){
							valid += 1;
							outputColor += texture2D(u_custom_texture0, realUV) * weight;
						}
						else if(i == 1 && u_texture_valid1 == 1){
							valid += 1;
							outputColor += texture2D(u_custom_texture1, realUV) * weight;
						}
						else if(i == 2 && u_texture_valid2 == 1){
							valid += 1;
							outputColor += texture2D(u_custom_texture2, realUV) * weight;
						}
						else if(i == 3 && u_texture_valid3 == 1){
							valid += 1;
							outputColor += texture2D(u_custom_texture3, realUV) * weight;
						}
						else if(i == 4 && u_texture_valid4 == 1){
							valid += 1;
							outputColor += texture2D(u_custom_texture4, realUV) * weight;
						}
						else if(i == 5 && u_texture_valid5 == 1){
							valid += 1;
							outputColor += texture2D(u_custom_texture5, realUV) * weight;
						}
						else if(i == 6 && u_texture_valid6 == 1){
							valid += 1;
							outputColor += texture2D(u_custom_texture6, realUV) * weight;
						}
						
						else if(i == 7 && u_texture_valid7 == 1){
							valid += 1;
							outputColor += texture2D(u_custom_texture7, realUV) * weight;
						}
					}
				}
			}
		}
		
		if(valid > 0 && accumulateWeight > 0.0){
			// output color maybe overweight
			outputColor = outputColor / accumulateWeight;
		}
		else{
			outputColor = vec4(0.4,0,0,1);
		}
			
		gl_FragColor.rgb = outputColor.rgb;
	}
	else{
		gl_FragColor.rgb = vec3(0,0,0);
	}
}
