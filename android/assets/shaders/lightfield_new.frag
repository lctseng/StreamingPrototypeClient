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



#ifdef diffuseTextureFlag
varying HIGH vec4 v_wordCoord;
varying HIGH vec4 v_position;
varying HIGH vec2 v_diffuseUV;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

uniform vec3 u_test_position;
uniform mat4 u_rk_to_rf;

uniform mat4 u_myProjView;

uniform float u_apertureSize;

uniform mat4 u_rf_to_rd0_0;
uniform mat4 u_rf_to_rd0_1;
uniform mat4 u_rf_to_rd0_2;
uniform mat4 u_rf_to_rd0_3;
uniform mat4 u_rf_to_rd1_0;
uniform mat4 u_rf_to_rd1_1;
uniform mat4 u_rf_to_rd1_2;
uniform mat4 u_rf_to_rd1_3;
uniform mat4 u_rf_to_rd2_0;
uniform mat4 u_rf_to_rd2_1;
uniform mat4 u_rf_to_rd2_2;
uniform mat4 u_rf_to_rd2_3;
uniform mat4 u_rf_to_rd3_0;
uniform mat4 u_rf_to_rd3_1;
uniform mat4 u_rf_to_rd3_2;
uniform mat4 u_rf_to_rd3_3;


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
	#if defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
		
		
		if(u_texture_valid0 == 1){
			diffuse = texture2D(u_custom_texture0, v_diffuseUV);
		}
		//diffuse = vec4(gl_FragCoord.x / 1600.0,gl_FragCoord.y / 900.0,0,1);


		int cols = 4;
		int rows = 4;

		float spanX = 1.0 / float(cols);
		float spanY = 1.0 / float(rows);

		// project 
		// RK(s,t) -> RF(s,t)
		float screen_x = 2.0 * ((gl_FragCoord.x - 0)/1600.0) - 1.0;
		float screen_y = (2.0 * ((gl_FragCoord.y - 0)/900.0 ) - 1.0) * -1.0;
		vec4 rk = vec4(screen_x,screen_y, 1.0, 1.0);
		/*
		float l_w = 1.0 / (rk.x * u_rk_to_rf[3][0] +
							rk.y * u_rk_to_rf[3][1] + 
							rk.z * u_rk_to_rf[3][2] + 
							u_rk_to_rf[3][3]);

									
		vec4 rf = vec4((rk.x * u_rk_to_rf[0][0] + rk.y * u_rk_to_rf[0][1] + rk.z * u_rk_to_rf[0][2] +  u_rk_to_rf[0][3])*l_w,
					   (rk.x * u_rk_to_rf[1][0] + rk.y * u_rk_to_rf[1][1] + rk.z * u_rk_to_rf[1][2] +  u_rk_to_rf[1][3])*l_w,
					   (rk.x * u_rk_to_rf[2][0] + rk.y * u_rk_to_rf[2][1] + rk.z * u_rk_to_rf[2][2] +  u_rk_to_rf[2][3])*l_w,
			1.0);
		*/
		vec4 rf = u_rk_to_rf * rk;

		diffuse = vec4(0,0,0,0);
		int valid = 0;

		float initCameraX = -1.0 + 0.5 * spanX;
		float initCameraY = -1.0 + 0.5 * spanY;

		// for each D(s,t)
		for(int i=0;i<cols;++i){
			for(int j=0;j<rows;++j){


				float cameraX = initCameraX + i * spanX;
				float cameraY = initCameraY + j * spanY;
				float s2 = v_diffuseUV.s * 2.0 - 1.0;
				float t2 = v_diffuseUV.t * 2.0 - 1.0;
				float dx = cameraX - s2;
				float dy = cameraY - t2;
				if((dx * dx +  dy * dy) < u_apertureSize){

					// RC(s,t) = v_diffuseUV
					// compute RD(s,t)
					vec4 rd;
					if(i == 0 && j == 0){
						rd = u_rf_to_rd0_0 * rf;
					}
					else if(i == 0 && j == 1){
						rd = u_rf_to_rd0_1 * rf;
					}
					else if(i == 0 && j == 2){
						rd = u_rf_to_rd0_2 * rf;
					}
					else if(i == 0 && j == 3){
						rd = u_rf_to_rd0_3 * rf;
					}
					else if(i == 1 && j == 0){
						rd = u_rf_to_rd1_0 * rf;
					}
					else if(i == 1 && j == 1){
						rd = u_rf_to_rd1_1 * rf;
					}
					else if(i == 1 && j == 2){
						rd = u_rf_to_rd1_2 * rf;
					}
					else if(i == 1 && j == 3){
						rd = u_rf_to_rd1_3 * rf;
					}
					else if(i == 2 && j == 0){
						rd = u_rf_to_rd2_0 * rf;
					}
					else if(i == 2 && j == 1){
						rd = u_rf_to_rd2_1 * rf;
					}
					else if(i == 2 && j == 2){
						rd = u_rf_to_rd2_2 * rf;
					}
					else if(i == 2 && j == 3){
						rd = u_rf_to_rd2_3 * rf;
					}
					else if(i == 3 && j == 0){
						rd = u_rf_to_rd3_0 * rf;
					}
					else if(i == 3 && j == 1){
						rd = u_rf_to_rd3_1 * rf;
					}
					else if(i == 3 && j == 2){
						rd = u_rf_to_rd3_2 * rf;
					}
					else if(i == 3 && j == 3){
						rd = u_rf_to_rd3_3 * rf;
					}
					// RF(s,t) -> RD(s,t): Given
					// sample texture with RD(s,t)
					// RD is in clip space
					// Map RD into NDC
					vec3 ndc_pos = rd.xyz / rd.w;
					// need to map to [0,1] for sampling
					vec2 UV;
					UV.s = ndc_pos.s / 2.0 + 0.5;
					UV.t = ndc_pos.t / 2.0 + 0.5;

					if(UV.s >= 0.0 && UV.s <= 1.0 && UV.t >= 0.0 && UV.t <= 1.0){
						// UV valid, sample D-ij(s,t)
						// v scaling
						vec2 realUV;
						realUV.s = UV.s;
						realUV.t = j * spanY + UV.t * spanY;
						//if(i == 0 && j == 0){
						if(i == 0 && u_texture_valid0 == 1){
							valid += 1;
							diffuse += texture2D(u_custom_texture0, realUV);
						}
						else if(i == 1 && u_texture_valid1 == 1){
							valid += 1;
							diffuse += texture2D(u_custom_texture1, realUV);
						}
						else if(i == 2 && u_texture_valid2 == 1){
							valid += 1;
							diffuse += texture2D(u_custom_texture2, realUV);
						}
						else if(i == 3 && u_texture_valid3 == 1){
							valid += 1;
							diffuse += texture2D(u_custom_texture3, realUV);
						}
						//}

					}
					/*
					//vec2 testUv = v_diffuseUV;
					if(i == 0 && j == 0){
						//if(v_diffuseUV.s < 0.5 && v_diffuseUV.t < 0.5){
							diffuse = vec4(UV.s,UV.t,0,1);
						//}
					}
					else if(i == 0 && j == 1){
						if(v_diffuseUV.s < 0.5 && v_diffuseUV.t > 0.5){
							diffuse = vec4(UV.s,UV.t,0,1);
						}
					}
					else if(i == 1 && j == 0){
						if(v_diffuseUV.s > 0.5 && v_diffuseUV.t < 0.5){
							diffuse = vec4(UV.s,UV.t,0,1);
						}
					}
					else if(i == 1 && j == 1){
						if(v_diffuseUV.s > 0.5 && v_diffuseUV.t > 0.5){
							diffuse = vec4(UV.s,UV.t,0,1);
						}
					}
					*/
				}
			}
		}
		
		if(valid > 0){
			diffuse = diffuse / valid;
		}
		else{
			diffuse = vec4(0.2,0,0,1);
		}
		

		float diff_x = u_test_position.x - v_wordCoord.x;
		float diff_y = u_test_position.y - v_wordCoord.y;
		if(abs(diff_x) < 0.05 && abs(diff_y) < 0.05){
		  diffuse = vec4(1,0,0,1);
		}
		else{
			//diffuse = vec4(v_wordCoord.x ,v_wordCoord.y,0,1);
		}
		
		/*
		if(v_diffuseUV.t > 0.67){
			diffuse = vec4(rf.x, 0,0,1);
			
		}
		else if(v_diffuseUV.t > 0.33){
			diffuse = vec4(v_wordCoord.x ,0,0,1);
		}
		else{
			diffuse = vec4(rk.x, 0,0,1);
		}
		if(diffuse.x >= 1.0){
			diffuse.x = 0;
		}
		*/

		//diffuse = vec4(v_wordCoord.x ,v_wordCoord.y,0,1);
		//diffuse = vec4(v_diffuseUV.x, v_diffuseUV.y,0,1);
		//diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
		/*
		if(v_diffuseUV.s > 0.50){
			vec4 temp = u_myProjView * v_wordCoord;
			//temp.xyz /= temp.w;
			diffuse = vec4(temp.y / temp.w ,0,0,1);
		}
		if(v_diffuseUV.s > 0.60){
			vec4 temp = u_myProjView * rf;
			//temp.xyz /= temp.w;
			diffuse = vec4(temp.y / temp.w ,0,0,1);
			
		}
		if(v_diffuseUV.s > 0.70){
			diffuse = vec4(rk.y ,0,0,1);
		}
		if(v_diffuseUV.s > 0.80){
			diffuse = vec4(0 ,v_wordCoord.z + 0.5,0,1);
		}
		if(v_diffuseUV.s > 0.90){
			diffuse = vec4(rf.z + 0.5,0,0,1);
		}
		if(v_diffuseUV.t > 0.80){
			diffuse = vec4(0 ,v_wordCoord.w,0,1);
		}
		if(v_diffuseUV.t > 0.90){
			diffuse = vec4(rf.w ,0,0,1);
		}
		*/
	#else
		vec4 diffuse = vec4(1.0);
	#endif
	gl_FragColor.rgb = diffuse.rgb;
}
