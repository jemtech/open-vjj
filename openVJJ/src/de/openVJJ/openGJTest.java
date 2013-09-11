package de.openVJJ;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLMemory.Mem;


public class openGJTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		doCLTest();
//		doGLTest();
	}
	
	public static void doCLTest(){
		CLContext context = CLContext.create();
		CLDevice device = context.getMaxFlopsDevice();
		CLCommandQueue queue = device.createCommandQueue();
		
		int elementCount = 1444477;
		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 256);
		int globalWorkSize = roundUp(localWorkSize, elementCount);
		
		try {
			CLProgram program = context.createProgram(openGJTest.class.getResourceAsStream("kernelProgramms/VectorAdd")).build();
			CLBuffer<FloatBuffer> clBufferA = context.createFloatBuffer(globalWorkSize, Mem.READ_ONLY);
			CLBuffer<FloatBuffer> clBufferB = context.createFloatBuffer(globalWorkSize, Mem.READ_ONLY);
			CLBuffer<FloatBuffer> clBufferC = context.createFloatBuffer(globalWorkSize, Mem.WRITE_ONLY);
			
			fillBufferRandom(clBufferA.getBuffer(), 3245);
			fillBufferRandom(clBufferB.getBuffer(), 28349);
			
			CLKernel kernel = program.createCLKernel("VectorAdd");
			kernel.putArgs(clBufferA, clBufferB, clBufferC).putArg(elementCount);
			queue.putWriteBuffer(clBufferA, false).putWriteBuffer(clBufferB, false).put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize).putReadBuffer(clBufferC, true);
			
			for(int i = 0; i<10; i++){
				System.out.println(clBufferC.getBuffer().get() + ", ");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			context.release();
		}
		
		
	}
	
	public static void doGLTest(){
		System.out.println("start glTest");
		openGJTest openGJTest = new openGJTest();
		openGJTest.runTest();
		System.out.println("end glTest");
	}
	
	private JFrame createFrame(){
		JFrame frame = new JFrame();
		frame.setTitle("OpenGlTest");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setBounds(0, 0, 800, 600);
		return frame;
	}
	
	private void runTest(){
		JFrame jFrame = createFrame();
		GLCapabilities glCapabilities = getCapabilities();
		MyJoglCanvas joglCanvas = new MyJoglCanvas(600, 400, glCapabilities);
		jFrame.add(joglCanvas);
		jFrame.repaint();
		//jFrame.dispose();
		//jFrame.setVisible(false);
	}
	public GLCapabilities getCapabilities(){
		GLProfile glProfile = GLProfile.getDefault();
		GLCapabilities capabilities = new GLCapabilities(glProfile);
	    capabilities.setRedBits(16);
	    capabilities.setBlueBits(16);
	    capabilities.setGreenBits(16);
	    capabilities.setAlphaBits(16);
	    return capabilities;
	}
	
	public class MyJoglCanvas extends GLCanvas implements GLEventListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MyJoglCanvas(int width, int height, GLCapabilities capabilities) {
	        super(capabilities);
	        setSize(width, height);
	    }
	    public void init(GLAutoDrawable drawable) {}
	    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
	    
		@Override
		public void dispose(GLAutoDrawable arg0) {
			disposeGLEventListener(this, true);
		}
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	        GL gl = drawable.getGL();
	        gl.glViewport(0, 0, width, height);
	    }
		
		public void display(GLAutoDrawable drawable) {
	        GL gl = drawable.getGL();
	        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	    }
	}
	
	
	private static int roundUp(int groupSize, int globalSize){
		int r = globalSize % groupSize;
		if(r == 0){
			return globalSize;
		}else{
			return globalSize + groupSize - r;
		}
	}
	
	private static void fillBufferRandom(FloatBuffer floatBuffer, int seed){
		Random random = new Random(seed);
		while(floatBuffer.remaining() != 0){
			floatBuffer.put(random.nextFloat() * 100);
		}
		floatBuffer.rewind();
	}

}
