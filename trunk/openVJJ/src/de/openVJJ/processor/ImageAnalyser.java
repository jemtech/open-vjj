package de.openVJJ.processor;

import org.jdom2.Element;

import de.openVJJ.graphic.VideoFrame;
import de.openVJJ.processor.Sorbel.SorbelResult;

/*
 * Copyright (C) 2013  Jan-Erik Matthies
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

public class ImageAnalyser extends ImageProcessor {

	Sorbel sorbel = new Sorbel();
	
	@Override
	public void getConfig(Element element) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConfig(Element element) {
		// TODO Auto-generated method stub

	}

	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		SorbelResult sorbelResult =  sorbel.calculateSorbelResult(videoFrame);
		return null;
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub

	}

}
