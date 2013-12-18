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
	
	/**
	 * 
	 * @param point
	 * @return matching neighbours of this list
	 */
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
		if(contains(point)){
			return true;
		}
		point.hadSearched = true;
		for(ComplexLinePoint neighbour : point.neighbours){
			if(crossing(neighbour)){
				return true;
			}
		}
		return false;
	}
	
	public boolean addLine(ComplexLine line){
		List<ComplexLinePoint> crossings = getCrossingPoints(line);
		if(crossings.size() < 1){
			return false;
		}
		addWithout(crossings.get(0), crossings);
		crossings.get(0).unsetSearched();
		return true;
	}
	
	private void addWithout(ComplexLinePoint point, List<ComplexLinePoint> without){
		if(point.hadSearched){
			return;
		}

		point.hadSearched = true;
		
		boolean thisNot = false;
		for(ComplexLinePoint notMe : without){
			if(point.equals(notMe)){
				thisNot = true;
				break;
			}
		}
		if(!thisNot){
			if(!addPoint(point)){
				System.err.println("not able to add");
			}
		}
		for(ComplexLinePoint neighbour : point.neighbours){
			addWithout(neighbour, without);
		}
	}
	
	public List<ComplexLinePoint> getCrossingPoints(ComplexLine line){
		List<ComplexLinePoint> matches = new ArrayList<ComplexLinePoint>();
		getCrossingPoints(line.start, matches);
		line.start.unsetSearched();
		return matches;
	}
	
	private void getCrossingPoints(ComplexLinePoint point, List<ComplexLinePoint> matches){
		if(point.hadSearched){
			return;
		}
		if(contains(point)){
			matches.add(point);
		}
		point.hadSearched = true;
		for(ComplexLinePoint neighbour : point.neighbours){
			getCrossingPoints(neighbour, matches);
		}
	}
}
