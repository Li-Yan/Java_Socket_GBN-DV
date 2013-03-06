import java.net.DatagramSocket;
import java.net.SocketException;


public class GBN {
	public static enum DropMode {DETERMINISTIC, PROBABILISTIC};
	
	public static int self_port;
	public static int peer_port;
	public static int window_size;
	public static DropMode dropMode;
	public static String dropValue;
	
	public static DatagramSocket gbn_sock;
	public static char[] buffer;
	public static int buffer_size;
	public static int window_point;
	public static int send_point;
	
	public GBN(int Self, int Peer, int Win, DropMode Mode, String Value) {
		self_port = Self;
		peer_port = Peer;
		window_size = Win;
		dropMode = Mode;
		dropValue = Value;
		
		Prepare();
		StartThread();
	}
	
	private void Prepare() {
		buffer_size = window_size * 2;
		buffer = new char[buffer_size];
		window_point = 0;
		try {
			gbn_sock = new DatagramSocket(self_port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private void StartThread() {
		Sender sender = new Sender();
		sender.start();
		Receiver receiver = new Receiver();
		receiver.start();
	}
	
	public static void ClearBuffer() {
		window_point = 0;
	}
}
