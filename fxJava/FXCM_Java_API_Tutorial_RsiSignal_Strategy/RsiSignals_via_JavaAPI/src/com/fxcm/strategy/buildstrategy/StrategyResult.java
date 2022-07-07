package com.fxcm.strategy.buildstrategy;

import java.util.Comparator;
import java.util.List;

public class StrategyResult implements Comparator<StrategyResult> {
	private double profit;
	private double maxDrawdown;
	private double maxProfit;
	private int numOfWinningTrades;
	private int numOfLosingTrades;
	private int numOfTrades;
	private double winsRatio;
	private double lossesRatio;
	private Strategy strategy;

	// StrategyResult.java continued...
	public StrategyResult() {
	}


	
	
	public double getProfit() {
		return profit;
	}




	public void setProfit(double profit) {
		this.profit = profit;
	}




	public double getMaxDrawdown() {
		return maxDrawdown;
	}




	public void setMaxDrawdown(double maxDrawdown) {
		this.maxDrawdown = maxDrawdown;
	}




	public double getMaxProfit() {
		return maxProfit;
	}




	public void setMaxProfit(double maxProfit) {
		this.maxProfit = maxProfit;
	}




	public int getNumOfWinningTrades() {
		return numOfWinningTrades;
	}




	public void setNumOfWinningTrades(int numOfWinningTrades) {
		this.numOfWinningTrades = numOfWinningTrades;
	}




	public int getNumOfLosingTrades() {
		return numOfLosingTrades;
	}




	public void setNumOfLosingTrades(int numOfLosingTrades) {
		this.numOfLosingTrades = numOfLosingTrades;
	}




	public int getNumOfTrades() {
		return numOfTrades;
	}




	public void setNumOfTrades(int numOfTrades) {
		this.numOfTrades = numOfTrades;
	}




	public double getWinsRatio() {
		return winsRatio;
	}




	public void setWinsRatio(double winsRatio) {
		this.winsRatio = winsRatio;
	}




	public double getLossesRatio() {
		return lossesRatio;
	}




	public void setLossesRatio(double lossesRatio) {
		this.lossesRatio = lossesRatio;
	}




	public Strategy getStrategy() {
		return strategy;
	}




	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}




	public StrategyResult(double profit, double maxProfit, double maxDrawdown, int numOfWinningTrade,
			int numOfLosingTrades, Strategy strategy) {
		this.profit = profit;
		this.maxProfit = maxProfit;
		this.maxDrawdown = maxDrawdown;
		this.numOfWinningTrades = numOfWinningTrades;
		this.numOfLosingTrades = numOfLosingTrades;
		this.numOfTrades = numOfWinningTrades + numOfLosingTrades;
		this.strategy = strategy;
		if (this.numOfTrades == 0) { // no trades were made => ratio is 0.
			this.winsRatio = 0;
			this.lossesRatio = 0;
		} else {
			this.winsRatio = ((double) this.numOfWinningTrades) / this.numOfTrades;
			this.lossesRatio = ((double) this.numOfLosingTrades) / this.numOfTrades;
		}
	}

	// getters and setters goes here...
	@Override
	public int compare(StrategyResult sr1, StrategyResult sr2) {
		if (sr1.getProfit() < sr2.getProfit())
			return 1;
		if (sr1.getProfit() > sr2.getProfit())
			return -1;
		return 0;
	}

	public static double calculateAvgStrategyProfit(List<StrategyResult> strategySummary) {
		// profit in %. 0.01 means 1%.
		double sumOfProfits = 0;
		for (StrategyResult strategyResult : strategySummary) {
			sumOfProfits += strategyResult.getProfit();
		}
		double avgProfit = sumOfProfits / strategySummary.size();
		return avgProfit;
	}
}