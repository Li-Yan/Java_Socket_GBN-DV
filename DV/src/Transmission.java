import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Transmission {

	public static String AddPort(int port, String s) {
		return String.valueOf(port) + "@"+ s;
	}
	
	public static int ReadPort(String s) {
		int index = s.indexOf('@');
		String seqString = s.substring(0, index);
		return Integer.parseInt(seqString);
	}
	
	public static String RemovePort(String s) {
		int index = s.indexOf('@');
		return s.substring(index + 1);
	}
	
	public static void Send(DatagramSocket socket, String sendString, int Port) {
		byte[] sendByte = new byte[Constant.Max_Msg_Length];
		sendByte = sendString.getBytes();
		DatagramPacket sendPacket =null; 
		try {
			sendPacket = new DatagramPacket(sendByte, sendByte.length, 
					InetAddress.getByName(Constant.ip), Port);
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String Recv(DatagramSocket socket) {
		byte[] recvByte = new byte[1024];
		DatagramPacket recvPacket = new DatagramPacket(recvByte, recvByte.length);
		try {
			socket.receive(recvPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (recvPacket.getAddress() == null) {
			return null;
		}
		return new String(recvByte, 0, recvPacket.getLength());
	}
}
