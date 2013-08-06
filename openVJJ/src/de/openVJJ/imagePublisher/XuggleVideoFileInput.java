package de.openVJJ.imagePublisher;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;

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

public class XuggleVideoFileInput extends ImagePublisher {
	String inputFileName;
	long framerate = 50;
	long framerateLimit = 50;
	float speed = 1f;
	
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
			if(lastTimestamp != -1){
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

	@Override
	public void openConfigPanel() {
		fileChooser();
	}

}
