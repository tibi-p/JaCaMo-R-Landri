public class Bet {
	private final double sum;
	private final String type;
	private Object[] betValues;

	public Bet(final double sum, final String type, final Object[] betValues) {
		this.sum = sum;
		this.type = type;
		this.betValues = betValues;
	}

	public final double getSum() {
		return sum;
	}

	public final String getType() {
		return type;
	}

	public final Object[] getBetValues() {
		return betValues;
	}
}
