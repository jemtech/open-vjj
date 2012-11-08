package de.openVJJ.processor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.openVJJ.graphic.VideoFrame;

public class GammaCorrection extends ImageProcessor {
	double gammaR = 1;
	double gammaG = 1;
	double gammaB = 1;
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(videoFrame == null){
			return null;
		}
		return calculateGammaCorrection(videoFrame);
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

}
