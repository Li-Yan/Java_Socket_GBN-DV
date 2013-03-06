import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;

public class Sender extends Thread {
	public static Timer timer;
	public static int buffer_end_point;
	
	public Sender() {
		timer = new Timer();
	}
	
	private void Buffered_Send(String sendString) {
		while (true) {
			if (sendString.length() > GBN.buffer_size) {
				for (int i = 0; i < GBN.buffer_size; i++) {
					GBN.buffer[(i + GBN.window_point) % GBN.buffer_size] = sendString.charAt(i);
				}
				GBN_Send(GBN.buffer_size + GBN.window_point);
				sendString = sendString.substring(GBN.buffer_size);
			} else {
				for (int i = 0; i < sendString.length(); i++) {
					GBN.buffer[(i + GBN.window_point) % GBN.buffer_size] = sendString.charAt(i);
				}
				GBN_Send(sendString.length() + GBN.window_point);
				break;
			}
		}
	}
	
	private void GBN_Send(int buffer_end_point) {
		Sender.buffer_end_point = buffer_end_point;
		GBN.send_point = GBN.window_point;
		timer.schedule(new SenderTimerTask(), Constant.Timer_Delay, Constant.Timer_Delay);
		while (GBN.window_point < buffer_end_point) {
			if ((GBN.send_point < GBN.window_point + GBN.window_size) && (GBN.send_point < buffer_end_point)) {
				System.out.println("[" + Accessory.GetTimeStamp() + "] packet" + GBN.send_point + " "
						+ String.valueOf(GBN.buffer[GBN.send_point % GBN.buffer_size]) + " sent");
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Transmission.Send(GBN.gbn_sock, Transmission.AddSeq(GBN.send_point, 
						String.valueOf(GBN.buffer[GBN.send_point % GBN.buffer_size])), GBN.peer_port);
				GBN.send_point++;
			} else {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		timer.cancel();
		timer = new Timer();
	}
	
	public void run() {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("node> ");
			String inputString = null, subString = null;
			try {
				inputString = input.readLine();
				int i = inputString.indexOf(' ');
				if ((i == -1) || (i == inputString.length())) {
					System.out.println("Error: Argument");
					continue;
				}
				subString = inputString.substring(0, i);
				if (!subString.equalsIgnoreCase("send")) {
					System.out.println("Error: Only send allowed");
					continue;
				}
				subString = inputString.substring(i);
				while ((subString.charAt(0) == ' ') && (subString.length() > 1)) {
					subString = subString.substring(1);
				}
				if (subString.charAt(0) == ' ') {
					System.out.println("Error: Argument");
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Buffered_Send(subString);
			try {
				Thread.sleep(73);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
