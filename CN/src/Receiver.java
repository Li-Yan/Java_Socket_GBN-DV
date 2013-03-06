import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

public class Receiver extends Thread {
	Random rdm = new Random(System.currentTimeMillis());

	private synchronized void do_Update(String recvString) {
		int message_router_port = Transmission.ReadPort(recvString);
		recvString = Transmission.RemovePort(recvString);
		String[] strings = recvString.split("!");
		System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
				" Message received at Node " + CN.self_port + " from Node " + message_router_port);
		
		Boolean table_changed = false;
		
		// for the first table initialization
		if (CN.table == null) {
			CN.InitializeTable();
			table_changed = true;
		}
		int message_router_id = CN.port2routerIDHashMap.get(message_router_port);
		
		// for link update
		if (strings[0].length() > 0) {
			String[] sStrings = strings[0].split(";");
			for (int i = 0; i < sStrings.length; i++) {
				String[] ssStrings = sStrings[i].split(",");
				int link_router_port = Integer.parseInt(ssStrings[0]);
				if (link_router_port == CN.self_port) {
					double link_weight = Double.parseDouble(ssStrings[1]);
					Iterator<Entry<Integer, Link>> iter = CN.linkHashMap.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) iter.next();
						int port = (Integer) entry.getKey();
						if (port == message_router_port) {
							Link link = (Link) entry.getValue();
							link.linkWeight = link_weight;
							CN.linkHashMap.put(port, link);
							break;
						}
					}
					break;
				}
			}
		}
		
		// for table update
		String[] sStrings = strings[1].split(";");
		for (int i = 0; i < sStrings.length; i++) {
			String[] ssStrings = sStrings[i].split(",");
			int des_router_port = Integer.parseInt(ssStrings[0]);
			CN.Insert_Unkown_Port(des_router_port);
			int des_router_id = CN.port2routerIDHashMap.get(des_router_port);
			CN.table[message_router_id][des_router_id] = Double.parseDouble(ssStrings[1]);
		}
		
		// update self table
		if (CN.UpdateTable()) {
			table_changed = true;
		}
		
		if (table_changed) {
			CN.PrintTable();
			CN.SendTable();
		}
	}

	private void do_ACK(String recvString) {
		int port = Transmission.ReadPort(recvString);
		recvString = Transmission.RemovePort(recvString);

		Link link = CN.linkHashMap.get(port);
		if (link.linkRole != Constant.LinkRole.SEND) {
			System.err.println("Error: Port " + CN.self_port
					+ "should not receive ACK from port " + port);
			System.exit(0);
		}

		int seq = Integer.parseInt(recvString);
		CN.send_gbn[link.id].sent_succeed_number++;
		if (seq > CN.send_gbn[link.id].window_point) {
			CN.send_gbn[link.id].window_point = seq;
		}
	}

	private void do_Prob(String recvString) {
		int port = Transmission.ReadPort(recvString);
		recvString = Transmission.RemovePort(recvString);

		Link link = CN.linkHashMap.get(port);
		if (link.linkRole != Constant.LinkRole.RECEIVER) {
			System.err.println("Error: Port " + CN.self_port
					+ "should not receive probe from port " + port);
			System.exit(0);
		}

		int seq = Integer.parseInt(recvString);
		String ACKString = "";
		if (!Drop_Packet(link.lossRate)) {
			if (seq == CN.receive_expect_seq[link.id]) {
				CN.receive_expect_seq[link.id]++;
			}
			ACKString = Transmission.AddPort(CN.self_port,
					String.valueOf(CN.receive_expect_seq[link.id]));
			ACKString = Transmission.AddType(ACKString,
					Constant.MessageType.ACK);
			Transmission.Send(CN.cn_sock, ACKString, port);
		}
	}

	private Boolean Drop_Packet(double lose_rate) {
		double r = rdm.nextDouble();
		return r <= lose_rate;
	}

	public void run() {
		while (true) {
			String recvString = Transmission.Recv(CN.cn_sock);
			Constant.MessageType msg_type = Transmission.ReadType(recvString);
			recvString = Transmission.RemoveType(recvString);

			if (msg_type == Constant.MessageType.UPDATE) {
				do_Update(recvString);
			} else if (msg_type == Constant.MessageType.PROBE) {
				do_Prob(recvString);
			} else if (msg_type == Constant.MessageType.ACK) {
				do_ACK(recvString);
			}
		}
	}
}
