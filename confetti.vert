//  Confetti Cannon
//  derived from Orange Book

uniform   float time;  //  Time
attribute float Start; //  Start time
attribute vec3  Vel;   //  Velocity

void main(void)
{
   //  Particle location
   vec4 vert = gl_Vertex;
   //  Time offset mod 5
   float t = mod(time,5.0)-Start;

   //  Pre-launch color black
   if (t<0.0)
      gl_FrontColor = vec4(0,0,0,1);
   else
   {
      //  Particle color
      gl_FrontColor = gl_Color;
      //  Particle trajectory
      vert   += t*vec4(Vel,0);
      //  Gravity
      vert.y -= 4.9*t*t;
   }
   //  Transform particle location
   gl_Position = gl_ModelViewProjectionMatrix*vert;
}
