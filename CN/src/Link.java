
public class Link {
	public int id;
	public Constant.LinkRole linkRole;
	public double lossRate;
	public double linkWeight;
	
	public Link(int ID, Constant.LinkRole LinkRole, double Prob, double Weight) {
		id = ID;
		linkRole = LinkRole;
		lossRate = Prob;
		linkWeight = Weight;
	}
}
