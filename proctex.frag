varying float lightIntensity;
varying vec2 modelPos;
uniform float time;

void main() {
	// Divide by brick size for unit coordinates
	vec2 position = modelPos;
	position.x += time;
	// Don't care about the integer part
	position = fract(position);
	// Snap to 0 or 1
	// Interpolate color (0 or 1 gives sharp transition)
	vec3 color = mix(gl_Color.xyz, (gl_Color.xyz * abs(sin(time))), position.x * position.y);
	// Adjust color intensity for lighting (interpolated from vertex shader values)
	color *= lightIntensity;
	//vec4 color = gl_Color * lightIntensity;
	gl_FragColor = vec4(color, 1.0);
}