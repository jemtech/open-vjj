package de.openVJJ.processor;


import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.jdom2.Element;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

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

public class GaussFilter extends ImageProcessor {

	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(InputComponents.useGPU){
			return filterFrameGPU(videoFrame);
		}
		return filterFrame(videoFrame);
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub

	}

	int[][] gausMatrix = {{1,2,1},{2,4,2},{1,2,1}};
	private VideoFrame filterFrame(VideoFrame videoFrame){
		int xMax = videoFrame.getWidth();
		int yMax = videoFrame.getHeight();
		int matrixSize = gausMatrix.length;
		int matrixOfset = ((matrixSize - 1) / 2) + 1;
		int gausKomp = 0;
		for(int x = 0; x < matrixSize; x++ ){
			for(int y = 0; y < matrixSize; y++ ){
				gausKomp += gausMatrix[x][y];
			}
		}
		VideoFrame videoFrameRes = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
		for(int x = 0; x < xMax; x++ ){
			for(int y = 0; y < yMax; y++ ){
				int[] pointVal = new int[3];
				for(int xm = 0; xm < matrixSize; xm++ ){
					for(int ym = 0; ym < matrixSize; ym++ ){
						int xpm = x + xm - matrixOfset;
						int ypm = y + ym - matrixOfset;
						if(xpm<0){
							break;
						}
						if(ypm<0){
							continue;
						}
						int[] rgb = videoFrame.getRGB(xpm, ypm);
						int matrixVal = gausMatrix[xm][ym];
						pointVal[0] += rgb[0] * matrixVal;
						pointVal[1] += rgb[1] * matrixVal;
						pointVal[2] += rgb[2] * matrixVal;
						
					}
				}
				videoFrameRes.setColor(x, y, pointVal[0] / gausKomp, pointVal[1] / gausKomp, pointVal[2] / gausKomp) ;
			}
		}
		return videoFrameRes;
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
			program = getCLContext().createProgram(openGJTest.class.getResourceAsStream("kernelProgramms/gauss")).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//kernel = program.createCLKernel("gauss");
		gpuReady = true;
		gpuIniting = false;
	}

	private CLKernel getKernel(){
		return program.createCLKernel("gauss");
	}
	
	private void shutdownGPU(){
		if(program != null){
			program.release();
		}
	}
	
	private VideoFrame filterFrameGPU(VideoFrame videoFrame){
		if(!gpuReady){
			initGPU();
		}
		if(!gpuReady){
			return videoFrame;
		}
		
		int width = videoFrame.getWidth();
		int height = videoFrame.getHeight();

		int localWorkSize = getLocalWorkSize2D();
		int globalWorkSizeX = getGlobalWorkSizeX(width);
		int globalWorkSizeY = getGlobalWorkSizeY(height);

		VideoFrame resultVideoFrame = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
		
		calculateOnGPU(videoFrame.getRedCLBuffer(this), resultVideoFrame.getRedCLBuffer(this), width, height, globalWorkSizeX, localWorkSize, globalWorkSizeY, localWorkSize);
		calculateOnGPU(videoFrame.getGreenCLBuffer(this), resultVideoFrame.getGreenCLBuffer(this), width, height, globalWorkSizeX, localWorkSize, globalWorkSizeY, localWorkSize);
		calculateOnGPU(videoFrame.getBlueCLBuffer(this), resultVideoFrame.getBlueCLBuffer(this), width, height, globalWorkSizeX, localWorkSize, globalWorkSizeY, localWorkSize);
		
		return resultVideoFrame;
	}
	
	private synchronized void calculateOnGPU(CLBuffer<FloatBuffer> in, CLBuffer<FloatBuffer> out, int width, int height, int globalWorkSizeX, int localWorkSizeX, int globalWorkSizeY, int localWorkSizeY){
		CLKernel kernel = getKernel();
		//kernel.rewind();
		kernel.putArg(in);
		kernel.putArg(out);
		kernel.putArg(width);
		kernel.putArg(height);
		CLCommandQueue clCommandQueue = getCLCommandQueue();
		synchronized (clCommandQueue) {
			clCommandQueue.putWriteBuffer(in, false);
			//clCommandQueue.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, localWorkSizeX, localWorkSizeY);
			clCommandQueue.put2DRangeKernel(kernel, 0, 0, width, height, 0, 0);//auto calc by driver
			clCommandQueue.putReadBuffer(out, true);
			clCommandQueue.finish();
		}
		kernel.release();
	}
	
	@Override
	public void remove() {
		super.remove();
		shutdownGPU();
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
