/**
 * 
 */
package de.openVJJ.plugins;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.ArtNetPacketValue;
import de.openVJJ.values.IntArrayValue;
import de.openVJJ.values.RGBIntArrayValue;
import de.openVJJ.values.RGBIntColorValue;

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
public class IntArrayToRGBIntArray extends Plugin {

	public IntArrayToRGBIntArray(){
		addInput("Color", RGBIntColorValue.class);
		addInput("Int Array", IntArrayValue.class);
		
		addOutput("Colored Array", RGBIntArrayValue.class);
	}
	
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	int[] color;
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if(inpuName == "Color"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					RGBIntColorValue colorVal = (RGBIntColorValue) value;
					color = colorVal.getValue();
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}else if(inpuName == "Int Array"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntArrayValue intArrayValue = (IntArrayValue) value;
					calculate(intArrayValue.getValue(), color);
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
	
	private void calculate(int[] array, int[] color){
		int[][] result = new int[array.length][color.length];
		for(int i = 0; i < array.length; i++ ){
			for(int c = 0; c < color.length; c++ ){
				result[i][c] = color[c] * array[i] / 255;
			}
		}
		getConnection("Colored Array").transmitValue(new RGBIntArrayValue(result));
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
