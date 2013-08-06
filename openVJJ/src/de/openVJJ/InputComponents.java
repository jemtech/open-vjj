package de.openVJJ;

import java.util.ArrayList;
import java.util.List;

import de.openVJJ.ImageListener.ImageListener;
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


public class InputComponents {
	static List<ImagePublisher> imagePublishers;
	
	public static void addComponent(ImagePublisher imagePublisher){
		if(imagePublishers == null){
			imagePublishers = new ArrayList<ImagePublisher>();
		}
		imagePublishers.add(imagePublisher);
	}
	
	public static void addComponent(ImageListener imageListener, ImagePublisher toAddTo){
		toAddTo.addListener(imageListener);
	}
	
	public static List<ImagePublisher> getImagePublisher(){
		return imagePublishers;
	}
	
	public static void reattach(ImageListener imageListener, ImagePublisher toAddTo){
		ImagePublisher oldImagePublisher = getPrevius(imageListener);
		System.out.println("Old publisher" + oldImagePublisher);
		oldImagePublisher.removeListener(imageListener);
		toAddTo.addListener(imageListener);
	}
	
	public static void remove(VJJComponent vjjComponent){
		if(imagePublishers == null || vjjComponent == null){
			return;
		}
		if(ImageListener.class.isInstance(vjjComponent)){
			System.out.println("To remove is a Listener");
			ImageListener imageListener = (ImageListener) vjjComponent;
			ImagePublisher imagePublisher = getPrevius(imageListener);
			if(imagePublisher != null){
				System.out.println("Found publisher " + imagePublisher + " remove listener");
				imagePublisher.removeListener(imageListener);
			}
		}else{
			synchronized (imagePublishers){
				System.out.println("To remove is a Publisher");
				if(imagePublishers.remove(vjjComponent)){
					System.out.println("Found publisher remove from list");
				}else{
					System.out.println("Could not remove publisher from list");
				}
			}
		}
		vjjComponent.remove();
	}
	
	public static ImagePublisher getPrevius(ImageListener imageListener){
		synchronized (imagePublishers){
			for(ImagePublisher imagePublisher : imagePublishers){
				ImagePublisher publisher = recursiveFindPreviusComponet(imagePublisher, imageListener);
				if(publisher != null){
					return publisher;
				}
			}
		}
		return null;
	}
	
	private static ImagePublisher recursiveFindPreviusComponet(ImagePublisher toSearchInimagePublisher, VJJComponent toFindePrevVjjComponent){
		List<ImageListener> listenerList = toSearchInimagePublisher.getImageListenerList();
		if(listenerList == null){
			return null;
		}
		for(ImageListener imageListener : listenerList ){
			if(imageListener == toFindePrevVjjComponent){
				System.out.println("found component " + imageListener +  "return its Publisher " + toSearchInimagePublisher);
				return toSearchInimagePublisher;
			}
			if(ImagePublisher.class.isInstance(imageListener)){
				ImagePublisher found =  recursiveFindPreviusComponet((ImagePublisher)imageListener, toFindePrevVjjComponent);
				if(found == null){
					continue;
				}else{
					return found;
				}
			}
		}
		return null;
	}
}
