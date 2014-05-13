/**
 * 
 */
package de.openVJJ.plugins;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Plugin;
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
public class IntInput extends Plugin {
	
	Integer integer;
	
	/**
	 * 
	 */
	public IntInput() {
		addOutput("Int", IntValue.class);
	}
	
	/**
	 * @param integer the integer to set
	 */
	public void setInteger(Integer integer) {
		this.integer = integer;
		getConnection("Int").transmitValue(new IntValue(integer));
	}
	
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		getConnection("Int").transmitValue(new IntValue(integer));
		
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

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		JPanel configPanel = new JPanel();
		JTextField textValueField = new JTextField("", 30);
		if(integer != null){
			textValueField.setText(integer.toString());
		}
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

			Integer enteredInt = null;
			textValueField.setEditable(false);
			try {
				enteredInt = Integer.parseInt(textValueField.getText());
			} catch (Exception e1) {
			}
			if(enteredInt != null){
				setInteger(enteredInt);
				textValueField.setText(enteredInt.toString());
				textValueField.setForeground(Color.BLACK);
			}else{
				textValueField.setForeground(Color.red);
			}
			textValueField.setEditable(true);
		}
		
	}

}
