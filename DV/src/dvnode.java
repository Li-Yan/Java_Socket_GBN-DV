
public class dvnode {

	public static void main(String[] args) {
		DV dv = new DV();
		int condition = -1;
		if ((condition = Accessory.CreateDV(args, dv)) == 0) {
			System.err.println("Error: Argument!");
			return;
		} else {
			dv.StartReceiver();
			if (condition == 2) {
				DV.InitializeTable();
				DV.PrintTable();
				DV.SendTable();
			}
		}
	}

}
