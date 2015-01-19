// NDC to RGB Shader

varying vec4 color;

void main() {
	// Normalized Device Coordinates: gl_Vertex
	// RGB Color Vector: color
	color = mix(vec4(0), vec4(1), ((gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex / vec4(gl_Vertex.w)) + vec4(1)) / vec4(2));
}