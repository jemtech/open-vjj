package de.openVJJ.imagePublisher;

import java.awt.image.BufferedImage;

import javax.swing.JFileChooser;

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
		openFilechooser();
	}
	private void openFilechooser(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					fileChooser();
				}
			}
		}).start();
	}
	
	public void setInputFileName(String inputFileName) {
		stopReading();
		this.inputFileName = inputFileName;
		startReading();
	}
	public void stopReading(){
		read = false;
		if(mediaReader != null){
			mediaReader.close();
			mediaReader = null;
		}
	}
	boolean read = true;
	IMediaReader mediaReader;
	public void startReading() {
		mediaReader = ToolFactory.makeReader(inputFileName);
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		mediaReader.addListener(new MyInputListener(this));
		read = true;
		while (read){
			if(mediaReader.readPacket() == null){
			}
			try {
				Thread.sleep((long)(((1000)/framerate)/speed));
			} catch (Exception e) {
			}
		}
	}
	JFileChooser chooser;
	private void fileChooser(){
		if(chooser == null){
			chooser = new JFileChooser();
		}
		chooser.showOpenDialog(null);
		setInputFileName(chooser.getSelectedFile().getPath());
		chooser = null;
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
			xuggleVideoFileInput.publishImage(videoFrame);
		}
		
		
	}
	
}
