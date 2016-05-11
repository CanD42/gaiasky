#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define exposure 1.0

uniform vec3 v3LightPos;
uniform float g;
uniform float g2;
uniform float fColorOpacity;
uniform float fAlpha;

// Direction from the vertex to the camera
varying vec3 v3Direction;
// Calculated colors
varying vec3 frontColor;
varying vec3 frontSecondaryColor;

void main(void) {
    float fCos = dot (v3LightPos, v3Direction) / length (v3Direction);
    float fCos2 = fCos * fCos;
    float fRayleighPhase = 0.75 + 0.75 * fCos2;
    float fMiePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + fCos2) / pow (1.0 + g2 - 2.0 * g * fCos, 1.5);

    gl_FragColor.rgb = (fRayleighPhase * frontColor.rgb + fMiePhase * frontSecondaryColor.rgb);
    gl_FragColor.rgb = vec3(1.0) - exp(gl_FragColor.rgb * -exposure);
    
    gl_FragColor.a = fAlpha * (gl_FragColor.b + fColorOpacity * length (gl_FragColor));
    
    // Prevent saturation
    gl_FragColor = clamp(gl_FragColor, 0.0, 1.0);
    gl_FragColor.rgb *= 0.98;
}
