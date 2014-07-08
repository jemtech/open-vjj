/**
 * 
 */
package de.openVJJ.plugins;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.Integer2DArrayValue;
import de.openVJJ.values.PointCloud;
import de.openVJJ.values.PointCloundList;

/**
 * 
 * Copyright (C) 2014 Jan-Erik Matthies
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
 * 
 * @author Jan-Erik Matthies
 * 
 */
public class LineFromSorbel2DIntArray extends Plugin {
	
	/**
	 * 
	 */
	public LineFromSorbel2DIntArray() {
		addInput("Sorbel", Integer2DArrayValue.class);
		addOutput("Lines", PointCloundList.class);
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Sorbel".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Integer2DArrayValue sorbel = (Integer2DArrayValue) value;
					Lock lock = sorbel.lock();
					calculate(sorbel.getIngegerArray());
					sorbel.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean xDirection = true;
	private int lineLimit = 500;
	private int histery = 30;
	private boolean copyValue = true;
	
	private void calculate(int[][] sorbel){
		if(copyValue){
			sorbel = copySorbel(sorbel);
		}
		PointCloundList lines;
		if(xDirection){
			lines = processX(sorbel);
		}else{
			lines = processY(sorbel);
		}
		getConnection("Lines").transmitValue(lines);
	}
	
	private int[][] copySorbel(int [][] toCopy){
		int [][] myInt = new int[toCopy.length][];
		for(int i = 0; i < toCopy.length; i++)
		{
		  int[] aMatrix = toCopy[i];
		  int   aLength = aMatrix.length;
		  myInt[i] = new int[aLength];
		  System.arraycopy(aMatrix, 0, myInt[i], 0, aLength);
		}
		return myInt;
	}
	
	
	private PointCloundList processX(int[][] sorbel){
		
		int xMax = sorbel.length -1;
		int yMax = sorbel[0].length -1;
		ArrayList<PointCloud> lines = new ArrayList<PointCloud>();
		for(int cykle = 0; cykle < lineLimit ; cykle++){
			Point point = getStrongest(sorbel);
			if(point == null){
				break;
			}
			deactivatePoint(sorbel, point);
			ArrayList<Point> points = new ArrayList<Point>();
			points.add(point);
			PointCloud line = new PointCloud(points);
			lines.add(line);
			
			combinedLineU(point, sorbel, line, 0, 0, yMax);
			combinedLineD(point, sorbel, line, xMax, 0, yMax);
			
		}
		return new PointCloundList(lines);
	}
	

	
	private PointCloundList processY(int[][] sorbel){
		
		int xMax = sorbel.length -1;
		int yMax = sorbel[0].length -1;
		ArrayList<PointCloud> lines = new ArrayList<PointCloud>();
		for(int cykle = 0; cykle < lineLimit ; cykle++){
			Point point = getStrongest(sorbel);
			if(point == null){
				break;
			}
			deactivatePoint(sorbel, point);
			ArrayList<Point> points = new ArrayList<Point>();
			points.add(point);
			PointCloud line = new PointCloud(points);
			lines.add(line);

			combinedLineL(point, sorbel, line, 0, 0, xMax);
			combinedLineR(point, sorbel, line, yMax, 0, xMax);
			
		}
		return new PointCloundList(lines);
	}
	
	private final static int USED_POINT = -1;
	private void deactivatePoint(int[][] valueMatrix, Point point){
		valueMatrix[point.x][point.y] = USED_POINT;
	}
	
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
		if(ValMax<histery){
			return null;
		}
		return point;
	}
	
	private void combinedLineU(Point position, int[][] sorbel, PointCloud line, int xMin, int yMin, int yMax){
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
			line.getValue().add(next);
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
			if(last<histery){
				return;
			}
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
			if(last<histery){
				return;
			}
			thinlineR(watch, sorbel, yMax, last);
		}
	}
	
	private void combinedLineD(Point position, int[][] sorbel, PointCloud line, int xMax, int yMin, int yMax){
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
			line.getValue().add(next);
			thinlineL(next, sorbel, yMin, val);
			thinlineR(next, sorbel, yMax, val);
			combinedLineD(next, sorbel, line, xMax, yMin, yMax);
		}
	}

	private void combinedLineL(Point position, int[][] sorbel, PointCloud line, int yMin, int xMin, int xMax){
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
			line.getValue().add(next);
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
			if(last<histery){
				return;
			}
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
			if(last<histery){
				return;
			}
			thinlineD(watch, sorbel, xMax, last);
		}
	}

	private void combinedLineR(Point position, int[][] sorbel, PointCloud line, int yMax, int xMin, int xMax){
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
			line.getValue().add(next);
			thinlineU(next, sorbel, xMin, val);
			thinlineD(next, sorbel, xMax, val);
			combinedLineR(next, sorbel, line, yMax, xMin, xMax);
		}
		
	}

}
