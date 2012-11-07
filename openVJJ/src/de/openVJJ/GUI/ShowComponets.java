package de.openVJJ.GUI;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import de.openVJJ.InputComponents;
import de.openVJJ.VJJComponent;
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


public class ShowComponets extends JPanel {
	private List<ShowComponetsListener> componetsListeners;
	public static final int MODUS_DISABLE_NOT_PUBLISHERS = 1;
	int modus = 0;
	public ShowComponets(){
		setLayout(new GridBagLayout());
		buidStructure();
	}
	
	public ShowComponets(int modus){
		this.modus = modus;
		setLayout(new GridBagLayout());
		buidStructure();
	}
	
	public void addShowComponetsListener(ShowComponetsListener componetsListener){
		if(componetsListeners == null){
			componetsListeners = new ArrayList<ShowComponets.ShowComponetsListener>();
		}
		componetsListeners.add(componetsListener);
	}
	
	private void buidStructure(){
		List<ImagePublisher> imagePublishers = InputComponents.getImagePublisher();
		if(imagePublishers == null){
			return;
		}
		synchronized (imagePublishers) {
			GridBagConstraints gridBagConstraints =  new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			int i =0;
			for(ImagePublisher imagePublisher : imagePublishers){
				gridBagConstraints.gridy =i;
				gridBagConstraints.gridx =0;
				JButton button = new JButton(imagePublisher.getClass().getSimpleName());
				button.addActionListener(new ButtonListener(imagePublisher));
				this.add(button, gridBagConstraints);
				gridBagConstraints.gridx =1;
				Component result = doPublisherRecursion(imagePublisher);
				if(result!= null){
						this.add(result,gridBagConstraints);
				}
				i++;
			}
		}
	}
	
	private Component doPublisherRecursion(ImagePublisher imagePublisher){
		List<ImageListener> imageListeners =  imagePublisher.getImageListenerList();
		if(imageListeners == null){
			return null;
		}
		if(imageListeners.size()<1){
			return null;
		}
		int i =0;
		synchronized (imageListeners) {
			
			JPanel componetPanel = new JPanel();
			componetPanel.setLayout(new GridBagLayout());
			GridBagConstraints gridBagConstraints =  new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			for(ImageListener imageListener : imageListeners){
				gridBagConstraints.gridy = i;
				gridBagConstraints.gridx = 0;
				JButton componetButton = new JButton(imageListener.getClass().getSimpleName());
				componetButton.addActionListener(new ButtonListener(imageListener));
				componetPanel.add(componetButton, gridBagConstraints);
				if(ImagePublisher.class.isInstance(imageListener)){
					Component component = doPublisherRecursion((ImagePublisher) imageListener);
					if(component != null){
						gridBagConstraints.gridx = 1;
						componetPanel.add(component, gridBagConstraints);
					}
				}else{
					if(modus == MODUS_DISABLE_NOT_PUBLISHERS){
						componetButton.setEnabled(false);
					}
				}

				i++;
			}
			return componetPanel;
		}
	}
	
	public interface ShowComponetsListener{
		public void componentClicked(VJJComponent vjjComponent);
	}
	
	private void componentClicked(VJJComponent vjjComponent){
		if(componetsListeners == null){
			return;
		}
		synchronized (componetsListeners) {
			for(ShowComponetsListener listener : componetsListeners){
				listener.componentClicked(vjjComponent);
			}
		}
	}
	
	private class ButtonListener implements ActionListener{
		VJJComponent componentButtonIsFor;
		public ButtonListener(VJJComponent vjjComponent){
			componentButtonIsFor = vjjComponent;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			componentClicked(componentButtonIsFor);
		}
		
	}
}
