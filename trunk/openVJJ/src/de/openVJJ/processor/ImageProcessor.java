package de.openVJJ.processor;

import java.awt.Image;

import de.openVJJ.ImageListener.ImageListener;
import de.openVJJ.graphic.VideoFrame;
import de.openVJJ.imagePublisher.ImagePublisher;

public abstract class ImageProcessor extends ImagePublisher implements ImageListener {
	
	@Override
	public void newImageReceived(VideoFrame videoFrame) {
		publishImage(processImage(videoFrame));
	}
	
	public abstract VideoFrame processImage(VideoFrame videoFrame);
}
