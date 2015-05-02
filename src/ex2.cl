// OpenCL Kernel Function for element by element vector addition
    kernel void VectorAdd(global const int* a, global const int* b, global int* c, int numElements) {

        // get index into global data array
        int iGID = get_global_id(0);

        // bound check, equivalent to the limit on a 'for' loop
        if (iGID >= numElements)  {
            return;
        }

        // add the vector elements
        //c[iGID] = a[iGID] + b[iGID];
        // process 5 ints at a time
        c[(iGID * 5)] = 1;
        c[(iGID * 5) + 1] = 2;
        c[(iGID * 5) + 2] = 3;
        c[(iGID * 5) + 3] = 4;
        c[(iGID * 5) + 4] = 5;
    }