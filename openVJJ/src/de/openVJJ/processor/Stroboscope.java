package de.openVJJ.processor;

import org.jdom2.Element;

import de.openVJJ.graphic.VideoFrame;

/*
 * Copyright (C) 2013  Jan-Erik Matthies
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

public class Stroboscope extends ImageProcessor {

	private VideoFrame strobFrame;
	private int modus = 0;
	public final static int MODUS_BLACK = 0;
	public final static int MODUS_WHITE = 1;
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(videoFrame == null){
			return null;
		}
		return strobeFrame(videoFrame);
	}
	
	private int lastHeight = 0;
	private int lastWidth = 0;
	private boolean lastFrameStrob = false;
	public VideoFrame strobeFrame(VideoFrame videoFrame){
		if(lastHeight != videoFrame.getHeight() || lastWidth != videoFrame.getWidth()){
			lastHeight = videoFrame.getHeight();
			lastWidth = videoFrame.getWidth();
		}
		if(lastFrameStrob){
			lastFrameStrob = false;
			return videoFrame;
		}else{
			lastFrameStrob = true;
			if(strobFrame == null){
				setStrobFrame();
			}
			return strobFrame;
		}
	}
	
	private void setStrobFrame(){
		strobFrame = new VideoFrame(lastWidth, lastWidth);
		switch(modus){
			case MODUS_BLACK:{
				return;
			}
			case MODUS_WHITE:{
				for(int x = 0; x<lastWidth; x++){
					for(int y = 0; y<lastHeight;y++){
						strobFrame.setColor(x, y, 255, 255, 255);
					}
				}
			}
		}
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getConfig(Element element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfig(Element element) {
		// TODO Auto-generated method stub
		
	}

}
