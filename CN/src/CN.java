import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class CN {
	class UpdateRoutingTask extends TimerTask {
		@Override
		public void run() {
			probe.interrupt();
			Boolean has_changed = UpdateLink();
			if (has_changed) {
				SendTable();
				PrintTable();
			}
			probe = new GBNProbe();
			probe.start();
		}
	}
	
	class PrintStatusTask extends TimerTask {
		@Override
		public void run() {
			if (send_gbn.length == 0) return;
			
			for (int i = 0; i < send_gbn.length; i++) {
				System.out.println("[" + Accessory.GetTimeStamp() + "] Link to " +
						send_gbn[i].peer_port + ": " + send_gbn[i].sent_number + " packets sent, " +
						send_gbn[i].Get_Loss_Number() + " packets lost, loss rate " + 
						send_gbn[i].Calculate_Loss_Rate());
			}
		}
	}
	
	class GBNProbe extends Thread {
		public void run() {
			if (send_gbn.length == 0) return;
			
			for (int i = 0; i < send_gbn.length; i++) {
				send_gbn[i].SetTimer();
			}
			
			while (true) {
				for (int i = 0; i < send_gbn.length; i++) {
					send_gbn[i].Send_Prob();
				}
				try {
					sleep(5);
				} catch (InterruptedException e) {
					for (int i = 0; i < send_gbn.length; i++) {
						send_gbn[i].CancelTimer();
					}
					return;
				}
			}
		}
	}
	
	public static int self_port;
	public static HashMap<Integer, Integer> port2routerIDHashMap;
	public static int routerID2port[];
	public static int known_router_number;
	public static HashMap<Integer, Link> linkHashMap;
	public static double table[][];
	public static DatagramSocket cn_sock;
	
	public static int receive_expect_seq[];
	public static GBN send_gbn[];
	
	public static Boolean activated; 
	public GBNProbe probe;
	public Timer update_routing_timer;
	public Timer print_status_timer;
	public UpdateRoutingTask update_routing_task;
	public PrintStatusTask print_status_task;
	
	public CN() {
		self_port = 0;
		port2routerIDHashMap = new HashMap<Integer, Integer>();
		routerID2port = new int[Constant.Max_Router_Number];
		linkHashMap = new HashMap<Integer, Link>();
		table = null;
		cn_sock = null;
		
		receive_expect_seq = null;
		send_gbn = null;
		
		activated = false;
		probe = new GBNProbe();
		update_routing_timer = new Timer();
		print_status_timer = new Timer();
		update_routing_task = new UpdateRoutingTask();
		print_status_task = new PrintStatusTask();
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
		
		Iterator<Entry<Integer, Link>> iter = linkHashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) iter.next();
			int port = (Integer) entry.getKey();
			Insert_Unkown_Port(port);
			int id = port2routerIDHashMap.get(port);
			table[0][id] = 0;
		}

		for (int i = 0; i < known_router_number; i++) {
			table[i][i] = 0;
		}
		
		activated = true;
	}
	
	public Boolean UpdateLink() {
		for (int i = 0; i < send_gbn.length; i++) {
			send_gbn[i].Update_Current_Loss_Rate();
			double link_weight = send_gbn[i].current_loss_rate;
			int link_router_port = send_gbn[i].peer_port;
			Iterator<Entry<Integer, Link>> iter = linkHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) iter.next();
				int port = (Integer) entry.getKey();
				if (port == link_router_port) {
					Link link = (Link) entry.getValue();
					link.linkWeight = link_weight;
					linkHashMap.put(port, link);
					break;
				}
			}
		}
		return UpdateTable();
	}
	
	public static Boolean UpdateTable() {
		Boolean table_changed = false;
		for (int i = 1; i < CN.known_router_number; i++) {
			double d1 = CN.table[0][i];
			double d2 = Constant.Infinity;
			Iterator<Entry<Integer, Link>> iter = CN.linkHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) iter.next();
				int link_router_port = (int) entry.getKey();
				double link_router_weight = ((Link) entry.getValue()).linkWeight;
				double d = link_router_weight + CN.table[CN.port2routerIDHashMap.get(link_router_port)][i];
				if (Accessory.Double_GT(d2, d)) {
					d2 = d;
				}
			}
			if (!Accessory.Double_Equal(d1, d2)) {
				CN.table[0][i] = d2;
				table_changed = true;
			}
		}
		return table_changed;
	}
	
	public static void SendTable() {
		// packet format:
		// 	message_type#self_port@link_port1,link_weight1;...!linkdest_port1,weight1;...
		// PS: there is no ';' in the end of the packet
		String updateString = "";
		
		//  write link condition
		for (int i = 0; i < send_gbn.length; i++) {
			updateString += send_gbn[i].peer_port + "," + 
					String.valueOf(send_gbn[i].current_loss_rate) + ";";
		}
		if (updateString.length() > 0) {
			updateString = updateString.substring(0, updateString.length() - 1);
		}
		updateString += "!";
		
		// write routing table
		for (int i = 0; i < known_router_number; i++) {
			updateString += String.valueOf(routerID2port[i]) + "," + String.valueOf(table[0][i]) + ";";
		}
		updateString = updateString.substring(0, updateString.length() - 1);
		updateString = Transmission.AddPort(self_port, updateString);
		updateString = Transmission.AddType(updateString, Constant.MessageType.UPDATE);
		
		// iterate again to send update message to neighbour
		Iterator<Entry<Integer, Link>> linkIterator = linkHashMap.entrySet().iterator();
		while (linkIterator.hasNext()) {
			Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) linkIterator.next();
			int port = (Integer) entry.getKey();
			Transmission.Send(cn_sock, updateString, port);
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
			if ((id != i) && (id != -1)) {
				System.out.print("; Next hop -> Node " + routerID2port[id]);
			}
			System.out.println();
		}
	}
	
	private static int Find_Next_Hop_ID(int des_router_id) {
		Iterator<Entry<Integer, Link>> iter = CN.linkHashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) iter.next();
			int link_router_port = (int) entry.getKey();
			double link_router_weight = ((Link) entry.getValue()).linkWeight;
			int link_router_id = port2routerIDHashMap.get(link_router_port);
			double d = link_router_weight + CN.table[link_router_id][des_router_id];
			if (Accessory.Double_Equal(table[0][des_router_id], d)) {
				return link_router_id;
			}
		}
		return -1;
	}
	
	public void Start_Prob() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		update_routing_timer.schedule(update_routing_task, Constant.DV_Update_Inteval, Constant.DV_Update_Inteval);
		print_status_timer.schedule(print_status_task, Constant.Print_Status_Inteval, Constant.Print_Status_Inteval);
		probe.start();
	}
}
