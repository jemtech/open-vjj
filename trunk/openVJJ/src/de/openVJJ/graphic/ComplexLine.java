package de.openVJJ.graphic;

import java.util.ArrayList;
import java.util.List;

public class ComplexLine {
	
	private ComplexLinePoint start;
	
	private boolean addPoint(ComplexLinePoint point){
		List<ComplexLinePoint> neighbours = searchNeighbour(point);
		if(neighbours.size()<1){
			return false;
		}
		for(ComplexLinePoint neighbour : neighbours){
			if(!ComplexLinePoint.link(point, neighbour)){
				System.err.println("Dublicate neighbour: " + point.x + " " + point.y);
			}
		}
		return true;
	}
	
	public List<ComplexLinePoint> searchNeighbour(ComplexLinePoint point){
		List<ComplexLinePoint> neighbours = new ArrayList<ComplexLinePoint>();
		getNeighbour(start, point, neighbours);
		start.unsetSearched();
		return neighbours;
	}
	
	private static void getNeighbour(ComplexLinePoint list, ComplexLinePoint point, List<ComplexLinePoint> neighbours){
		if(list.hadSearched){
			return;
		}
		list.hadSearched = true;
		if(list.isNeighbour(point)){
			neighbours.add(list);
		}
		for(ComplexLinePoint neighbour : list.neighbours){
			getNeighbour(neighbour, point, neighbours);
		}
	}
	
	public boolean contains(ComplexLinePoint point){
		boolean contains = contains(start, point);
		start.unsetSearched();
		return contains;
	}
	
	private static boolean contains(ComplexLinePoint list, ComplexLinePoint point){
		if(list.hadSearched){
			return false;
		}
		list.hadSearched = true;
		if(list.equals(point)){
			return true;
		}
		for(ComplexLinePoint neighbour : list.neighbours){
			if(contains(neighbour, point)){
				return true;
			}
		}
		return false;
	}
	
	public boolean uniqeAdd(ComplexLinePoint point){
		if(contains(point)){
			return false;
		}
		addPoint(point);
		return true;
	}
	
	public boolean crossing(ComplexLine line){
		boolean crossing = crossing(line.start);
		line.start.unsetSearched();
		return crossing;
	}
	
	private boolean crossing(ComplexLinePoint point){
		if(point.hadSearched){
			return false;
		}
		contains(point);
		point.hadSearched = true;
		for(ComplexLinePoint neighbour : point.neighbours){
			if(crossing(neighbour)){
				return true;
			}
		}
		return false;
	}
	
	
	
}
