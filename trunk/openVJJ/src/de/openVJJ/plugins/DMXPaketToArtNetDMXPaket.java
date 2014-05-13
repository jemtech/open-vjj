/**
 * 
 */
package de.openVJJ.plugins;

import javax.swing.JPanel;

import artnet4j.packets.ArtDmxPacket;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.values.ArtNetDMXPaketValue;
import de.openVJJ.values.DMXPacketValue;
import de.openVJJ.values.IntValue;
import de.openVJJ.values.IntegerArrayImageValue;

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
public class DMXPaketToArtNetDMXPaket extends Plugin {

    private int sequenceID;
    private int subnetID;
    private int universeID;
    
    public DMXPaketToArtNetDMXPaket(){
    	addInput("Sequence ID", IntValue.class);
    	addInput("Subnet ID", IntValue.class);
    	addInput("Universe ID", IntValue.class);
    	addInput("DMX Data", DMXPacketValue.class);
    	
    	addOutput("ArtNet DMX", ArtNetDMXPaketValue.class);
    }
    
    private void setSequenceID(int sequenceID){
    	this.sequenceID = sequenceID;
    }
    
    private void setSubnetID(int subnetID){
    	this.subnetID = subnetID;
    }
    
    private void setUniverseID(int universeID){
    	this.universeID = universeID;
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
		if(inpuName == "Sequence ID"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntValue intValue = (IntValue) value;
					setSequenceID(intValue.getValue());
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}else if(inpuName == "Subnet ID"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntValue intValue = (IntValue) value;
					setSubnetID(intValue.getValue());
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
			
		}else if(inpuName == "Universe ID"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					IntValue intValue = (IntValue) value;
					setUniverseID(intValue.getValue());
					value.free(lock);
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
			
		}else if(inpuName == "DMX Data"){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					Lock lock = value.lock();
					DMXPacketValue dmxValue = (DMXPacketValue) value;
					sendDMXData(dmxValue.getDmxData());
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
	
	private void sendDMXData(byte[] dmxData){
		ArtDmxPacket artDmxPacket = new ArtDmxPacket();
		artDmxPacket.setDMX(dmxData, dmxData.length);
		artDmxPacket.setSequenceID(sequenceID);
		artDmxPacket.setSubnetID(subnetID);
		artDmxPacket.setUniverseID(universeID);
		ArtNetDMXPaketValue artNetDMXPaketValue = new ArtNetDMXPaketValue(artDmxPacket);
		getConnection("ArtNet DMX").transmitValue(artNetDMXPaketValue);
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
