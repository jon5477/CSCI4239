package proj;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

import java.io.IOException;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

public class FuckThis {
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
			CLProgram program = context.createProgram(FuckThis.class.getResourceAsStream("/ex2.cl")).build();

			// A, B are input buffers, C is for the result
			CLBuffer<IntBuffer> clBufferA = context.createIntBuffer(globalWorkSize, READ_ONLY);
			CLBuffer<IntBuffer> clBufferB = context.createIntBuffer(globalWorkSize, READ_ONLY);
			CLBuffer<IntBuffer> clBufferC = context.createIntBuffer(globalWorkSize * 5, WRITE_ONLY);
			System.out.println("Global Work Size: " + globalWorkSize);
			System.out.println("Allocated: " + globalWorkSize * 5);

			out.println("used device memory: " + (clBufferA.getCLSize()+clBufferB.getCLSize()+clBufferC.getCLSize())/1000000 + "MB");

			// fill input buffers with random numbers
			// (just to have test data; seed is fixed -> results will not change between runs).
			fillBuffer(clBufferA.getBuffer(), 12345);
			fillBuffer(clBufferB.getBuffer(), 67890);

			// get a reference to the kernel function with the name 'VectorAdd'
			// and map the buffers to its input parameters.
			CLKernel kernel = program.createCLKernel("VectorAdd");
			kernel.putArgs(clBufferA, clBufferB, clBufferC).putArg(elementCount);

			// asynchronous write of data to GPU device,
			// followed by blocking read to get the computed results back.
			long time = nanoTime();
			queue.putWriteBuffer(clBufferA, false)
				 .putWriteBuffer(clBufferB, false)
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

			out.println("computation took: "+(time/1000000)+"ms");
			//out.println("computed  hashes.")
		}finally{
			// cleanup all resources associated with this context.
			context.release();
		}

	}

	private static void fillBuffer(IntBuffer buffer, int seed) {
		//Random rnd = new Random(seed);
		while (buffer.remaining() != 0) {
			buffer.put(1337);
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