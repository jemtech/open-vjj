package de.openVJJ.imagePublisher;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
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

public class MultiPlace extends ImagePublisher{
	List<Placement> placementList;
	int width = 640;
	int height = 480;
	int frameRate = 15;
	
	
	private void addPlacement(Placement placement){
		if(placementList == null){
			placementList = new ArrayList<MultiPlace.Placement>();
		}
		synchronized (placementList) {
			placementList.add(placement);
		}
	}
	

	public VideoFrame calculateImage(){
		if(placementList == null || placementList.isEmpty()){
			return null;
		}
		VideoFrame outFrame = new VideoFrame(width, height);
		
		for(int x =0; x<width; x++){
			for(int y=0; y<height; y++){
				for(Placement placement : placementList){
					int[] pixel = placement.getPixelAt(x, y);
					if(pixel != null){
						outFrame.setRGB(x, y, pixel);
					}
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
	
	JFrame controllerFrame;
	@Override
	public void openConfigPanel() {
		controllerFrame = new JFrame();
		controllerFrame.setTitle("MultiPlace");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		int lineNr = 0;
		if(placementList != null){
			for(Placement placement : placementList){
				JPanel jPanelPlacement = getPlacementLine(placement);
				gridBagConstraints.gridy = lineNr;
				controllerFrame.add(jPanelPlacement, gridBagConstraints);
				lineNr++;
			}
		}
		
		JButton addFrameButton = new JButton("add Frame");
		addFrameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Placement placement = new Placement();
				addPlacement(placement);
				selectToAddTo(placement);
			}
		});
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = lineNr;
		controllerFrame.add(addFrameButton, gridBagConstraints);

		controllerFrame.setVisible(true);
		controllerFrame.pack();
	}
	
	private JPanel getPlacementLine(Placement placement){
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		SpinnerNumberModel xSpinnerNumberModel = new SpinnerNumberModel(placement.x, 0, width, 1);
		JSpinner xSpinner = new JSpinner(xSpinnerNumberModel);
		xSpinner.addChangeListener(new PlacementChangeListener(placement) {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				placement.x = ((Number)((JSpinner)arg0.getSource()).getValue()).intValue();
			}
		});
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		jPanel.add(xSpinner, gridBagConstraints);
		
		SpinnerNumberModel ySpinnerNumberModel = new SpinnerNumberModel(placement.y, 0, height, 1);
		JSpinner ySpinner = new JSpinner(ySpinnerNumberModel);
		ySpinner.addChangeListener(new PlacementChangeListener(placement) {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				placement.y = ((Number)((JSpinner)arg0.getSource()).getValue()).intValue();
			}
		});
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		jPanel.add(ySpinner, gridBagConstraints);
		
		return jPanel;
	}
	
	abstract class PlacementChangeListener implements ChangeListener{
		Placement placement;
		public PlacementChangeListener(Placement placement){
			this.placement = placement;
		}
	}
	
	class Placement implements ImageListener{
		int x;
		int y;
		int width;
		int height;
		VideoFrame videoFrame;
		ImagePublisher imagePublisher;
		
		public Placement() {
			
		}
		
		public void setImagePublisher(ImagePublisher imagePublisher){
			removeFromImagePublisher();
			this.imagePublisher = imagePublisher;
			imagePublisher.addListener(this);
		}
		
		private void removeFromImagePublisher(){
			if(imagePublisher != null){
				imagePublisher.removeListener(this);
			}
			imagePublisher = null;
		}

		int[] getPixelAt(int x, int y){
			if(videoFrame == null){
				return null;
			}
			x = x-this.x;
			y = y-this.y;
			if(x < 0 || y < 0){
				return null;
			}
			if(x < videoFrame.getWidth() && y < videoFrame.getHeight()){
				return videoFrame.getRGB(x, y);
			}
			return null;
		}
		
		@Override
		public void openConfigPanel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void newImageReceived(VideoFrame videoFrame) {
			this.videoFrame = videoFrame;
			//this.videoFrame.transValue = new int[]{0,0,0};
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
		
	}
	
	JFrame selectFrameToAdd = null;
	
	private void selectToAddTo(Placement placement){
		selectFrameToAdd = new JFrame();
		ShowComponents showComponets = new ShowComponents(ShowComponents.MODUS_DISABLE_NOT_PUBLISHERS);
		showComponets.addShowComponetsListener(new MyShowComponetsListener(placement));
		selectFrameToAdd.add(showComponets);
		selectFrameToAdd.setVisible(true);
		selectFrameToAdd.pack();
	}
	
	@Override
	public void remove() {
		myOutGenerator.stop();
		shutdownListener();
		
	}
	
	private class MyShowComponetsListener implements ShowComponetsListener{
		Placement placement;
		public MyShowComponetsListener(Placement placement){
			this.placement = placement;
		}
		@Override
		public void componentClicked(VJJComponent vjjComponent) {
			if(ImagePublisher.class.isInstance(vjjComponent)){
				placement.setImagePublisher((ImagePublisher)vjjComponent);
				selectFrameToAdd.dispose();
				controllerFrame.dispose();
				openConfigPanel();
			}
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
}
