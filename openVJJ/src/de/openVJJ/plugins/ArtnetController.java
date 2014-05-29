/**
 * 
 */
package de.openVJJ.plugins;

import java.net.SocketException;

import javax.swing.JPanel;

import artnet4j.ArtNetException;
import artnet4j.ArtNetServer;
import artnet4j.events.ArtNetServerListener;
import artnet4j.packets.ArtNetPacket;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.ArtNetPacketValue;
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
public class ArtnetController extends Plugin {
	
	private static ArtNetServer artNetServer;
	
	public ArtnetController(){
		addOutput("Unicasted", ArtNetPacketValue.class);
		addOutput("Received", ArtNetPacketValue.class);
		addOutput("Broadcasted", ArtNetPacketValue.class);
		addInput("Broadcast", ArtNetPacketValue.class);
		addInput("Unicast", UnicastArtNetPacketValue.class);
		
		if(artNetServer == null){
			artNetServer = new ArtNetServer();
		}
		
		artNetServer.addListener(new ArtNetServerListener() {
			
			@Override
			public void artNetServerStopped(ArtNetServer arg0) {
				// not yet interesting
			}
			
			@Override
			public void artNetServerStarted(ArtNetServer arg0) {
				// not yet interesting
			}
			
			@Override
			public void artNetPacketUnicasted(ArtNetPacket arg0) {
				getConnection("Unicasted").transmitValue(new ArtNetPacketValue(arg0));
			}
			
			@Override
			public void artNetPacketReceived(ArtNetPacket arg0) {
				getConnection("Received").transmitValue(new ArtNetPacketValue(arg0));
			}
			
			@Override
			public void artNetPacketBroadcasted(ArtNetPacket arg0) {
				getConnection("Broadcasted").transmitValue(new ArtNetPacketValue(arg0));
			}
		});
		
		try {
			artNetServer.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArtNetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		if("Broadcast".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					ArtNetPacketValue artNetPacketValue = (ArtNetPacketValue) value;
					artNetServer.broadcastPacket(artNetPacketValue.getArtNetPacket());
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}else if("Unicast".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					UnicastArtNetPacketValue artNetPacketValue = (UnicastArtNetPacketValue) value;
					artNetServer.unicastPacket(artNetPacketValue.getArtNetPacket(), artNetPacketValue.getAddress());
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

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#getConfigPannel()
	 */
	@Override
	public JPanel getConfigPannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
