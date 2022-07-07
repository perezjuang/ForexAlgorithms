package com.fxcm.strategy.buildstrategy;

import java.util.List;

import com.fxcm.strategy.rsi.priceretrieve.CandleStick;

public class RsiSignals implements Strategy {

	private final double STOP_LOSS;
	private final double TAKE_PROFIT;
	private final int FLOOR_RSI;
	private final int CEILING_RSI;

	public RsiSignals(double stopLoss, double takeProfit, int floorRSI) throws Exception {
		if (floorRSI <= 0 || floorRSI >= 100) {
			throw new Exception("Invalid RSI: " + floorRSI + ". Should be between 0-100");
		}
		this.STOP_LOSS = stopLoss;
		this.TAKE_PROFIT = takeProfit;
		this.FLOOR_RSI = floorRSI;
		this.CEILING_RSI = 100 - floorRSI;
	}

	
	
	public double getStopLoss() {
		return STOP_LOSS;
	}



	public double getTakeProfit() {
		return TAKE_PROFIT;
	}



	public int getFloorRSI() {
		return FLOOR_RSI;
	}



	public int getCeilingRSI() {
		return CEILING_RSI;
	}



	public static void calculateRsiForDataSet(List<CandleStick> rsiCandleSticksList, int duration) {
		// duration must be smaller than the data set size
		if (duration >= rsiCandleSticksList.size())
			return;
		double sessionGainSum = 0;
		double sessionLossSum = 0;
		double sessionChange;
		for (int i = 1; i <= duration; i++) { // calculate first RSI.
			sessionChange = rsiCandleSticksList.get(i).getClose() - rsiCandleSticksList.get(i - 1).getClose();
			if (sessionChange > 0) {
				sessionGainSum += sessionChange;
			} else {
				sessionLossSum += sessionChange;
			}
		}
		double averageGain = sessionGainSum / duration;
		double averageLoss = sessionLossSum / duration;
		double rs = (averageGain / -averageLoss);
		double rsi = 100 - (100 / (1 + rs)); // first RSI.
		rsiCandleSticksList.get(duration).setRsi(rsi);
		for (int i = duration + 1; i < rsiCandleSticksList.size(); i++) { // for
																		// smoothing
			sessionChange = rsiCandleSticksList.get(i).getClose() - rsiCandleSticksList.get(i - 1).getClose();
			if (sessionChange > 0) {
				averageGain = (averageGain * (duration - 1) + sessionChange) / duration;
				averageLoss = (averageLoss * (duration - 1) + 0) / duration;
			} else {
				averageGain = (averageGain * (duration - 1) + 0) / duration;
				averageLoss = (averageLoss * (duration - 1) + sessionChange) / duration;
			}
			rs = (averageGain / -averageLoss);
			rsi = 100 - (100 / (1 + rs));
			rsiCandleSticksList.get(i).setRsi(rsi);
		}
	}

	@Override
	public StrategyResult runSrtategy(List<CandleStick> candleSticksList) {
		double entryBuy;
		double entrySell;
		double stopLossPrice = 0;
		double takeProfitPrice = 0;
		double strategyProfit = 0;
		double maxProfit = 0;
		double maxDrawdown = 0;
		boolean isOpenPosition = false;
		int winCounter = 0;
		int lossCounter = 0;
		for (int i = 250; i < candleSticksList.size(); i++) {
			if (isOpenPosition) {
				if (stopLossPrice < takeProfitPrice) { // long position
					if (candleSticksList.get(i).getLow() < stopLossPrice) {
						isOpenPosition = false; // position closed at a loss
												// lossCounter++;
						strategyProfit -= STOP_LOSS;
						maxDrawdown = calculateMaxDrawdown(maxProfit, strategyProfit, maxDrawdown);
					} else if (candleSticksList.get(i).getHigh() > takeProfitPrice) {
						isOpenPosition = false; // position closed at a profit
						winCounter++;
						strategyProfit += TAKE_PROFIT;
						maxProfit = updateMaxProfit(strategyProfit, maxProfit);
					}
				} else { // short position
					if (candleSticksList.get(i).getHigh() > stopLossPrice) {
						isOpenPosition = false; // position closed at a loss
						lossCounter++;
						strategyProfit -= STOP_LOSS;
						maxDrawdown = calculateMaxDrawdown(maxProfit, strategyProfit, maxDrawdown);
					} else if (candleSticksList.get(i).getLow() < takeProfitPrice) {
						isOpenPosition = false; // position closed at a profit
						winCounter++;
						strategyProfit += TAKE_PROFIT;
						maxProfit = updateMaxProfit(strategyProfit, maxProfit);
					}
				}
			} else if (candleSticksList.get(i).getRsi() < FLOOR_RSI) { // no
																		// open
																		// positions.
																		// check
																		// RSI
				entryBuy = candleSticksList.get(i).getClose(); // use
																	// closeAsk
																	// for long
																	// position
				stopLossPrice = ((1 - STOP_LOSS) * entryBuy);
				takeProfitPrice = ((1 + TAKE_PROFIT) * entryBuy);
				isOpenPosition = true;
			} else if (candleSticksList.get(i).getRsi() > CEILING_RSI) { // no
																			// open
																			// positions.
																			// check
																			// RSI
				entrySell = candleSticksList.get(i).getClose(); // use close
																// (bid) for
																// short
																// position
				stopLossPrice = ((1 + STOP_LOSS) * entrySell);
				takeProfitPrice = ((1 - TAKE_PROFIT) * entrySell);
				isOpenPosition = true;
			}
		}
		StrategyResult sr = new StrategyResult(strategyProfit, maxProfit, maxDrawdown, winCounter, lossCounter, this);
		return sr;
	}

	private static double updateMaxProfit(double strategyProfit, double maxProfit) {
		if (strategyProfit > maxProfit) {
			maxProfit = strategyProfit;
		}
		return maxProfit;
	}

	private static double calculateMaxDrawdown(double maxProfit, double strategyProfit, double maxDrawdown) {
		double currentDrawdown = ((1 + strategyProfit) - (1 + maxProfit)) / (1 + maxProfit);
		if (currentDrawdown < maxDrawdown) {
			maxDrawdown = currentDrawdown;
		}
		return maxDrawdown;
	}
}
