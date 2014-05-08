package de.openVJJ;

import javax.swing.JFrame;

import de.openVJJ.GUI.ModuleInsightPannel;
import de.openVJJ.basic.Module;

public class OpenVJJ2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame mainFrame = new JFrame("Oper VJJ");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Module baseModule = new Module();
		ModuleInsightPannel baseModuleInsightPannel = new ModuleInsightPannel(baseModule);
		mainFrame.add(baseModuleInsightPannel);
		mainFrame.setBounds(0, 0, 1800, 1000);
		mainFrame.setVisible(true);
	}

}
