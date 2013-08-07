package de.openVJJ.processor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.openVJJ.graphic.VideoFrame;

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

public class Resulution extends ImageProcessor {
	int width = 800;
	int height = 600;

	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(videoFrame == null){
			return null;
		}
		videoFrame.scaleTo(width, height);
		return videoFrame;
	}

	JFrame controllerFrame;
	@Override
	public void openConfigPanel() {

		controllerFrame = new JFrame();
		controllerFrame.setTitle("Video size");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel widthLabel = new JLabel("Width");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerFrame.add(widthLabel, gridBagConstraints);
		
		final JTextField widthJTextField = new JTextField(String.valueOf(width), 5);
		gridBagConstraints.gridx = 1;
		controllerFrame.add(widthJTextField, gridBagConstraints);
		
		JLabel heightLabel = new JLabel("Height");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(heightLabel, gridBagConstraints);
		
		final JTextField heightJTextField = new JTextField(String.valueOf(height), 5);
		gridBagConstraints.gridx = 1;
		controllerFrame.add(heightJTextField, gridBagConstraints);

		JButton saveButton = new JButton("Set");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				width = Integer.parseInt(widthJTextField.getText());
				height = Integer.parseInt(heightJTextField.getText());
				controllerFrame.setVisible(false);
				controllerFrame.dispose();
				controllerFrame = null;
			}
		});
		controllerFrame.add(saveButton, gridBagConstraints);

		controllerFrame.setVisible(true);
		controllerFrame.pack();
	}


}
