package build.strategy.stochastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.plaf.basic.BasicBorders.MarginBorder;

import build.strategy.stochastic.StrategyResult;
import pulling.historical.prices.stochastic.CandleStick;
import pulling.historical.prices.stochastic.Utilities;

public class RangeStrategy implements Strategy {

	private double stopLoss;
	private double takeProfit;
	private int supportLevel;
	private int resistanceLevel;
	static List<Double> listOfStochasticsK = new ArrayList<>();

	public RangeStrategy(double stopLoss, double takeProfit, int bottomLimit) {
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
		this.supportLevel = supportLevel;
		this.resistanceLevel = 100 - resistanceLevel;
	}

	public RangeStrategy() {
		// TODO Auto-generated constructor stub
	}


	public double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public int getBottomLimit() {
		return supportLevel;
	}

	public void setBottomLimit(int bottomLimit) {
		this.supportLevel = bottomLimit;
	}

	public int getUpperLimit() {
		return resistanceLevel;
	}

	public void setUpperLimit(int upperLimit) {
		this.resistanceLevel = upperLimit;
	}

	public static void calculateStochasticsK(List<CandleStick> candleSticksList, int duration) {
		double theCurrentClose = 0;
		double theHighestHigh = 0;
		double theLowestLow = 0;
		double stochasticK = 0;
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
	}

	public static void calculateStochasticsD(List<CandleStick>candleSticksList, int duration) {
		int counter = 0;
		List<Double> listOfStochasticD = new ArrayList<>();
		double stochasticD = 0;
		for (int j = 2; j <  listOfStochasticsK.size()+1; j++) {
			if(j % 3 == 0) {
				stochasticD = ( listOfStochasticsK.get(j-1) + listOfStochasticsK.get(j-2) +  listOfStochasticsK.get(j-3))/3;
				listOfStochasticD.add(stochasticD);
				counter++;
			}
		}
		stochasticD = Utilities.addAllNumbers(listOfStochasticD)/counter;
		for (int i = 0; i < candleSticksList.size(); i++) {
			candleSticksList.get(i).setStochasticD(stochasticD);
		}

	}

	@Override
	public StrategyResult runRangeStrategyWithStochastics(List<CandleStick> candleSticksList) {
		double entryBuy;
		double entrySell;
		double stopLossPrice=0;
		double takeProfitPrice=0;
		double strategyProfit=0;
		double maxProfit=0;
		double maxDrawdown=0;
		boolean isOpenPosition=false;
		int winCounter=0;
		int lossCounter=0;


		for (int i = 250; i < candleSticksList.size(); i++) {

			if(isOpenPosition) {
				if(stopLossPrice<takeProfitPrice) {
					if(candleSticksList.get(i).getLow()<stopLossPrice) {
						isOpenPosition=false;
						lossCounter++;
						strategyProfit-=stopLoss;
						maxDrawdown=calculateMaxDrawdown(maxProfit, strategyProfit, maxDrawdown);
					}else if(candleSticksList.get(i).getHigh()>takeProfitPrice) {
						isOpenPosition=false;
						winCounter++;
						strategyProfit+=takeProfit;
						maxProfit=updateMaxProfit(strategyProfit,maxProfit);
					}
				}else {
					if(candleSticksList.get(i).getHigh()>stopLossPrice) {
						isOpenPosition=false;
						lossCounter++;
						strategyProfit-=stopLoss;
					}else if(candleSticksList.get(i).getHigh()>stopLossPrice) {
						isOpenPosition=false;
						winCounter++;
						strategyProfit+=takeProfit;
						maxProfit=updateMaxProfit(strategyProfit, maxProfit);
					}
				}
				
			}else if(candleSticksList.get(i).getStochasticK()>supportLevel && candleSticksList.get(i).getStochasticK()>supportLevel) {
				entryBuy=candleSticksList.get(i).getCloseAsk();
				stopLossPrice= ((1-stopLoss)*entryBuy);
				takeProfitPrice = ((1+takeProfit)*entryBuy);
				isOpenPosition=true;
			}else if(candleSticksList.get(i).getStochasticK()<resistanceLevel && candleSticksList.get(i).getStochasticD()<resistanceLevel ) {
				entrySell=candleSticksList.get(i).getCloseBid();
				stopLossPrice=((1+stopLoss)*entrySell);
				takeProfitPrice=((1-takeProfit)*entrySell);
				isOpenPosition=true;
			}else if(candleSticksList.get(i).getStochasticK() == candleSticksList.get(i).getStochasticD()){
				entryBuy=candleSticksList.get(i).getCloseAsk();
				stopLossPrice= ((1-stopLoss)*entryBuy);
				takeProfitPrice = ((1+takeProfit)*entryBuy);
				isOpenPosition=true;
			}else if(candleSticksList.get(i).getStochasticK() == candleSticksList.get(i).getStochasticD() && candleSticksList.get(i).getStochasticK()<supportLevel && candleSticksList.get(i).getStochasticD()<supportLevel){
				entryBuy=candleSticksList.get(i).getCloseAsk();
				stopLossPrice= ((1-stopLoss)*entryBuy);
				takeProfitPrice = ((1+takeProfit)*entryBuy);
				isOpenPosition=true;
			}else if(candleSticksList.get(i).getStochasticK() == candleSticksList.get(i).getStochasticD() && candleSticksList.get(i).getStochasticK()>supportLevel && candleSticksList.get(i).getStochasticD()>supportLevel){
				entrySell=candleSticksList.get(i).getCloseBid();
				stopLossPrice=((1+stopLoss)*entrySell);
				takeProfitPrice=((1-takeProfit)*entrySell);
				isOpenPosition=true;
			}else if(candleSticksList.get(i).getStochasticK()<supportLevel){ 
				entryBuy=candleSticksList.get(i).getCloseAsk(); 
				stopLossPrice=((1-stopLoss)*entryBuy);
				takeProfitPrice=((1+takeProfit)*entryBuy);
				isOpenPosition=true;
			}else if(candleSticksList.get(i).getStochasticK()>resistanceLevel){ 
				entrySell=candleSticksList.get(i).getCloseBid (); 
				stopLossPrice=((1+stopLoss)*entrySell);
				takeProfitPrice=((1-takeProfit)*entrySell);
				isOpenPosition=true;
			}
		}
		StrategyResult sr = new StrategyResult(strategyProfit, maxProfit, maxDrawdown, winCounter, lossCounter, this);
		return sr;
	}
	private static double updateMaxProfit(double strategyProfit, double maxProfit) {
		if(strategyProfit>maxProfit) {
			maxProfit=strategyProfit;
		}
		return maxProfit;
	}

	private static double calculateMaxDrawdown(double maxProfit, double strategyProfit, double maxDrawdown) {
		double currentDrawdown = ((1+strategyProfit)-(1+maxProfit))/(1+maxProfit);
		if(currentDrawdown<maxDrawdown) {
			maxDrawdown=currentDrawdown;
		}
		return maxDrawdown;
	}
}
