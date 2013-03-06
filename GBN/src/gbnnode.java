
public class gbnnode {

	public static void main(String[] args) {
		GBN gbn = null;
		if (!Accessory.CreateGBN(args, gbn)) {
			System.err.println("Error: Argument!");
			return;
		}
	}

}
