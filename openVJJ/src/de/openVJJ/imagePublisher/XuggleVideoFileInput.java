package de.openVJJ.imagePublisher;

import java.awt.image.BufferedImage;

import javax.swing.JFileChooser;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;

import de.openVJJ.graphic.VideoFrame;

public class XuggleVideoFileInput extends ImagePublisher {
	String inputFileName;
	long framerate = 50;
	long framerateLimit = 50;
	float speed = 1f;
	
	public XuggleVideoFileInput() {
		openFilechooser();
	}
	private void openFilechooser(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					fileChooser();
				}
			}
		}).start();
	}
	
	public void setInputFileName(String inputFileName) {
		stopReading();
		this.inputFileName = inputFileName;
		startReading();
	}
	public void stopReading(){
		read = false;
		if(mediaReader != null){
			mediaReader.close();
			mediaReader = null;
		}
	}
	boolean read = true;
	IMediaReader mediaReader;
	public void startReading() {
		mediaReader = ToolFactory.makeReader(inputFileName);
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		mediaReader.addListener(new MyInputListener(this));
		read = true;
		while (read){
			if(mediaReader.readPacket() == null){
			}
			try {
				Thread.sleep((long)(((1000)/framerate)/speed));
			} catch (Exception e) {
			}
		}
	}
	JFileChooser chooser;
	private void fileChooser(){
		if(chooser == null){
			chooser = new JFileChooser();
		}
		chooser.showOpenDialog(null);
		setInputFileName(chooser.getSelectedFile().getPath());
		chooser = null;
	}

	private class MyInputListener extends MediaListenerAdapter {
		XuggleVideoFileInput xuggleVideoFileInput;
		long lastTimestamp =-1;
		int jumpt = 1;
		public MyInputListener(XuggleVideoFileInput xuggleVideoFileInput) {
			this.xuggleVideoFileInput = xuggleVideoFileInput;
		}
		@Override
		public void onVideoPicture(IVideoPictureEvent event) {
			long accTimestamp= event.getTimeStamp();
			if(lastTimestamp != -1){
				framerate = (long) (1/((accTimestamp - lastTimestamp) / 1000000f));
			}
			lastTimestamp = accTimestamp;
			if(framerate*speed > framerateLimit){
				int jump = (int) ((framerate*speed)/framerateLimit);
				if(jumpt<jump){
					jumpt++;
					return;
				}else{
					jumpt = 1;
				}
			}else{
				jumpt = 1;
			}
			VideoFrame videoFrame = new VideoFrame(event.getImage());
			xuggleVideoFileInput.publishImage(videoFrame);
		}
		
		
	}
	
}
