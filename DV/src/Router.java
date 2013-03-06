
public class Router {
	public int next_hop_port;
	public double weight;
	
	public Router(int Port, double Weight) {
		next_hop_port = Port;
		weight = Weight;
	}
}
