/**
 * 
 */
package de.openVJJ.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.DoubleValue;
import de.openVJJ.values.IntArrayValue;
import de.openVJJ.values.IntValue;

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
public class SinIntArrayGen extends Plugin {
	private double phase;
	private int min;
	private int max;
	private int length;
	
	public SinIntArrayGen(){
		addInput("phase", DoubleValue.class);
		addInput("min", IntValue.class);
		addInput("max", IntValue.class);
		addInput("length", IntValue.class);
		
		addOutput("curve", IntArrayValue.class);
	}
	
	private int[] calculate(){
		int[] res = new int[length];
		double range = (max - min) /2;
		for(int i = 0; i < length; i++){
			res[i] = (int) (min + ((Math.sin(phase + ((Math.PI * i) / length) ) + 1) * range));
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		getConnection("curve").transmitValue(new IntArrayValue(calculate()));
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if(inpuName == "phase"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					DoubleValue val = (DoubleValue) value;
					phase = val.getValue();
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}else if(inpuName == "min"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntValue val = (IntValue) value;
					min = val.getValue();
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}else if(inpuName == "max"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntValue val = (IntValue) value;
					max = val.getValue();
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}else if(inpuName == "length"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntValue val = (IntValue) value;
					length = val.getValue();
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

	private boolean trigerToPhase = false;
	private boolean trigerToMin = false;
	private boolean trigerToMax = false;
	private boolean trigerToLength = false;
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		JPanel configPanel = new JPanel();
		JCheckBox trigerToPhaseCheckBox = new JCheckBox("Triger to Phase");
		trigerToPhaseCheckBox.setSelected(trigerToPhase);
		trigerToPhaseCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				trigerToPhase = ((JCheckBox)arg0.getSource()).isSelected();
			}
		});
		configPanel.add(trigerToPhaseCheckBox);
		
		JCheckBox trigerToMinCheckBox = new JCheckBox("Triger to min");
		trigerToMinCheckBox.setSelected(trigerToMin);
		trigerToMinCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				trigerToMin = ((JCheckBox)arg0.getSource()).isSelected();
			}
		});
		configPanel.add(trigerToMinCheckBox);
		
		JCheckBox trigerToMaxCheckBox = new JCheckBox("Triger to max");
		trigerToMaxCheckBox.setSelected(trigerToMax);
		trigerToMaxCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				trigerToMax = ((JCheckBox)arg0.getSource()).isSelected();
			}
		});
		configPanel.add(trigerToMaxCheckBox);
		
		JCheckBox trigerToLengthCheckBox = new JCheckBox("Triger to Length");
		trigerToLengthCheckBox.setSelected(trigerToLength);
		trigerToLengthCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				trigerToLength = ((JCheckBox)arg0.getSource()).isSelected();
			}
		});
		configPanel.add(trigerToLengthCheckBox);
		return configPanel;
	}

}
