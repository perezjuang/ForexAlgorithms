package pulling.historical.prices.stochastic;

public class CandleStick {

	private String date;
	private double open;
	private double low;
	private double high;
	private double closeBid;
	private double closeAsk;
	private double stochasticK;
	private double stochasticD;

	public CandleStick(String date, double open, double low, double high, double closeBid,
			double closeAsk) {
		this.date = date;
		this.open = open;
		this.low = low;
		this.high = high;
		this.closeBid = closeBid;
		this.closeAsk = closeAsk;
	}
	public CandleStick(String date, double open, double low, double high, double closeBid) {
		this.date = date;
		this.open = open;
		this.low = low;
		this.high = high;
		this.closeBid = closeBid;
	}
	@Override
	public String toString() {
		return "CandleStick [date=" + date + ", open=" + open + ", low=" + low + ", high=" + high + ", closeBid="
				+ closeBid + ", closeAsk=" + closeAsk + ", stochasticK=" + stochasticK + "]";
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getCloseBid() {
		return closeBid;
	}
	public void setCloseBid(double closeBid) {
		this.closeBid = closeBid;
	}
	public double getCloseAsk() {
		return closeAsk;
	}
	public void setCloseAsk(double closeAsk) {
		this.closeAsk = closeAsk;
	}
	public double getStochasticK() {
		return stochasticK;
	}
	public void setStochasticK(double stochasticK) {
		this.stochasticK = stochasticK;
	}
	
	public double getStochasticD() {
		return stochasticD;
	}
	public void setStochasticD(double stochasticD) {
		this.stochasticD = stochasticD;
	}
}