varying float lightIntensity;
uniform float time;

void main() {
	vec4 color = gl_Color * lightIntensity;
	gl_FragColor = color;
}