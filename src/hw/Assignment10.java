package hw;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLProgram;

public final class Assignment10 {
	public static final void main(String[] args) {
		// CPU Computation
		String s = "teststring";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new InternalError(e.getLocalizedMessage(), e);
		}
		long ctime = System.nanoTime();
		byte[] digested = md.digest(s.getBytes());
		ctime = System.nanoTime() - ctime;
		System.out.println(byteArrayToHex(digested));
		System.out.println("computation took: " + (ctime / 1000000) + "ms");
		// GPU Computation
		CLContext context = CLContext.create();
		System.out.println(context.toString());
		try {
		// select fastest device
		CLDevice device = context.getMaxFlopsDevice();
		System.out.println("using " + device);

		// create command queue on device.
		CLCommandQueue queue = device.createCommandQueue();

		int elementCount = 1444477; // Length of arrays to process
		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 256); // Local work size dimensions
		int globalWorkSize = roundUp(localWorkSize, elementCount); // rounded up to the nearest multiple of the localWorkSize

		// load sources, create and build program
		CLProgram program = context.createProgram(ClassLoader.getSystemResourceAsStream("md5.cl")).build();

		// A, B are input buffers, C is for the result
		//CLBuffer<IntBuffer> size_bytes = context.createIntBuffer(1, CLMemory.Mem.READ_ONLY);
		//CLBuffer<ByteBuffer> password = context.createByteBuffer(60, CLMemory.Mem.READ_ONLY);
		//CLBuffer<IntBuffer> output_size = context.createIntBuffer(1, CLMemory.Mem.WRITE_ONLY);
		//CLBuffer<ByteBuffer> output = context.createByteBuffer(16, CLMemory.Mem.WRITE_ONLY);
		CLBuffer<ByteBuffer> password_t = context.createByteBuffer(64, CLMemory.Mem.READ_ONLY);
		CLBuffer<ByteBuffer> password_hash_t = context.createByteBuffer(20, CLMemory.Mem.WRITE_ONLY);
		
		/*CLBuffer<FloatBuffer> clBufferA = context.createFloatBuffer(globalWorkSize, CLMemory.Mem.READ_ONLY);
		CLBuffer<FloatBuffer> clBufferB = context.createFloatBuffer(globalWorkSize, CLMemory.Mem.READ_ONLY);
		CLBuffer<FloatBuffer> clBufferC = context.createFloatBuffer(globalWorkSize, CLMemory.Mem.WRITE_ONLY);*/

		//System.out.println("used device memory: " + (size_bytes.getCLSize() + password.getCLSize() + output_size.getCLSize() + output.getCLSize()) / 1000000 +"MB");

		// fill input buffers with random numbers
		// (just to have test data; seed is fixed -> results will not change between runs).
		//fillBuffer(clBufferA.getBuffer(), 12345);
		//fillBuffer(clBufferB.getBuffer(), 67890);
		//size_bytes.getBuffer().put(0, s.length());
		//password.getBuffer().put(s.getBytes());
		System.out.println("S length: " + s.length());
		System.out.println("buf length: " + password_t.getBuffer().position() + "/" + password_t.getBuffer().capacity());
		password_t.getBuffer().putInt(s.length());
		System.out.println("buf length: " + password_t.getBuffer().position() + "/" + password_t.getBuffer().capacity());
		password_t.getBuffer().put(s.getBytes(), 4, 4);

		// get a reference to the kernel function with the name 'VectorAdd'
		// and map the buffers to its input parameters.
		CLKernel kernel = program.createCLKernel("do_md5s");
		System.out.println("Num args: " + kernel.numArgs);
		kernel.putArgs(password_t, password_hash_t);
		

		// asynchronous write of data to GPU device,
		// followed by blocking read to get the computed results back.
		long time = System.nanoTime();
		queue.putWriteBuffer(password_t, false)
		.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
		.putReadBuffer(password_hash_t, true);
		time = System.nanoTime() - time;

		// print first few elements of the resulting buffer to the console.
		System.out.println(s + " md5 hash: ");
		int outlength = password_hash_t.getBuffer().getInt(0);
		System.out.println(outlength);
		byte[] output = new byte[outlength];
		password_hash_t.getBuffer().position(4);
		password_hash_t.getBuffer().get(output, 4, output.length);
		System.out.println(byteArrayToHex(output));

		System.out.println("computation took: "+(time/1000000)+"ms");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			context.release();
		}
	}

	private static int roundUp(int groupSize, int globalSize) {
		int r = globalSize % groupSize;
		if (r == 0) {
			return globalSize;
		} else {
			return globalSize + groupSize - r;
		}
	}

	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}
}