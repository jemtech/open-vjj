package de.openVJJ.processor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.nio.FloatBuffer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

public class LinearRGBCorrection extends ImageProcessor {
	double mulR = 1;
	double mulG = 1;
	double mulB = 1;
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(videoFrame == null){
			return null;
		}
		videoFrame.lock();
		VideoFrame res = null;
		if(InputComponents.useGPU){
			res = calculateLinearRGBCorrectionGPU(videoFrame);
		}else{
			res = calculateLinearRGBCorrection(videoFrame);
		}
		videoFrame.free();
		return res;
	}
	
	private VideoFrame calculateLinearRGBCorrectionGPU(VideoFrame videoFrame){
		if((mulR == 1) && (mulG == 1) && (mulB == 1)){
			return videoFrame;
		}
		if(!gpuReady){
			initGPU();
		}
		if(!gpuReady){
			return videoFrame;
		}
		
		int xMax = videoFrame.getWidth();
		int yMax = videoFrame.getHeight();
		VideoFrame resultVideoFrame = new VideoFrame(xMax, yMax);
		calculateOnGPU(videoFrame.getRedCLBuffer(this), (float) mulR, resultVideoFrame.getRedCLBuffer(this), xMax, yMax);
		calculateOnGPU(videoFrame.getGreenCLBuffer(this), (float) mulG, resultVideoFrame.getGreenCLBuffer(this), xMax, yMax);
		calculateOnGPU(videoFrame.getBlueCLBuffer(this), (float) mulB, resultVideoFrame.getBlueCLBuffer(this), xMax, yMax);
		return resultVideoFrame;
	}

	private synchronized void calculateOnGPU(CLBuffer<FloatBuffer> in, float multiplier, CLBuffer<FloatBuffer> out, int width, int height){
		CLKernel kernel = getKernel();
		//kernel.rewind();
		kernel.putArg(in);
		kernel.putArg(multiplier);
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
			program = getCLContext().createProgram(openGJTest.class.getResourceAsStream("kernelProgramms/multiply")).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		gpuReady = true;
		gpuIniting = false;
	}

	private CLKernel getKernel(){
		return program.createCLKernel("multiply");
	}
	
	private VideoFrame calculateLinearRGBCorrection(VideoFrame videoFrame){
		if((mulR == 1) && (mulG == 1) && (mulB == 1)){
			return videoFrame;
		}
		int xMax = videoFrame.getWidth();
		int yMax = videoFrame.getHeight();
		VideoFrame resultVideoFrame = new VideoFrame(xMax, yMax);
		for(int x=0; x<xMax;x++){
			for(int y=0;y<yMax;y++){
				int[] rgb = videoFrame.getRGB(x, y);
				int[] rgbNew = resultVideoFrame.getRGB(x, y);
				if(mulR != 1){
					rgbNew[0] = Math.min((int) (rgb[0]*mulR), 255);
				}else{
					rgbNew[0] = rgb[0];
				}
				if(mulG != 1){
					rgbNew[1] = Math.min((int) (rgb[1]*mulG), 255);
				}else{
					rgbNew[1] = rgb[1];
				}
				if(mulB!= 1){
					rgbNew[2] = Math.min((int) (rgb[2]*mulB), 255);
				}else{
					rgbNew[2] = rgb[2];
				}
			}
		}
		return resultVideoFrame;
	}

	JFrame controllerFrame;
	static final double mul1 = 64.0;
	@Override
	public void openConfigPanel() {
		controllerFrame = new JFrame();
		controllerFrame.setTitle("Linear RGB");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel rLabel = new JLabel("Red");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerFrame.add(rLabel, gridBagConstraints);
		
		JSlider rSlider = new JSlider();
		rSlider.setMinimum(0);
		rSlider.setMaximum(256);
		rSlider.setMajorTickSpacing((int)mul1);
		rSlider.setMinorTickSpacing(8);
		rSlider.setPaintTicks(true);
		rSlider.setValue((int) (mulR*mul1));
		rSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				mulR = ((JSlider) arg0.getSource()).getValue()/mul1;
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(rSlider, gridBagConstraints);
		
		JLabel gLabel = new JLabel("Green");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(gLabel, gridBagConstraints);
		
		JSlider gSlider = new JSlider();
		gSlider.setMinimum(0);
		gSlider.setMaximum(256);
		gSlider.setMajorTickSpacing((int)mul1);
		gSlider.setMinorTickSpacing(8);
		gSlider.setPaintTicks(true);
		gSlider.setValue((int) (mulG*mul1));
		gSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				mulG = ((JSlider) arg0.getSource()).getValue()/mul1;
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(gSlider, gridBagConstraints);
		
		
		JLabel bLabel = new JLabel("Blue");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		controllerFrame.add(bLabel, gridBagConstraints);
		
		JSlider bSlider = new JSlider();
		bSlider.setMinimum(0);
		bSlider.setMaximum(256);
		bSlider.setMajorTickSpacing((int)mul1);
		bSlider.setMinorTickSpacing(8);
		bSlider.setPaintTicks(true);
		bSlider.setValue((int) (mulB*mul1));
		bSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				mulB = ((JSlider) arg0.getSource()).getValue()/mul1;
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(bSlider, gridBagConstraints);
		
		controllerFrame.setVisible(true);
		controllerFrame.pack();
		
	}

	@Override
	public void getConfig(Element element) {
		element.setAttribute("mulR", String.valueOf(mulR));
		element.setAttribute("mulG", String.valueOf(mulG));
		element.setAttribute("mulB", String.valueOf(mulB));
	}

	@Override
	public void setConfig(Element element) {
		String mulR = element.getAttribute("mulR").getValue();
		if(mulR != null){
			this.mulR = Double.parseDouble(mulR);
		}
		String mulG = element.getAttribute("mulG").getValue();
		if(mulG != null){
			this.mulG = Double.parseDouble(mulG);
		}
		String mulB = element.getAttribute("mulB").getValue();
		if(mulB != null){
			this.mulB = Double.parseDouble(mulB);
		}
	}

}
