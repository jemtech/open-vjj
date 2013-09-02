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

public class Sorbel extends ImageProcessor {

	boolean blueForNeg = false;
	
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		VideoFrame videoFrameRes = calculateSorbel(videoFrame);
		return videoFrameRes;
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub

	}
	
	private VideoFrame calculateSorbel(VideoFrame videoFrame){
		VideoFrame result = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
		for(int col = 0; col < 3; col++ ){
			for(int x = 1; x < videoFrame.getWidth()-1; x++ ){
				for(int y = 1; y < videoFrame.getHeight()-1; y++){
					int xRes = videoFrame.getRGB(x-1, y-1)[col]*-3;
					xRes += videoFrame.getRGB(x-1, y)[col]*-10;
					xRes += videoFrame.getRGB(x-1, y+1)[col]*-3;
					xRes += videoFrame.getRGB(x+1, y-1)[col]*3;
					xRes += videoFrame.getRGB(x+1, y)[col]*10;
					xRes += videoFrame.getRGB(x+1, y+1)[col]*3;
					setXValue(x, y, xRes, result);
					
	
					int yRes = videoFrame.getRGB(x-1, y-1)[col]*3;
					yRes += videoFrame.getRGB(x, y-1)[col]*10;
					yRes += videoFrame.getRGB(x+1, y-1)[col]*3;
					yRes += videoFrame.getRGB(x-1, y+1)[col]*-3;
					yRes += videoFrame.getRGB(x, y+1)[col]*-10;
					yRes += videoFrame.getRGB(x+1, y+1)[col]*-3;
					setYValue(x, y, yRes, result);
				}
			}
		}
		return result;
	}
	
	private void setXValue(int x, int y, int value, VideoFrame result){
		if(value>-32 && value<32){
			return;
		}
		int[] rgb = result.getRGB(x, y);
		if(blueForNeg && value<0){
			rgb[2] += 15;
		}
		rgb[0] += Math.abs(value / 100);
		result.setColor(x, y, rgb);
	}
	

	private void setYValue(int x, int y, int value, VideoFrame result){
		if(value>-32 && value<32){
			return;
		}
		int[] rgb = result.getRGB(x, y);
		if(blueForNeg && value<0){
			rgb[2] += 60;
		}
		rgb[1] += Math.abs(value / 100);
		result.setColor(x, y, rgb);
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
