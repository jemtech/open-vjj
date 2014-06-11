package de.openVJJ.plugins;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.FloatBuffer;

import javax.swing.JPanel;

import com.jogamp.opencl.CLBuffer;

import de.openVJJ.GPUComponent;
import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.BufferedImageValue;
import de.openVJJ.values.CLFloatBufferValue;

public class BufferdRGBImageToCLFloatBuffer extends Plugin {

	public BufferdRGBImageToCLFloatBuffer(){
		addInput("RGB Image", BufferedImageValue.class);
		addOutput("R CLFloat", CLFloatBufferValue.class);
		addOutput("G CLFloat", CLFloatBufferValue.class);
		addOutput("B CLFloat", CLFloatBufferValue.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("RGB Image".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					BufferedImageValue bufferedImageValue = (BufferedImageValue) value;
					converte(bufferedImageValue.getImage());
					value.free(lock);
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
	
	private void converte(BufferedImage image){
		
		int width = image.getWidth();
		int height = image.getHeight();
		MySyncedExequtor mySyncedExequtor = new MySyncedExequtor(width * height);
		GPUComponent.execute(mySyncedExequtor);
		CLBuffer<FloatBuffer> rCLBuffer = mySyncedExequtor.rCLBuffer;
		CLBuffer<FloatBuffer> gCLBuffer = mySyncedExequtor.gCLBuffer;
		CLBuffer<FloatBuffer> bCLBuffer = mySyncedExequtor.bCLBuffer;
		
		
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
		rValue.width = width;
		rValue.height = height;
		CLFloatBufferValue gValue = new CLFloatBufferValue(gCLBuffer);
		gValue.width = width;
		gValue.height = height;
		CLFloatBufferValue bValue = new CLFloatBufferValue(bCLBuffer);
		bValue.width = width;
		bValue.height = height;
		getConnection("R CLFloat").transmitValue(rValue);
		getConnection("G CLFloat").transmitValue(gValue);
		getConnection("B CLFloat").transmitValue(bValue);
	}
	
	protected class MySyncedExequtor extends GPUComponent.SyncedExequtor{
		
		private int pixelCount;
		public CLBuffer<FloatBuffer> rCLBuffer;
		public CLBuffer<FloatBuffer> gCLBuffer;
		public CLBuffer<FloatBuffer> bCLBuffer;
		
		protected MySyncedExequtor(int pixelCount){
			this.pixelCount = pixelCount;
		}

		/* (non-Javadoc)
		 * @see de.openVJJ.GPUComponent.SyncedExequtor#toExequte()
		 */
		@Override
		public void toExequte() {
			rCLBuffer = GPUComponent.createFloatBuffer(pixelCount);
			gCLBuffer = GPUComponent.createFloatBuffer(pixelCount);
			bCLBuffer = GPUComponent.createFloatBuffer(pixelCount);
		}
		
	}

}
