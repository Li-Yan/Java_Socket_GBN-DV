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

	public static int CreateDV(String[] args, DV dv) {
		// return 0: false; 1: true, but not the last; 2: true and is the last 
		if (!CheckPositiveIntString(args[0])) {
			return 0;
		}
		DV.self_port = Integer.parseInt(args[0]);
		try {
			DV.dv_sock = new DatagramSocket(DV.self_port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		int i = 0;
		while ((args[2 * i + 1] != null) && (args[2 * i +2] != null)) {
			if (!CheckValidPort(args[2 * i + 1]) || 
					!CheckPositiveDoubleString(args[2 * i + 2])) {
				return 0;
			}
			DV.linkHashMap.put(Integer.parseInt(args[2 * i + 1]), Double.parseDouble(args[2 * i + 2]));
			if (2 * (++i) +2 >= args.length) break;
		}
		if (2 * i + 1 == args.length - 1) {
			if (args[2 * i + 1].equalsIgnoreCase("last")) {
				return 2;
			}
		}
		return 1;
	}
	
	public static double Double_Round(double d) {
		double dd = (double) Math.round(d * 10000000) / 10000000;
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
