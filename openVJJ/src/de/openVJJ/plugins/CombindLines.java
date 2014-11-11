/**
 * 
 */
package de.openVJJ.plugins;

import java.util.ArrayList;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Value;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.basic.Value.Lock;
import de.openVJJ.basic.Plugin;
import de.openVJJ.values.PointCloud;
import de.openVJJ.values.PointCloundList;

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
public class CombindLines extends Plugin {

	public CombindLines(){
		addInput("Lines 1", PointCloundList.class);
		addInput("Lines 2", PointCloundList.class);
		addOutput("Lines", PointCloundList.class);
	}
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}


	private PointCloundList lines1;
	private Lock lock1;
	private PointCloundList lines2;
	private Lock lock2;
	
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		if("Lines 1".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					if(lines1 != null){
						lines1.free(lock1);
					}
					lock1 = value.lock();
					lines1 = (PointCloundList) value;
					channelValueSet();
				}
				
				@Override
				protected void connectionShutdownCalled() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		if("Lines 2".equals(inpuName)){
			return new ConnectionListener(connection) {
				
				@Override
				protected void valueReceved(Value value) {
					if(lines2 != null){
						lines2.free(lock2);
					}
					lock2 = value.lock();
					lines2 = (PointCloundList) value;
					channelValueSet();
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
	
	private void channelValueSet(){
		if(lines1 == null || lines2 == null){
			return;
		}
		if(lines1.getValue() == null || lines2.getValue() == null){
			lines1.free(lock1);
			lines1 = null;
			lines2.free(lock2);
			lines2 = null;
			return;
		}
		
		ArrayList<PointCloud> combindedLines = new ArrayList<PointCloud>();
		ArrayList<PointCloud> lines2Copy = new ArrayList<PointCloud>(lines2.getValue());
		for(PointCloud line1 : lines1.getValue()){
			
			ArrayList<PointCloud> linesYMatch = new ArrayList<PointCloud>();
			for(PointCloud line : lines2Copy){
				if(line1.hasMatches(line)){
					linesYMatch.add(line);
				}
			}
			ArrayList<PointCloud> combidedMatch = new ArrayList<PointCloud>();
			for(PointCloud line : combindedLines){
				if(line1.hasMatches(line)){
					combidedMatch.add(line);
				}
			}
			
			if(linesYMatch.isEmpty() && combidedMatch.isEmpty()){
				continue;
			}
			for(PointCloud line : linesYMatch){
				line1.uniqeAdd(line);
				lines2Copy.remove(line);
			}
			
			for(PointCloud line : combidedMatch){
				line1.uniqeAdd(line);
				combindedLines.remove(line);
			}
			
			combindedLines.add(line1);
			
		}
		getConnection("Lines").transmitValue(new PointCloundList(combindedLines));
		
		if(lines1 != null && lock1 != null){
			lines1.free(lock1);
			lines1 = null;
		}
		if(lines2 != null && lock2 != null){
			lines2.free(lock2);
			lines2 = null;
		}
	}

}
