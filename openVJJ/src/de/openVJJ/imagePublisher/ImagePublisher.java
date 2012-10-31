package de.openVJJ.imagePublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.openVJJ.ImageListener.ImageListener;
import de.openVJJ.graphic.VideoFrame;

public class ImagePublisher {
	private List<ImageListener> imageListener = null;
	
	synchronized protected List<ImageListener> getImageListener(){
		return imageListener;
	}
	
	synchronized public void addListener(ImageListener imageListener){
		if(this.imageListener == null){
			this.imageListener = new ArrayList<ImageListener>();
		}
		this.imageListener.add(imageListener);
	}
	
	synchronized public void removeListener(ImageListener imageListener) {
		if(this.imageListener != null){
			this.imageListener.remove(imageListener);
		}
	}
	
	ExecutorService executor = Executors.newCachedThreadPool();
	public void publishImage(VideoFrame videoFrame){
		if(imageListener == null){
			return;
		}
		synchronized (imageListener) {
			for(ImageListener imageListenerElement : imageListener){
				ListenerUpdater listenerUpdater = new ListenerUpdater(imageListenerElement, videoFrame);
				executor.execute(listenerUpdater);
			}
		}
	}
	
	private class ListenerUpdater implements Runnable{
		
		ImageListener imageListener;
		VideoFrame videoFrame;
		
		public ListenerUpdater(ImageListener imageListener, VideoFrame videoFrame) {
			this.imageListener = imageListener;
			this.videoFrame = videoFrame;
		}
		
		@Override
		public void run() {
			try{
				imageListener.newImageReceived(videoFrame);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
}
