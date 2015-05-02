kernel void hash_SHA1(global const char* input, global int* output, int numElements) {
    // get index into global data array
    int iGID = get_global_id(0);
    // bound check, equivalent to the limit on a 'for' loop
    if (iGID >= numElements) {
        return;
    }
	// Perform write operations here.
    for (int i = 0; i < 5; i++) {
        output[(iGID * 5) + i] = input[i] + i + 1;
    }
    // Perform read-write operations here
    //for (int i = 0; i < 5; i++) {
    //    output[(iGID * 5) + i] = output[(iGID * 5) + i] + (i + 1);
    //}
}