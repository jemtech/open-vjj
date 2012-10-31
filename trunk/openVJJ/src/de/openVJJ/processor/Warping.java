package de.openVJJ.processor;

import java.util.Arrays;

import de.openVJJ.graphic.VideoFrame;

public class Warping extends ImageProcessor {
	private Point[][][] imageMatrix;
	int imageWidth = 800;
	int imageHeight = 600;
	Point pointTL;
	Point pointTR;
	Point pointBR;
	Point pointBL;
	private final static boolean FAST = true;
	
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		int newImageX = videoFrame.getHeight();
		int newImageY = videoFrame.getWidth();
		if(imageHeight != newImageX || imageWidth != newImageY){
			imageHeight = newImageX;
			imageWidth = newImageY;
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
		imageMatrix = new Point[imageWidth][imageHeight][];
		for(double x=0; x<imageWidth; x++){
			for(double y=0; y<imageHeight; y++){
				int xPos = (int) (x*(fX[0] - (fX[3]*y )) + fX[1] + y*fX[2] ); 
				int yPos = (int) (y*(fY[2] + (fY[3]*x )) + fY[1] + x*fY[0] );
				if(xPos>=imageWidth || yPos>=imageHeight){
					continue;
				}
				try{
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
						if(FAST || imageMatrix[x][y].length == 1 ){
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
}
