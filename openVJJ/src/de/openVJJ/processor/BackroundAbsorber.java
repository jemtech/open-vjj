package de.openVJJ.processor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.sql.rowset.spi.SyncResolver;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom2.Element;

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

public class BackroundAbsorber extends ImageProcessor {

	ImageChangeStack changeStack = null;
	
	public BackroundAbsorber(){
	}
	
	@Override
	public void getConfig(Element element) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConfig(Element element) {
		// TODO Auto-generated method stub

	}

	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(initing){
			return videoFrame;
		}
		checkStack(videoFrame);
		
		int[][][] diffs = analyseImage(videoFrame.getIntArray());
		VideoFrame outFrame = new VideoFrame(diffs.length, diffs[0].length);
		if(backround){
			outFrame.setIntArray(changeStack.tempImage);
		}else{
			outFrame.setIntArray(diffs);
		}
		
		return outFrame;
	}
	
	boolean reinit = false;
	boolean initing = false;
	private synchronized void checkStack(VideoFrame videoFrame){
		initing = true;
		if(changeStack == null || reinit){
			reinit = false;
			changeStack = new ImageChangeStack(videoFrame);
		}
		initing = false;
	}

	JFrame controllerFrame;
	boolean backround = false;
	@Override
	public void openConfigPanel() {
		controllerFrame = new JFrame();
		controllerFrame.setTitle("Backrund-Absorber");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel rLabel = new JLabel("Diff");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerFrame.add(rLabel, gridBagConstraints);
		
		JSlider rSlider = new JSlider();
		rSlider.setMinimum(0);
		rSlider.setMaximum(768);
		rSlider.setMajorTickSpacing(64);
		rSlider.setMinorTickSpacing(8);
		rSlider.setPaintTicks(true);
		rSlider.setValue(maxDiff);
		rSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				maxDiff = ((JSlider) arg0.getSource()).getValue();
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(rSlider, gridBagConstraints);
		
		JCheckBox chinButton = new JCheckBox("Backround");
	    chinButton.setSelected(backround);
	    chinButton.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				backround = ((JCheckBox) arg0.getSource()).isSelected();
			}
		});
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(chinButton, gridBagConstraints);
		
		JButton reinitButton = new JButton("reinit BG");
		reinitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				reinit = true;
			}
		});
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		controllerFrame.add(reinitButton, gridBagConstraints);

		controllerFrame.setVisible(true);
		controllerFrame.pack();
	}
	
	

	int maxDiff=10;
	public int[][][] analyseImage(int[][][] pixels){
		int xMax = changeStack.tempImage.length;
		int yMax = changeStack.tempImage[0].length;
		
		
		int[][][] changePixels = new int[xMax][yMax][3];
		for(int x = 0; x < xMax; x++){
			for(int y = 0; y < yMax; y++){
				int diff = 0;
				for(int c = 0; c < 3; c++){
					int diffTemp =  pixels[x][y][c] - changeStack.tempImage[x][y][c];
					if(diffTemp < 0){
						diff -= diffTemp;
					}else{
						diff += diffTemp;
					}
				}

				if(diff < maxDiff && diff > -1 * maxDiff){
					for(int c = 0; c < 3; c++){
						changeStack.tempImage[x][y][c] = (changeStack.tempImage[x][y][c] + pixels[x][y][c])/2;
						changePixels[x][y][c] = 100;
					}
				}else{
					if(changePixels == null){
						//changePixels = new int[xMax][yMax][3];
					}
					for(int c = 0; c < 3; c++){
						changePixels[x][y][c] = pixels[x][y][c];
					}
					
				}
			}
		}
		return changePixels;
	}
	
	public class ImageChangeStack{
		ArrayList<PartImage> layer;
		int[][][] tempImage;
		
		public ImageChangeStack(VideoFrame initVideoFrame) {
			//layer = new ArrayList<PartImage>();
			int xMax = initVideoFrame.getWidth();
			int yMax = initVideoFrame.getHeight();
			int[][][] frame = initVideoFrame.getIntArray();
			tempImage = new int[xMax][yMax][3];
			for(int x = 0; x < xMax; x++){
				for(int y = 0; y < yMax; y++){
					for(int c = 0; c < 3; c++){
						tempImage[x][y][c] = frame[x][y][c];
					}
				}
			}
		}
		
		public class PartImage{
			int xPos;
			int yPos;
			int[][][] pixels;
			
			public PartImage(int xPos, int yPos, int[][][] pixels){
				this.xPos = xPos;
				this.yPos = yPos;
				this.pixels = pixels;
			}
		}
	}

}
