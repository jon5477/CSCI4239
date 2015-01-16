package util;

class Material {
	// Material name
	String name;
	// Colors and shininess
	float[] Ke = new float[4];
	float[] Ka = new float[4];
	float[] Kd = new float[4];
	float[] Ks = new float[4];
	float[] Ns = new float[4];
	// Transparency
	float d;
	// Texture
	int map;
}