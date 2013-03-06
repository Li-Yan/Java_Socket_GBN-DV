import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class Receiver extends Thread {
	
	public void run() {
		while (true) {
			String recvString = Transmission.Recv(DV.dv_sock);
			int message_router_port = Transmission.ReadPort(recvString);
			recvString = Transmission.RemovePort(recvString);
			System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
					" Message received at Node " + DV.self_port + " from Node " + message_router_port);
			
			boolean table_changed = false;
			
			// for the first table initialization
			if (DV.table == null) {
				DV.InitializeTable();
				table_changed = true;
			}
			
			// read updating table from nearby
			int message_router_id = DV.port2routerIDHashMap.get(message_router_port);
			String[] sStrings = recvString.split(";");
			for (int i = 0; i < sStrings.length; i++) {
				String[] ssStrings = sStrings[i].split(",");
				int des_router_port = Integer.parseInt(ssStrings[0]);
				DV.Insert_Unkown_Port(des_router_port);
				int des_router_id = DV.port2routerIDHashMap.get(des_router_port);
				DV.table[message_router_id][des_router_id] = Double.parseDouble(ssStrings[1]);
			}
			
			// update self table
			for (int i = 1; i < DV.known_router_number; i++) {
				double d1 = DV.table[0][i];
				double d2 = Constant.Infinity;
				Iterator<Entry<Integer, Double>> iter = DV.linkHashMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) iter.next();
					int link_router_port = (int) entry.getKey();
					double link_router_weight = (double) entry.getValue();
					double d = link_router_weight + DV.table[DV.port2routerIDHashMap.get(link_router_port)][i];
					if (Accessory.Double_GT(d2, d)) {
						d2 = d;
					}
				}
				if (!Accessory.Double_Equal(d1, d2)) {
					DV.table[0][i] = d2;
					table_changed = true;
				}
			}
			
			if (table_changed) {
				DV.PrintTable();
				DV.SendTable();
			}
		}
	}
}
