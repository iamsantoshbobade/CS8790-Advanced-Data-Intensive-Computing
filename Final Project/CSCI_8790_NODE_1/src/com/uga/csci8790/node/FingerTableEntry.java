package com.uga.csci8790.node;


/******************************************************************************************************
 * Bean for the FingerTable data.
 * @author Vishnu Gowda Harish, Santosh Uttam Bobade, Anuja Nagare
 * @version 1.0
 */
public class FingerTableEntry {
	
    /** Starting range of the finger table entry**/
	private int start;
	
	 /** Starting range of the finger table entry**/
	private int end;
	
	/** ID of the successor node **/
	private int succ;
	/** IP address of the successor node **/
	private String ipSucc;
	
	/** port where the nodeThread2 will be listening on the successor **/
	private String portThread2;
	
	public FingerTableEntry (int s, int e, int suc, String ipSuc, String p) {
		start = s; end = e; succ = suc; ipSucc = ipSuc; portThread2 = p;
	} // FingerTableValue
	
	
	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}
	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}
	/**
	 * @param end the end to set
	 */
	public void setEnd(int end) {
		this.end = end;
	}
	/**
	 * @return the succ
	 */
	public int getSucc() {
		return succ;
	}
	/**
	 * @param succ the succ to set
	 */
	public void setSucc(int succ) {
		this.succ = succ;
	}
	/**
	 * @return the ipSucc
	 */
	public String getIpSucc() {
		return ipSucc;
	}
	/**
	 * @param ipSucc the ipSucc to set
	 */
	public void setIpSucc(String ipSucc) {
		this.ipSucc = ipSucc;
	}
	/**
	 * @return the portThread2
	 */
	public String getPortThread2() {
		return portThread2;
	}
	/**
	 * @param portThread2 the portThread2 to set
	 */
	public void setPortThread2(String portThread2) {
		this.portThread2 = portThread2;
	}
	
	
	
}
