package de.openVJJ.basic;

import java.util.ArrayList;
import java.util.List;
/**
 * 
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
public abstract class Value {
	private List<Lock> locks = new ArrayList<Lock>();
	
	/**
	 * Adds a {@link Lock} to the Value so it will not be deleted.  
	 * @return the created {@link Lock} to unlock the Value
	 */
	public Lock lock(){
		Lock lock = new Lock();
		locks.add(lock);
		return lock;
	}
	
	/**
	 * removes the given {@link Lock}. If all {@link Lock}s are removed the {@link Value} will be {@link Value#finalize()}
	 * @param lock the {@link Lock} received through {@link Value#lock()}
	 * @see Value#lock()
	 */
	public void free(Lock lock){
		locks.remove(lock);
		if(locks.isEmpty()){
			try {
				finalize();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Override if a Variable is not thread save
	 * @return default true
	 */
	public boolean isThreadSave(){
		return true;
	}
	
	/**
	 * 
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
	public static class Lock{
		private static long idRun = 0;
		private long id;
		
		private Lock(){
			id = idRun;
			idRun++;
		}
	}
}
