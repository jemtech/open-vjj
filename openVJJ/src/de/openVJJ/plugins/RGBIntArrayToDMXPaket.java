/**
 * 
 */
package de.openVJJ.plugins;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.DMXPacketValue;
import de.openVJJ.values.RGBIntArrayValue;

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
public class RGBIntArrayToDMXPaket extends Plugin {

	public RGBIntArrayToDMXPaket() {
		addInput("RGB Array", RGBIntArrayValue.class);
		addOutput("DMX Paket", DMXPacketValue.class);
	}
	
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if(inpuName == "RGB Array"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					RGBIntArrayValue rgbArrayVal = (RGBIntArrayValue) value;
					mapRGBArray(rgbArrayVal.getValue());
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
	private int rMap = 0;
	private int gMap = 1;
	private int bMap = 2;
	private int chCount = 3;
	private int arrayOfset = 0;
	public static int MAX_DMX_LENGTH = 512;
	private void mapRGBArray(int[][] rgbArray){
		int pixels = MAX_DMX_LENGTH / chCount;
		if(rgbArray.length - arrayOfset < pixels){
			pixels = rgbArray.length - arrayOfset;
		}
		byte[] dmx = new byte[pixels * chCount];
		int p = 0;
		for(int i = arrayOfset; i < pixels + arrayOfset ; i++){
			int[] rgb = rgbArray[i];
			dmx[p + rMap] = (byte) rgb[0];
			dmx[p + gMap] = (byte) rgb[1];
			dmx[p + bMap] = (byte) rgb[2];
			p += chCount;
		}
		DMXPacketValue res = new DMXPacketValue(dmx);
		getConnection("DMX Paket").transmitValue(res);
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.PAGE_AXIS));
		
		JLabel label = new JLabel("Red channel");
		configPanel.add(label);
		JTextField rField = new JTextField(String.valueOf(rMap) , 3);
		configPanel.add(rField);
		label = new JLabel("Green channel");
		configPanel.add(label);
		JTextField gField = new JTextField(String.valueOf(gMap) , 3);
		configPanel.add(gField);
		label = new JLabel("Blue channel");
		configPanel.add(label);
		JTextField bField = new JTextField(String.valueOf(bMap) , 3);
		configPanel.add(bField);
		label = new JLabel("Channel count");
		configPanel.add(label);
		JTextField chField = new JTextField(String.valueOf(chCount) , 3);
		configPanel.add(chField);
		
		
		JButton setButton = new JButton();
		setButton.addActionListener(new ConfigSetButtonListener(rField, gField, bField, chField));
		setButton.setText("Set");
		configPanel.add(setButton);
		return configPanel;
	}
	
	private class ConfigSetButtonListener implements ActionListener{

		private JTextField rField;
		private JTextField gField;
		private JTextField bField;
		private JTextField chField;
		public ConfigSetButtonListener(JTextField rField, JTextField gField, JTextField bField, JTextField chField){
			this.rField = rField;
			this.gField = gField;
			this.bField = bField;
			this.chField = chField;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			rField.setEditable(false);
			gField.setEditable(false);
			bField.setEditable(false);
			chField.setEditable(false);
			
			Integer res;
			if( (res = checkField(rField)) != null){
				rMap = res;
			}
			if( (res = checkField(gField)) != null){
				gMap = res;
			}
			if( (res = checkField(bField)) != null){
				bMap = res;
			}
			if( (res = checkField(chField)) != null){
				chCount = res;
			}
			
			rField.setEditable(true);
			gField.setEditable(true);
			bField.setEditable(true);
			chField.setEditable(true);
		}
		
		private Integer checkField(JTextField field){
			Integer res = null;
			try {
				res = Integer.parseInt(field.getText());
			} catch (Exception e1) {
			}
			if(res != null && res < 512){
				field.setText(res.toString());
				field.setForeground(Color.BLACK);
				return res;
			}else{
				field.setForeground(Color.red);
				return null;
			}
		}
		
	}

}
