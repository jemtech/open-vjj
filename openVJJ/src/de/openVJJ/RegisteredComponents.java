package de.openVJJ;

import java.util.ArrayList;
import java.util.List;

import de.openVJJ.ImageListener.ImageViweFrame;
import de.openVJJ.ImageListener.Recorder;
import de.openVJJ.imagePublisher.IPCam_250E_IGuard;
import de.openVJJ.imagePublisher.XuggleVideoFileInput;
import de.openVJJ.processor.EdgeBlender;
import de.openVJJ.processor.GammaCorrection;
import de.openVJJ.processor.LinearRGBCorrection;
import de.openVJJ.processor.Resulution;
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
	static List<Class> registeredComponents;
	static{
		registeredComponents = new ArrayList<Class>();
		registeredComponents.add(XuggleVideoFileInput.class);
		registeredComponents.add(IPCam_250E_IGuard.class);
		registeredComponents.add(ImageViweFrame.class);
		registeredComponents.add(Recorder.class);
		registeredComponents.add(EdgeBlender.class);
		registeredComponents.add(Resulution.class);
		registeredComponents.add(Warping.class);
		registeredComponents.add(GammaCorrection.class);
		registeredComponents.add(LinearRGBCorrection.class);
	}
	
	static public List<Class> getRegisteredComponents(){
		return registeredComponents;
	}
}
