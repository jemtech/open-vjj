package de.openVJJ;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;

public class GPUComponent {


	static private CLContext context = null;
	static private CLDevice device;
	static private CLCommandQueue queue;
	static private boolean gpuReady = false;
	private static synchronized void initGPU(){
		if(gpuReady){
			return;
		}
		System.out.println("Start init gpu");
		if(context == null){
			context = CLContext.create();
		}
		device = context.getMaxFlopsDevice();
		System.out.println("Divice name: " + device.getName() + " device type: " + device.getType());
		queue = device.createCommandQueue();
		System.out.println("Finish init gpu");
		gpuReady = true;
		
	}
	
	private void shutdownGPU(){
		queue.finish();
		queue.release();
	}
	
	public synchronized CLContext getCLContext(){
		if(context == null){
			initGPU();
		}
		return context;
	}
	
	public synchronized CLDevice getCLDevice(){
		if(device == null){
			initGPU();
		}
		return device;
	}
	
	public synchronized CLCommandQueue getCLCommandQueue(){
		if(queue == null){
			initGPU();
		}
		return queue;
	}
	
	public int getLocalWorkSize(){
		return Math.min(getCLDevice().getMaxWorkGroupSize(), 256);
	}

	public int getLocalWorkSize2D(){
		return Math.min(getCLDevice().getMaxWorkGroupSize(), 16);
	}
	
	public int getGlobalWorkSize(int pixelCount){
		return roundUp(getLocalWorkSize(), pixelCount);
	}
	
	public int getGlobalWorkSizeX(int width){
		return roundUp(getLocalWorkSize(), width);
	}
	
	public int getGlobalWorkSizeY(int height){
		return roundUp(getLocalWorkSize(), height);
	}

	private static int roundUp(int groupSize, int globalSize){
		int r = globalSize % groupSize;
		if(r == 0){
			return globalSize;
		}else{
			return globalSize + groupSize - r;
		}
	}
	

	@Override
	protected void finalize() throws Throwable {
		shutdownGPU();
		super.finalize();
	}
}
