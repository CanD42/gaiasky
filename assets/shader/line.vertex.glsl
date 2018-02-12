#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

attribute vec4 a_position;
attribute vec4 a_color;

uniform mat4 u_projModelView;
uniform vec2 u_viewport;

varying vec4 v_col;

void main() {
   gl_Position = u_projModelView * a_position;
   v_col = a_color;
}