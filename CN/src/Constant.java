
public class Constant {
	public static enum LinkRole {SEND, RECEIVER};
	public static enum MessageType {PROBE, ACK, UPDATE};
	
	public static final String ip = "127.0.0.1";
	
	public static final int Max_Msg_Length = 10;
	public static final int Max_Sequence_Number = 65534;
	public static final int Window_Size = 5;
	
	public static final double Error = 0.005;
	public static final int GBN_Timeout = 500;
	public static final int Print_Status_Inteval = 1000;
	public static final int DV_Update_Inteval = 5000;
	
	public static final int Max_Router_Number = 16;
	public static final double Infinity = 7373;
}
