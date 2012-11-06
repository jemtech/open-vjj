package de.openVJJ.GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import de.openVJJ.InputComponents;
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
	//private JTable componentsTable;
	public ShowComponets(){
		setLayout(new GridBagLayout());
		buidStructure();
	}
	
	private void buidStructure(){
		List<ImagePublisher> imagePublishers = InputComponents.getImagePublisher();
		if(imagePublishers == null){
			return;
		}
		synchronized (imagePublishers) {
			GridBagConstraints gridBagConstraints =  new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridx =0;
			int i =0;
			for(ImagePublisher imagePublisher : imagePublishers){
				gridBagConstraints.gridy =i;
				JButton button = new JButton(imagePublisher.getClass().getSimpleName()) ;
				this.add(button, gridBagConstraints);
				//System.out.println(imagePublisher.getClass().getSimpleName());
				doPublisherRecursion(imagePublisher);
				i++;
			}
		}
	}
	
	private void doPublisherRecursion(ImagePublisher imagePublisher){
		List<ImageListener> imageListeners =  imagePublisher.getImageListenerList();
		synchronized (imageListeners) {
			for(ImageListener imageListener : imageListeners){
				if(ImagePublisher.class.isInstance(imageListener)){
					doPublisherRecursion((ImagePublisher) imageListener);
				}
				System.out.println(imageListener.getClass().getSimpleName());
			}
		}
	}
}
