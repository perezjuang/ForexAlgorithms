package pulling.historical.prices.stochastic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import com.fxcm.fix.Instrument;
import com.fxcm.fix.UTCDate;
import com.fxcm.fix.UTCTimeOnly;
import com.fxcm.fix.UTCTimestamp;
import com.fxcm.fix.entity.MarketDataSnapshot;

public class RatesToCandlesticks {
	
	public void convertHistoricalRatesToCandleSticks(HashMap<UTCDate, MarketDataSnapshot> historicalRatesGeneral, List<CandleStick> candleStickListGeneral) {
		SortedSet<UTCDate> dateList = new TreeSet<>(historicalRatesGeneral.keySet());
		SimpleDateFormat sdf= new SimpleDateFormat("dd.MM.yyyy HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));

		for(UTCDate date : dateList) {

			MarketDataSnapshot candleData;

			candleData = historicalRatesGeneral.get(date);
			//convert the key to a Date
			Date candleDate = date.toDate();
			String sdfDate = sdf.format(candleDate);
			double open = candleData.getBidOpen();
			double low = candleData.getBidLow();
			double high = candleData.getAskHigh();
			double closeBid = candleData.getBidClose();
			double closeAsk = candleData.getAskClose();


			CandleStick candleStick = new CandleStick(sdfDate, open, low, high, closeBid, closeAsk);
			candleStickListGeneral.add(candleStick);
		}
	}
}
