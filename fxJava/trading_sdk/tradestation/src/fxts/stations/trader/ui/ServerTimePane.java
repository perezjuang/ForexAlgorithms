/*
 * Copyright 2006 FXCM LLC
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
 * $History: $
 *
 * 09/08/2003   KAV     Initial creation
 * 4/30/2004    USHIK   fixed BUG of TT #1463 RapidFX - new: Mem usage High
 *
 */
package fxts.stations.trader.ui;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.IServerTimeListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.ServerTime;

import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * This is status bar pane that is used to show server time.
 * This class listens the TradeDesk for changins of time.
 */
public class ServerTimePane extends JLabel implements ILocaleListener {
    /**
     * Is the first tick?
     */
    private boolean mFirstTick = true;

    /**
     * Time line format
     */
    private SimpleDateFormat mFormatter;

    /**
     * Update label thread.
     */
    private IServerTimeListener mServerTimeListener;
    /**
     * Localized text.
     */
    private String mText;
    /* Runnable interface instance for updating time field at status bar */
    private TextUpdater mTextUpdater;
    /**
     * Formated time string.
     */
    private String mTime;

    /**
     * Constructor.
     */
    public ServerTimePane() {
        setText("");
        mTextUpdater = new TextUpdater();
    }

    @Override
    protected void paintComponent(Graphics aGraphics) {
        if (UIManager.getInst().isAAEnabled()) {
            Graphics2D g2d = (Graphics2D) aGraphics;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        super.paintComponent(aGraphics);
    }

    /**
     * This method is called when the locale of the aMan is changed.
     */
    public void onChangeLocale(ResourceManager aMan) {
        if (TradeDesk.getInst().getServerTime() != ServerTime.UNKNOWN) {
            mText = aMan.getString("IDS_SERVER_TIME");
            mFormatter = new SimpleDateFormat(aMan.getString("IDS_SERVER_TIME_FORMAT"));
            String sTime = mFormatter.format(TradeDesk.getInst().getServerTime());
            //if time is changed
            setText(mText + " " + sTime);
            mTime = sTime;
            updateWidth();
        }
    }

    /**
     * Starts timer thread.
     */
    public void startTimer() {
        //gets instance of application
        TradeApp app = TradeApp.getInst();
        //gets resouce manager
        ResourceManager resmng = app.getResourceManager();
        //sets localized text
        mText = resmng.getString("IDS_SERVER_TIME");
        mFirstTick = true;
        mFormatter = new SimpleDateFormat(resmng.getString("IDS_SERVER_TIME_FORMAT"));
        TimeZone tz = TimeZone.getTimeZone(TradingServerSession.getInstance().getParameterValue("BASE_TIME_ZONE"));
        if (tz != null) {
            mFormatter.setTimeZone(tz);
        }
        mServerTimeListener = new IServerTimeListener() {
            public void timeUpdated(ServerTime aTime) {
                String sTime = mFormatter.format(aTime);
                //if time is changed
                if (!sTime.equals(mTime)) {
                    mTextUpdater.setTimeString(sTime);
                    EventQueue.invokeLater(mTextUpdater);
                }
            }
        };
        try {
            TradeDesk.getInst().addServerTimeListener(mServerTimeListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops timer thread. Returns when the thread is finished.
     */
    public void stopTimer() {
        setText("");
        IServerTimeListener tmp = mServerTimeListener;
        mServerTimeListener = null;
        if (tmp != null) {
            try {
                TradeDesk.getInst().removeServerTimeListener(tmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates width of components.
     */
    protected void updateWidth() {
        try {
            Dimension oldSize = getSize();
            String text = getText();
            if (text != null) {
                FontMetrics fm = getGraphics().getFontMetrics(getFont());
                int width = fm.stringWidth(text);
                setSize(new Dimension(width + 15, (int) oldSize.getHeight()));
                setPreferredSize(new Dimension(width + 15, (int) oldSize.getHeight()));
            }
        } catch (Exception e) {
            //
        }
    }

    private class TextUpdater implements Runnable {
        private String mTimeString;

        public void run() {
            setText(mText + " " + mTimeString);
            mTime = mTimeString;
            if (mFirstTick) {
                updateWidth();
            }
            mFirstTick = false;
        }

        void setTimeString(String aTime) {
            mTimeString = aTime;
        }
    }
}
