/**
 * 
 */
package de.openVJJ.values;

/**
 * 
 * Copyright (C) 2015 Jan-Erik Matthies
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
public class DataPoint2D<T> extends DataPoint<T> {
	private int y;
	
	public DataPoint2D(int x, int y, T data){
		super(x, data);
		this.y = y;
	}

	public int getYPosition(){
		return y;
	}
	
	public boolean isSamePosition(DataPoint2D<?> dataPoint) {
		return super.isSamePosition(dataPoint) && dataPoint.y == y;
	}
}
