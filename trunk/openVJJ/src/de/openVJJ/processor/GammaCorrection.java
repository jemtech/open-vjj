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

public class GammaCorrection extends ImageProcessor {
	double gammaR = 1;
	double gammaG = 1;
	double gammaB = 1;
	
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(videoFrame == null){
			return null;
		}
		if(InputComponents.useGPU){
			return calculateGammaCorrectionGPU(videoFrame);
		}else{
			return calculateGammaCorrection(videoFrame);
		}
	}
	
	private VideoFrame calculateGammaCorrection(VideoFrame videoFrame){
		if((gammaR == 1) && (gammaG == 1) && (gammaB == 1)){
			return videoFrame;
		}
		int xMax = videoFrame.getWidth();
		int yMax = videoFrame.getHeight();
		double rCorr = Math.pow(255.0, (gammaR-1)*-1);
		double gCorr = Math.pow(255.0, (gammaG-1)*-1);
		double bCorr = Math.pow(255.0, (gammaB-1)*-1);
		VideoFrame resultVideoFrame = new VideoFrame(xMax, yMax);
		for(int x=0; x<xMax;x++){
			for(int y=0;y<yMax;y++){
				int[] rgb = videoFrame.getRGB(x, y);
				int[] rgbNew = resultVideoFrame.getRGB(x, y);
				if(gammaR != 1){
					rgbNew[0] = Math.min((int) (Math.pow(rgb[0], gammaR)*rCorr), 255);
				}else{
					rgbNew[0] = rgb[0];
				}
				if(gammaG != 1){
					rgbNew[1] = Math.min((int) (Math.pow(rgb[1], gammaG)*gCorr), 255);
				}else{
					rgbNew[1] = rgb[1];
				}
				if(gammaB!= 1){
					rgbNew[2] = Math.min((int) (Math.pow(rgb[2], gammaB)*bCorr), 255);
				}else{
					rgbNew[2] = rgb[2];
				}
			}
		}
		return resultVideoFrame;
	}

	JFrame controllerFrame;
	static final double gamma1 = 64.0;
	@Override
	public void openConfigPanel() {
		controllerFrame = new JFrame();
		controllerFrame.setTitle("Gamma RGB");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel rLabel = new JLabel("Red");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerFrame.add(rLabel, gridBagConstraints);
		
		JSlider rSlider = new JSlider();
		rSlider.setMinimum(1);
		rSlider.setMaximum(256);
		rSlider.setMajorTickSpacing(64);
		rSlider.setMinorTickSpacing(8);
		rSlider.setPaintTicks(true);
		rSlider.setValue((int) (gammaR*gamma1));
		rSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				gammaR = gamma1/((JSlider) arg0.getSource()).getValue();
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(rSlider, gridBagConstraints);
		
		JLabel gLabel = new JLabel("Green");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(gLabel, gridBagConstraints);
		
		JSlider gSlider = new JSlider();
		gSlider.setMinimum(1);
		gSlider.setMaximum(256);
		gSlider.setMajorTickSpacing(64);
		gSlider.setMinorTickSpacing(8);
		gSlider.setPaintTicks(true);
		gSlider.setValue((int) (gammaG*gamma1));
		gSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				gammaG = gamma1/((JSlider) arg0.getSource()).getValue();
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(gSlider, gridBagConstraints);
		
		
		JLabel bLabel = new JLabel("Blue");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		controllerFrame.add(bLabel, gridBagConstraints);
		
		JSlider bSlider = new JSlider();
		bSlider.setMinimum(1);
		bSlider.setMaximum(256);
		bSlider.setMajorTickSpacing(64);
		bSlider.setMinorTickSpacing(8);
		bSlider.setPaintTicks(true);
		bSlider.setValue((int) (gammaB*gamma1));
		bSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				gammaB = gamma1/((JSlider) arg0.getSource()).getValue();
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(bSlider, gridBagConstraints);
		
		controllerFrame.setVisible(true);
		controllerFrame.pack();
		
	}

	@Override
	public void getConfig(Element element) {
		element.setAttribute("gammaR", String.valueOf(gammaR));
		element.setAttribute("gammaG", String.valueOf(gammaG));
		element.setAttribute("gammaB", String.valueOf(gammaB));
	}

	@Override
	public void setConfig(Element element) {
		String gammaR = element.getAttribute("gammaR").getValue();
		if(gammaR != null){
			this.gammaR = Double.parseDouble(gammaR);
		}
		String gammaG = element.getAttribute("gammaG").getValue();
		if(gammaG != null){
			this.gammaG = Double.parseDouble(gammaG);
		}
		String gammaB = element.getAttribute("gammaB").getValue();
		if(gammaB != null){
			this.gammaB = Double.parseDouble(gammaB);
		}
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
			program = InputComponents.getCLContext().createProgram(openGJTest.class.getResourceAsStream("kernelProgramms/gamma")).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		gpuReady = true;
		gpuIniting = false;
	}
	
	private CLKernel getKernel(){
		return program.createCLKernel("gamma");
	}
	
	private void shutdownGPU(){
		if(program != null){
			program.release();
		}
	}
	
	private VideoFrame calculateGammaCorrectionGPU(VideoFrame videoFrame){
		/*if((gammaR == 1) && (gammaG == 1) && (gammaB == 1)){
			return videoFrame;
		}*/
		if(!gpuReady){
			initGPU();
		}
		if(!gpuReady){
			return videoFrame;
		}

		float rCorr = (float) Math.pow(255.0, (gammaR-1)*-1);
		float gCorr = (float) Math.pow(255.0, (gammaG-1)*-1);
		float bCorr = (float) Math.pow(255.0, (gammaB-1)*-1);
		
		int pixelCount = videoFrame.getPixelCount();

		int localWorkSize = InputComponents.getLocalWorkSize();
		int globalWorkSize = InputComponents.getGlobalWorkSize(pixelCount);

		VideoFrame resultVideoFrame = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
		calculateOnGPU(videoFrame.getRedCLBuffer(), resultVideoFrame.getRedCLBuffer(), (float)gammaR, rCorr, 255, pixelCount, globalWorkSize, localWorkSize);

		calculateOnGPU(videoFrame.getGreenCLBuffer(), resultVideoFrame.getGreenCLBuffer(), (float)gammaG, gCorr, 255, pixelCount, globalWorkSize, localWorkSize);

		calculateOnGPU(videoFrame.getBlueCLBuffer(), resultVideoFrame.getBlueCLBuffer(), (float)gammaB, bCorr, 255, pixelCount, globalWorkSize, localWorkSize);
		
		return resultVideoFrame;
	}
	
	private synchronized void calculateOnGPU(CLBuffer<FloatBuffer> in, CLBuffer<FloatBuffer> out, float gamma, float corr, float maxCol, int pixcount, int globalWorkSize, int localWorkSize){
		CLKernel kernel = getKernel();
		kernel.putArg(in);
		kernel.putArg(out);
		kernel.putArg(gamma);
		kernel.putArg(corr);
		kernel.putArg(maxCol);
		kernel.putArg(pixcount);
		CLCommandQueue clCommandQueue = InputComponents.getCLCommandQueue();
		synchronized (clCommandQueue) {
			clCommandQueue.putWriteBuffer(in, false);
			clCommandQueue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize);
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
}
