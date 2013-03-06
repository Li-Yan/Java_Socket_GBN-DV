import java.util.Calendar;
import java.util.TimerTask;

public class SenderTimerTask extends TimerTask {
	@Override
	public void run() {
		System.out.println("[" + Calendar.getInstance().getTimeInMillis() + "]" + 
				" packet" + GBN.window_point + " timeout");
		GBN.send_point = GBN.window_point;
	}

}