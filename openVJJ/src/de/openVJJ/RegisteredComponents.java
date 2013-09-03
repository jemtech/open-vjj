package de.openVJJ;

import java.util.ArrayList;
import java.util.List;

import de.openVJJ.ImageListener.ImageViewFrame;
import de.openVJJ.ImageListener.MJPEGServer;
import de.openVJJ.ImageListener.Recorder;
import de.openVJJ.imagePublisher.CrossFader;
import de.openVJJ.imagePublisher.IPCam_250E_IGuard;
import de.openVJJ.imagePublisher.MultiPlace;
import de.openVJJ.imagePublisher.XuggleVideoFileInput;
import de.openVJJ.processor.EdgeBlender;
import de.openVJJ.processor.GammaCorrection;
import de.openVJJ.processor.GaussFilter;
import de.openVJJ.processor.LinearRGBCorrection;
import de.openVJJ.processor.ObjectFromLine;
import de.openVJJ.processor.Resolution;
import de.openVJJ.processor.Sorbel;
import de.openVJJ.processor.Stroboscope;
import de.openVJJ.processor.Warping;


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


public class RegisteredComponents {
	static List<Class<? extends VJJComponent>> registeredComponents;
	static{
		registeredComponents = new ArrayList<Class<? extends VJJComponent>>();
		registeredComponents.add(XuggleVideoFileInput.class);
		registeredComponents.add(IPCam_250E_IGuard.class);
		registeredComponents.add(ImageViewFrame.class);
		registeredComponents.add(Recorder.class);
		registeredComponents.add(EdgeBlender.class);
		registeredComponents.add(Resolution.class);
		registeredComponents.add(Warping.class);
		registeredComponents.add(GammaCorrection.class);
		registeredComponents.add(LinearRGBCorrection.class);
		registeredComponents.add(Stroboscope.class);
		registeredComponents.add(CrossFader.class);
		registeredComponents.add(MultiPlace.class);
		registeredComponents.add(MJPEGServer.class);
		registeredComponents.add(Sorbel.class);
		registeredComponents.add(ObjectFromLine.class);
		registeredComponents.add(GaussFilter.class);
	}
	
	static public List<Class<? extends VJJComponent>> getRegisteredComponents(){
		return registeredComponents;
	}
}
