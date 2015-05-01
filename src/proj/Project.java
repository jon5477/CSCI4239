package proj;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;

public final class Project {
	public static void main(String[] args) throws NoSuchAlgorithmException {
		// start
		args = new String[2];
		args[0] = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
		args[1] = "sha1";
		// end debug
		String hash = null;
		String algorithm = null;
		int hashSize;
		if (args.length >= 2) {
			hash = args[0];
			algorithm = args[1];
		} else {
			System.out.println("Not enough arguments.");
			System.out.println("Usage: [hash] [algorithm]");
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
			//int elementCount = 32; // Processing 32 hashes at a time (this can be changed, please be a power of 2)
			//int workGroupSize = Math.min(device.getMaxWorkGroupSize(), 256) / elementCount; // Max number of threads
			System.out.println("device max workgroup size: " + device.getMaxWorkGroupSize());
			// load the opencl source code
			try (InputStream is = new FileInputStream(codeFile);) {
				CLProgram program = context.createProgram(is).build();
				// grab device memory
				//CLBuffer<ByteBuffer> input = context.createByteBuffer(workGroupSize * 32 * hashSize, Mem.READ_ONLY);
				//CLBuffer<ByteBuffer> output = context.createByteBuffer(workGroupSize * 32 * hashSize, Mem.WRITE_ONLY);
				CLBuffer<ByteBuffer> input = context.createByteBuffer(20, Mem.READ_ONLY);
				CLBuffer<ByteBuffer> output = context.createByteBuffer(20, Mem.WRITE_ONLY);
				input.getBuffer().put(0, (byte) 7);
				System.out.println("used device memory: " + (input.getCLSize() + output.getCLSize()));
				CLKernel kernel = program.createCLKernel("hashSHA1");
				kernel.putArgs(input, output);
				queue.putWriteBuffer(input, false).put1DRangeKernel(kernel, 0, 1, 1).putReadBuffer(output, false);
				//for(int i = 0; i < 10; i++)
				//	System.out.print(output.getBuffer().get() + ", ");
				//System.out.println("...; " + output.getBuffer().remaining() + " more");
				byte[] out = new byte[20];
				for (int i = 0; i < 20; i++) {
					out[i] = output.getBuffer().get(i);
				}
				System.out.println(DatatypeConverter.printHexBinary(out));
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
}