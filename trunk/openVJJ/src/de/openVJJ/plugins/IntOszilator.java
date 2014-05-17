/**
 * 
 */
package de.openVJJ.plugins;

import javax.swing.JPanel;

import de.openVJJ.basic.Connection;
import de.openVJJ.basic.Plugin;
import de.openVJJ.basic.Connection.ConnectionListener;
import de.openVJJ.values.IntValue;

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
public class IntOszilator extends Plugin {
	
	public final static int OSZILATOR_TYPE_SIN = 1;
	public final static int OSZILATOR_TYPE_RAMP_UP = 2;
	public final static int OSZILATOR_TYPE_RAMP_DOWN = 3;
	public final static int OSZILATOR_TYPE_TRIANGLE = 4;
	
	private int refreshMillS = 20;
	private int frequency = 1;
	private int minVal = 0;
	private int maxVal = 255;
	private int oszilatorType = 1;
	
	public IntOszilator(){
		addOutput("Oszilation", IntValue.class);
		start();
	}

	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugin#sendStatics()
	 */
	@Override
	public void sendStatics() {
		// TODO Auto-generated method stub

	}

	private int getSteps(){
		int waveLength = 1000 / frequency;
		return waveLength / refreshMillS;
	}
	
	int stepCount = 0;
	private void step(){
		Integer result = null;
		int totalSteps = getSteps();
		stepCount %= totalSteps;
		switch (oszilatorType) {
		case OSZILATOR_TYPE_SIN:{
			result = (int) (minVal + (Math.sin((Math.PI * stepCount)/ totalSteps) * (maxVal - minVal)));
			break;
		}
		case OSZILATOR_TYPE_RAMP_UP:{
			result = minVal + (((maxVal - minVal) * stepCount)/totalSteps );
			break;
		}
		case OSZILATOR_TYPE_RAMP_DOWN:{
			result = minVal + (((maxVal - minVal) * (totalSteps - stepCount))/totalSteps );
			break;
		}
		case OSZILATOR_TYPE_TRIANGLE:{
			int middel = totalSteps/2;
			if(stepCount < middel){
				result = minVal + (((maxVal - minVal) * stepCount)/middel );
			}else{
				result = minVal + (((maxVal - minVal) * (totalSteps - stepCount))/middel );
			}
			break;
		}
		default:
			break;
		}
		
		getConnection("Oszilation").transmitValue(new IntValue(result));
		stepCount ++;
	}
	
	boolean run = false;
	
	private void start(){
		if(run){
			System.out.println("already oszilating");
			return;
		}
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				run = true;
				while(run){
					step();
					try {
						Thread.sleep(refreshMillS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}
	
	private void stop(){
		run = false;
	}
	
	/* (non-Javadoc)
	 * @see de.openVJJ.basic.Plugable#createConnectionListener(java.lang.String, de.openVJJ.basic.Connection)
	 */
	@Override
	protected ConnectionListener createConnectionListener(String inpuName,
			Connection connection) {
		// TODO Auto-generated method stub
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
