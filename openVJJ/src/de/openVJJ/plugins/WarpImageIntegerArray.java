package de.openVJJ.plugins;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.swing.JPanel;

import org.jdom2.Element;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLMemory.Mem;

import de.openVJJ.InputComponents;
import de.openVJJ.openGJTest;
import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.controler.WarpingControl;
import de.openVJJ.graphic.VideoFrame;
import de.openVJJ.processor.Warping.Point;
import de.openVJJ.values.IntegerArrayImageValue;

public class WarpImageIntegerArray extends Plugin {
	private Point[][][] imageMatrix;
	private int[][][] fastImageMatrix;
	int imageWidth = -1;
	int imageHeight = -1;
	Point pointTL;
	Point pointTR;
	Point pointBR;
	Point pointBL;
	
	public WarpImageIntegerArray(){
		addInput("Frame", IntegerArrayImageValue.class);
		addOutput("Frame", IntegerArrayImageValue.class);
	}

	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int speed = 2;
	private final static int FAST = 2;
	private final static int NORMAL = 1;
	
	
	public int[][][] processImage(int[][][] videoFrame) {
		int newImageX = videoFrame.length;
		int newImageY = videoFrame[0].length;
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
		if(imageWidth < 1 || imageHeight < 1){
			return;
		}
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
	
	private int[][][] warpe(int[][][] videoFrame){
		if(speed>NORMAL){
			if(fastImageMatrix == null){
				return videoFrame;
			}
			int xMax = fastImageMatrix.length;
			int yMax = fastImageMatrix[0].length;
			
			int[][][] result = new int[videoFrame.length][videoFrame[0].length][3];
			
			for(int x=0; x < xMax; x++){
				for(int y=0; y < yMax; y++){
					if(fastImageMatrix[x][y][2] != 1){
						continue;
					}
					result[x][y] = videoFrame[fastImageMatrix[x][y][0]][fastImageMatrix[x][y][1]];
				}
			}
			return result;
		}
		/*
		if(imageMatrix == null){
			return videoFrame;
		}
		
		int[][][] result = new int[videoFrame.length][videoFrame[0].length][3];
		
		for(int x=0; x < imageMatrix.length; x++){
			for(int y=0; y < imageMatrix[x].length; y++){
				if(imageMatrix[x][y] == null){
					continue;
				}
				try{
					if(speed == NORMAL || imageMatrix[x][y].length == 1 ){
						result[x][y] = videoFrame[imageMatrix[x][y][0].x][imageMatrix[x][y][1].y];
						continue;
					}
					for(int i = 0; i <imageMatrix[x][y].length; i++){
						for(int j = 0; j < result[x][y].length; j++ ){
							
						}
						result[x][y] = videoFrame[imageMatrix[x][y][0].x][imageMatrix[x][y][1].y];
						
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
		*/
		System.err.println("no other implemented");
		return null;
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


}
