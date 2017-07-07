#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

void main() {
	#if defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
		diffuse = vec4(v_diffuseUV.x,v_diffuseUV.y,0,1);
	#else
		vec4 diffuse = vec4(1.0);
	#endif
	gl_FragColor.rgb = diffuse.rgb;
}
