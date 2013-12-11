package de.openVJJ.processor;

import java.util.ArrayList;

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
		int[][] combindetX = combindColors(sorbelResult.resultsPerChanelX);
		Point point = getStrongest(combindetX);
		
		for(int cykle = 0; cykle < 5 ; cykle++){
			
		}
		int[][] combindetY = combindColors(sorbelResult.resultsPerChanelY);
		return null;
	}

	@Override
	public void openConfigPanel() {
		// TODO Auto-generated method stub

	}
	
	private int[][] combindColors(ArrayList<int[][]> resultsPerChanel){
		int index = 0;
		int chanelCount = resultsPerChanel.size();
		int[][] chanelResult = resultsPerChanel.get(index);
		int[][] combind = chanelResult;
		index++;
		int xMax = combind.length;
		int yMax = combind[0].length;
		while(index < chanelCount){
			chanelResult = resultsPerChanel.get(index);
			for(int x = 0; x < xMax; x++){
				for(int y = 0; y < yMax; y++){
					combind[x][y] += chanelResult[x][y];
				}
			}
			index++;
		}
		return combind;
	}
	/*
	private void processX(){
		
	}
	
	private void processY(){
		
	}*/
	
	private Point getStrongest(int[][] sorbelresult){
		Point point = new Point();
		int ValMax = -1;
		
		int xMax = sorbelresult.length;
		int yMax = sorbelresult[0].length;
		for(int x = 0; x < xMax; x++){
			for(int y = 0; y < yMax; y++){
				int res = sorbelresult[x][y];
				if(res > ValMax){
					ValMax = res;
					point.x = x;
					point.y = y;
				}
			}
		}
		return point;
	}
	
	private void combinedLineU(Point position, int dirction, int[][] sorbel){
		if(position.x == 0){
			return;
		}
	}
	
	private void combinedLineD(Point position, int dirction){
		
	}

	private void combinedLineL(Point position, int dirction){
		
	}

	private void combinedLineR(Point position, int dirction){
		
	}
	
	private class Line{
		ArrayList<Point> points = new ArrayList<Point>();
		
		public void addPoint(Point point){
			points.add(point);
		}
	}
	private class Point{
		int x;
		int y;
	}

}
