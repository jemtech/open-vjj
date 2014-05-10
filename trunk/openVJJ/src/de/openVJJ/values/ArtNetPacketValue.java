/**
 * 
 */
package de.openVJJ.values;

import artnet4j.packets.ArtNetPacket;
import de.openVJJ.basic.Value;

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
public class ArtNetPacketValue extends Value {
	private ArtNetPacket artNetPacket;
	
	public ArtNetPacketValue(ArtNetPacket artNetPacket){
		this.artNetPacket = artNetPacket;
	}
	
	/**
	 * @return the artNetPacket
	 */
	public ArtNetPacket getArtNetPacket() {
		return artNetPacket;
	}
}
