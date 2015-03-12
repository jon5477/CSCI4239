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
	if (lightIntensity > 0.1) {
		color *= lightIntensity;
	} else {
		color *= 0.1;
	}
	float fun = sin(cos(time));
	if (fun > 0.25 && fun < 0.39) {
		color *= fun;
	}
	if (color.x > 0.5) {
		color.x = 0.5;
	}
	if (color.y < 0.1) {
		color.x = 0.1;
	}
	if (color.z > 0.9) {
		color.z = 0.9;
	}
	gl_FragColor = vec4(color, 1.0);
}