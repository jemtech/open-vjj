package de.openVJJ.plugins;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.swing.JPanel;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

import de.openVJJ.GPUComponent;
import de.openVJJ.openGJTest;
import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.ProjectConf;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.CLFloatBufferValue;

public class SorbelCL extends Plugin {

	public SorbelCL(){
		addInput("CLFloat", CLFloatBufferValue.class);
		addOutput("Sorbel X", CLFloatBufferValue.class);
		addOutput("Sorbel Y", CLFloatBufferValue.class);
		MySyncedExequtor mySyncedExequtor = new MySyncedExequtor(null, 0, 0);
		GPUComponent.execute(mySyncedExequtor);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("CLFloat".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					CLFloatBufferValue bufferValue = (CLFloatBufferValue) value;
					Lock lock = bufferValue.lock();
					calculate(bufferValue);
					bufferValue.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}

	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private synchronized void calculate(CLFloatBufferValue bufferValue){
		try{
			MySyncedExequtor mySyncedExequtor = new MySyncedExequtor(bufferValue.getValue(), bufferValue.width, bufferValue.height);
			GPUComponent.execute(mySyncedExequtor);
			if(mySyncedExequtor.xCLBuffer == null || mySyncedExequtor.yCLBuffer == null){
				return;
			}
			getConnection("Sorbel X").transmitValue(new CLFloatBufferValue(mySyncedExequtor.xCLBuffer));
			getConnection("Sorbel Y").transmitValue(new CLFloatBufferValue(mySyncedExequtor.yCLBuffer));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	
	protected static class MySyncedExequtor extends GPUComponent.SyncedExequtor{
		public int width;
		public int height;
		public CLBuffer<FloatBuffer> inCLBuffer;
		public CLBuffer<FloatBuffer> xCLBuffer;
		public CLBuffer<FloatBuffer> yCLBuffer;
		
		protected MySyncedExequtor(CLBuffer<FloatBuffer> inCLBuffer, int width, int height){
			this.inCLBuffer = inCLBuffer;
			this.width = width;
			this.height = height;
		}

		/* (non-Javadoc)
		 * @see de.openVJJ.GPUComponent.SyncedExequtor#toExequte()
		 */
		@Override
		public void toExequte() {
			initGPU();
			if(inCLBuffer != null){
				calculateSorbelchannelGPU(inCLBuffer, width, height);
			}
		}

		private synchronized void calculateSorbelchannelGPU(CLBuffer<FloatBuffer> in,  int width, int height){
			synchronized (GPUComponent.getCLContext()) {
				CLKernel kernel = getKernel();
				CLBuffer<FloatBuffer> outx = GPUComponent.createFloatBuffer(width*height);
				CLBuffer<FloatBuffer> outy = GPUComponent.createFloatBuffer(width*height);
				try{
					System.out.println("use: " + in.ID);
					kernel.putArg(in);
					kernel.putArg(outx);
					kernel.putArg(outy);
					kernel.putArg(width);
					kernel.putArg(height);
					CLCommandQueue clCommandQueue = GPUComponent.getCLCommandQueue();
					synchronized (clCommandQueue) {
						clCommandQueue.putWriteBuffer(in, false);
						clCommandQueue.put2DRangeKernel(kernel, 0, 0, width, height, 0, 0);//auto calc by driver
						clCommandQueue.putReadBuffer(outx, true);
						clCommandQueue.putReadBuffer(outy, true);
						clCommandQueue.finish();
					}
					xCLBuffer = outx;
					yCLBuffer = outy;
				}catch(Exception e){
					if(outx != null){
						if(!outx.isReleased()){
							outx.release();
						}
					}
					if(outy != null){
						if(!outy.isReleased()){
							outy.release();
						}
					}
					System.err.println("Error while Sorbel: " + e.getMessage());
					outy = null;
					outx = null;
				}finally{
					kernel.release();
				}
			}
			
		}
		

		private CLKernel getKernel(){
			return program.createCLKernel("sorbel");
		}
		

		private static CLProgram program;
		private static boolean  gpuIniting = false;
		private static synchronized void initGPU(){
			if(gpuIniting || program != null){
				return;
			}
			gpuIniting = true;
			try {
				synchronized (GPUComponent.getCLContext()) {
					ProjectConf.getGPU();
					program = GPUComponent.getCLContext().createProgram(openGJTest.class.getResourceAsStream("kernelProgramms/sorbel")).build();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			gpuIniting = false;
		}
	}

}
