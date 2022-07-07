/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/frames/ComparatorFactory.java#1 $
 *
 * Copyright (c) 2008 FXCM, LLC.
 * 32 Old Slip, New York NY, 10005 USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Andre Mermegas
 * Created: Apr 4, 2008 2:03:53 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.frames;

import java.util.Comparator;

/**
 */
public class ComparatorFactory<E> {
    private Comparator mDateComparator;
    private Comparator mDoubleComparator;
    private Comparator mIntComparator;
    private Comparator mStringComparator;
    private ATableFrame mTableFrame;

    public ComparatorFactory(ATableFrame aTableFrame) {
        mTableFrame = aTableFrame;
        mStringComparator = new StringComparator<E>() {
            public boolean isDescendingMode() {
                return mTableFrame.isDescendingMode();
            }

            public Object getValue1(E aElement) {
                return mTableFrame.getComparatorValue(aElement);
            }

            public Object getValue2(E aElement) {
                return mTableFrame.getComparatorValue(aElement);
            }
        };

        mIntComparator = new IntComparator<E>() {
            public boolean isDescendingMode() {
                return mTableFrame.isDescendingMode();
            }

            public Object getValue1(E aElement) {
                return mTableFrame.getComparatorValue(aElement);
            }

            public Object getValue2(E aElement) {
                return mTableFrame.getComparatorValue(aElement);
            }
        };

        mDoubleComparator = new DoubleComparator<E>() {
            public boolean isDescendingMode() {
                return mTableFrame.isDescendingMode();
            }

            public Object getValue1(E aElement) {
                return mTableFrame.getComparatorValue(aElement);
            }

            public Object getValue2(E aElement) {
                return mTableFrame.getComparatorValue(aElement);
            }
        };

        mDateComparator = new DateComparator<E>() {
            public boolean isDescendingMode() {
                return mTableFrame.isDescendingMode();
            }

            public Object getValue1(E aElement) {
                return mTableFrame.getComparatorValue(aElement);
            }

            public Object getValue2(E aElement) {
                return mTableFrame.getComparatorValue(aElement);
            }
        };
    }

    public Comparator getDateComparator() {
        return mDateComparator;
    }

    public Comparator getDoubleComparator() {
        return mDoubleComparator;
    }

    public Comparator getIntComparator() {
        return mIntComparator;
    }

    public Comparator getStringComparator() {
        return mStringComparator;
    }
}
