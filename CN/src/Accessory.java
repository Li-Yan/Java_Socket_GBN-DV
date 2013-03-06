import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;


public class Accessory {
	
	public static String GetTimeStamp() {
		return String.valueOf(Calendar.getInstance().getTimeInMillis());
	}
	
	public static Boolean CheckIntString(String intString) {
		// return true if the string can be converted into a double
		if (intString.length() == 0) return false;
		for (int i = 0; i < intString.length(); i++) {
			if ((i == 0) && (intString.charAt(0) == '-')) {
				continue;
			}
			if ((intString.charAt(i) < '0') || (intString.charAt(i) > '9')) {
				return false;
			}
		}
		return true;
	}
	
	public static Boolean CheckPositiveIntString(String intString) {
		if (!CheckIntString(intString)) {
			return false;
		}
		return (intString.charAt(0) != '-');
	}
	
	public static Boolean CheckPositiveDoubleString(String doubleString) {
		// return true if the string can be converted into a double
		if (doubleString.length() == 0) return false;
		int index = doubleString.indexOf('.');
		if (index == -1) {
			return CheckPositiveIntString(doubleString);
		}
		if (index != 0) {
			String subString = doubleString.substring(0, index);
			if (!CheckPositiveIntString(subString)) return false;
			if (Integer.parseInt(subString) != 0) return false;
		}
		if (index != doubleString.length() - 1) {
			String subString = doubleString.substring(index + 1);
			if (!CheckPositiveIntString(subString)) return false;
		}
		return true;
	}
	
	public static Boolean CheckValidPort(String intString) {
		if (!CheckPositiveIntString(intString)) {
			return false;
		}
		int port = Integer.parseInt(intString);
		return (port >= 1024) && (port <= 65534);
	}

	public static int CreateDV(String[] args, CN cn) {
		// return 0: false; 1: true, but not the last; 2: true and is the last 
		if (!CheckPositiveIntString(args[0])) {
			return 0;
		}
		CN.self_port = Integer.parseInt(args[0]);
		try {
			CN.cn_sock = new DatagramSocket(CN.self_port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if (!args[1].equalsIgnoreCase("receive")) {
			return 0;
		}
		
		// read receive information
		int i = 2;
		int ID = 0;
		while (!args[i].equalsIgnoreCase("send")) {
			if (!CheckValidPort(args[i]) || 
					!CheckPositiveDoubleString(args[i + 1])) {
				return 0;
			}
			CN.linkHashMap.put(Integer.parseInt(args[i]), new Link(ID++, Constant.LinkRole.RECEIVER, 
					Double.parseDouble(args[i + 1]), 0));
			i = i+ 2;
			if (i > Constant.Max_Router_Number) return 0;
		}
		CN.receive_expect_seq = new int[ID];
		for (int j = 0; j < ID; j++) {
			CN.receive_expect_seq[j] = 0;
		}
		
		// read send information
		ID = 0;
		for (int j = i + 1; j < args.length; j++) {
			if (args[j].equalsIgnoreCase("last")) break;
			if (!CheckValidPort(args[j])) {
				return 0;
			}
			CN.linkHashMap.put(Integer.parseInt(args[j]), new Link(ID++, Constant.LinkRole.SEND, 0, 0));
		}
		CN.send_gbn = new GBN[ID];
		for (int j = 0; j < ID; j++) {
			int port = Integer.parseInt(args[i + 1 + j]);
			CN.send_gbn[j] = new GBN(port, Constant.Window_Size);
		}
		
		if (args[args.length - 1].equalsIgnoreCase("last")) return 2;
		return 1;
	}
	
	public static double Double_Round(double d) {
		double dd = (double) Math.round(d * 100) / 100;
		return dd;
	}
	
	public static Boolean Double_Equal(double d1, double d2) {
		return Math.abs(d1 - d2) < Constant.Error;
	}
	
	public static Boolean Double_GT(double d1, double d2) {
		return d1 - d2 > Constant.Error;
	}
	
	public static Boolean Double_LT(double d1, double d2) {
		return d1 - d2 < -Constant.Error;
	}
}
