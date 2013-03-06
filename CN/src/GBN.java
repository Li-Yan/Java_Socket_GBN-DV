import java.util.Timer;
import java.util.TimerTask;


public class GBN {
	public class TimeoutTask extends TimerTask {
		@Override
		public void run() {
			seq_point = window_point;
		}

	}
	
	public int peer_port;
	public int window_size;
	
	public int seq_point;
	public int window_point;
	public int sent_number;
	public int sent_succeed_number;
	public double current_loss_rate;
	
	Timer timer;
	
	public GBN(int Peer_Port, int Window_Size) {
		peer_port = Peer_Port;
		window_size = Window_Size;
		
		seq_point = 0;
		window_point = 0;
		sent_number = 0;
		sent_succeed_number = 0;
		current_loss_rate = 0;
	}
	
	public void SetTimer() {
		timer = new Timer();
		timer.schedule(new TimeoutTask(), Constant.GBN_Timeout, Constant.GBN_Timeout);
	}
	
	public void CancelTimer() {
		timer.cancel();
	}
	
	public int Get_Loss_Number() {
		return sent_number - sent_succeed_number;
	}
	
	public void Update_Current_Loss_Rate() {
		if (sent_number == 0) {
			current_loss_rate = 0;
			return;
		}
		double rate = (double) (sent_number - sent_succeed_number) / (double) sent_number;
		current_loss_rate = Accessory.Double_Round(rate);
	}
	
	public double Calculate_Loss_Rate() {
		if (sent_number == 0) {
			return 0;
		}
		double rate = (double) (sent_number - sent_succeed_number) / (double) sent_number;
		return Accessory.Double_Round(rate);
	}
	
	public double Get_Current_Loss_Rate() {
		return current_loss_rate;
	}
	
	public void Send_Prob() {
		// format: message_type#self_port@seq
		if (seq_point >= window_point + window_size) return;
		String sendString = Transmission.AddPort(CN.self_port, String.valueOf(seq_point++));
		sendString = Transmission.AddType(sendString, Constant.MessageType.PROBE);
		Transmission.Send(CN.cn_sock, sendString, peer_port);
		sent_number++;
	}
	
}
