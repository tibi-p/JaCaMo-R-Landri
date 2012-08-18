public class Bet {
	private final double sum;
	private final String type;
	private Object[] betValues;

	public Bet(final double sum, final String type, final Object[] betValues) {
		this.sum = sum;
		this.type = type;
		this.betValues = betValues;
	}

	public double getSum() {
		return sum;
	}

	public String getType() {
		return type;
	}

	public Object[] getBetValues() {
		return betValues;
	}
}
