// NDC to RGB Shader

varying vec4 color;

void main() {
	// Normalized Device Coordinates
	vec4 vert = gl_Vertex / vec4(gl_Vertex.w); // Divide by w
	color = (vert + vec4(1)) / vec4(2);
	color.w = 0.0;
}