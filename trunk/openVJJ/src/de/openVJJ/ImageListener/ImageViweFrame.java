package de.openVJJ.ImageListener;

import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.openVJJ.graphic.VideoFrame;
import de.openVJJ.imagePublisher.ImagePublisher;

public class ImageViweFrame implements ImageListener{
	JLabel oldLable = null;
	protected JFrame frame = null;
	ImagePublisher imagePublisher;
	protected JLabel camImage;
	protected int windowWidth = 800;
	protected int windowHeight= 600;
	
	
	public  ImageViweFrame(ImagePublisher imagePublisher){
		this.imagePublisher = imagePublisher;
		startWatching();
	}
	
	public  ImageViweFrame(ImagePublisher imagePublisher, int width, int height){
		this.imagePublisher = imagePublisher;
		windowWidth = width;
		windowHeight = height;
		startWatching();
	}
	
	public void startWatching(){
		frame = new JFrame();
		imagePublisher.addListener(this);
		frame.addWindowListener(new MyWindowListener(this, imagePublisher));
		frame.setBounds(0, 0, windowWidth, windowHeight);
		frame.setVisible(true);
		camImage = new JLabel();
		camImage.setVisible(true);
		frame.add(camImage);
	}
	
	int camImageH;
	int camImageW;
	public void newImageReceived(VideoFrame videoFrame) {
		if(videoFrame == null){
			return;
		}
		ImageIcon imageIcon = new ImageIcon(videoFrame.getImage());
		camImage.setIcon(imageIcon);
		if(camImageH != imageIcon.getIconHeight() || camImageW != imageIcon.getIconWidth()){
			camImageH = imageIcon.getIconHeight();
			camImageW = imageIcon.getIconWidth();
			frame.pack();
		}
	}
	public void newImageReceived(Image image) {
		if(image == null){
			return;
		}
		ImageIcon imageIcon = new ImageIcon(image);
		camImage.setIcon(imageIcon);
		if(camImageH != imageIcon.getIconHeight() || camImageW != imageIcon.getIconWidth()){
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
}
