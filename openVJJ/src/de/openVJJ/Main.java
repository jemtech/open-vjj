package de.openVJJ;


import de.openVJJ.controler.WarpingControl;
import de.openVJJ.imagePublisher.XuggleVideoFileInput;
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


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XuggleVideoFileInput fileInput = new XuggleVideoFileInput();
		Resulution resulution = new Resulution();
		fileInput.addListener(resulution);
		
		Warping warping = new Warping();
		warping.setWarp(new Warping.Point(0, 0), new Warping.Point(800, 0), new Warping.Point(800, 600),  new Warping.Point(0, 600));
		resulution.addListener(warping);
		new WarpingControl(warping);
		fileInput.fileChooser();

	}

}
