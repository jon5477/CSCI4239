package proj;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;

public final class Project {
	public static void main(String[] args) throws NoSuchAlgorithmException {
//		MessageDigest md = MessageDigest.getInstance("SHA1");
//		byte[] b = md.digest("test".getBytes());
//		System.out.println(toHexString(b));
		CLContext context = CLContext.create();
		try {
			CLDevice device = context.getMaxFlopsDevice();
			CLCommandQueue queue = device.createCommandQueue();
			// load the opencl source code
			try (InputStream is = Project.class.getResourceAsStream("sha1.cl");) {
				CLProgram program = context.createProgram(is).build();
				// md5 size = 128 bits (16 bytes)
				// sha1 size = 160 bits (20 bytes)
				// bcrypt size = 184 bits (23 bytes)
				// grab device memory
				CLBuffer<ByteBuffer> input = context.createByteBuffer(20, Mem.READ_ONLY);
				CLBuffer<ByteBuffer> output = context.createByteBuffer(20, Mem.WRITE_ONLY);
				CLKernel kernel = program.createCLKernel("SHA1Hasher");
				kernel.putArgs(input, output);
				
				queue.putWriteBuffer(output, false).put1DRangeKernel(kernel, 0, arg2, arg3);
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
}