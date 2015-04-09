// Based on information obtained from:
// http://practicalcryptography.com/hashes/Cryptographic-category/md5/

// Define macros for the MD5 functions
#define F(x, y, z) (z ^ (x & (y ^ z)))
#define G(x, y, z) (y ^ (z & (x ^ y)))
#define H(x, y, z) (x ^ y ^ z)
#define I(x, y, z) (y ^ (x | ~z))

static void operate(func, a, b, c, d, x, t) {
	a += func(b, c, d) + x + t;
	
}