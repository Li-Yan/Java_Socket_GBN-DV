import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class DV {
	public static int self_port;
	public static HashMap<Integer, Integer> port2routerIDHashMap;
	public static int routerID2port[];
	public static int known_router_number;
	public static HashMap<Integer, Double> linkHashMap;
	public static double table[][];
	public static DatagramSocket dv_sock;
	
	public DV() {
		self_port = 0;
		port2routerIDHashMap = new HashMap<Integer, Integer>();
		routerID2port = new int[Constant.Max_Router_Number];
		linkHashMap = new HashMap<Integer, Double>();
		table = null;
		dv_sock = null;
	}
	
	public static void InitializeTable() {
		port2routerIDHashMap.put(self_port, 0);
		routerID2port[0] = self_port;
		known_router_number = 1;
		
		table = new double[Constant.Max_Router_Number][Constant.Max_Router_Number];
		for (int i = 0; i < Constant.Max_Router_Number; i++) {
			for (int j = 0; j < Constant.Max_Router_Number; j++) {
				table[i][j] = Constant.Infinity;
			}
		}
		
		Iterator<Entry<Integer, Double>> iter = linkHashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) iter.next();
			int port = (Integer) entry.getKey();
			Insert_Unkown_Port(port);
			int id = port2routerIDHashMap.get(port);
			table[0][id] = (double) entry.getValue();
		}

		for (int i = 0; i < known_router_number; i++) {
			table[i][i] = 0;
		}
	}
	
	public static void Insert_Unkown_Port(int port) {
		if (port2routerIDHashMap.get(port) == null) {
			port2routerIDHashMap.put(port, known_router_number);
			routerID2port[known_router_number++] = port;
		}
	}
	
	public void StartReceiver() {
		Receiver receiver = new Receiver();
		receiver.start();
	}
	
	public static void SendTable() {
		// packet format:
		// 	port@dest_port1,weight1;dest_port2,weight2
		// PS: there is no ';' in the end of the packet
		String updateString = "";
		for (int i = 0; i < known_router_number; i++) {
			updateString += String.valueOf(routerID2port[i]) + "," + String.valueOf(table[0][i]) + ";";
		}
		updateString = updateString.substring(0, updateString.length() - 1);
		updateString = Transmission.AddPort(self_port, updateString);
		
		// iterate again to send update message to neighbour
		Iterator<Entry<Integer, Double>> linkIterator = linkHashMap.entrySet().iterator();
		while (linkIterator.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) linkIterator.next();
			int port = (Integer) entry.getKey();
			Transmission.Send(dv_sock, updateString, port);
			System.out.println("[" +  Accessory.GetTimeStamp() + "]" + " Message sent from Node " 
					+ self_port + " to Node " + port);
		}
	}
	
	public synchronized static void PrintTable() {
		System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
				" Node " + self_port + " Routing Table");
		for (int i = 1; i < known_router_number; i++) {
			String doubleString = String.valueOf(Accessory.Double_Round(table[0][i]));
			if (doubleString.indexOf("0.") == 0) {
				doubleString = doubleString.substring(1);
			}
			System.out.print("- (" + doubleString + ") -> Node " + routerID2port[i]);
			int id = Find_Next_Hop_ID(i);
			if (id != i) {
				System.out.print("; Next hop -> Node " + routerID2port[id]);
			}
			System.out.println();
		}
	}
	
	private static int Find_Next_Hop_ID(int des_router_id) {
		Iterator<Entry<Integer, Double>> iter = DV.linkHashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) iter.next();
			int link_router_port = (int) entry.getKey();
			double link_router_weight = (double) entry.getValue();
			int link_router_id = port2routerIDHashMap.get(link_router_port);
			double d = link_router_weight + DV.table[link_router_id][des_router_id];
			if (Accessory.Double_Equal(table[0][des_router_id], d)) {
				return link_router_id;
			}
		}
		return -1;
	}
}
