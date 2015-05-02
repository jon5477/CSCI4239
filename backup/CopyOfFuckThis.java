package proj;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

public class CopyOfFuckThis {
	public static void main(String[] args) throws IOException {
		// set up (uses default CLPlatform and creates context for all devices)
		CLContext context = CLContext.create();
		out.println("created "+context);
		
		// always make sure to release the context under all circumstances
		// not needed for this particular sample but recommented
		try{
			
			// select fastest device
			CLDevice device = context.getMaxFlopsDevice();
			out.println("using "+device);

			// create command queue on device.
			CLCommandQueue queue = device.createCommandQueue();

			int elementCount = 1; // Amount of hashes to process
			int localWorkSize = min(device.getMaxWorkGroupSize(), 256); // Local work size dimensions
			int globalWorkSize = roundUp(localWorkSize, elementCount); // rounded up to the nearest multiple of the localWorkSize
			// load sources, create and build program
			CLProgram program;
			File cf = new File("shatest.cl");
			try (FileInputStream fis = new FileInputStream(cf);) {
				program = context.createProgram(fis).build();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			// A, B are input buffers, C is for the result
			CLBuffer<ByteBuffer> clBufferA = context.createByteBuffer(elementCount * 5, READ_ONLY);
			CLBuffer<IntBuffer> clBufferC = context.createIntBuffer(globalWorkSize * 5, WRITE_ONLY);
			System.out.println("Global Work Size: " + globalWorkSize);
			System.out.println("Allocated: " + globalWorkSize * 5);

			out.println("used device memory: " + (clBufferA.getCLSize()+clBufferC.getCLSize())/1000000 + "MB");

			// fill input buffers with random numbers
			// (just to have test data; seed is fixed -> results will not change between runs).
			fillBuffer(clBufferA.getBuffer(), elementCount * 5);

			// get a reference to the kernel function with the name 'VectorAdd'
			// and map the buffers to its input parameters.
			CLKernel kernel = program.createCLKernel("hash_SHA1");
			kernel.putArgs(clBufferA, clBufferC).putArg(elementCount);

			// asynchronous write of data to GPU device,
			// followed by blocking read to get the computed results back.
			long time = nanoTime();
			queue.putWriteBuffer(clBufferA, false)
				 .put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
				 .putReadBuffer(clBufferC, true);
			time = nanoTime() - time;

			// print first few elements of the resulting buffer to the console.
			out.println("a+b=c results snapshot: ");
			for(int i = 0; i < 50; i++) {
				//out.print(clBufferA.getBuffer().get() + "+" + clBufferB.getBuffer().get() + "=");
				out.print(clBufferC.getBuffer().get() + ", ");
			}
			out.println("...; " + clBufferC.getBuffer().remaining() + " more");

			out.println("computation took: " + (time/1000000) + "ms");
			//out.println("computed  hashes.")
		}finally{
			// cleanup all resources associated with this context.
			context.release();
		}

	}

	private static void fillBuffer(ByteBuffer buffer, int size) {
		for (int i = 0; i < size; i++) {
			buffer.put(i, (byte) 1);
		}
		buffer.rewind();
	}

	private static int roundUp(int groupSize, int globalSize) {
		int r = globalSize % groupSize;
		if (r == 0) {
			return globalSize;
		}
		return globalSize + groupSize - r;
	}
}