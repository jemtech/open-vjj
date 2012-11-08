package de.openVJJ.ImageListener;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.openVJJ.graphic.VideoFrame;
import de.openVJJ.imagePublisher.ImagePublisher;

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

public class ImageViweFrame implements ImageListener{
	JLabel oldLable = null;
	protected MyFrame frame = null;
	ImagePublisher imagePublisher;
	protected JLabel camImage;
	protected int windowWidth = 800;
	protected int windowHeight= 600;
	protected boolean sizeByFrame = true;
	protected boolean onClickToggelFullscreen = true;
	protected boolean deactivateWindowClose = true;
	
	public ImageViweFrame(){
	}
	
	public  ImageViweFrame(ImagePublisher imagePublisher){
		this.imagePublisher = imagePublisher;
	}
	
	public  ImageViweFrame(ImagePublisher imagePublisher, int width, int height){
		this.imagePublisher = imagePublisher;
		windowWidth = width;
		windowHeight = height;
	}
	boolean starting = false;
	public void startWatching(){
		starting = true;
		frame = new MyFrame();
		if(deactivateWindowClose){
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
		if(imagePublisher != null){
			imagePublisher.addListener(this);
			frame.addWindowListener(new MyWindowListener(this, imagePublisher));
		}

		if(onClickToggelFullscreen){
		frame.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
					toggleFullSceen();
			}
		});
		}
		frame.setBounds(0, 0, windowWidth, windowHeight);
		frame.setVisible(true);
		camImage = new JLabel();
		camImage.setVisible(true);
		frame.add(camImage);
		starting = false;
	}
	boolean fullScreen = false;
	Rectangle oldBounds;
	public void toggleFullSceen(){
		if(fullScreen){
			toWindowScreen();
		}else{
			toFullScreen();
		}
		fullScreen = !fullScreen;
	}
	
	public void toWindowScreen(){
		frame.dispose();
		frame.setExtendedState(JFrame.NORMAL);
		frame.setUndecorated(false);
		frame.setBounds(oldBounds);
		frame.setVisible(true);
	}
	
	public void toFullScreen(){
		oldBounds = frame.getBounds();
		frame.setVisible(false);
		frame.dispose();
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	
	
	int camImageH;
	int camImageW;
	public void newImageReceived(VideoFrame videoFrame) {
		if(videoFrame == null){
			return;
		}
		newImageReceived(videoFrame.getImage());
	}
	
	GraphicsConfiguration gConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	public void newImageReceived(Image image) {
		if(image == null){
			return;
		}
		if(frame == null){
			if(starting){
				return;
			}
			startWatching();
		}
		if(sizeByFrame){
			if(BufferedImage.class.isInstance(image)){
				image = ((BufferedImage)image).getScaledInstance(windowWidth, windowHeight, BufferedImage.SCALE_DEFAULT);
			}else{
				BufferedImage bufferedImage = gConfiguration.createCompatibleImage(windowWidth, windowHeight);
				Graphics2D g = bufferedImage.createGraphics();
				g.drawImage(image, 0, 0, windowWidth, windowHeight, null);
				g.dispose();
			}
		}
		ImageIcon imageIcon = new ImageIcon(image);
		camImage.setIcon(imageIcon);
		if(!sizeByFrame && (camImageH != imageIcon.getIconHeight() || camImageW != imageIcon.getIconWidth())){
			camImageH = imageIcon.getIconHeight();
			camImageW = imageIcon.getIconWidth();
			frame.pack();
		}
	}
	
	private class MyWindowListener implements WindowListener{
		ImagePublisher imagePublisher;
		ImageListener imageListener;
		public MyWindowListener(ImageListener imageListener, ImagePublisher imagePublisher){
			this.imagePublisher = imagePublisher;
			this.imageListener = imageListener;
		}
		@Override
		public void windowActivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosed(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosing(WindowEvent e) {
			// TODO Auto-generated method stub
			imagePublisher.removeListener(imageListener);
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowIconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowOpened(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public void openConfigPanel() {
		
	}
	
	public class MyFrame extends JFrame{
		@Override
		public void validate() {
			super.validate();
			windowWidth = getWidth();
			windowHeight = getHeight();
		}
	}
}
