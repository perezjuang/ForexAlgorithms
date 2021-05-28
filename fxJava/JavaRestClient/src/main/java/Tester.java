package com.fxcm.strategy.backtest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import com.fxcm.fix.Instrument;
import com.fxcm.fix.UTCDate;
import com.fxcm.fix.UTCTimeOnly;
import com.fxcm.strategy.buildstrategy.RsiSignals;
import com.fxcm.strategy.buildstrategy.Strategy;
import com.fxcm.strategy.buildstrategy.StrategyResult;
import com.fxcm.strategy.rsi.priceretrieve.CandleStick;
import com.fxcm.strategy.rsi.priceretrieve.HistoryMiner;

public class Tester {
	public static void main(String[] args) {

		List<CandleStick> candleSticksList;
		double stopLossPerc = 0.003;
		double takeProfitPerc = 0.003;
		int bottomRSI = 10;
		List<StrategyResult> strategySummary = new ArrayList<StrategyResult>();
		UTCDate startDate;
		UTCTimeOnly startTime;
		Instrument asset = new Instrument("AUD/USD");
		// get the current time and roll back 1 year
		Calendar instance = Calendar.getInstance();
		instance.roll(Calendar.YEAR, -1);
		// set the starting date and time of the historical data
		startDate = new UTCDate(instance.getTime());
		startTime = new UTCTimeOnly(instance.getTime());
		try {
			// create an instance of the JavaFixHistoryMiner
			HistoryMiner miner = new HistoryMiner("D102185580001", "1404", "Demo", startDate, startTime,
					asset);
			// login to the api
			miner.login(miner, miner);
			// keep mining for historical data before logging out
			while (miner.stillMining) {
				Thread.sleep(1000);
			}
			Thread.sleep(1000);
			// log out of the api
			miner.logout(miner, miner);
			// convert rates to candlesticks
			miner.convertHistoricalRatesToCandleSticks();
			// display the collected rates
			miner.displayHistory();
			candleSticksList = miner.candlesticksList;
			RsiSignals.calculateRsiForDataSet(candleSticksList, 14);
			// Tester class continued...
			for (double i = stopLossPerc; i < stopLossPerc + 0.005; i += 0.0005) { // adjust
																					// stop
																					// loss
				for (double j = takeProfitPerc; j < takeProfitPerc + 0.005; j += 0.0005) { // adjust
																							// take
																							// profit
					for (int k = bottomRSI; k <= bottomRSI + 20; k += 5) { // adjust
																			// bottom
																			// and
																			// top
																			// rsi
						Strategy rsiSignals = new RsiSignals(i, j, k);
						StrategyResult sr = ((RsiSignals) rsiSignals).runSrtategy(candleSticksList);
						strategySummary.add(sr);
					}
				}
			}
			Collections.sort(strategySummary, new StrategyResult()); // sort
																		// results
																		// list
			double avergeProfit = StrategyResult.calculateAvgStrategyProfit(strategySummary);
			System.out.println("Average profit=> " + (double) Math.round(10000 * avergeProfit) / 100 + "%");
			System.out.println("10 best results ______________________________________________________________ ");
			for (int i = 0; i < 10; i++) {
				RsiSignals rs = (RsiSignals) strategySummary.get(i).getStrategy();
				System.out.println("profit:" + (double) Math.round(strategySummary.get(i).getProfit() * 10000) / 100
						+ "%" + " | max-drawdown:"
						+ (double) Math.round(strategySummary.get(i).getMaxDrawdown() * 10000) / 100 + "%" + " | wins:"
						+ (double) Math.round(10000 * strategySummary.get(i).getWinsRatio()) / 100 + "%" + " | losses:"
						+ (double) Math.round(10000 * strategySummary.get(i).getLossesRatio()) / 100 + "%" + " | s-l:"
						+ (double) Math.round(rs.getStopLoss() * 10000) / 100 + "%" + " | t-p:"
						+ (double) Math.round(rs.getTakeProfit() * 10000) / 100 + "%" + " | bottom-rsi:"
						+ rs.getFloorRSI() + " | top-rsi:" + rs.getCeilingRSI() + "\n");
			}
			System.out.println("10 worst results _____________________________________________________________ ");
			for (int i = strategySummary.size() - 1; i > strategySummary.size() - 11; i--) {
				RsiSignals rs = (RsiSignals) strategySummary.get(i).getStrategy();
				System.out.println("profit: " + (double) Math.round(strategySummary.get(i).getProfit() * 10000) / 100
						+ "%" + " | max-drawdown:"
						+ (double) Math.round(strategySummary.get(i).getMaxDrawdown() * 10000) / 100 + "%" + " | wins:"
						+ (double) Math.round(10000 * strategySummary.get(i).getWinsRatio()) / 100 + "%" + " | losses:"
						+ (double) Math.round(10000 * strategySummary.get(i).getLossesRatio()) / 100 + "%" + " | s-l:"
						+ (double) Math.round(rs.getStopLoss() * 10000) / 100 + "%" + " | t-p:"
						+ (double) Math.round(rs.getTakeProfit() * 10000) / 100 + "%" + " | bottom-rsi:"
						+ rs.getFloorRSI() + " | top-rsi:" + rs.getCeilingRSI() + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
