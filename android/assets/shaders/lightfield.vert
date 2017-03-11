
#ifdef GL_ES
    precision mediump float;
#endif

attribute vec4 a_position;
attribute vec2 a_texCoord0;

varying vec4 position;
varying vec2 textureCoords;

uniform mat4 projectionMatrix;
uniform mat4 modelviewMatrix;

void main(void) {
		gl_Position = projectionMatrix * modelviewMatrix * a_position;
		position  = modelviewMatrix * a_position;
		textureCoords = a_texCoord0;
}
