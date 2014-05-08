/**
 * 
 */
package de.openVJJ.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.values.StringValue;

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
public class StringInput extends Plugin {
	
	private String value;
	/**
	 * 
	 */
	public StringInput() {
		addOutput("String out", StringValue.class);
	}
	
	public void setString(String string){
		value = string;
		getConnection("String out").transmitValue(new StringValue(value));
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		getConnection("String out").transmitValue(new StringValue(value));
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel getConfigPannel() {
		JPanel configPanel = new JPanel();
		JTextField textValueField = new JTextField(value, 30);
		configPanel.add(textValueField);
		JButton setButton = new JButton();
		setButton.addActionListener(new ConfigSetButtonListener(textValueField));
		setButton.setText("Set");
		configPanel.add(setButton);
		return configPanel;
	}
	
	private class ConfigSetButtonListener implements ActionListener{

		private JTextField textValueField;
		public ConfigSetButtonListener(JTextField textValueField){
			this.textValueField = textValueField;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			value = textValueField.getText();
		}
		
	}

}
