package de.openVJJ.processor;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

import de.openVJJ.graphic.VideoFrame;


public class EdgeBlender extends ImageProcessor{
	GraphicsConfiguration gConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	public static final int TOP =0;
	public static final int Bottom =1;
	public static final int Left =2;
	public static final int RIGHT =3;
	double xBlend=600;
	double yBlend=100;
	int edge = 0;
	double blendGrade = 2.2F;
	
	public EdgeBlender(){
		
	}
	
	public EdgeBlender(int xBlend, int yBlend, int edge){
		this.xBlend = (double)xBlend;
		this.yBlend = (double)yBlend;
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
		for(int y = 0; y<yBlend; y++){
			double reducktion = Math.pow((y+1.0)/yBlend,blendGrade);
			for(int x = 0; x<xBlend; x++){
				try{
					videoFrame.multiply(x, y, reducktion);
				}catch (Exception e) {
					System.out.println("blendTop " + e.getMessage());
					System.out.println("reducktion" + reducktion + " y=" + y);
				}
			}
		}
	}
	
	private void blendBottom(VideoFrame videoFrame){
		int ymax = videoFrame.getHeight();
		for(int y = 0; y<yBlend; y++){
			double reducktion = Math.pow((yBlend-(double)y)/yBlend,blendGrade);
			int yt = ymax-(int)yBlend+y;
			for(int x = 0; x<xBlend; x++){
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
		for(int x = 0; x<xBlend; x++){
			double reducktion = Math.pow((x+1.0)/xBlend,blendGrade);
			for(int y = 0; y<yBlend; y++){
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
		int xmax = videoFrame.getWidth();
		for(int x = 0; x<xBlend; x++){
			double reducktion = Math.pow((xBlend-(double)x)/xBlend,blendGrade);
			int xt = xmax-(int)xBlend+x;
			for(int y = 0; y<yBlend; y++){
				try{
					videoFrame.multiply(xt, y, reducktion);
				}catch (Exception e) {
					System.out.println("blendRight" + e.getMessage());
					System.out.println("reducktion" + reducktion + "x=" + x);
				}
			}
		}
	}

}
