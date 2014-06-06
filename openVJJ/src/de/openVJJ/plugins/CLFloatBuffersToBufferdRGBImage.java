package de.openVJJ.plugins;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
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
		if("G CLFloat".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					if(gBufferValue != null){
						gBufferValue.free(gLock);
					}
					gLock = value.lock();
					gBufferValue = (CLFloatBufferValue) value;
					channelValueSet();
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		if("B CLFloat".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					if(bBufferValue != null){
						bBufferValue.free(bLock);
					}
					bLock = value.lock();
					bBufferValue = (CLFloatBufferValue) value;
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
		int[] pixels = new int[rBufferValue.width * rBufferValue.height * 3];
		FloatBuffer rBuffer = rBufferValue.getValue().getBuffer();
		rBuffer.rewind();
		FloatBuffer gBuffer = gBufferValue.getValue().getBuffer();
		rBuffer.rewind();
		FloatBuffer bBuffer = bBufferValue.getValue().getBuffer();
		rBuffer.rewind();
		try{
			for(int i = 0; i < pixels.length; i += 3){
				pixels[i] = (int) rBuffer.get();
				pixels[i+1] = (int) gBuffer.get();
				pixels[i+2] = (int) bBuffer.get();
			}
			BufferedImage bufferedImage = new BufferedImage(rBufferValue.width, rBufferValue.height, BufferedImage.TYPE_INT_RGB);
			bufferedImage.getRaster().setPixels(0, 0, rBufferValue.width, rBufferValue.height, pixels);

			getConnection("RGB Image").transmitValue(new BufferedImageValue(bufferedImage));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			rBufferValue.free(rLock);
			rBufferValue = null;
			gBufferValue.free(gLock);
			gBufferValue = null;
			bBufferValue.free(bLock);
			bBufferValue = null;
		}
		
		
		
	}

}
