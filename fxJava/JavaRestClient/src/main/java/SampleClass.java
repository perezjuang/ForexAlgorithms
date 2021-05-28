package test.strategy.stochastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.fxcm.external.api.transport.sso.SSOAuthenticator;
import com.fxcm.fix.Instrument;
import com.fxcm.fix.UTCDate;
import com.fxcm.fix.UTCTimeOnly;

import build.strategy.stochastic.RangeStrategy;
import pulling.historical.prices.stochastic.HistoryMiner;
import pulling.historical.prices.stochastic.Utilities;
import pulling.historical.prices.stochastic.CandleStick;

public class SampleClass {
	public static void main(String[] args){
		UTCDate startDate;
		UTCTimeOnly startTime;
		int duration = 21;
		List<CandleStick> candleSticksList = null;
		Instrument asset = new Instrument("EUR/USD");
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
			candleSticksList = miner.candleStickList;

			List<Double> allCloseBids = new ArrayList<>();
			List<Double> allCloseBidsAfter = new ArrayList<>();
			System.out.println("************STEP 1**********************");

			//The Stochastic K			
			double theCurrentClose = 0;
			double theHighestHigh = 0;
			double theLowestLow = 0;
			double stochasticK = 0;
			List<Double> listOfStochasticK = new ArrayList<>();
			for (int i = 0; i < candleSticksList.size(); i++) {
				//theCurrentClose
				theCurrentClose = candleSticksList.get(i).getCloseBid();
				//the Lowest Low
				List<Double> allListedLows = new ArrayList<>();
				allListedLows.add(candleSticksList.get(i).getLow());
				theLowestLow = candleSticksList.get(0).getLow();
				if(theLowestLow>candleSticksList.get(i).getLow()) {
					theLowestLow = candleSticksList.get(i).getLow();
				}
				//the Highest High
				theHighestHigh  = candleSticksList.get(0).getHigh();
				if(theHighestHigh <candleSticksList.get(i).getHigh()) {
					theHighestHigh  = candleSticksList.get(i).getHigh();
				}
				stochasticK = ((theCurrentClose - theLowestLow) / (theHighestHigh - theLowestLow)) * 100;
				candleSticksList.get(i).setStochasticK(stochasticK);
			}
			for (int i = 0; i < candleSticksList.size(); i++) {
				listOfStochasticK.add(candleSticksList.get(i).getStochasticK());
			}

			// FROM HERE STARTS WHAT I NEED
			//StochasticD
			System.out.println(Arrays.asList(listOfStochasticK) + "List of StochasticK");
			List<Double> listOfStochasticD = new ArrayList<>();
			double stochasticD = 0;
			for (int j = 2; j <  listOfStochasticK.size()+1; j++) {
				if(j % 3 == 0) {
					stochasticD = ( listOfStochasticK.get(j-1) + listOfStochasticK.get(j-2) + listOfStochasticK.get(j-3))/3;
					listOfStochasticD.add(stochasticD);
					System.out.println(stochasticD + "Stochastic D");
					System.out.println(Arrays.asList(listOfStochasticD) +  " List of Stochastic D");
				}
			}
			System.out.println(Arrays.asList(listOfStochasticD) +  " List of Stochastic D OUTSIIIIIIIIDE");
			//Assigning StochasticD
//			int counter = 0;
//			for (int i = counter; i < listOfStochasticD.size(); i++){
//				counter++;
//				for (int j = 0; j < candleSticksList.size(); j++) {
//					if (j % 3 == 0) {
//						candleSticksList.get(j).setStochasticD(listOfStochasticD.get(counter));
//						System.out.println(counter + "Counter");
//						System.out.println(j + " J");
//					}
//				}
//			}

			for (int i = 0; i < candleSticksList.size(); i++) {
				System.out.println(candleSticksList.get(i) + "Element");
				System.out.println(candleSticksList.get(i).getStochasticD() + "The StochasticD of the Element");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}