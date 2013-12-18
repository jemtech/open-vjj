package de.openVJJ.graphic;

import java.util.ArrayList;
import java.util.List;


public class ComplexLinePoint {
	int x;
	int y;
	
	boolean hadSearched = false;
	public void unsetSearched(){
		if(!hadSearched){
			return;
		}
		hadSearched = false;
		for(ComplexLinePoint neighbour : neighbours){
			neighbour.unsetSearched();
		}
	}
	
	List<ComplexLinePoint> neighbours = new ArrayList<ComplexLinePoint>();
	
	private boolean addNeighbour(ComplexLinePoint newNeighbour){
		for(ComplexLinePoint neighbour : neighbours){
			if(neighbour.equals(newNeighbour)){
				return false;
			}
		}
		neighbours.add(newNeighbour);
		return true;
	}
	
	public static boolean link(ComplexLinePoint point1, ComplexLinePoint point2){
		if(point1.addNeighbour(point2)){
			if( ! point2.addNeighbour(point1)){
				point1.removeNeighbour(point2);
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean removeNeighbour(ComplexLinePoint neighbour){
		return neighbours.remove(neighbour);
	}

	public boolean equals(ComplexLinePoint point) {
		return (x == point.x && y == point.y);
	}
	
	public boolean isNeighbour(ComplexLinePoint point) {
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
