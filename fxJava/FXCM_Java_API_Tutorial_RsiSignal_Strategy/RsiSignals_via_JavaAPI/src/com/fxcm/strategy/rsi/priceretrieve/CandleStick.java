package com.fxcm.strategy.rsi.priceretrieve;

public class CandleStick {
	private String date;
	private double open;
	private double low;
	private double high;
	private double close;
	private double rsi;

	public CandleStick(String date, double open, double low, double high, double close) {
		this.date = date;
		this.open = open;
		this.low = low;
		this.high = high;
		this.close = close;

	}

	
	
	public double getRsi() {
		return rsi;
	}



	public void setRsi(double rsi) {
		this.rsi = rsi;
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



	public double getClose() {
		return close;
	}



	public void setClose(double close) {
		this.close = close;
	}



	// getters and setters goes here...
	@Override
	public String toString() {
		return "CandleStick [date=" + date + ", open=" + open + ", low=" + low + ",high=" + high + ", close="
				+ close + "]";
	}
}
