package de.openVJJ.GUI;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import de.openVJJ.InputComponents;
import de.openVJJ.RegisteredComponents;
import de.openVJJ.ImageListener.ImageListener;
import de.openVJJ.imagePublisher.ImagePublisher;

/*
 * Copyright (C) 2012  Jan-Erik Matthies
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
 */

public class AddComponent extends JPanel {
	public AddComponent(){
		createComponetSelector();
	}
	
	List<Class> componets;
	JComboBox comboBox;
	private void createComponetSelector(){
		comboBox = new JComboBox();
		comboBox.addItem("please choose");
		componets = RegisteredComponents.getRegisteredComponents();
		for(Class componet : componets){
			comboBox.addItem(new ComboBoxComponentItem(componet));
		}
		comboBox.setEditable(false);
		comboBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				Object selected = comboBox.getSelectedItem();
				if(ComboBoxComponentItem.class.isInstance(selected)){
					selected((ComboBoxComponentItem)selected);
				}else{
					System.out.println("Illegal selection = " + selected);
				}
			}
		});
		this.add(comboBox);
	}
	
	ComboBoxComponentItem selectedItem = null;
	private void selected(ComboBoxComponentItem boxComponentItem){
		if(boxComponentItem == selectedItem){
			return;
		}
		selectedItem = boxComponentItem;
		System.out.println(boxComponentItem);
		Class componet = selectedItem.getComponetClass();
		Object componetInstance = null;
		try {
			componetInstance = componet.newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		if(ImageListener.class.isInstance(componetInstance)){
			System.out.println("must find add to!");
		}else if(ImagePublisher.class.isInstance(componetInstance)){
			InputComponents.addComponent((ImagePublisher) componetInstance);
		}else{
			System.out.println("Unknown class :" + componet.getName());
		}
	}
	
	public class ComboBoxComponentItem{
		Class componet;
		public ComboBoxComponentItem(Class componet){
			this.componet = componet;
		}
		
		@Override
		public String toString() {
			return componet.getSimpleName();
		}
		
		public Class getComponetClass(){
			return componet;
		}
	}
}
