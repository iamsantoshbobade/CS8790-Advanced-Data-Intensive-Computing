package com.uga.csci8790.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

/******************************************************************************************************
 * Class that creates and runs the thread responsible for Node to/from Client communication.
 * @author Vishnu Gowda Harish, Santosh Uttam Bobade, Anuja Nagare
 * @version 1.0
 */
public class NodeThread1 implements Runnable{
	
	/** Instance variable to hold the port Number */
	private int portNo;
	
	/** Instance variable to hold the server socket object before calling accept*/
	private ServerSocket sSocket;
	
	/** Socket object that holds connection between this node and the client **/
	private Socket socketConn;
	
	/** Input Stream for socket connection **/
	DataInputStream inputStream;
	
	/** Output Stream for socket connection **/
	DataOutputStream outputStream;
	
	/** The keys contained in this node **/
	private LinkedHashMap<Integer, Object> keys = new LinkedHashMap<>();
	
	/**Identifier for this node **/
	private int nodeID;
	
	/**Queue to process command backlogs **/
	Queue<String> commandQueue = new LinkedList<>();
	
	/** Variable to coordinate access to finger table between nodethread1 and nodethread2 **/
	private SharedObject sharedObj;
	
	/******************************************************************************************************
	 * Constructor for the node thread. Creates server socket connection and intializes finger table info
	 */
	public NodeThread1 (int pNo, int id, SharedObject sObj) {
		portNo = pNo;
		nodeID = id;
		sharedObj = sObj;
		try {
			sSocket = new ServerSocket (portNo);
			initFingerTable ();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/******************************************************************************************************
	 * Run method
	 */
	public void run() {	
		System.out.println("Opened socket connection (server end)");
		try {
		Socket socket = sSocket.accept(); // accept only once, as only one client service will 
										  // connect to this server socket connection at this port
		while (true) {			// process multiple commands from the client service
				inputStream  = new DataInputStream  (socket.getInputStream());
				outputStream = new DataOutputStream (socket.getOutputStream());
				String command = inputStream.readUTF(); // blocker call	
				commandQueue.add (command);
				Object returnVal = processCommand (command);	
				commandQueue.remove (command);
				if (returnVal.equals("key_not_present_in_system")) 
					System.out.println("Key is not with me but present somewhere else, "
										+ "forwarding lookup request"
										+ " to other node");
				else if(returnVal.equals("sent_to_other_node"))
					System.out.println("key sent to other node");
				else
				outputStream.writeUTF(String.valueOf(returnVal));
			} // while
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // run
	
	
	/************************************************************************************************
	 * Sets up the initial finger table (see project update presentation for state of finger tables)
	 */
	private void initFingerTable () {
		FingerTableEntry fingerTableEnt1 = new FingerTableEntry (1, 2, 1, "127.0.0.1","9999");
		FingerTableEntry fingerTableEnt2 = new FingerTableEntry (2, 4, 3, "127.0.0.1","9998" );
		FingerTableEntry fingerTableEnt3 = new FingerTableEntry (4, 8, 0, "127.0.0.1","9997");
		FingerTableEntry fingerTableEnt4 = new FingerTableEntry (8, 0, 0, "127.0.0.1","9996");
		
		synchronized(sharedObj) {
			HashMap<Integer, FingerTableEntry> fingerTable = sharedObj.getFingerTable();
			fingerTable.put(1, fingerTableEnt1);
			fingerTable.put(2, fingerTableEnt2);
			fingerTable.put(4, fingerTableEnt3);
			fingerTable.put(8, fingerTableEnt4);				
		} // synchronized
		
		//Enter the keys
		keys.put (6,"Hello World");
	} // initFingerTable
	
	/************************************************************************************************
	 * Router Function for appropriate processing of commands
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 */
	private Object processCommand (String command) throws NumberFormatException, UnknownHostException, IOException {	
			if(command.startsWith("get")) {
				return (doGet (command));
			}else if(command.startsWith("put")) {
				  doPut (command);
			}else if(command.startsWith("addNode")) {
				
			}else {
				System.out.println("Command not supported!!!");
			}
			return null;
	} // processCommand

	/************************************************************************************************
	 * Handles the lookup.
	 * i.e syntax of get command = get <key>_<messagecounter>   , where key is an integer
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 */
	private Object doGet (String command) throws NumberFormatException, UnknownHostException, IOException {
		
		String keyCommand = command.split("_")[0];
		String keytoLookUp = keyCommand.split(" ")[1];
		int messageCounter = Integer.parseInt(command.split("_")[1]);
		
		//check if key is present in the current node
		if(keys.containsKey(keytoLookUp))
			return keys.get(keytoLookUp) + "_"+ ++messageCounter ;
		else {									// should go through the finger table
								
			Iterator <FingerTableEntry> fingerTabItr = sharedObj.getFingerTable().values().iterator();
			int currHashMapSize = sharedObj.getFingerTable().size();
			
			int counter = 1;
			while (fingerTabItr.hasNext()) {
				FingerTableEntry Fingentry=  fingerTabItr.next();
				Integer key = Integer.parseInt(keytoLookUp);
				if ( (key >= Fingentry.getStart() && key< Fingentry.getEnd()) || (key >= Fingentry.getStart() && counter == currHashMapSize) ) {
					int succ = Fingentry.getSucc();
					String succIp = Fingentry.getIpSucc();
					String thread2Port = Fingentry.getPortThread2();
					
					if (succ == nodeID) { 
						System.out.println("KEY NOT PRESENT IN THE SYSTEM");
						return "key_not_present_in_system";
					}else {
						//creates a client socket conn connects to nodthread2 of the other node(Server side for this connection).
						//increment the message counter(i.e to count the number of messages)						
						Socket sock = new Socket (succIp, Integer.parseInt(thread2Port));
						System.out.println("Successfully connected to a different node to forward the lookup request");
						DataInputStream dIStream = new DataInputStream (sock.getInputStream());
						DataOutputStream dOStream = new DataOutputStream (sock.getOutputStream());						
						dOStream.writeUTF(keyCommand +"_"+ ++messageCounter);
						//wait for acknowledgement from Node 2
						dIStream.readUTF();
						return "sent_to_other_node";
						//end this workflow gracefully
					} // if-else
				} // if-else
				counter ++;
			} // while
		}		
		return null;	
	} // doGet
	
	/************************************************************************************************
	 * Handles the insertion of keys.
	 * i.e syntax of put command = put <key> <value>_messagecounter , where key is an integer
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 */
	private Object doPut (String command) throws NumberFormatException, UnknownHostException, IOException {
		String keyCommand = command.split("_")[0];
		String keytoPut = keyCommand.split(" ")[1];
		String valuetoPut = keyCommand.split(" ")[2];
		int messageCounter = Integer.parseInt(command.split("_")[1]);
		
		Iterator <FingerTableEntry> fingerTabItr = sharedObj.getFingerTable().values().iterator();
		int currHashMapSize = sharedObj.getFingerTable().size();
		
		int counter = 1;
		
		while (fingerTabItr.hasNext()) {
			FingerTableEntry Fingentry=  fingerTabItr.next();
			Integer key = Integer.parseInt(keytoPut);
		
			if ( (key >= Fingentry.getStart() && key< Fingentry.getEnd()) || (key >= Fingentry.getStart() && counter == currHashMapSize) ) {
				int succ = Fingentry.getSucc();
				String succIp = Fingentry.getIpSucc();
				String thread2Port = Fingentry.getPortThread2();
				if (succ == nodeID) {  // put the key in this ndoe itself
					keys.put(Integer.parseInt(keytoPut), valuetoPut);
					return "insert_success";
				}else {				//forward to successor node to insert the key
					//creates a client socket conn connects to nodthread2 of the other node(Server side for this connection).
					//increment the message counter(i.e to count the number of messages)						
					Socket sock = new Socket (succIp, Integer.parseInt(thread2Port));
					System.out.println("Successfully connected to a different node to forward the put request");
					DataInputStream dIStream = new DataInputStream (sock.getInputStream());
					DataOutputStream dOStream = new DataOutputStream (sock.getOutputStream());						
					dOStream.writeUTF(keyCommand +"_"+ ++messageCounter);
					//wait for acknowledgement from Node 2
					dIStream.readUTF();
					return "sent_to_other_node";
					//end this workflow gracefully		
				} // if			
		    } // if	
	} // while	
		return null;
	} // // doPut	
} // NodeThread1
