package de.openVJJ.plugins;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.VectorValue;
import de.openVJJ.values.VectorValueList;
import de.openVJJ.values.VectorValueListList;

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
public class CombineVectors extends Plugin {

	public CombineVectors() {
		addInput("Vectors1", VectorValueListList.class);
		addInput("Vectors2", VectorValueListList.class);
		addOutput("Vectors", VectorValueList.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}
	
	private VectorValueListList vectors1value;
	private Lock vectors1valueLock;
	private VectorValueListList vectors2value;
	private Lock vectors2valueLock;
	
	private void setVectors1value(VectorValueListList vectorsValue){
		if(this.vectors1value != null && vectors1valueLock != null ){
			this.vectors1value.free(vectors1valueLock);
		}
		this.vectors1value = vectorsValue;
		vectors1valueLock = this.vectors1value.lock();
	}
	
	private void setVectors2value(VectorValueListList vectorsValue){
		if(this.vectors2value != null && vectors2valueLock != null ){
			this.vectors2value.free(vectors2valueLock);
		}
		this.vectors2value = vectorsValue;
		vectors2valueLock = this.vectors2value.lock();
	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Vectors1".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					setVectors1value((VectorValueListList) value);
					calculate();
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		if("Vectors2".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					setVectors2value((VectorValueListList) value);
					calculate();
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
	
	private void calculate(){
		if(vectors1value == null || vectors2value == null){
			return;
		}
		VectorValueListList my1ValueList = vectors1value;
		Lock my1Lock = my1ValueList.lock();
		vectors1value.free(vectors1valueLock);
		vectors1value = null;

		VectorValueListList my2ValueList = vectors2value;
		Lock my2Lock = my2ValueList.lock();
		vectors2value.free(vectors2valueLock);
		vectors2value = null;
		
		List<VectorValue> valueList = new LinkedList<VectorValue>();
		for(VectorValueList values : my1ValueList.getVectorValues()){
			valueList.addAll(values.getVectorValues());
		}
		for(VectorValueList values : my2ValueList.getVectorValues()){
			valueList.addAll(values.getVectorValues());
		}
		
		VectorValueList result = new VectorValueList(valueList);
		
		my1ValueList.free(my1Lock);
		my2ValueList.free(my2Lock);
		getConnection("Vectors").transmitValue(result);
	}

}
