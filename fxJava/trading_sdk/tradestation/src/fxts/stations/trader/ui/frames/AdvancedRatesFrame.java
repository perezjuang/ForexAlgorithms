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
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.core.IClickModel;
import fxts.stations.core.RatePanel;
import fxts.stations.core.Rates;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Rate;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ISignal;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.Signaler;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.signals.ChangeSignal;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Andre Mermegas
 *         Date: Jan 5, 2006
 *         Time: 2:01:39 PM
 */
public class AdvancedRatesFrame<E> extends RatesFrame<E> {
    private ComponentAdapter mComponentAdaptor;
    private MouseAdapter mMouseAdapter;
    private JPanel mRatePanel;
    private Map<String, RatePanel> mRatePanelMap;

    /**
     * @param aMan resource manager
     * @param aMainFrame mainframe
     */
    public AdvancedRatesFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(aMan, aMainFrame);
        mRatePanelMap = new TreeMap<String, RatePanel>();
        mRatePanel = new JPanel(true);
        mRatePanel.setPreferredSize(new Dimension(100, 0));

        JScrollPane advancedScrollpane = new JScrollPane(mRatePanel);
        advancedScrollpane.getVerticalScrollBar().setUnitIncrement(10);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Advanced", advancedScrollpane);
        tabbedPane.addTab("Simple", mScrollPane);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_A);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_S);
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent aEvent) {
                JTabbedPane source = (JTabbedPane) aEvent.getSource();
                UserPreferences.getUserPreferences().set("selectedRateTab", source.getSelectedIndex());
            }
        });
        tabbedPane.setSelectedIndex(UserPreferences.getUserPreferences().getInt("selectedRateTab"));
        getContentPane().removeAll();
        getContentPane().add(tabbedPane);
        mComponentAdaptor = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent aEvent) {
                RatePanel rp = (RatePanel) mRatePanel.getComponent(mRatePanel.getComponentCount() - 1);
                mRatePanel.setPreferredSize(new Dimension(100, (int) (rp.getY() * 1.3)));
            }

            @Override
            public void componentShown(ComponentEvent aEvent) {
                RatePanel rp = (RatePanel) mRatePanel.getComponent(mRatePanel.getComponentCount() - 1);
                mRatePanel.setPreferredSize(new Dimension(100, (int) (rp.getY() * 1.3)));
            }
        };
        getContentPane().addComponentListener(mComponentAdaptor);

        Rates rates = TradeDesk.getInst().getRates();
        Enumeration enumeration = rates.elements();
        while (enumeration.hasMoreElements()) {
            Rate rate = (Rate) enumeration.nextElement();
            RatePanel rp = createPanel(rate);
            if (rate.isSubscribed()) {
                mRatePanel.add(rp);
            }
        }

        final JPopupMenu ccypop = UIManager.getInst().createPopupMenu();
        ccypop.add(getCCYSubsMenu());
        getScrollPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent aEvent) {
                ccypop.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
            }
        });

        setTitle(getLocalizedTitle(aMan));
        //sets icon to internal frame
        URL iconUrl = aMan.getResource("ID_RATES_FRAME_ICON");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setFrameIcon(icon);
        }

        // Set the component to show the popup menu
        final JPopupMenu menu = UIManager.getInst().createPopupMenu();
        menu.add(getCCYSubsMenu());
        mMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent aEvent) {
                if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                    menu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                }
            }
        };
        mRatePanel.addMouseListener(mMouseAdapter);
    }

    private RatePanel createPanel(Rate aRate) {
        RatePanel rp = new RatePanel(aRate, this);
        if (getSelectedCurrency() == null) {
            setSelectedCurrency(rp.getCurrency());
            rp.setSelected();
        } else if (getSelectedCurrency().equals(rp.getCurrency())) {
            setSelectedCurrency(rp.getCurrency());
            rp.setSelected();
        }
        final JPopupMenu menu = UIManager.getInst().createPopupMenu();
        menu.add(getCCYSubsMenu());
        rp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent aEvent) {
                if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                    menu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                }
            }
        });
        mRatePanelMap.put(aRate.getCurrency(), rp);
        return rp;
    }

    /**
     * Returns localized title.
     *
     * @param aResourceMan current resource manager
     */
    @Override
    protected String getLocalizedTitle(ResourceManager aResourceMan) {
        if (aResourceMan != null) {
            String mode = UserPreferences.getUserPreferences().getString(IClickModel.TRADING_MODE);
            int ratesLength = mRatePanel == null ? 0 : mRatePanel.getComponents().length;
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_RATES_TITLE"));
            if (ratesLength > 0) {
                titleBuffer.append(" (").append(ratesLength).append(")");
            }
            if (IClickModel.SINGLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ ONE CLICK TRADING ~~~~~~");
            } else if (IClickModel.DOUBLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ DOUBLE CLICK TRADING ~~~~~~");
            }
            return titleBuffer.toString();
        }
        mLogger.debug("Error: AdvancedRatesFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    @Override
    protected void onCloseFrame(InternalFrameEvent aEvent) {
        super.onCloseFrame(aEvent);
        getContentPane().removeAll();
        getContentPane().removeComponentListener(mComponentAdaptor);
        mRatePanelMap.clear();
        mRatePanel.removeMouseListener(mMouseAdapter);
        mRatePanel.removeAll();
        mRatePanel.removeNotify();
        removeAll();
        removeNotify();
    }

    @Override
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        super.onSignal(aSrc, aSignal);
        if (aSignal != null && aSignal instanceof ChangeSignal) {
            ChangeSignal cs = (ChangeSignal) aSignal;
            Rate rate = (Rate) cs.getNewElement();
            if (mRatePanelMap != null) {
                RatePanel rp = mRatePanelMap.get(rate.getCurrency());
                if (rp != null) {
                    rp.updateRate(rate);
                }
            }
        }
    }

    @Override
    protected void refresh() {
        super.refresh();
        mRatePanel.removeAll();
        Enumeration enumeration = mSubscribedRates.elements();
        while (enumeration.hasMoreElements()) {
            Rate rate = (Rate) enumeration.nextElement();
            mRatePanel.add(mRatePanelMap.get(rate.getCurrency()));
        }
        setTitle(getLocalizedTitle(mResourceManager));
        mRatePanel.revalidate();
        mRatePanel.repaint();
    }

    @Override
    public void setSelectedCurrency(String aSelectedCurrency) {
        super.setSelectedCurrency(aSelectedCurrency);
        String[] keys = mRatePanelMap.keySet().toArray(new String[mRatePanelMap.size()]);
        for (String key : keys) {
            RatePanel rp = mRatePanelMap.get(key);
            if (getSelectedCurrency().equals(key)) {
                rp.setSelected();
            } else {
                rp.setUnselected();
            }
        }
    }
}
