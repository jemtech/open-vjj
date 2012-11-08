package de.openVJJ.processor;

import de.openVJJ.graphic.VideoFrame;

public class Stroboscope extends ImageProcessor {

	private VideoFrame strobFrame;
	private int modus = 0;
	public final static int MODUS_BLACK = 0;
	public final static int MODUS_WHITE = 1;
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		if(videoFrame == null){
			return null;
		}
		return strobeFrame(videoFrame);
	}
	
	private int lastHeight = 0;
	private int lastWidth = 0;
	private boolean lastFrameStrob = false;
	public VideoFrame strobeFrame(VideoFrame videoFrame){
		if(lastHeight != videoFrame.getHeight() || lastWidth != videoFrame.getWidth()){
			lastHeight = videoFrame.getHeight();
			lastWidth = videoFrame.getWidth();
		}
		if(lastFrameStrob){
			lastFrameStrob = false;
			return videoFrame;
		}else{
			lastFrameStrob = true;
			if(strobFrame == null){
				setStrobFrame();
			}
			return strobFrame;
		}
	}
	
	private void setStrobFrame(){
		strobFrame = new VideoFrame(lastWidth, lastWidth);
		switch(modus){
			case MODUS_BLACK:{
				return;
			}
			case MODUS_WHITE:{
				for(int x = 0; x<lastWidth; x++){
					for(int y = 0; y<lastHeight;y++){
						strobFrame.setColor(x, y, 255, 255, 255);
					}
				}
			}
		}
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub

	}

}
