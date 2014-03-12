package de.openVJJ.processor;


import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.jdom2.Element;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLMemory.Mem;

import de.openVJJ.InputComponents;
import de.openVJJ.openGJTest;
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
		if(InputComponents.useGPU){
			return calculateSorbelGPU(videoFrame);
		}
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
	
	private VideoFrame calculateSorbelGPU(VideoFrame videoFrame){
		if(!gpuReady){
			initGPU();
		}
		if(!gpuReady){
			return videoFrame;
		}
		CLKernel kernel = getRGBKernel();
		
		int width = videoFrame.getWidth();
		int height = videoFrame.getHeight();
		
		VideoFrame resultVideoFrame = new VideoFrame(width, height);
		
		CLBuffer<FloatBuffer> rIn = videoFrame.getRedCLBuffer(this);
		CLBuffer<FloatBuffer> gIn = videoFrame.getGreenCLBuffer(this);
		CLBuffer<FloatBuffer> bIn = videoFrame.getBlueCLBuffer(this);
		
		CLBuffer<FloatBuffer> rOut = resultVideoFrame.getRedCLBuffer(this);
		CLBuffer<FloatBuffer> gOut = resultVideoFrame.getGreenCLBuffer(this);
		CLBuffer<FloatBuffer> bOut = resultVideoFrame.getBlueCLBuffer(this);
		
		kernel.putArg(rIn);
		kernel.putArg(gIn);
		kernel.putArg(bIn);
		kernel.putArg(rOut);
		kernel.putArg(gOut);
		kernel.putArg(bOut);
		kernel.putArg(width);
		kernel.putArg(height);
		CLCommandQueue clCommandQueue = getCLCommandQueue();
		synchronized (clCommandQueue) {
			clCommandQueue.putWriteBuffer(rIn, false);
			clCommandQueue.putWriteBuffer(gIn, false);
			clCommandQueue.putWriteBuffer(bIn, false);
			//clCommandQueue.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, localWorkSizeX, localWorkSizeY);
			clCommandQueue.put2DRangeKernel(kernel, 0, 0, width, height, 0, 0);//auto calc by driver
			clCommandQueue.putReadBuffer(rOut, true);
			clCommandQueue.putReadBuffer(gOut, true);
			clCommandQueue.putReadBuffer(bOut, true);
			clCommandQueue.finish();
		}
		kernel.release();
		return resultVideoFrame;
	}
	
	private CLProgram program;
	//private CLKernel kernel;
	boolean gpuReady = false;
	boolean gpuIniting = false;
	private void initGPU(){
		if(gpuIniting){
			return;
		}
		gpuIniting = true;
		try {
			program = getCLContext().createProgram(openGJTest.class.getResourceAsStream("kernelProgramms/sorbel")).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		gpuReady = true;
		gpuIniting = false;
	}

	private CLKernel getRGBKernel(){
		return program.createCLKernel("sorbelRGB");
	}
	
	private void shutdownGPU(){
		if(program != null){
			program.release();
		}
	}

	@Override
	public void getConfig(Element element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfig(Element element) {
		// TODO Auto-generated method stub
		
	}
	
	public SorbelResult calculateSorbelResult(VideoFrame videoFrame){
		SorbelResult result = new SorbelResult();
		int width = videoFrame.getWidth();
		int height = videoFrame.getHeight();
		for(int col = 0; col < 3; col++ ){
			int[][] resultx = new int[width][height];
			int[][] resulty = new int[width][height];
			for(int x = 1; x < width-1; x++ ){
				for(int y = 1; y < height-1; y++){
					int xRes = videoFrame.getRGB(x-1, y-1)[col]*-3;
					xRes += videoFrame.getRGB(x-1, y)[col]*-10;
					xRes += videoFrame.getRGB(x-1, y+1)[col]*-3;
					xRes += videoFrame.getRGB(x+1, y-1)[col]*3;
					xRes += videoFrame.getRGB(x+1, y)[col]*10;
					xRes += videoFrame.getRGB(x+1, y+1)[col]*3;
					resultx[x][y] =  Math.abs(xRes);
					
	
					int yRes = videoFrame.getRGB(x-1, y-1)[col]*3;
					yRes += videoFrame.getRGB(x, y-1)[col]*10;
					yRes += videoFrame.getRGB(x+1, y-1)[col]*3;
					yRes += videoFrame.getRGB(x-1, y+1)[col]*-3;
					yRes += videoFrame.getRGB(x, y+1)[col]*-10;
					yRes += videoFrame.getRGB(x+1, y+1)[col]*-3;
					resulty[x][y] =  Math.abs(yRes);
				}
			}
			result.add(resultx, resulty);
		}
		return result;
	}
	

	private CLKernel getKernel(){
		return program.createCLKernel("sorbel");
	}
	
	public SorbelResultGPU calculateSorbelResultGPU(VideoFrame videoFrame){
		
		if(!gpuReady){
			initGPU();
		}
		if(!gpuReady){
			return null;
		}
		CLKernel kernel = getKernel();
		
		int width = videoFrame.getWidth();
		int height = videoFrame.getHeight();
		SorbelResultGPU result = new SorbelResultGPU(width, height);
		
		calculateSorbelchannelGPU(kernel, videoFrame.getRedCLBuffer(this), result, width, height);
		
		calculateSorbelchannelGPU(kernel, videoFrame.getGreenCLBuffer(this), result, width, height);
		
		calculateSorbelchannelGPU(kernel, videoFrame.getBlueCLBuffer(this), result, width, height);
		
		kernel.release();
		
		return result;
	}
	
	private void calculateSorbelchannelGPU(CLKernel kernel, CLBuffer<FloatBuffer> in, SorbelResultGPU result, int width, int height){
		CLBuffer<FloatBuffer> outx = getCLContext().createFloatBuffer(width*height, Mem.READ_WRITE);
		CLBuffer<FloatBuffer> outy = getCLContext().createFloatBuffer(width*height, Mem.READ_WRITE);
		kernel.putArg(in);
		kernel.putArg(outx);
		kernel.putArg(outy);
		kernel.putArg(width);
		kernel.putArg(height);
		CLCommandQueue clCommandQueue = getCLCommandQueue();
		synchronized (clCommandQueue) {
			clCommandQueue.putWriteBuffer(in, false);
			clCommandQueue.put2DRangeKernel(kernel, 0, 0, width, height, 0, 0);//auto calc by driver
			clCommandQueue.putReadBuffer(outx, true);
			clCommandQueue.putReadBuffer(outy, true);
			clCommandQueue.finish();
		}
		result.add(outx, outy);
		
	}

	public class SorbelResultGPU{
		ArrayList<CLBuffer<FloatBuffer>> resultsPerChanelX = new ArrayList<CLBuffer<FloatBuffer>>();
		ArrayList<CLBuffer<FloatBuffer>> resultsPerChanelY = new ArrayList<CLBuffer<FloatBuffer>>();
		int width;
		int height;
		
		public SorbelResultGPU(int width, int height){
			this.height = height;
			this.width = width;
		}
		
		public void add(CLBuffer<FloatBuffer> resultx, CLBuffer<FloatBuffer> resulty){
			resultsPerChanelX.add(resultx);
			resultsPerChanelY.add(resulty);
		}
		
		public CLBuffer<FloatBuffer> getXbyChanel(int chanal){
			return resultsPerChanelX.get(chanal);
		}
		
		public CLBuffer<FloatBuffer> getYbyChanel(int chanal){
			return resultsPerChanelY.get(chanal);
		}
	}
	
	public class SorbelResult{
		ArrayList<int[][]> resultsPerChanelX = new ArrayList<int[][]>();
		ArrayList<int[][]> resultsPerChanelY = new ArrayList<int[][]>();
		
		public void add(int[][] resultx, int[][] resulty){
			resultsPerChanelX.add(resultx);
			resultsPerChanelY.add(resulty);
		}
		
		public int[][] getXbyChanel(int chanal){
			return resultsPerChanelX.get(chanal);
		}
		
		public int[][] getYbyChanel(int chanal){
			return resultsPerChanelY.get(chanal);
		}
	}
}
