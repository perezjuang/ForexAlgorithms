package com.fxcm.strategy.buildstrategy;

import java.util.List;

import com.fxcm.strategy.rsi.priceretrieve.CandleStick;

public interface Strategy {
	StrategyResult runSrtategy(List <CandleStick> candleStickList);
}
