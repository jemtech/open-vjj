package de.openVJJ.graphic;

import java.util.ArrayList;
import java.util.List;

public class ComplexLine {
	
	LinePoint start;
	
	public void addPoint(LinePoint point){
		points.add(point);
	}
	
	public boolean contains(LinePoint point){
		return contains(start, point);
	}
	
	private static boolean contains(LinePoint list, LinePoint point){
		if(list.hadSearched){
			return false;
		}
		list.hadSearched = true;
		if(list.equals(point)){
			return true;
		}
		for(LinePoint neighbour : list.neighbours){
			if(contains(neighbour, point)){
				return true;
			}
		}
		return false;
	}
	
	/*
	public void uniqeAdd(ComplexLine line){
		for(Point point : line.points){
			uniqeAdd(point);
		}
	}
	*/
	
	public void uniqeAdd(LinePoint point){
		if(contains(point)){
			return;
		}
		addPoint(point);
	}
	
	public boolean crossing(ComplexLine line){
		boolean crossing = crossing(line.start);
		line.start.unsetSearched();
		return crossing;
	}
	
	public boolean crossing(LinePoint point){
		if(point.hadSearched){
			return false;
		}
		contains(point);
		point.hadSearched = true;
		for(LinePoint neighbour : point.neighbours){
			if(crossing(neighbour)){
				return true;
			}
		}
		return false;
	}
	
	
	private class LinePoint{
		int x;
		int y;
		
		boolean hadSearched = false;
		public void unsetSearched(){
			if(!hadSearched){
				return;
			}
			hadSearched = false;
			for(LinePoint neighbour : neighbours){
				neighbour.unsetSearched();
			}
		}
		
		List<LinePoint> neighbours = new ArrayList<LinePoint>();

		public boolean equals(LinePoint point) {
			return (x == point.x && y == point.y);
		}
		
		public boolean isNeighbour(LinePoint point) {
			for(int ys = -1; ys < 2; y++){
				for(int xs = -1; xs < 2; x++){
					if(x + xs == point.x && y + xs == point.y){
						return true;
					}
				}
			}
			return false;
		}
		
	}
}
