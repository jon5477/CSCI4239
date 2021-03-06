varying float lightIntensity;
varying vec2 modelPos;
uniform float time;

// Phong lighting intensity only
float phong() {
	//  P is the vertex coordinate on body
	vec3 P = vec3(gl_ModelViewMatrix * gl_Vertex);
	//  N is the object normal at P
	vec3 N = normalize(gl_NormalMatrix * gl_Normal);
	//  Light Position for light 0
	vec3 LightPos = vec3(gl_LightSource[0].position);
	//  L is the light vector
	vec3 L = normalize(LightPos - P);
	//  R is the reflected light vector R = 2(L.N)N - L
	vec3 R = reflect(-L, N);
	//  V is the view vector (eye at the origin)
	vec3 V = normalize(-P);

	//  Diffuse light intensity is cosine of light and normal vectors
	float Id = max(dot(L,N) , 0.0);
	//  Shininess intensity is cosine of light and reflection vectors to a power
	float Is = (Id>0.0) ? pow(max(dot(R,V) , 0.0) , gl_FrontMaterial.shininess) : 0.0;

	//  Vertex color (ignores emission and global ambient)
	vec3 color = gl_FrontLightProduct[0].ambient.rgb + Id*gl_FrontLightProduct[0].diffuse.rgb + Is*gl_FrontLightProduct[0].specular.rgb;

	//  Vertex intensity
	return length(color);
}

void main() {
	// Scalar light intensity (for fragment shader)
	lightIntensity = phong();
	// Save model coordinates (for fragment shader)
	modelPos = gl_Vertex.xy;
	// Color calculations
	vec4 pos = gl_ModelViewMatrix * gl_Vertex;
	gl_Position = gl_ProjectionMatrix * pos;
	gl_FrontColor = ((pos / pos.w + 1.0) / 2.0);
}