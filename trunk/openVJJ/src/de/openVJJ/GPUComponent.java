package de.openVJJ;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLMemory.Mem;

public class GPUComponent {


	static private CLContext context = null;
	static private CLDevice device;
	static private CLCommandQueue queue;
	static private boolean gpuReady = false;
	static private Thread gpuThread = null;
	private static void initGPU(){
		if(gpuReady){
			return;
		}
		System.out.println("Start init gpu");
		if(context == null){
			try{
				context = CLContext.create();
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
		device = context.getMaxFlopsDevice();
		System.out.println("Divice name: " + device.getName() + " device type: " + device.getType());
		queue = device.createCommandQueue();
		System.out.println("Finish init gpu");
		gpuReady = true;
	}
	
	public static void startGPU(){
		if(gpuThread != null){
			return;
		}
		GPUThread gpurunnable = new GPUThread();
		gpuThread = new Thread(gpurunnable);
		gpuThread.setName("GPU executor Thread");
		gpuThread.start();
	}
	
	private static void shutdownGPU(){
		queue.finish();
		queue.release();
	}
	
	public static CLContext getCLContext(){
		if(context == null){
			initGPU();
		}
		return context;
	}
	
	public static CLDevice getCLDevice(){
		if(device == null){
			initGPU();
		}
		return device;
	}
	
	public static CLCommandQueue getCLCommandQueue(){
		if(queue == null){
			initGPU();
		}
		return queue;
	}
	
	public static int getLocalWorkSize(){
		return Math.min(getCLDevice().getMaxWorkGroupSize(), 256);
	}

	public static int getLocalWorkSize2D(){
		return Math.min(getCLDevice().getMaxWorkGroupSize(), 16);
	}
	
	public static int getGlobalWorkSize(int pixelCount){
		return roundUp(getLocalWorkSize(), pixelCount);
	}
	
	public static int getGlobalWorkSizeX(int width){
		return roundUp(getLocalWorkSize(), width);
	}
	
	public static int getGlobalWorkSizeY(int height){
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
	
	public static synchronized CLBuffer<FloatBuffer> createFloatBuffer(int size){
		return getCLContext().createFloatBuffer(size, Mem.READ_WRITE);
	}
	

	@Override
	protected void finalize() throws Throwable {
		shutdownGPU();
		super.finalize();
	}
	
	public static void execute(SyncedExequtor exequtor){
		startGPU();
		Thread callingThread = Thread.currentThread();
		synchronized (todoList) {
			todoList.add(new Queelement(callingThread, exequtor));
		}
		synchronized (gpuThread) {
			gpuThread.notify();
		}
		try {
			synchronized (callingThread) {
				callingThread.wait();
			}
		} catch (InterruptedException e) {
			System.err.println("error while waiting for gpu execution");
			e.printStackTrace();
		}
	}
	
	private static class Queelement{
		private Thread callingThread;
		private SyncedExequtor todo;
		
		public Queelement(Thread callingThread, SyncedExequtor todo){
			this.callingThread = callingThread;
			this.todo = todo;
		}
	}
	
	public static abstract class SyncedExequtor{
		
		public abstract void toExequte();
	}
	
	private static List<GPUComponent.Queelement> todoList = new ArrayList<GPUComponent.Queelement>();
	
	private static class GPUThread implements Runnable{

		private boolean run = true;
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			initGPU();
			exeutor();
		}
		
		private void exeutor(){
			while(run){
				try{
					List<GPUComponent.Queelement> exequted = new ArrayList<GPUComponent.Queelement>();
					synchronized (todoList) {
						for(GPUComponent.Queelement toExequte : todoList){
							if(toExequte.todo != null){
								try{
									toExequte.todo.toExequte();
								}catch(Exception e){
									System.err.println("Error while running GPU process.");
									e.printStackTrace();
								}
							}else {
								System.out.println("todo ist null");
							}
							synchronized (toExequte.callingThread) {
								toExequte.callingThread.notify();
							}
							exequted.add(toExequte);
						}
					}
					for(GPUComponent.Queelement toRemove : exequted){
						todoList.remove(toRemove);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				try {
					synchronized (gpuThread) {
						gpuThread.wait();
					}
				} catch (InterruptedException e) {
					System.err.println("GPU exiqutor failed to wait");
					e.printStackTrace();
				}
			}
		}
		
	}
}
