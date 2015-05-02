package hw;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

public final class Assignment10 {
	private static final String plaintext = "plaintexttotestsha1hashing";

	public static void main(String[] args) {
		// Create the OpenCL context
		CLContext context = CLContext.create();
		try {
			// select fastest device
			CLDevice device = context.getMaxFlopsDevice();
			System.out.println("using " + device);
			// create command queue on device
			CLCommandQueue queue = device.createCommandQueue();
			int elementCount = 5000; // Amount of hashes to process
			int localWorkSize = min(device.getMaxWorkGroupSize(), 256); // Local work size dimensions
			int globalWorkSize = roundUp(localWorkSize, elementCount); // rounded up to the nearest multiple of the localWorkSize
			System.out.println("device max workgroup size: " + device.getMaxWorkGroupSize());
			// load the opencl source code
			try (InputStream is = new FileInputStream(new File("sha1.cl"));) {
				CLProgram program = context.createProgram(is).build();
				// grab device memory
				CLBuffer<ByteBuffer> input = context.createByteBuffer(elementCount * 5, READ_ONLY);
				CLBuffer<IntBuffer> output = context.createIntBuffer(globalWorkSize * 5, WRITE_ONLY);
				System.out.println("Global Work Size: " + globalWorkSize);
				out.println("used device memory: " + (input.getCLSize() + output.getCLSize()) / 1000000 + " MB");
				// fill input buffers with random numbers
				// (just to have test data; seed is fixed -> results will not change between runs).
				fillBuffer(input.getBuffer(), plaintext, plaintext.length());
				// get a reference to the kernel function
				// and map the buffers to its input parameters.
				CLKernel kernel = program.createCLKernel("hash_SHA1");
				kernel.putArgs(input).putArg(plaintext.length()).putArgs(output).putArg(elementCount);
				// asynchronous write of data to GPU device,
				// followed by blocking read to get the computed results back.
				System.out.println("hashing \"" + plaintext + "\" with " + elementCount + " iterations on GPU");
				long time = nanoTime();
				queue.putWriteBuffer(input, false)
					 .put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
					 .putReadBuffer(output, true);
				time = nanoTime() - time;
				// print first few elements of the resulting buffer to the console.
				out.println("results snapshot: ");
				for (int i = 0; i < 5; i++) {
					out.print(output.getBuffer().get() + ", ");
				}
				out.println("...; " + output.getBuffer().remaining() + " more");
				int[] stuff = new int[5];
				for (int i = 0; i < stuff.length; i++) {
					stuff[i] = output.getBuffer().get(i);
				}
				System.out.println("hash output: " + Arrays.toString(stuff));
				System.out.print("hash output (hex): ");
				System.out.print(Integer.toHexString(stuff[0]));
				System.out.print(Integer.toHexString(stuff[1]));
				System.out.print(Integer.toHexString(stuff[2]));
				System.out.print(Integer.toHexString(stuff[3]));
				System.out.print(Integer.toHexString(stuff[4]));
				System.out.println();
				out.println("computation took: " + (time/1000000) + " ms on the GPU");
				// free the allocated memory
				input.release();
				output.release();
			}
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			System.out.println("hashing \"" + plaintext + "\" with " + elementCount + " iterations on CPU");
			long time = nanoTime();
			for (int i = 0; i < elementCount; i++) {
				sha1.update(plaintext.getBytes());
				sha1.digest();
			}
			time = nanoTime() - time;
			out.println("computation took: " + (time/1000000) + " ms on the CPU");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			context.release();
		}
	}

	public static String toHexString(byte[] buf) {
		StringBuffer sb = new StringBuffer();
		for (byte b : buf) {
			sb.append(String.format("%x", b));
		}
		return sb.toString();
	}

	private static int roundUp(int groupSize, int globalSize) {
		int r = globalSize % groupSize;
		if (r == 0) {
			return globalSize;
		}
		return globalSize + groupSize - r;
	}

	private static void fillBuffer(ByteBuffer buffer, String hash, int size) {
		for (int i = 0; i < size; i++) {
			buffer.put(i, (byte) hash.charAt(i));
		}
		buffer.rewind();
	}
}