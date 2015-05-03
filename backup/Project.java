package proj;

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
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

public final class Project {
	public static void main(String[] args) {
		// start
		args = new String[2];
		args[0] = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3";
		args[1] = "sha1";
		// end debug
		String hashstr = null;
		String algorithm = null;
		int hashSize;
		if (args.length >= 2) {
			hashstr = args[0];
			algorithm = args[1];
		} else {
			System.out.println("Not enough arguments.");
			System.out.println("Usage: [string to hash] [algorithm]");
			return;
		}
		File codeFile;
		switch (algorithm) {
			case "md5": {
				codeFile = new File("md5.cl");
				hashSize = 16;
				break;
			}
			case "sha1": {
				codeFile = new File("sha1.cl");
				hashSize = 20;
				if (hashstr.length() != 40) {
					System.out.println("Input hash must be 40 characters.");
					return;
				}
				break;
			}
			default: {
				System.out.println("Unsupported Hashing Algorithm. Valid Options: md5, sha1");
				System.exit(1);
				return;
			}
		}
		if (!codeFile.exists()) {
			System.out.println("OpenCL code file does not exist!");
			System.exit(1);
			return;
		}
		// Create the OpenCL context
		CLContext context = CLContext.create();
		try {
			// select fastest device
			CLDevice device = context.getMaxFlopsDevice();
			System.out.println("using " + device);
			// create command queue on device
			CLCommandQueue queue = device.createCommandQueue();
			// md5 size = 128 bits (16 bytes)
			// sha1 size = 160 bits (20 bytes)
			// bcrypt size = 184 bits (23 bytes)
			int elementCount = 5; // Amount of hashes to process
			int localWorkSize = min(device.getMaxWorkGroupSize(), 256); // Local work size dimensions
			int globalWorkSize = roundUp(localWorkSize, elementCount); // rounded up to the nearest multiple of the localWorkSize
			System.out.println("device max workgroup size: " + device.getMaxWorkGroupSize());
			// load the opencl source code
			try (InputStream is = new FileInputStream(codeFile);) {
				CLProgram program = context.createProgram(is).build();
				// grab device memory
				CLBuffer<IntBuffer> hash = context.createIntBuffer(5, READ_ONLY);
				CLBuffer<ByteBuffer> input = context.createByteBuffer(elementCount * 55, READ_ONLY); // each string can go up to 55 characters long
				CLBuffer<IntBuffer> output = context.createIntBuffer(globalWorkSize * 5, WRITE_ONLY); // each hash is returned as 5 * 4 bytes (160 bits)
				CLBuffer<ByteBuffer> matched = context.createByteBuffer(elementCount, WRITE_ONLY); // 0 if hash didn't match, 1 if hash matched
				//System.out.println("Global Work Size: " + globalWorkSize);
				//System.out.println("Allocated: " + globalWorkSize * 5);
				out.println("used device memory: " + (input.getCLSize() + output.getCLSize()) / 1000000 + "MB");
				byte[] b = DatatypeConverter.parseHexBinary(hashstr);
				ByteBuffer bb = ByteBuffer.wrap(b);
				IntBuffer ib = bb.asIntBuffer();
				int[] ints = new int[5];
				for (int i = 0; i < ints.length; i++) {
					ints[i] = ib.get(i);
				}
				System.out.println(Arrays.toString(ints));
				if (true) {
					return;
				}
				// fill buffer with randomly generated values
				//fillBuffer(hash.getBuffer(), hashstr, hashstr.length());
				// get a reference to the kernel function
				// and map the buffers to its input parameters.
				CLKernel kernel = program.createCLKernel("hash_SHA1");
				kernel.putArgs(hash, input).putArg(hashstr.length()).putArgs(output, matched).putArg(elementCount);
				// asynchronous write of data to GPU device,
				// followed by blocking read to get the computed results back.
				long time = nanoTime();
				queue.putWriteBuffer(input, false)
					 .put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
					 .putReadBuffer(output, true);
				time = nanoTime() - time;
				// print first few elements of the resulting buffer to the console.
				out.println("results snapshot: ");
				for (int i = 0; i < 50; i++) {
					out.print(output.getBuffer().get() + ", ");
				}
				out.println("...; " + output.getBuffer().remaining() + " more");
				int[] stuff = new int[5];
				for (int i = 0; i < stuff.length; i++) {
					stuff[i] = output.getBuffer().get(i);
				}
				System.out.println(Arrays.toString(stuff));
				System.out.print(Integer.toHexString(stuff[0]));
				System.out.print(Integer.toHexString(stuff[1]));
				System.out.print(Integer.toHexString(stuff[2]));
				System.out.print(Integer.toHexString(stuff[3]));
				System.out.print(Integer.toHexString(stuff[4]));
				System.out.println();
				out.println("computation took: " + (time/1000000) + "ms");
				// free the allocated memory
				input.release();
				output.release();
			}
		} catch (IOException e) {
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