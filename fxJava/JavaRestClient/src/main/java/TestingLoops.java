package build.strategy.stochastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pulling.historical.prices.stochastic.CandleStick;
import pulling.historical.prices.stochastic.Utilities;

public class TestingLoops {

	public static void main(String[] args) {


		List<Integer> list1 = new ArrayList<>();
		List<Integer> list2 = new ArrayList<>();
		for (int i = 1; i < 31; i++) {
			list1.add(i);
		}
		for (int j = 1; j < 11; j++) {
			list2.add(j);
		}
		int counter;
		
		int looper = 0;
		 for (int i = looper; i < list2.size(); i++) {
			  for (int j= 0; j < list1.size(); j++) {
				  if(j == 0 || j == 1){
					 continue;
				  }
				if( i  % 3 == 0) {
					System.out.println(j + "AND" + i);
					System.out.println(j-1 + "AND" + i);
					System.out.println(j-2 + "AND" + i);
				    looper++;
				}
			}
		}
			
		
		
		// 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30
		//     1     2     3       4         5        6        7        8        9       10
		System.out.println(list1.subList(0, 3));
		
	}
}
