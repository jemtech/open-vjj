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

/**
 * 
 * Copyright (C) 2014 Jan-Erik Matthies
 * 
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Jan-Erik Matthies
 * 
 */
public class BufferdImageToIntegerArray extends Plugin {

	public BufferdImageToIntegerArray(){
		addInput("Frame", BufferedImageValue.class);
		addOutput("Frame", IntegerArrayImageValue.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Frame".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					BufferedImageValue bImageValue = (BufferedImageValue) value;
					frameReceived(bImageValue.getImage());
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
	
	private void frameReceived(BufferedImage bufferedImage){
		int[][][] rgbImageArray = new int[bufferedImage.getWidth()][bufferedImage.getHeight()][3];
		for(int x = 0; x < rgbImageArray.length; x++ ){
			for(int y = 0; y < rgbImageArray[0].length; y++ ){
				Color c = new Color(bufferedImage.getRGB(x, y));
				rgbImageArray[x][y][0] = c.getRed();
				rgbImageArray[x][y][1] = c.getGreen();
				rgbImageArray[x][y][2] = c.getBlue();
			}
		}
		IntegerArrayImageValue imageValue = new IntegerArrayImageValue(rgbImageArray);
		getConnection("Frame").transmitValue(imageValue);
	}

	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
