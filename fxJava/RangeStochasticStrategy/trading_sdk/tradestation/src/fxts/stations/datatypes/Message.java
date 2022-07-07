/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/datatypes/Message.java#1 $
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
 * Created: Jul 6, 2007 12:09:05 PM
 *
 * $History: $
 */
package fxts.stations.datatypes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements IKey {
    private Date mDate;
    private String mFrom;
    private String mFullText;
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    private String mText;

    public Message(Date aDate, String aFrom, String aText, String aFullText) {
        mDate = (Date) aDate.clone();
        mFrom = aFrom;
        if (aText == null || "".equals(aText.trim())) {
            mText = aFullText;
        } else {
            mText = aText;
        }
        mFullText = aFullText;
    }

    public Date getDate() {
        return (Date) mDate.clone();
    }

    public String getFrom() {
        return mFrom;
    }

    public void setFrom(String aFrom) {
        mFrom = aFrom;
    }

    public String getFullText() {
        return mFullText;
    }

    public void setFullText(String aFullText) {
        mFullText = aFullText;
    }

    public Object getKey() {
        return mSimpleDateFormat.format(getDate());
    }

    public String getText() {
        return mText;
    }

    public void setText(String aText) {
        mText = aText;
    }

    public void setDate(Date aDate) {
        mDate = (Date) aDate.clone();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Message");
        sb.append("{mDate=").append(mDate);
        sb.append(", mFrom='").append(mFrom).append('\'');
        sb.append(", mFullText='").append(mFullText).append('\'');
        sb.append(", mText='").append(mText).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
