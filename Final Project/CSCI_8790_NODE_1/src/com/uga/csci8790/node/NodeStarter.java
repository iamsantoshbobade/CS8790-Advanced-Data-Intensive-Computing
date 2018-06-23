package com.uga.csci8790.node;
/**
 * Class to start the node.
 * To deployed on node 1.
 * @author vishnu
 *
 */
public class NodeStarter {

	/******************************************************************************************************
	 * Starter code for Node. 
	 * @param args[0]  port where nodeThread1 will be listening. (Client to/from Node communication)
	 * @param args[1]  port where nodeThread2 will be listening. (Node to Node communication)
	 */
	public static void main (String[] args) {
		try {
			System.out.println("Node Started");
			System.out.println("Will open up socket connection (server end) now");
			SharedObject sharedObject = new SharedObject();
			Thread nodeThread1 = new Thread(new NodeThread1 (Integer.parseInt (args[0]), 0, sharedObject )); // for Client to Node communication
			nodeThread1.start();
			
			Thread nodeThread2 = new Thread(new NodeThread2 (Integer.parseInt (args[1]), 0, sharedObject)); // for Node to Node communication
			nodeThread2.start();
		}catch(Exception e) {
			
		}

	}

}
