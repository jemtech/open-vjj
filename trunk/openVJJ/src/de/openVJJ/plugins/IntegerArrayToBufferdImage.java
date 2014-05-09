package de.openVJJ.plugins;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.BufferedImageValue;
import de.openVJJ.values.IntegerArrayImageValue;

public class IntegerArrayToBufferdImage extends Plugin {

	public IntegerArrayToBufferdImage(){
		addInput("Frame", IntegerArrayImageValue.class);
		addOutput("Frame", BufferedImageValue.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if(inpuName == "Frame"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntegerArrayImageValue iAImageValue = (IntegerArrayImageValue) value;
					frameReceived(iAImageValue.getImageArray());
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
	
	private void frameReceived(int[][][] integerArrayImage){
		BufferedImage bufferedImage = new BufferedImage(integerArrayImage.length, integerArrayImage[0].length, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < integerArrayImage.length; x++ ){
			for(int y = 0; y < integerArrayImage[0].length; y++ ){
				bufferedImage.setRGB(x, y, new Color(integerArrayImage[x][y][0], integerArrayImage[x][y][1], integerArrayImage[x][y][2]).getRGB());
			}
		}
		BufferedImageValue imageValue = new BufferedImageValue(bufferedImage);
		getConnection("Frame").transmitValue(imageValue);
	}

	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
