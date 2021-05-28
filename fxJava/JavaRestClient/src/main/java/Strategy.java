package build.strategy.stochastic;

import java.util.List;

import pulling.historical.prices.stochastic.CandleStick;

public interface Strategy {
		
	StrategyResult runRangeStrategyWithStochastics(List<CandleStick> candleStickList);
}
