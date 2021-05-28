package test.strategy.stochastic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.fxcm.fix.Instrument;
import com.fxcm.fix.UTCDate;
import com.fxcm.fix.UTCTimeOnly;

import build.strategy.stochastic.RangeStrategy;
import build.strategy.stochastic.Strategy;
import build.strategy.stochastic.StrategyResult;
import pulling.historical.prices.stochastic.*;

public class TestingClass {
	public static void main(String[] args){
		UTCDate startDate;
		UTCTimeOnly startTime;
		int bottomLimit = 80;
		double stopLoss = 0.003;
		double takeProfit = 0.003;
		List<CandleStick> candleSticksList;
		List<StrategyResult> strategySummary = new ArrayList<StrategyResult>();
		Instrument asset = new Instrument("AUD/USD");
		Calendar instance = Calendar.getInstance();
		instance.roll(Calendar.DAY_OF_YEAR, -30);
		startDate = new UTCDate(instance.getTime());
		startTime = new UTCTimeOnly(instance.getTime());

		try{
			HistoryMiner miner = new HistoryMiner("D25611997", "6925", "Demo", startDate, startTime, asset);
			miner.login(miner,miner);
			while(miner.stillMining) {
				Thread.sleep(1000);
			}
			Thread.sleep(1000);
			miner.logout(miner,miner);
			miner.convertHistoricalRatesToCandleSticks();
			miner.displayHistory();

			candleSticksList=miner.candleStickList;
			RangeStrategy.calculateStochasticsD(candleSticksList, 20);
			RangeStrategy.calculateStochasticsK(candleSticksList, 20);
			for (double i = stopLoss; i < stopLoss+0.010 ; i+=0.0005) { 
				for (double j = takeProfit; j < takeProfit+0.05; j+=0.0005) { 
					for (int k = bottomLimit; k <= bottomLimit+20; k+=5) { 
						Strategy rangeStrategy = new RangeStrategy(i, j, k);
						StrategyResult sr = ((RangeStrategy) rangeStrategy).runRangeStrategyWithStochastics(candleSticksList);
						strategySummary.add(sr);
					}
				}
			}
			
			Collections.sort(strategySummary, new StrategyResult()); 
			double avergeProfit=StrategyResult.calculateAvgStrategyProfit(strategySummary);
			System.out.println("Average profit=> "+(double)Math.round(10000*avergeProfit)/100+"%");
			
			System.out.println("10 best results ______________________________________________________________ ");
			for (int i = 0; i < 10; i++) {
			RangeStrategy rs = (RangeStrategy) strategySummary.get(i).getStrategy();
			System.out.println("profit:"+(double)Math.round(strategySummary.get(i).getProfit()*10000)/100 +"%" +
			" | max-drawdown:"+(double)Math.round(strategySummary.get(i).getMaxDrawdown()*10000)/100 +"%" +
			" | wins:"+(double)Math.round(10000*strategySummary.get(i).getWinsRatio())/100 + "%" +
			" | losses:"+(double)Math.round(10000*strategySummary.get(i).getLossesRatio())/100 + "%" +
			" | s-l:"+(double)Math.round(rs.getStopLoss()*10000)/100 +"%" +
			" | t-p:"+(double)Math.round(rs.getTakeProfit()*10000)/100 +"%" +
			" | bottom-rsi:"+ rs.getBottomLimit() +
			" | top-rsi:"+ rs.getUpperLimit() + "\n");
			}
			
			System.out.println("10 worst results _____________________________________________________________ ");
			for (int i = strategySummary.size()-1; i > strategySummary.size()-11; i--) {
			RangeStrategy rs = (RangeStrategy) strategySummary.get(i).getStrategy();
			System.out.println("profit: "+(double)Math.round(strategySummary.get(i).getProfit()*10000)/100+"%" +
			" | max-drawdown:"+(double)Math.round(strategySummary.get(i).getMaxDrawdown()*10000)/100 +"%" +
			" | wins:"+(double)Math.round(10000*strategySummary.get(i).getWinsRatio())/100 + "%" +
			" | losses:"+(double)Math.round(10000*strategySummary.get(i).getLossesRatio())/100 + "%" +
			" | s-l:"+(double)Math.round(rs.getStopLoss()*10000)/100 +"%" +
			" | t-p:"+(double)Math.round(rs.getTakeProfit()*10000)/100 +"%" +
			" | bottom-rsi:"+rs.getBottomLimit() +
			" | top-rsi:"+rs.getUpperLimit() + "\n");
			}

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}