/**
 * 
 */
package de.openVJJ.plugins;

import java.net.InetAddress;

import javax.swing.JPanel;

import artnet4j.packets.ArtNetPacket;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.ArtNetPacketValue;
import de.openVJJ.values.InetAddressValue;
import de.openVJJ.values.UnicastArtNetPacketValue;

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
public class ArtNetPacketToUnicastArtNetPacket extends Plugin {

	public ArtNetPacketToUnicastArtNetPacket(){
		addInput("Adress", InetAddressValue.class);
		addInput("ArtNetPacket", ArtNetPacketValue.class);
		addOutput("Unicast", UnicastArtNetPacketValue.class);
	}
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	private InetAddress inetAddress;
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Adress".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					InetAddressValue adressValue = (InetAddressValue) value;
					inetAddress = adressValue.getInetAddress();
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}else if("ArtNetPacket".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					ArtNetPacketValue parketValue = (ArtNetPacketValue) value;
					createUnicast(parketValue.getArtNetPacket());
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		return null;
	}
	
	private void createUnicast(ArtNetPacket artNetPacket){
		getConnection("Unicast").transmitValue(new UnicastArtNetPacketValue(artNetPacket, inetAddress));
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
