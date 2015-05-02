//  Lame fire

#version 120

uniform   float time;      //  Time
attribute float Start;     //  Start time
attribute vec3  Vel;       //  Initial velocity
uniform sampler3D Noise3D; //  Noise texture

//  Perlin noise function
float noise(vec3 X)
{
   vec4 nv = texture3D(Noise3D,X);
   return nv[0]+nv[1]+nv[2]+nv[3]+0.03135;
}

void main(void)
{
   //  Particle life is mod Start time
   float t = mod(time,Start);

   //  Initial position
   vec4 vert = gl_Vertex;
   //  Noisy movement
   vec3 X = vec3(0.1*t,vert.x,vert.y)+gl_Color.rgb;
   vert.y += t;
   vert.x += noise(X)-0.2;
   vert.z += noise(X)-0.2;
   //  Contract with elevation
   vert.x *= 1.0-0.5*vert.y;
   vert.z *= 1.0-0.5*vert.y;
   //  Noisy yellow trending to red
   gl_FrontColor = vec4(1,gl_Color.r-0.2*vert.y,0,1);
   //  Transform particle location
   gl_Position = gl_ModelViewProjectionMatrix*vert;
}
