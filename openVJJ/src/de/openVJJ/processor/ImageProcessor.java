package de.openVJJ.processor;

import de.openVJJ.VJJComponent;
import de.openVJJ.ImageListener.ImageListener;
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

public abstract class ImageProcessor extends ImagePublisher implements ImageListener, VJJComponent {
	
	@Override
	public void newImageReceived(VideoFrame videoFrame) {
		publishImage(processImage(videoFrame));
	}
	
	public abstract VideoFrame processImage(VideoFrame videoFrame);
	

	@Override
	public void remove() {
		shutdownListener();
	}
}
