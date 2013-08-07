package de.openVJJ.ImageListener;

import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

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

public class Recorder implements ImageListener {
	IMediaWriter writer;
	long startTime = -1;
	int width;
	int height;
	long restartAfterNanos = -1;
	
	public Recorder(){
		this(800, 600);
	}
	
	public Recorder(int width, int height) {
		this.width = width;
		this.height = height;
		startRecording();
		setShutdownHook();
	}
	
	private void setShutdownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    	endRecording();
		    }
		});
	}
	
	public void startRecording(){
		writer = ToolFactory.makeWriter("output_" + System.currentTimeMillis() + ".mp4");
		writer.addVideoStream(0, 0, width, height);
		shutdown = false;
	}
	
	public void endRecording(){
		shutdown = true;
		if(writer == null){
			return;
		}
		if(!writer.isOpen()){
			return;
		}
    	writer.flush();
    	System.out.println("Closing stream");
    	writer.close();
    	
	}
	
	public void restartRecording(){
		endRecording();
		startRecording();
	}
	
	boolean shutdown = true;
	@Override
	public void newImageReceived(VideoFrame videoFrame) {
		if(shutdown){
			return;
		}
		long now = System.nanoTime();
		if(restartAfterNanos > 0 && startTime > 0){
			if((startTime + restartAfterNanos) < now){
				restartRecording();
				startTime = -1;
			}
		}
		if(videoFrame == null){
			return;
		}
		
		if(startTime<0){
			startTime = now;
		}
		
		writer.encodeVideo(0, videoFrame.getImage(), now-startTime, TimeUnit.NANOSECONDS);
	}
	

	public void remove(){
		endRecording();
	}

	@Override
	public void openConfigPanel() {
		
	}

}
