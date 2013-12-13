package de.openVJJ.imagePublisher;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;

import de.openVJJ.graphic.VideoFrame;

/*
 * Copyright (C) 2012-213  Jan-Erik Matthies
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

public class XuggleVideoFileInput extends ImagePublisher {
	String inputFileName;
	long framerate = 50;
	long framerateLimit = 50;
	float speed = 1f;
	boolean lockFramerate = false;
	
	public XuggleVideoFileInput() {
	}
	
	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
		if(read){
			stopReading();
			startReading();
		}else{
			startReading();
		}
	}
	public void stopReading(){
		read = false;
		while(lockRead){
			System.out.println("Still reading");
			try {
				Thread.sleep((long)(((1000)/framerate)/speed));
			} catch (Exception e) {
			}
		}
		if(inputListener != null){
			mediaReader.removeListener(inputListener);
			inputListener = null;
		}
		if(mediaReader != null){
			mediaReader.close();
			mediaReader = null;
		}
	}
	
	boolean read = false;
	IMediaReader mediaReader;
	MyInputListener inputListener;
	boolean lockRead;
	public void startReading() {
		inputListener = new MyInputListener(this);
		new Thread(new Runnable(){

			@Override
			public void run() {
				mediaReader = ToolFactory.makeReader(inputFileName);
				mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
				mediaReader.addListener(inputListener);
				read = true;
				while (read){
					lockRead = true;
					try{
					if(mediaReader.readPacket() == null){
					}
					}catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep((long)(((1000)/framerate)/speed));
					} catch (Exception e) {
					}
					lockRead = false;
				}
			}
			
		}).start();
	}
	
	public void fileChooser(){
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		File selectedFile = chooser.getSelectedFile();
		if(selectedFile == null){
			return;
		}
		String path = selectedFile.getPath();
		if(path == null){
			return;
		}
		setInputFileName(path);
	}

	private class MyInputListener extends MediaListenerAdapter {
		XuggleVideoFileInput xuggleVideoFileInput;
		long lastTimestamp =-1;
		int jumpt = 1;
		public MyInputListener(XuggleVideoFileInput xuggleVideoFileInput) {
			this.xuggleVideoFileInput = xuggleVideoFileInput;
		}
		@Override
		public void onVideoPicture(IVideoPictureEvent event) {
			long accTimestamp= event.getTimeStamp();
			if(lastTimestamp != -1 && !lockFramerate){
				framerate = (long) (1/((accTimestamp - lastTimestamp) / 1000000f));
			}
			lastTimestamp = accTimestamp;
			if(framerate*speed > framerateLimit){
				int jump = (int) ((framerate*speed)/framerateLimit);
				if(jumpt<jump){
					jumpt++;
					return;
				}else{
					jumpt = 1;
				}
			}else{
				jumpt = 1;
			}
			VideoFrame videoFrame = new VideoFrame(event.getImage());
			xuggleVideoFileInput.publishImage(videoFrame, true);
		}
		
		
	}
	
	@Override
	public void remove() {
		stopReading();
		shutdownListener();
		
	}

	JFrame controllerFrame;
	@Override
	public void openConfigPanel() {

		controllerFrame = new JFrame();
		controllerFrame.setTitle("Xuglge Video");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel rLabel = new JLabel("f/s (" + framerate + ")");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerFrame.add(rLabel, gridBagConstraints);
		
		JSlider rSlider = new JSlider();
		rSlider.setMinimum(0);
		rSlider.setMaximum((int)framerateLimit);
		rSlider.setMajorTickSpacing(64);
		rSlider.setMinorTickSpacing(8);
		rSlider.setPaintTicks(true);
		rSlider.setValue((int)framerate);
		rSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				framerate = ((JSlider) arg0.getSource()).getValue();
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(rSlider, gridBagConstraints);

		JCheckBox chinButton = new JCheckBox("Framerate lock");
	    chinButton.setSelected(lockFramerate);
	    chinButton.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				lockFramerate = ((JCheckBox) arg0.getSource()).isSelected();
			}
		});
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(chinButton, gridBagConstraints);
		
		JButton selctFileButton = new JButton("Select video");
		selctFileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fileChooser();
			}
		});
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		controllerFrame.add(selctFileButton, gridBagConstraints);

		controllerFrame.setVisible(true);
		controllerFrame.pack();
	}
	

	@Override
	public void getConfig(Element element) {
		if(inputFileName != null){
			element.setAttribute("inputFileName", inputFileName);
		}
	}

	@Override
	public void setConfig(Element element) {
		Attribute inputFileName = element.getAttribute("inputFileName");
		if(inputFileName != null){
			setInputFileName(inputFileName.getValue());
		}
	}

}
