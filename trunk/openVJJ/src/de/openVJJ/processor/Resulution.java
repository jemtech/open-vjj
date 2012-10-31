package de.openVJJ.processor;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

import de.openVJJ.graphic.VideoFrame;

public class Resulution extends ImageProcessor {
	int width = 1080;
	int height = 800;

	private GraphicsConfiguration gConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(videoFrame == null){
			return null;
		}
		videoFrame.scaleTo(width, height);
		return videoFrame;
	}

}
