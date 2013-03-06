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

	public static Boolean CreateGBN(String[] args, GBN gbn) {
		if (!CheckPositiveIntString(args[0]) || !CheckPositiveIntString(args[1]) 
				|| !CheckPositiveIntString(args[2])) {
			return false;
		}
		int self_port = Integer.parseInt(args[0]);
		int peer_port = Integer.parseInt(args[1]);
		int window_size = Integer.parseInt(args[2]);
		GBN.DropMode dropMode;
		if (args[3].equals("-d")) {
			dropMode = GBN.DropMode.DETERMINISTIC;
			if (!CheckPositiveIntString(args[4])) return false;
		} else if (args[3].equals("-p")) {
			dropMode = GBN.DropMode.PROBABILISTIC;
			if (!CheckPositiveDoubleString(args[4])) return false;
		} else {
			return false;
		}
		gbn = new GBN(self_port, peer_port, window_size, dropMode, args[4]);
		return true;
	}
	
}
