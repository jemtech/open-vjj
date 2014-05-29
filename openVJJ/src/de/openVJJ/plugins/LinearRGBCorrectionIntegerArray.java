package de.openVJJ.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.IntegerArrayImageValue;

public class LinearRGBCorrectionIntegerArray extends Plugin {

	public LinearRGBCorrectionIntegerArray(){
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
		if("Frame".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntegerArrayImageValue frame = (IntegerArrayImageValue) value;
					processFrame(frame.getImageArray());
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}
	
	public void processFrame(int[][][] frame){
		getConnection("Frame").transmitValue(new IntegerArrayImageValue(calculateLinearRGBCorrection(frame)));
	}


	static final double mul1 = 64.0;
	@Override
	public JPanel getConfigPannel() {

		JPanel controllerPanel = new JPanel();
		controllerPanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel rLabel = new JLabel("Red");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerPanel.add(rLabel, gridBagConstraints);
		
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
		controllerPanel.add(rSlider, gridBagConstraints);
		
		JLabel gLabel = new JLabel("Green");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerPanel.add(gLabel, gridBagConstraints);
		
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
		controllerPanel.add(gSlider, gridBagConstraints);
		
		
		JLabel bLabel = new JLabel("Blue");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		controllerPanel.add(bLabel, gridBagConstraints);
		
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
		controllerPanel.add(bSlider, gridBagConstraints);
		
		controllerPanel.setVisible(true);
		return controllerPanel;
	}
	
	double mulR = 1;
	double mulG = 1;
	double mulB = 1;
	
	private int[][][] calculateLinearRGBCorrection(int[][][] videoFrame){
		if((mulR == 1) && (mulG == 1) && (mulB == 1)){
			return videoFrame;
		}
		int xMax = videoFrame.length;
		int yMax = videoFrame[0].length;
		int[][][] resultVideoFrame = new int[xMax][yMax][3];
		for(int x=0; x<xMax;x++){
			for(int y=0;y<yMax;y++){
				int[] rgb = videoFrame[x][y];
				int[] rgbNew = resultVideoFrame[x][y];
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

}
