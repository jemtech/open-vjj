package de.openVJJ.graphic;

import java.util.ArrayList;
import java.util.List;


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
