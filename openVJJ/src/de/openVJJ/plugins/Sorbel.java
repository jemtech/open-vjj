/**
 * 
 */
package de.openVJJ.plugins;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.Integer2DArrayValue;
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
public class Sorbel extends Plugin {

	
	public Sorbel(){
		addInput("3DIntegerArray", IntegerArrayImageValue.class);
		addOutput("Sorbel X", Integer2DArrayValue.class);
		addOutput("Sorbel Y", Integer2DArrayValue.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("3DIntegerArray".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					IntegerArrayImageValue image = (IntegerArrayImageValue) value;
					Lock lock = image.lock();
					calculate(image);
					image.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}

	int colorChannel = 0; 
	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void calculate(IntegerArrayImageValue image){
		int[][][] imageChannel = image.getImageArray();
		int width = imageChannel.length;
		int height = imageChannel[0].length;
		
		int[][] resultx = new int[width][height];
		int[][] resulty = new int[width][height];
		for(int x = 1; x < width-1; x++ ){
			for(int y = 1; y < height-1; y++){
				int xRes = imageChannel[x-1][y-1][colorChannel] * -3;
				xRes += imageChannel[x-1][y][colorChannel] *-10;
				xRes += imageChannel[x-1][y+1][colorChannel] *-3;
				xRes += imageChannel[x+1][y-1][colorChannel] *3;
				xRes += imageChannel[x+1][y][colorChannel] *10;
				xRes += imageChannel[x+1][y+1][colorChannel] *3;
				resultx[x][y] =  Math.abs(xRes);
				

				int yRes = imageChannel[x-1][y-1][colorChannel] *3;
				yRes += imageChannel[x][y-1][colorChannel] *10;
				yRes += imageChannel[x+1][y-1][colorChannel] *3;
				yRes += imageChannel[x-1][y+1][colorChannel] *-3;
				yRes += imageChannel[x][y+1][colorChannel] *-10;
				yRes += imageChannel[x+1][y+1][colorChannel] *-3;
				resulty[x][y] =  Math.abs(yRes);
			}
		}
		Integer2DArrayValue xRes = new Integer2DArrayValue(resultx);
		Integer2DArrayValue yRes = new Integer2DArrayValue(resulty);
		getConnection("Sorbel Y").transmitValue(xRes);
		getConnection("Sorbel X").transmitValue(yRes);
		
	}

}
