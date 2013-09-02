package de.openVJJ.processor;

import java.util.Arrays;

import org.jdom2.Element;

import de.openVJJ.controler.WarpingControl;
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

public class Warping extends ImageProcessor {
	private Point[][][] imageMatrix;
	private int[][][] fastImageMatrix;
	int imageWidth = -1;
	int imageHeight = -1;
	Point pointTL;
	Point pointTR;
	Point pointBR;
	Point pointBL;
	
	public int speed = 2;
	private final static int FAST = 2;
	private final static int NORMAL = 1;
	
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		int newImageX = videoFrame.getHeight();
		int newImageY = videoFrame.getWidth();
		if(imageHeight != newImageX || imageWidth != newImageY){
			imageHeight = newImageX;
			imageWidth = newImageY;
			if(pointTL == null){
				pointTL = new Point(0, 0);
			}
			if(pointTR == null){
				pointTR = new Point(imageWidth, 0);
			}
			if(pointBR == null){
				pointBR = new Point(imageWidth, imageHeight);
			}
			if(pointBL == null){
				pointBL = new Point(0, imageHeight);
			}
			if(pointTL.y > imageHeight){
				pointTL.y = imageHeight;
			}
			if(pointTR.y > imageHeight){
				pointTR.y = imageHeight;
			}
			if(pointBL.y > imageHeight){
				pointBL.y = imageHeight;
			}
			if(pointBR.y > imageHeight){
				pointBR.y = imageHeight;
			}
			if(pointTL.x > imageWidth){
				pointTL.x = imageWidth;
			}
			if(pointTR.x > imageWidth){
				pointTR.x = imageWidth;
			}
			if(pointBL.x > imageWidth){
				pointBL.x = imageWidth;
			}
			if(pointBR.x > imageWidth){
				pointBR.x = imageWidth;
			}
			refrefshMatrix();
		}
		return warpe(videoFrame);
	}
	
	public Point[] getWarpPoints(){
		return new Point[]{pointTL, pointTR, pointBR, pointBL};
	}
	public void setWarp(Point[] points){
		setWarp(points[0], points[1], points[2],points[3]);
	}
	public void setWarp(Point pointTL, Point pointTR, Point pointBR, Point pointBL){
		this.pointTL = pointTL;
		this.pointBL = pointBL;
		this.pointTR = pointTR;
		this.pointBR = pointBR;
		refrefshMatrix();
	}
	
	private void refrefshMatrix(){
		double[] resultx = new double[]{(pointTR.x-pointTL.x)/(double)imageWidth ,pointTL.x, (pointBL.x-pointTL.x)/(double)imageHeight, (((pointTR.x-pointTL.x)/(double)imageWidth-(pointBR.x-pointBL.x)/(double)imageWidth)/imageHeight)};
		double[] resulty = new double[]{(pointTR.y-pointTL.y)/(double)imageWidth ,pointTL.y, (pointBL.y-pointTL.y)/(double)imageHeight, (((pointTL.y-pointBL.y)/(double)imageHeight-(pointTR.y-pointBR.y)/(double)imageHeight)/imageWidth)};
		generateMatrix(resultx, resulty);
	}
	
	private void generateMatrix(double[] fX, double[] fY){
		if(speed<FAST){
			imageMatrix = new Point[imageWidth][imageHeight][];
		}else{
			fastImageMatrix = new int[imageWidth][imageHeight][3];
		}
		for(double x=0; x<imageWidth; x++){
			for(double y=0; y<imageHeight; y++){
				int xPos = (int) (x*(fX[0] - (fX[3]*y )) + fX[1] + y*fX[2] ); 
				int yPos = (int) (y*(fY[2] + (fY[3]*x )) + fY[1] + x*fY[0] );
				if(xPos>=imageWidth || yPos>=imageHeight){
					continue;
				}
				try{
					if(speed>NORMAL){
						if(fastImageMatrix[xPos][yPos][2] != 1){
							fastImageMatrix[xPos][yPos][2] = 1;//for isSet
							fastImageMatrix[xPos][yPos][0] = (int)x;
							fastImageMatrix[xPos][yPos][1] = (int)y;
						}
						continue;
					}
					if(imageMatrix[xPos][yPos]==null){
						imageMatrix[xPos][yPos] = new Point[1];
						imageMatrix[xPos][yPos][0] = new Point((int)x, (int)y);
					}else{
						int length = imageMatrix[xPos][yPos].length;
						imageMatrix[xPos][yPos] = Arrays.copyOf(imageMatrix[xPos][yPos], length+1);
						imageMatrix[xPos][yPos][length] = new Point((int)x, (int)y);
					}
				}catch (Exception e) {
					System.out.println("at x=" + x + " : xPos=" + xPos +" xmax=" + imageWidth + " y=" + y + " : yPos=" + yPos+" ymax=" + imageHeight );
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	private VideoFrame warpe(VideoFrame videoFrame){
		if(speed>NORMAL){
			if(fastImageMatrix == null){
				return videoFrame;
			}
			int xMax = fastImageMatrix.length;
			int yMax = fastImageMatrix[0].length;
			
			VideoFrame newVideoFrame = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
			
			for(int x=0; x < xMax; x++){
				for(int y=0; y < yMax; y++){
					if(fastImageMatrix[x][y][2] != 1){
						continue;
					}
					newVideoFrame.setRGB(x,y, videoFrame.getRGB(fastImageMatrix[x][y][0], fastImageMatrix[x][y][1]));
				}
			}
			return newVideoFrame;
		}
		
		if(imageMatrix == null){
			return videoFrame;
		}
		VideoFrame newVideoFrame = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
		
			for(int x=0; x < imageMatrix.length; x++){
				for(int y=0; y < imageMatrix[x].length; y++){
					if(imageMatrix[x][y] == null){
						continue;
					}
					try{
						if(speed == NORMAL || imageMatrix[x][y].length == 1 ){
							newVideoFrame.setRGB(x,y, videoFrame.getRGB(imageMatrix[x][y][0].x, imageMatrix[x][y][0].y));
							continue;
						}
						for(int i = 0; i <imageMatrix[x][y].length; i++){
							newVideoFrame.addColor(x, y, videoFrame.getRGB(imageMatrix[x][y][i].x, imageMatrix[x][y][i].y));
						}
						newVideoFrame.divide(x, y, imageMatrix[x][y].length);
					}catch (Exception e) {
						e.printStackTrace();
						System.out.println("x=" + x + " y=" + y);
						return null;
					}
				}
			}
			return newVideoFrame;
	}

	public static class Point{
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public int x;
		public int y;
	}

	@Override
	public void openConfigPanel() {
		new WarpingControl(this);
	}

	@Override
	public void getConfig(Element element) {
		element.setAttribute("pointTLx", String.valueOf(pointTL.x));
		element.setAttribute("pointTLy", String.valueOf(pointTL.y));
		element.setAttribute("pointBLx", String.valueOf(pointBL.x));
		element.setAttribute("pointBLy", String.valueOf(pointBL.y));
		element.setAttribute("pointTRx", String.valueOf(pointTR.x));
		element.setAttribute("pointTRy", String.valueOf(pointTR.y));
		element.setAttribute("pointBRx", String.valueOf(pointBR.x));
		element.setAttribute("pointBRy", String.valueOf(pointBR.y));
	}

	@Override
	public void setConfig(Element element) {
		String pointTLx = element.getAttribute("pointTLx").getValue();
		String pointTLy = element.getAttribute("pointTLy").getValue();
		String pointBLx = element.getAttribute("pointBLx").getValue();
		String pointBLy = element.getAttribute("pointBLy").getValue();
		String pointTRx = element.getAttribute("pointTRx").getValue();
		String pointTRy = element.getAttribute("pointTRy").getValue();
		String pointBRx = element.getAttribute("pointBRx").getValue();
		String pointBRy = element.getAttribute("pointBRy").getValue();
		
		if(pointTLx != null && pointTLy != null && pointBLx != null && pointBLy != null && pointTRx != null && pointTRy != null && pointBRx != null && pointBRy != null){
			int pointTLxInt = Integer.parseInt(pointTLx);
			int pointTLyInt = Integer.parseInt(pointTLy);
			Point pointTL = new Point(pointTLxInt, pointTLyInt);
			
			int pointBLxInt = Integer.parseInt(pointBLx);
			int pointBLyInt = Integer.parseInt(pointBLy);
			Point pointBL = new Point(pointBLxInt, pointBLyInt);
			
			int pointTRxInt = Integer.parseInt(pointTRx);
			int pointTRyInt = Integer.parseInt(pointTRy);
			Point pointTR = new Point(pointTRxInt, pointTRyInt);
			
			int pointBRxInt = Integer.parseInt(pointBRx);
			int pointBRyInt = Integer.parseInt(pointBRy);
			Point pointBR = new Point(pointBRxInt, pointBRyInt);
			setWarp(pointTL, pointTR, pointBR, pointBL);
		}
	}

	
}
