// NDC to RGB Shader

varying vec4 color;

void main() {
	color = ((gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex) / gl_Position.w + vec4(1)) / vec4(2);
}