import java.util.Random;
import java.util.Timer;


public class Receiver extends Thread {
	private int count = 0;
	Random rdm = new Random(System.currentTimeMillis());

	public void run() {
		int ACK = 0;
		while (true) {
			String recvString = Transmission.Recv(GBN.gbn_sock);
			int seq = Transmission.ReadSeq(recvString);
			recvString = Transmission.RemoveSeq(recvString);
			
			if (recvString.length() == 0) {
				// it is ACK packet
				if (Drop_Packet(GBN.dropMode)) {
					System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
							" ACK" + String.valueOf(seq - 1) + " discarded");
					continue;
				}
				if (seq > GBN.window_point) {
					GBN.window_point = seq;
					System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
							" ACK" + String.valueOf(seq - 1) + " received, window moves to " + seq);
					if (seq < Sender.buffer_end_point) {
						Sender.timer.cancel();
						Sender.timer = new Timer();
						Sender.timer.schedule(new SenderTimerTask(), Constant.Timer_Delay, Constant.Timer_Delay);
					}
				} else {
					System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
							" ACK" + String.valueOf(seq - 1) + " received");
				}
			} else {
				if (Drop_Packet(GBN.dropMode)) {
					System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
							" packet" + seq + " " + recvString + " discarded");
					continue;
				}
				if (seq == ACK) {
					// receive the expected packet
					Transmission.Send(GBN.gbn_sock, Transmission.AddSeq(++ACK, ""), GBN.peer_port);
					System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
							" packet" + seq + " " + recvString + " received");
					System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
							" ACK" + String.valueOf(ACK - 1) + " sent, expecting packet" + ACK);
				} else {
					Transmission.Send(GBN.gbn_sock, Transmission.AddSeq(ACK, ""), GBN.peer_port);
					System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
							" packet" + seq + " " + recvString + " received");
					System.out.println("[" +  Accessory.GetTimeStamp() + "]" + 
							" ACK" + String.valueOf(ACK - 1) + " sent, expecting packet" + ACK);
				}
			}
		}
	}
	
	private Boolean Drop_Packet(GBN.DropMode mode) {
		if (mode == GBN.DropMode.DETERMINISTIC) {
			if (++count % Integer.parseInt(GBN.dropValue) == 0) {
				count = 0;
				return true;
			}
		} else {
			double r = rdm.nextDouble();
			return r <= Double.parseDouble(GBN.dropValue);
		}
		return false;
	}
}
