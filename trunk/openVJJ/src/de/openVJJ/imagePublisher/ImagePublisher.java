package de.openVJJ.imagePublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.openVJJ.GPUComponent;
import de.openVJJ.VJJComponent;
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

public abstract class ImagePublisher extends GPUComponent implements VJJComponent{
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
			if(!this.imageListener.remove(imageListener)){
				System.err.println("could not find Listener to remove:" + imageListener);
				System.out.println("Listener List:");
				for(ImageListener listener : this.imageListener){
					System.out.println(listener);
					
				}
			}
		}
	}
	
	public List<ImageListener> getImageListenerList(){
		return imageListener;
	}
	
	protected void shutdownListener(){
		if(imageListener != null){
			synchronized (imageListener) {
				for(ImageListener listener : imageListener){
					listener.remove();
				}
				imageListener.clear();
				imageListener = null;
			}
		}
	}
	
	public void publishImage(VideoFrame videoFrame){
		publishImage(videoFrame, true);
	}
	
	ExecutorService executor = Executors.newCachedThreadPool();
	public void publishImage(VideoFrame videoFrame, boolean forceThread){
		if(imageListener == null){
			return;
		}
		synchronized (imageListener) {
	        if(imageListener.size()<1){
	          return;
	        }
	        if(imageListener.size()==1 && !forceThread){
	        	imageListener.get(0).newImageReceived(videoFrame);
	        	return;
		    }
	        Updater updater = new Updater(videoFrame);
	        executor.execute(updater);
	        /*
			for(ImageListener imageListenerElement : imageListener){
				ListenerUpdater listenerUpdater = new ListenerUpdater(imageListenerElement, videoFrame);
				executor.execute(listenerUpdater);
			}
			*/
		}
	}
	
	private class Updater implements Runnable{
		VideoFrame videoFrame;
		public Updater(VideoFrame videoFrame){
			this.videoFrame = videoFrame;
		}
		@Override
		public void run() {
			Collection<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
			
			for(ImageListener imageListenerElement : imageListener){
				ListenerUpdater listenerUpdater = new ListenerUpdater(imageListenerElement, videoFrame);
				tasks.add(Executors.callable(listenerUpdater));
			}
			try {
				for(Future<?> future : executor.invokeAll(tasks)){
					future.get();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
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

	@Override
	public abstract void openConfigPanel();
}
