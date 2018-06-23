package com.uga.csci8790.node;

import java.util.HashMap;

/******************************************************************************************************
 * Object that does the sharing of finger table data between nodeThread1 and nodeThread2 within a node.
 * @author Vishnu Gowda Harish, Santosh Uttam Bobade, Anuja Nagare
 * @version 1.0
 */
public class SharedObject {
	
	/** Contains finger table info that will be shared between nodethread1 and nodethread2 **/
	private HashMap<Integer, FingerTableEntry> fingerTable = new HashMap<>();

	/**
	 * @return the fingerTable
	 */
	public HashMap<Integer, FingerTableEntry> getFingerTable() {
		return fingerTable;
	}

	/**
	 * @param fingerTable the fingerTable to set
	 */
	public void setFingerTable(HashMap<Integer, FingerTableEntry> fingerTable) {
		this.fingerTable = fingerTable;
	}
	
}
