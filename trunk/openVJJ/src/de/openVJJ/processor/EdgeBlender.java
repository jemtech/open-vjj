package de.openVJJ.processor;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

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

public class EdgeBlender extends ImageProcessor{
	GraphicsConfiguration gConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	public static final int TOP =0;
	public static final int Bottom =1;
	public static final int Left =2;
	public static final int RIGHT =3;
	int xBlend=600;
	int yBlend=100;
	int edge = 0;
	double blendGrade = 2.2F;
	
	public EdgeBlender(){
		
	}
	
	public EdgeBlender(int xBlend, int yBlend, int edge){
		this.xBlend = xBlend;
		this.yBlend = yBlend;
		this.edge = edge;
		
	}
	
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		switch(edge){
			case TOP:{
				blendTop(videoFrame);
				break;
			}
			case Bottom:{
				blendBottom(videoFrame);
				break;
			}
			case Left:{
				blendLeft(videoFrame);
				break;
			}
			case RIGHT:{
				blendRight(videoFrame);
				break;
			}
		}
		return videoFrame;
	}
	
	private void blendTop(VideoFrame videoFrame){
		double yMax = yBlend;
		if(yBlend > videoFrame.getHeight()){
			yMax = videoFrame.getHeight();
		}
		double xMax = xBlend;
		if(xBlend > videoFrame.getWidth()){
			xMax = videoFrame.getWidth();
		}
		for(int y = 0; y<yMax; y++){
			double reducktion = Math.pow((y+1.0)/yMax,blendGrade);
			for(int x = 0; x<xMax; x++){
				try{
					videoFrame.multiply(x, y, reducktion);
				}catch (Exception e) {
					System.out.println("blendTop " + e.getMessage());
					System.out.println("reducktion" + reducktion + " y=" + y);
					e.printStackTrace();
				}
			}
		}
	}
	
	private void blendBottom(VideoFrame videoFrame){
		double yMax = yBlend;
		if(yBlend > videoFrame.getHeight()){
			yMax = videoFrame.getHeight();
		}
		double xMax = xBlend;
		if(xBlend > videoFrame.getWidth()){
			xMax = videoFrame.getWidth();
		}
		int imH = videoFrame.getHeight();
		for(int y = 0; y<yMax; y++){
			double reducktion = Math.pow((yMax-(double)y)/yMax,blendGrade);
			int yt = imH-(int)yMax+y;
			for(int x = 0; x<xMax; x++){
				try{
					videoFrame.multiply(x, yt, reducktion);
				}catch (Exception e) {
					System.out.println("blendBottom " + e.getMessage());
					System.out.println("reducktion" + reducktion + " y=" + y);
				}
			}
		}
	}
	
	private void blendLeft(VideoFrame videoFrame){
		double yMax = yBlend;
		if(yBlend > videoFrame.getHeight()){
			yMax = videoFrame.getHeight();
		}
		double xMax = xBlend;
		if(xBlend > videoFrame.getWidth()){
			xMax = videoFrame.getWidth();
		}
		for(int x = 0; x<xMax; x++){
			double reducktion = Math.pow((x+1.0)/xMax,blendGrade);
			for(int y = 0; y<yMax; y++){
				try{
					videoFrame.multiply(x, y, reducktion);
				}catch (Exception e) {
					System.out.println("blendLeft " + e.getMessage());
					System.out.println("reducktion" + reducktion + "x=" + x);
				}
			}
		}
	}

	private void blendRight(VideoFrame videoFrame){
		double yMax = yBlend;
		if(yBlend > videoFrame.getHeight()){
			yMax = videoFrame.getHeight();
		}
		double xMax = xBlend;
		if(xBlend > videoFrame.getWidth()){
			xMax = videoFrame.getWidth();
		}
		int imW = videoFrame.getWidth();
		for(int x = 0; x<xMax; x++){
			double reducktion = Math.pow((xMax-(double)x)/xMax,blendGrade);
			int xt = imW-(int)xMax+x;
			for(int y = 0; y<yMax; y++){
				try{
					videoFrame.multiply(xt, y, reducktion);
				}catch (Exception e) {
					System.out.println("blendRight" + e.getMessage());
					System.out.println("reducktion" + reducktion + "x=" + x);
				}
			}
		}
	}

	@Override
	public void openConfigPanel() {
		
	}


}
