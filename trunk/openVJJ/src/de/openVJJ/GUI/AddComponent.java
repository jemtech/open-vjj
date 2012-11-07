package de.openVJJ.GUI;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.openVJJ.InputComponents;
import de.openVJJ.RegisteredComponents;
import de.openVJJ.VJJComponent;
import de.openVJJ.GUI.ShowComponets.ShowComponetsListener;
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
		Class<VJJComponent> componet = selectedItem.getComponetClass();
		Object componetInstance = null;
		try {
			componetInstance = componet.newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		if(ImageListener.class.isInstance(componetInstance)){
			selectToAddTo((ImageListener) componetInstance);
		}else if(ImagePublisher.class.isInstance(componetInstance)){
			InputComponents.addComponent((ImagePublisher) componetInstance);
		}else{
			System.out.println("Unknown class :" + componet.getName());
		}
	}
	
	JFrame selectToAddToFrame = null;
	private void selectToAddTo(ImageListener imageListener){
		selectToAddToFrame = new JFrame();
		ShowComponets showComponets = new ShowComponets(ShowComponets.MODUS_DISABLE_NOT_PUBLISHERS);
		showComponets.addShowComponetsListener(new MyShowComponetsListener(imageListener));
		selectToAddToFrame.add(showComponets);
		selectToAddToFrame.setVisible(true);
		selectToAddToFrame.pack();
	}
	
	private class MyShowComponetsListener implements ShowComponetsListener{
		ImageListener imageListener;
		public MyShowComponetsListener(ImageListener imageListener){
			this.imageListener = imageListener;
		}
		@Override
		public void componentClicked(VJJComponent vjjComponent) {
			if(ImagePublisher.class.isInstance(vjjComponent)){
				InputComponents.addComponent(imageListener, (ImagePublisher) vjjComponent);
				selectToAddToFrame.dispose();
			}
		}
		
	}
	
	public class ComboBoxComponentItem{
		Class<VJJComponent> componet;
		public ComboBoxComponentItem(Class<VJJComponent> componet){
			this.componet = componet;
		}
		
		@Override
		public String toString() {
			return componet.getSimpleName();
		}
		
		public Class<VJJComponent> getComponetClass(){
			return componet;
		}
	}
}
