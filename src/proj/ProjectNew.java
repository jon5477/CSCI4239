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

public final class ProjectNew {
	private static final int MAX_CHARACTERS = 1; // 5 for now (95 possible input characters)

	public static void main(String[] args) {
		// start
		args = new String[2];
		args[0] = "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8";
		args[1] = "sha1";
		// end debug
		String hashstr = null;
		String algorithm = null;
		int outputHashSize;
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
				outputHashSize = 4; // 4 ints = 16 bytes
				break;
			}
			case "sha1": {
				codeFile = new File("sha1.cl");
				outputHashSize = 5; // 5 ints = 20 bytes
				if (hashstr.length() != 40) {
					System.out.println("Input string must be 40 characters.");
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
			for (int e = 1; e <= MAX_CHARACTERS; e++) { // Limit since O(95^x) is huge on running time
				int elementCount = (int) Math.pow(95, e); // Amount of hashes to process
				int localWorkSize = min(device.getMaxWorkGroupSize(), 256); // Local work size dimensions
				int globalWorkSize = roundUp(localWorkSize, elementCount); // rounded up to the nearest multiple of the localWorkSize
				System.out.println("device max workgroup size: " + device.getMaxWorkGroupSize());
				// load the opencl source code
				try (InputStream is = new FileInputStream(codeFile);) {
					CLProgram program = context.createProgram(is).build();
					// grab device memory
					System.out.println("finding hash: " + hashstr);
					CLBuffer<IntBuffer> hash = context.createIntBuffer(outputHashSize, READ_ONLY);
					IntBuffer ib = ByteBuffer.wrap(DatatypeConverter.parseHexBinary(hashstr)).asIntBuffer();
					int[] ints = new int[5];
					for (int i = 0; i < ints.length; i++) {
						ints[i] = ib.get(i);
					}
					IntBuffer hashBuf = hash.getBuffer();
					for (int i = 0; i < 5; i++) {
						hashBuf.put(i, ints[i]);
					}
					hashBuf.rewind();
					for (int i = 0; i < 5; i++) {
						System.out.print(hashBuf.get(i) + ", ");
					}
					hashBuf.rewind();
					System.out.println();
					System.out.println("hash (int form): " + Arrays.toString(ints));
					System.out.println("Count: " + elementCount * e);
					CLBuffer<ByteBuffer> input = context.createByteBuffer(elementCount * e, READ_ONLY); // each input string is length e
					out.println("generating random input strings of length " + e + " (this might be slow!)");
					generateRandomInputs(input.getBuffer(), e);
					out.println("random string generation completed.");
					CLBuffer<ByteBuffer> check = context.createByteBuffer(elementCount, WRITE_ONLY); // flags to determine if the hash matched or not
					//System.out.println("Global Work Size: " + globalWorkSize);
					//System.out.println("Allocated: " + globalWorkSize * 5);
					out.println("used device memory: " + (input.getCLSize() + check.getCLSize()) /1000000 + "MB");
					// fill input buffers with random numbers
					// (just to have test data; seed is fixed -> results will not change between runs).
					//fillBuffer(input.getBuffer(), hash, hash.length());
					// get a reference to the kernel function
					// and map the buffers to its input parameters.
					CLKernel kernel = program.createCLKernel("hash_SHA1");
					kernel.putArgs(hash, input).putArg(e).putArgs(check).putArg(elementCount);
					// asynchronous write of data to GPU device,
					// followed by blocking read to get the computed results back.
					long time = nanoTime();
					queue.putWriteBuffer(hash, false)
						 .putWriteBuffer(input, false)
						 .put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
						 .putReadBuffer(check, true);
					time = nanoTime() - time;
					// print first few elements of the resulting buffer to the console.
					/*out.println("results snapshot: ");
					for (int i = 0; i < 90; i++) {
						out.print(input.getBuffer().get() + ", ");
					}
					out.println("...; " + input.getBuffer().remaining() + " more");*/
					out.println("results snapshot: ");
					for (int i = 0; i < 90; i++) {
						out.print(check.getBuffer().get(i) + ", ");
					}
					out.println("...; " + check.getBuffer().remaining() + " more");
					int foundIndex = -1;
					for (int i = 0; i < elementCount; i++) {
						if (check.getBuffer().get(i) == 1) {
							// we found the hash
							foundIndex = i;
							System.out.println("Found solution!");
						}
					}
					out.println("computed " + elementCount + " hashes in " + (time / 1000000) + "ms");
					if (foundIndex != -1) {
						StringBuilder soln = new StringBuilder();
						for (int i = 0; i < e; i++) {
							soln.append(input.getBuffer().getChar(i));
						}
						out.println("found solution for hash, plaintext is: " + soln);
					}
					// free the allocated memory
					hash.release();
					input.release();
					check.release();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			context.release();
		}
	}

	private static void generateRandomInputs(ByteBuffer buffer, int e) {
		if (e != 1) {
			throw new Error(); // TODO for now
		}
		for (byte b = 32; b < 127; b++) {
			buffer.put(b);
		}
		buffer.rewind();
		// TODO Generate random input strings
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