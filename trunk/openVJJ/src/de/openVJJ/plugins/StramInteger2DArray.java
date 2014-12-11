package de.openVJJ.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.zip.ZipOutputStream;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.Integer2DArrayValue;
import de.openVJJ.values.PointCloundList;

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
public class StramInteger2DArray extends Plugin {
	
	private String serverAdress = "localhost";
	private int port = 1234;
	private boolean compress = true;
	
	public StramInteger2DArray() {
		addInput("Integer2DArrays", Integer2DArrayValue.class);
	}
	
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Integer2DArrays".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					submit((Integer2DArrayValue) value);
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
		JPanel configPanel = new JPanel();

		configPanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();

		JLabel adressLabel = new JLabel("Adress");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		configPanel.add(adressLabel, gridBagConstraints);
		
		JTextField adressTextField = new JTextField(serverAdress);
		adressTextField.setColumns(20);
		adressTextField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				serverAdress =((JTextField)e.getSource()).getText();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		gridBagConstraints.gridx = 1;
		configPanel.add(adressTextField, gridBagConstraints);
		
		JLabel portLabel = new JLabel("Port");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy ++;
		configPanel.add(portLabel, gridBagConstraints);
		
		JFormattedTextField portTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
		portTextField.setValue(port);
		portTextField.setColumns(5);
		portTextField.addPropertyChangeListener("value",  new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				port = ((Number) ((JFormattedTextField)evt.getSource()).getValue()).intValue();
			}
		});
		
		gridBagConstraints.gridx = 1;
		configPanel.add(portTextField, gridBagConstraints);
		
		return configPanel;
	}
	
	public void submit(Integer2DArrayValue value){
		Socket socket = null;
		try {
			socket = new Socket(this.serverAdress, this.port);
			OutputStream outputStream = socket.getOutputStream();
			outputStream = new BufferedOutputStream(outputStream);
			if(compress){
				outputStream = new ZipOutputStream(outputStream);
			}
			ObjectOutputStream stream = new ObjectOutputStream(outputStream);
			
			int[][] intArray = value.getIngegerArray();
			int x = intArray.length;
			int y = intArray[0].length;
			stream.writeInt(x);
			stream.writeInt(y);
			for(int ix = 0; ix < x; ix++){
				for(int iy = 0; iy < y; iy++){
					stream.writeInt(intArray[x][y]);
				}
			}
			stream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
