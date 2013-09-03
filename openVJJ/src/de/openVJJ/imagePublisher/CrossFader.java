package de.openVJJ.imagePublisher;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom2.Element;

import de.openVJJ.VJJComponent;
import de.openVJJ.GUI.ShowComponents;
import de.openVJJ.GUI.ShowComponents.ShowComponetsListener;
import de.openVJJ.ImageListener.ImageListener;
import de.openVJJ.graphic.VideoFrame;


/*
 * Copyright (C) 2012  Jan-Erik Matthies
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


public class CrossFader extends ImagePublisher {
	
	int frameRate = 15;
	VideoFrame frameA;
	VideoFrame frameB;
	double fade = 0.5;
	
	int outHeight = 480;
	int outWidth = 640;
	
	boolean fitAToOut = true;
	boolean fitBToOut = true;
	private VideoFrame calculateImage(){
		double partA = 1-fade;
		double partB = fade;
		if(frameA == null){
			partA = 0;
		}
		if(frameB == null){
			partB = 0;
		}
		VideoFrame outFrame = new VideoFrame(outWidth, outHeight);
		
		int xMax = outWidth;
		int yMax = outHeight;
		int xAMax = (partA >0)?frameA.getWidth() : 0;
		int yAMax = (partA >0)?frameA.getHeight(): 0;
		int xBMax = (partB >0)?frameB.getWidth() : 0;
		int yBMax = (partB >0)?frameB.getHeight(): 0;
		
		float xAScale = 1;
		float yAScale = 1;
		if(fitAToOut && xAMax != 0 && yAMax != 0){
			xAScale = xAMax/(float)outWidth;
			yAScale = yAMax/(float)outHeight;
			xAMax = outWidth;
			yAMax = outHeight;
		}
		
		float xBScale = 1;
		float yBScale = 1;
		if(fitBToOut && xBMax != 0 && yBMax != 0){
			xBScale = xBMax/(float)outWidth;
			yBScale = yBMax/(float)outHeight;
			xBMax = outWidth;
			yBMax = outHeight;
		}
		
		for(int x = 0; x < xMax; x++){
			for(int y = 0; y < yMax; y++){
				if(x<xAMax && x<xBMax && y<yAMax && y<yBMax ){
					int[] rgbA = frameA.getRGB((int) (x*xAScale),(int) (y*yAScale));
					int[] rgbB = frameB.getRGB((int) (x*xBScale),(int) (y*yBScale));
					outFrame.setColor(x, y, (int) ((rgbA[0]*partA)+(rgbB[0]*partB)), (int) ((rgbA[1]*partA)+(rgbB[1]*partB)), (int) ((rgbA[2]*partA)+(rgbB[2]*partB)));
					continue;
				}
				if(x<xAMax && y<yAMax){
					int[] rgbA = frameA.getRGB((int) (x*xAScale),(int) (y*yAScale));
					outFrame.setColor(x, y, (int) (rgbA[0]*partA), (int) (rgbA[1]*partA), (int) (rgbA[2]*partA));
					continue;
				}
				if(x<xBMax && y<yBMax){
					int[] rgbB = frameB.getRGB((int) (x*xBScale),(int) (y*yBScale));
					outFrame.setColor(x, y, (int) (rgbB[0]*partB), (int) (rgbB[1]*partB), (int) (rgbB[2]*partB));
				}
				
			}
		}
		return outFrame;
	}
	
	OutGenerator myOutGenerator;
	@Override
	public synchronized void addListener(ImageListener imageListener) {
		super.addListener(imageListener);
		if(myOutGenerator == null){
			myOutGenerator = new OutGenerator();
			(new Thread(myOutGenerator)).start();
		}
	}
	
	@Override
	public synchronized void removeListener(ImageListener imageListener) {
		super.removeListener(imageListener);
		if(getImageListener().size()<1){
			myOutGenerator.stop();
		}
	}
	
	
	JFrame controllerFrame;
	@Override
	public void openConfigPanel() {
		controllerFrame = new JFrame();
		controllerFrame.setTitle("Crossfade");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JSlider rSlider = new JSlider();
		rSlider.setMinimum(0);
		rSlider.setMaximum(300);
		rSlider.setMajorTickSpacing(150);
		rSlider.setMinorTickSpacing(25);
		rSlider.setPaintTicks(true);
		rSlider.setValue((int) (300*fade));
		rSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				fade = ((JSlider) arg0.getSource()).getValue()/300.0;
			}
		});
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 2;
		controllerFrame.add(rSlider, gridBagConstraints);
		
		gridBagConstraints.gridwidth = 1;
		
		JButton channelAButton = new JButton("set A");
		channelAButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectToAddTo(CHANNEL_A);
			}
		});
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(channelAButton, gridBagConstraints);

		JButton channelBButton = new JButton("set B");
		channelBButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectToAddTo(CHANNEL_B);
			}
		});
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(channelBButton, gridBagConstraints);
		
		controllerFrame.setVisible(true);
		controllerFrame.pack();
	}
	
	private class OutGenerator implements Runnable{
		private boolean run = true;
		public void stop(){
			run = false;
		}
		@Override
		public void run() {
			while(run){
				long start = System.currentTimeMillis();
				publishImage(calculateImage());
				long waitTime = (1000/frameRate) - (System.currentTimeMillis() - start);
				if(waitTime > 0){
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	ImagePublisher inputA;
	ImageListener inputAListener;
	public void setInputA(ImagePublisher imagePublisher){
		if(inputA != null){
			inputA.removeListener(inputAListener);
		}
		inputA = imagePublisher;
		if(inputA != null){
			inputA.addListener(new ImageListener() {
				
				@Override
				public void openConfigPanel() {
				}
				
				@Override
				public void newImageReceived(VideoFrame videoFrame) {
					frameA = videoFrame;
					
				}

				@Override
				public void remove() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void getConfig(Element element) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void setConfig(Element element) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	
	ImagePublisher inputB;
	ImageListener inputBListener;
	public void setInputB(ImagePublisher imagePublisher){
		if(inputB != null){
			inputB.removeListener(inputBListener);
		}
		inputB = imagePublisher;
		if(inputB != null){
			inputB.addListener(new ImageListener() {
				
				@Override
				public void openConfigPanel() {
				}
				
				@Override
				public void newImageReceived(VideoFrame videoFrame) {
					frameB = videoFrame;
					
				}

				@Override
				public void remove() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void getConfig(Element element) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void setConfig(Element element) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	
	JFrame selectToAddToFrame = null;
	static final int CHANNEL_A = 0;
	static final int CHANNEL_B = 1;
	
	private void selectToAddTo(int channel){
		selectToAddToFrame = new JFrame();
		ShowComponents showComponets = new ShowComponents(ShowComponents.MODUS_DISABLE_NOT_PUBLISHERS);
		showComponets.addShowComponetsListener(new MyShowComponetsListener(channel));
		selectToAddToFrame.add(showComponets);
		selectToAddToFrame.setVisible(true);
		selectToAddToFrame.pack();
	}
	
	private class MyShowComponetsListener implements ShowComponetsListener{
		int channel;
		public MyShowComponetsListener(int channel){
			this.channel = channel;
		}
		@Override
		public void componentClicked(VJJComponent vjjComponent) {
			if(ImagePublisher.class.isInstance(vjjComponent)){
				switch (channel) {
				case CHANNEL_A:
					setInputA((ImagePublisher) vjjComponent);
					break;
				case CHANNEL_B:
					setInputB((ImagePublisher) vjjComponent);
					break;
				}
				selectToAddToFrame.dispose();
			}
		}
		
	}
	
	@Override
	public void remove() {
		setInputA(null);
		setInputB(null);
		myOutGenerator.stop();
		shutdownListener();
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
