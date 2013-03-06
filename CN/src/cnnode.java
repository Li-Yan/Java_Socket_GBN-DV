
public class cnnode {

	public static void main(String[] args) {
		CN cn = new CN();
		int condition = -1;
		if ((condition = Accessory.CreateDV(args, cn)) == 0) {
			System.err.println("Error: Argument!");
			return;
		} else {
			cn.StartReceiver();
			if (condition == 2) {
				CN.InitializeTable();
				CN.PrintTable();
				CN.SendTable();
			}
			
			while (true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (CN.activated) {
					cn.Start_Prob();
					break;
				}
			}
		}
	}

}
