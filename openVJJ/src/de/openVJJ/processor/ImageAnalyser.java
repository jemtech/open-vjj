package de.openVJJ.processor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

	private boolean showOriginal = true;
	private boolean showDetectedLines = true;
	@Override
	public VideoFrame processImage(VideoFrame videoFrame) {
		SorbelResult sorbelResult =  sorbel.calculateSorbelResult(videoFrame);
		ArrayList<Line> linesX = processX(sorbelResult);
		ArrayList<Line> linesY = processY(sorbelResult);
		VideoFrame out = new VideoFrame(videoFrame.getWidth(), videoFrame.getHeight());
		if(showOriginal){
			out.setIntArray(videoFrame.getIntArray());
		}
		if(showDetectedLines){
			paintLinesR(linesX, out);
			paintLinesB(linesY, out);
		}
		return out;
	}
	
	private void paintLinesR(ArrayList<Line> lines, VideoFrame out){
		for(Line line : lines){
			paintLineR(line, out);
		}
	}

	private void paintLinesB(ArrayList<Line> lines, VideoFrame out){
		for(Line line : lines){
			paintLineB(line, out);
		}
	}
	
	private int[] colorR = new int[]{255,0,0};
	private void paintLineR(Line line, VideoFrame out){
		for(Point point : line.points){
			out.setColor(point.x, point.y, colorR);
		}
	}

	private int[] colorB = new int[]{0,0,255};
	private void paintLineB(Line line, VideoFrame out){
		for(Point point : line.points){
			out.setColor(point.x, point.y, colorB);
		}
	}

	JFrame controllerFrame;
	@Override
	public void openConfigPanel() {
		controllerFrame = new JFrame();
		controllerFrame.setTitle("Analyser");
		controllerFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints =  new GridBagConstraints();
		
		JLabel rLabel = new JLabel("Histry");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		controllerFrame.add(rLabel, gridBagConstraints);
		
		JSlider rSlider = new JSlider();
		rSlider.setMinimum(0);
		rSlider.setMaximum(255*9);
		rSlider.setValue((int) (histery));
		rSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				histery = ((JSlider) arg0.getSource()).getValue();
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(rSlider, gridBagConstraints);
		
		JLabel gLabel = new JLabel("Limit lines");
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		controllerFrame.add(gLabel, gridBagConstraints);
		
		JSlider gSlider = new JSlider();
		gSlider.setMinimum(0);
		gSlider.setMaximum(1000);
		gSlider.setValue(lineLimit);
		gSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				lineLimit = ((JSlider) arg0.getSource()).getValue();
			}
		});
		gridBagConstraints.gridx = 1;
		controllerFrame.add(gSlider, gridBagConstraints);

		controllerFrame.setVisible(true);
		controllerFrame.pack();
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
	
	private int lineLimit = 500;
	
	private ArrayList<Line> processX(SorbelResult sorbelResult){
		
		int[][] combindetX = combindColors(sorbelResult.resultsPerChanelY);
		int xMax = combindetX.length -1;
		int yMax = combindetX[0].length -1;
		ArrayList<Line> lines = new ArrayList<Line>();
		for(int cykle = 0; cykle < lineLimit ; cykle++){
			Point point = getStrongest(combindetX);
			deactivatePoint(combindetX, point);
			Line line = new Line();
			line.addPoint(point);
			lines.add(line);
			
			combinedLineU(point, combindetX, line, 0, 0, yMax);
			combinedLineD(point, combindetX, line, xMax, 0, yMax);
			
		}
		return lines;
	}
	
	private ArrayList<Line> processY(SorbelResult sorbelResult){
		int[][] combindetY = combindColors(sorbelResult.resultsPerChanelX);
		int xMax = combindetY.length -1;
		int yMax = combindetY[0].length -1;
		ArrayList<Line> lines = new ArrayList<Line>();
		for(int cykle = 0; cykle < lineLimit ; cykle++){
			Point point = getStrongest(combindetY);
			deactivatePoint(combindetY, point);
			Line line = new Line();
			line.addPoint(point);
			lines.add(line);
			
			combinedLineL(point, combindetY, line, 0, 0, xMax);
			combinedLineR(point, combindetY, line, yMax, 0, xMax);
			
		}
		return lines;
	}
	
	private final static int USED_POINT = -1;
	private void deactivatePoint(int[][] valueMatrix, Point point){
		valueMatrix[point.x][point.y] = USED_POINT;
	}
	
	
	private int histery = 1000;
	
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
	
	private void combinedLineU(Point position, int[][] sorbel, Line line, int xMin, int yMin, int yMax){
		if(position.x <= xMin){
			return;
		}
		Point next = new Point();
		int x = position.x - 1;
		int y = position.y - 1;
		int val = USED_POINT;
		if( y >= yMin){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		y++;
		if(y >= yMin){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		y++;
		if(y <= yMax){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		if(val != USED_POINT && val > histery){
			deactivatePoint(sorbel, next);
			line.addPoint(next);
			thinlineL(next, sorbel, yMin, val);
			thinlineR(next, sorbel, yMax, val);
			combinedLineU(next, sorbel, line, xMin, yMin, yMax);
		}
	}
	
	private void thinlineL(Point position, int[][] sorbel, int yMin, int last){
		Point watch = new Point();
		watch.x = position.x;
		watch.y = position.y-1;
		if(watch.y <= yMin){
			return;
		}
		if(sorbel[watch.x][watch.y]<last){
			last = sorbel[watch.x][watch.y];
			sorbel[watch.x][watch.y] = USED_POINT;
			thinlineL(watch, sorbel, yMin, last);
		}
	}
	

	private void thinlineR(Point position, int[][] sorbel, int yMax, int last){
		Point watch = new Point();
		watch.x = position.x;
		watch.y = position.y+1;
		if(watch.y >= yMax){
			return;
		}
		if(sorbel[watch.x][watch.y]<last){
			last = sorbel[watch.x][watch.y];
			sorbel[watch.x][watch.y] = USED_POINT;
			thinlineR(watch, sorbel, yMax, last);
		}
	}
	
	private void combinedLineD(Point position, int[][] sorbel, Line line, int xMax, int yMin, int yMax){
		if(position.x >= xMax){
			return;
		}
		Point next = new Point();
		int x = position.x + 1;
		int y = position.y - 1;
		int val = USED_POINT;
		if( y >= yMin){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		y++;
		if(sorbel[x][y] > val){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		y++;
		if(y <= yMax){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		if(val != USED_POINT && val > histery){
			deactivatePoint(sorbel, next);
			line.addPoint(next);
			thinlineL(next, sorbel, yMin, val);
			thinlineR(next, sorbel, yMax, val);
			combinedLineD(next, sorbel, line, xMax, yMin, yMax);
		}
	}

	private void combinedLineL(Point position, int[][] sorbel, Line line, int yMin, int xMin, int xMax){
		if(position.y <= yMin){
			return;
		}
		Point next = new Point();
		int x = position.x - 1;
		int y = position.y - 1;
		int val = USED_POINT;
		if( x >= xMin){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		x++;
		if(sorbel[x][y] > val){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		x++;
		if(x <= xMax){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		if(val != USED_POINT && val > histery){
			deactivatePoint(sorbel, next);
			line.addPoint(next);
			thinlineU(next, sorbel, xMin, val);
			thinlineD(next, sorbel, xMax, val);
			combinedLineL(next, sorbel, line, yMin, xMin, xMax);
		}
		
	}
	
	private void thinlineU(Point position, int[][] sorbel, int xMin, int last){
		Point watch = new Point();
		watch.x = position.x-1;
		watch.y = position.y;
		if(watch.x <= xMin){
			return;
		}
		if(sorbel[watch.x][watch.y]<last){
			last = sorbel[watch.x][watch.y];
			sorbel[watch.x][watch.y] = USED_POINT;
			thinlineU(watch, sorbel, xMin, last);
		}
	}
	

	private void thinlineD(Point position, int[][] sorbel, int xMax, int last){
		Point watch = new Point();
		watch.x = position.x+1;
		watch.y = position.y;
		if(watch.x >= xMax){
			return;
		}
		if(sorbel[watch.x][watch.y]<last){
			last = sorbel[watch.x][watch.y];
			sorbel[watch.x][watch.y] = USED_POINT;
			thinlineD(watch, sorbel, xMax, last);
		}
	}

	private void combinedLineR(Point position, int[][] sorbel, Line line, int yMax, int xMin, int xMax){
		if(position.y >= yMax){
			return;
		}

		Point next = new Point();
		int x = position.x - 1;
		int y = position.y + 1;
		int val = USED_POINT;
		if( x >= xMin){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		x++;
		if(sorbel[x][y] > val){
			val = sorbel[x][y];
			next.x = x;
			next.y = y;
		}
		x++;
		if(x <= xMax){
			if(sorbel[x][y] > val){
				val = sorbel[x][y];
				next.x = x;
				next.y = y;
			}
		}
		if(val != USED_POINT && val > histery){
			deactivatePoint(sorbel, next);
			line.addPoint(next);
			thinlineU(next, sorbel, xMin, val);
			thinlineD(next, sorbel, xMax, val);
			combinedLineR(next, sorbel, line, yMax, xMin, xMax);
		}
		
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
