#define h0 0x67452301
#define h1 0xEFCDAB89
#define h2 0x98BADCFE
#define h3 0x10325476
#define h4 0xC3D2E1F0

#define MAX_LENGTH 6

__kernel void hashSHA1(__global unsigned char* target_hash, __global unsigned char* input, __global unsigned char* plaintext, const int len) {
	unsigned char data_buffer[len+1];
	for (int i = 0; i < len; i++) {
		data_buffer[i] = input[i];
	}
	data_buffer[len] = 0x80; // append the bit '1' to the message
	
	
}