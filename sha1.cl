#define h0 0x67452301
#define h1 0xEFCDAB89
#define h2 0x98BADCFE
#define h3 0x10325476
#define h4 0xC3D2E1F0

__kernel void hash_SHA1(__global unsigned char* input, unsigned int len, __global unsigned int* output) {
    int tid = get_global_id(0);
    int ml = len * 8;
    char buffer[64];
    for (int i = 0; i < len; i++) {
        buffer[i] = input[i];
    }
    buffer[len] = 0x80; // Append bit '1' to message
    // Now add the length (in bits) of the entire message as 64 bits
    buffer[60] = (ml >> 24 & 0xFF);
    buffer[61] = (ml >> 16 & 0xFF);
    buffer[62] = (ml >> 8 & 0xFF);
    buffer[63] = (ml & 0xFF);
    // Process the message in successive 512-bit chunks
    int w[80];
    for (int i = 0; i < 16; i++) {
        w[i] += (buffer[(4 * i)] & 0xFF) << 24;
        w[i] += (buffer[(4 * i) + 1] & 0xFF) << 16;
        w[i] += (buffer[(4 * i) + 2] & 0xFF) << 8;
        w[i] += (buffer[(4 * i) + 3] & 0xFF);
    }
    // Extend the sixteen 32-bit words into eighty 32-bit words:
    for (int i = 16; i < 80; i++) {
        w[i] = rotate((w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16]), 1);
    }
    // Initialize output
    output[tid] = h0;
    output[tid + 1] = h1;
    output[tid + 2] = h2;
    output[tid + 3] = h3;
    output[tid + 4] = h4;
    // Initialize hash value for this chunk:
    int a = output[tid];
    int b = output[tid + 1];
    int c = output[tid + 2];
    int d = output[tid + 3];
    int e = output[tid + 4];
    // Main loop:
    for (int i = 0; i < 80; i++) {
        int f;
        int k;
        if (i >= 0 && i <= 19) {
            f = (b & c) | (~b & d);
            k = 0x5A827999;
        } else if (i >= 20 && i <= 39) {
            f = b ^ c ^ d;
            k = 0x6ED9EBA1;
        } else if (i >= 40 && i <= 59) {
            f = (b & c) | (b & d) | (c & d);
            k = 0x8F1BBCDC;
        } else if (i >= 60 && i <= 79) {
            f = b ^ c ^ d;
            k = 0xCA62C1D6;
        }
        int temp = rotate(a, 5) + f + e + k + w[i];
        e = d;
        d = c;
        c = rotate(b, 30);
        b = a;
        a = temp;
    }
    // Add this chunk's hash to result so far:
    output[tid] = output[tid] + a;
    output[tid + 1] = output[tid + 1] + b;
    output[tid + 2] = output[tid + 2] + c;
    output[tid + 3] = output[tid + 3] + d;
    output[tid + 4] = output[tid + 4] + e;
}