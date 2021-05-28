package pulling.historical.prices.stochastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.text.StyledEditorKit.ForegroundAction;

public class Utilities { 

	public static void writeString(String text) {
		System.out.println(text);
	}

	public static Double addAllNumbers(List<Double> twentyDayHighs){
		double sumOfNumbers = twentyDayHighs.stream().mapToDouble(Double::doubleValue).sum();
		return sumOfNumbers;
	} 

//	public static Double calculateTheCurrentClose(List<CandleStick> candleSticksList, int duration) {
//		double theCurrentClose = 0;
//		for (int i = 0; i < candleSticksList.size(); i++) {
//			theCurrentClose = candleSticksList.get(i).getCloseBid();
//		}
//		
//		return theCurrentClose;
//	}
	public static Double calculateTheLowestLow(List<CandleStick>candleSticksList, int duration) {
		double min = 0;
		List<Double> allListedLows = new ArrayList<>();
		for (int j = 0; j <candleSticksList.size(); j++) {
			allListedLows.add(candleSticksList.get(j).getLow());
			min = candleSticksList.get(0).getLow();
			if(min>candleSticksList.get(j).getLow()) {
				min = candleSticksList.get(j).getLow();
			}
		}
		return min;
	}
	public static Double calculateTheHighestHigh(List<CandleStick> candleSticksList, int duration ) {
		double max = 0;
		for (int i = 0; i < candleSticksList.size(); i++) {
			max = candleSticksList.get(0).getHigh();
			if(max<candleSticksList.get(i).getHigh()) {
				max = candleSticksList.get(i).getHigh();
			}
		}
		return max;
	}
}
