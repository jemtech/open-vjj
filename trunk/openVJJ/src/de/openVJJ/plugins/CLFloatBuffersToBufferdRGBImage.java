package de.openVJJ.plugins;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.FloatBuffer;

import javax.swing.JPanel;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLMemory.Mem;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.ProjectConf;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.BufferedImageValue;
import de.openVJJ.values.CLFloatBufferValue;

public class CLFloatBuffersToBufferdRGBImage extends Plugin {

	public CLFloatBuffersToBufferdRGBImage(){
		addInput("R CLFloat", CLFloatBufferValue.class);
		addInput("G CLFloat", CLFloatBufferValue.class);
		addInput("B CLFloat", CLFloatBufferValue.class);
		addOutput("RGB Image", BufferedImageValue.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	private CLFloatBufferValue rBufferValue;
	private Lock rLock;
	private CLFloatBufferValue gBufferValue;
	private Lock gLock;
	private CLFloatBufferValue bBufferValue;
	private Lock bLock;
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("R CLFloat".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					if(rBufferValue != null){
						rBufferValue.free(rLock);
					}
					rLock = value.lock();
					rBufferValue = (CLFloatBufferValue) value;
					channelValueSet();
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
	
	private void channelValueSet(){
		if(rBufferValue == null || gBufferValue == null || bBufferValue == null){
			return;
		}
		
		
		rBufferValue.free(rLock);
		rBufferValue = null;
		gBufferValue.free(gLock);
		gBufferValue = null;
		bBufferValue.free(bLock);
		bBufferValue = null;
	}
	
	private void converte(BufferedImage image){
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		int globalWorkSize = ProjectConf.getGPU().getGlobalWorkSize(width * height);
		CLBuffer<FloatBuffer> rCLBuffer = ProjectConf.getGPU().getCLContext().createFloatBuffer(globalWorkSize, Mem.READ_WRITE);
		CLBuffer<FloatBuffer> gCLBuffer = ProjectConf.getGPU().getCLContext().createFloatBuffer(globalWorkSize, Mem.READ_WRITE);
		CLBuffer<FloatBuffer> bCLBuffer = ProjectConf.getGPU().getCLContext().createFloatBuffer(globalWorkSize, Mem.READ_WRITE);
		
		FloatBuffer rBuffer = rCLBuffer.getBuffer();
		FloatBuffer gBuffer = gCLBuffer.getBuffer();
		FloatBuffer bBuffer = bCLBuffer.getBuffer();
		rBuffer.rewind();
		gBuffer.rewind();
		bBuffer.rewind();
		
		boolean hasAlphaChannel = image.getAlphaRaster() != null;
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		if(!hasAlphaChannel){
			final int pixelLength = 3;
	        for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
	        	bBuffer.put((int) pixels[pixel] & 0xff); // blue
	        	gBuffer.put(((int) pixels[pixel + 1] & 0xff)); // green
	        	rBuffer.put(((int) pixels[pixel + 2] & 0xff)); // red
	        }
		}else{
			final int pixelLength = 4;
	        for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
	//          (((int) pixels[pixel] & 0xff)); // alpha
	        	bBuffer.put((int) pixels[pixel + 1] & 0xff); // blue
	        	gBuffer.put(((int) pixels[pixel + 2] & 0xff)); // green
	        	rBuffer.put(((int) pixels[pixel + 3] & 0xff)); // red
	        }
		}
		rBuffer.rewind();
		gBuffer.rewind();
		bBuffer.rewind();
		

		CLFloatBufferValue rValue = new CLFloatBufferValue(rCLBuffer);
		CLFloatBufferValue gValue = new CLFloatBufferValue(gCLBuffer);
		CLFloatBufferValue bValue = new CLFloatBufferValue(bCLBuffer);
		getConnection("R CLFloat").transmitValue(rValue);
		getConnection("G CLFloat").transmitValue(gValue);
		getConnection("B CLFloat").transmitValue(bValue);
	}

}
