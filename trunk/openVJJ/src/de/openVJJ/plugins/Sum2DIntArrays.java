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
import de.openVJJ.values.CLFloatBufferValue;
import de.openVJJ.values.Integer2DArrayValue;

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
public class Sum2DIntArrays extends Plugin {

	public Sum2DIntArrays(){
		addInput("summand 1", Integer2DArrayValue.class);
		addInput("summand 2", Integer2DArrayValue.class);
		addInput("summand 3", Integer2DArrayValue.class);
		addOutput("sum", Integer2DArrayValue.class);
	}
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	private Integer2DArrayValue array1;
	private Lock lock1;
	private Integer2DArrayValue array2;
	private Lock lock2;
	private Integer2DArrayValue array3;
	private Lock lock3;
	
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {

		if("summand 1".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					if(array1 != null){
						array1.free(lock1);
					}
					lock1 = value.lock();
					array1 = (Integer2DArrayValue) value;
					channelValueSet();
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		if("summand 2".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					if(array2 != null){
						array2.free(lock2);
					}
					lock2 = value.lock();
					array2 = (Integer2DArrayValue) value;
					channelValueSet();
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		if("summand 3".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					if(array3 != null){
						array3.free(lock3);
					}
					lock3 = value.lock();
					array3 = (Integer2DArrayValue) value;
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

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private synchronized void channelValueSet(){
		if(array1 == null || array2 == null || array3 == null){
			return;
		}
		if(array1.getIngegerArray() == null || array2.getIngegerArray() == null || array3.getIngegerArray() == null ){
			array1.free(lock1);
			array1 = null;
			array2.free(lock2);
			array2 = null;
			array3.free(lock3);
			array3 = null;
			return;
		}
		
		int[][] sum1 = array1.getIngegerArray();
		int[][] sum2 = array2.getIngegerArray();
		int[][] sum3 = array3.getIngegerArray();
		int xMax = sum1.length;
		int yMax = sum1[0].length;
		int[][] sumRes = new int[xMax][yMax];
		for(int x = 0; x < xMax; x++){
			for(int y = 0; y < yMax; y++){
				sumRes[x][y] = sum1[x][y] + sum2[x][y] + sum3[x][y];
			}
		}
		array1.free(lock1);
		array1 = null;
		array2.free(lock2);
		array2 = null;
		array3.free(lock3);
		array3 = null;
		getConnection("sum").transmitValue(new Integer2DArrayValue(sumRes));
	}

}
