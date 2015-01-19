// NDC to RGB Shader

varying vec4 color;

void main() {
	// Normalized Device Coordinates: gl_Vertex
	// RGB Color Vector: color
	vec4 transformed = gl_ModelViewProjectionMatrix * gl_Vertex;
	color = mix(vec4(0), vec4(1), ((transformed / vec4(gl_Vertex.w)) + vec4(1)) / vec4(2));
	gl_Position = transformed;
}