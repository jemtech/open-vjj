/**
 * 
 */
package de.openVJJ.plugins;

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
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.graphic.VideoFrame;
import de.openVJJ.values.BufferedImageValue;

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
public class XuggleVideoIn extends Plugin {

	public XuggleVideoIn(){
		addOutput("Frames", BufferedImageValue.class);
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
		// TODO Auto-generated method stub
		return null;
	}
	
	String inputFileName;
	long framerate = 50;
	long framerateLimit = 50;
	float speed = 1f;
	boolean lockFramerate = false;
	
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
		inputListener = new MyInputListener();
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
		long lastTimestamp =-1;
		int jumpt = 1;
		public MyInputListener() {
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
			BufferedImageValue value =new BufferedImageValue(event.getImage());
			getConnection("Frames").transmitValue(value);
			
		}
		
		
	}
	
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#shutdown()
	 */
	@Override
	protected void shutdown() {
		// TODO Auto-generated method stub
		stopReading();
		super.shutdown();
	}
	@Override
	public JPanel getConfigPannel() {
		JPanel configPanel = new JPanel();
		
		configPanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel rLabel = new JLabel("f/s (" + framerate + ")");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		configPanel.add(rLabel, gridBagConstraints);
		
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
		configPanel.add(rSlider, gridBagConstraints);

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
		configPanel.add(chinButton, gridBagConstraints);
		
		JButton selctFileButton = new JButton("Select video");
		selctFileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fileChooser();
			}
		});
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		configPanel.add(selctFileButton, gridBagConstraints);
		return configPanel;
	}

}
