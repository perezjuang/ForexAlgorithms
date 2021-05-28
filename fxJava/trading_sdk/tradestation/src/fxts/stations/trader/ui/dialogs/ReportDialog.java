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
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.IServerTimeListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.ServerTime;
import fxts.stations.util.Util;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;

/**
 * Dialog for creating and posting of reports.
 * Report dialog has wizard-like apperiance.
 */
public class ReportDialog extends ABaseDialog implements IServerTimeListener {
    private static final String[] PERIODS_NAMES = {
            "IDS_REPORT_PERIOD_SINGLE_DATE",
            "IDS_REPORT_PERIOD_MONTH",
            "IDS_REPORT_PERIOD_QUARTER",
            "IDS_REPORT_PERIOD_HALFYEAR",
            "IDS_REPORT_PERIOD_YEAR",
            "IDS_REPORT_PERIOD_RANGE"};

    /* A combo box that allows to select account */
    private SimpleAccountsComboBox mAccountComboBox;
    private JButton mBackButton;
    private JPanel mButtonPanel;
    private Calendar mCal;
    private CalendarComboBoxes mCalendarComboBegin = new CalendarComboBoxes(TradeDesk.getInst().getServerTime());
    private CalendarComboBoxes mCalendarComboEnd = new CalendarComboBoxes(TradeDesk.getInst().getServerTime());
    private int mCurrent = -1;
    private ADatePage[] mDataPages = {
            new SingleDatePage(TradeDesk.getInst().getServerTime()),
            new MonthDatePage(TradeDesk.getInst().getServerTime()),
            new QuarterDatePage(TradeDesk.getInst().getServerTime()),
            new HalfYearDatePage(TradeDesk.getInst().getServerTime()),
            new YearDatePage(TradeDesk.getInst().getServerTime()),
            new ManualDatePage(TradeDesk.getInst().getServerTime())};
    private ADatePage mDateSecondPage;
    private int mExitCode;
    private ActionListener mFinishListener = new ActionListener() {
        public void actionPerformed(ActionEvent aEvent) {
            closeDialog(JOptionPane.OK_OPTION);
        }
    };
    private JPanel mFirstPanel;
    private JButton mNextFinishButton;
    private ActionListener mNextListener = new ActionListener() {
        public void actionPerformed(ActionEvent aEvent) {
            ResourceManager resMan = TradeApp.getInst().getResourceManager();
            int i = mPeriodComboBox.getSelectedIndex();
            if (i >= 0 && i < mDataPages.length) {
                mDataPages[i].init();
                mDateSecondPage = mDataPages[i];
                String sOldTitle = getTitle();
                setTitle(sOldTitle.substring(0, sOldTitle.length() - 3) + "2/2");
                getContentPane().remove(mFirstPanel);
                getContentPane().remove(mButtonPanel);
                getContentPane().add("hfill vfill", mDataPages[i]);
                getContentPane().add("br center", mButtonPanel);
                pack();
                mCurrent = i;
                correctNextButtonEnabling();
                mBackButton.setEnabled(true);
                mNextFinishButton.setText(resMan.getString("IDS_REPORTWIZARD_FINISH"));
                mNextFinishButton.removeActionListener(this);
                mNextFinishButton.addActionListener(mFinishListener);
            }
        }
    };
    private int mPage;
    private KeySensitiveComboBox mPeriodComboBox;

    /**
     * Constructor.
     *
     * @param aOwner owner frame
     */
    public ReportDialog(Frame aOwner) {
        super(aOwner);
        try {
            mCal = Calendar.getInstance();
            mCal.setTime(TradeDesk.getInst().getServerTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mCalendarComboBegin.setDialog(this);
        mCalendarComboEnd.setDialog(this);
        adjustYear();
        try {
            ResourceManager resMan = TradeApp.getInst().getResourceManager();
            setTitle(resMan.getString("IDS_REPORTWIZARD_TITLE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle(getTitle() + " - 1/2");
        for (ADatePage dataPage : mDataPages) {
            dataPage.setCalendarComboBegin(mCalendarComboBegin);
            dataPage.setCalendarComboEnd(mCalendarComboEnd);
            dataPage.setDialog(this);
        }
        mAccountComboBox = new ReportAccountsComboBox();
        mAccountComboBox.setDialog(this);
        try {
            mAccountComboBox.init(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (mAccountComboBox.getItemCount() > 0) {
            setAccountID(mAccountComboBox.getItemAt(0).toString());
        }
        mPeriodComboBox = new KeySensitiveComboBox();
        mPeriodComboBox.setDialog(this);
        fillPeriodComboBox();
        mPeriodComboBox.setSelectedIndex(0);
        initComponents();
        pack();
    }

    /**
     * Sets up current year.
     */
    private void adjustYear() {
        int iStartYear = 2001;
        int iEndYear = mCal.get(Calendar.YEAR);
        if (iStartYear > iEndYear) {
            iStartYear = iEndYear;
        }
        mCalendarComboBegin.setStartYear(iStartYear);
        mCalendarComboBegin.setEndYear(iEndYear);
        mCalendarComboEnd.setStartYear(iStartYear);
        mCalendarComboEnd.setEndYear(iEndYear);
    }

    /**
     * Closes dialog.
     *
     * @param aExitCode code of exiting
     */
    @Override
    public void closeDialog(int aExitCode) {
        if (aExitCode == JOptionPane.OK_OPTION) {
            if (!verify()) {
                return;
            }
        }
        mExitCode = aExitCode;
        super.closeDialog(aExitCode);
    }

    private void correctNextButtonEnabling() {
        mNextFinishButton.setEnabled(isDialogEnabled() || mPage == 0);
    }

    /**
     * Method is called by App Window to notify dialog about situation when
     * its default action cannot be performed.
     * Sets/reset default button to enable state consider to internal state
     * If paramter is true, sets the dialog enable if
     * the logic of checking internal conditions to be enabled allowes that.
     * If paramtere is false, sets dialog disabled in all cases
     */
    @Override
    public void enableDialog(boolean aEnabled) {
        super.enableDialog(aEnabled);
        correctNextButtonEnabling();
    }

    /**
     * Fills perid combo box.
     */
    private void fillPeriodComboBox() {
        ResourceManager resMan = null;
        try {
            resMan = TradeApp.getInst().getResourceManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < PERIODS_NAMES.length; i++) {
            PeriodItem item = new PeriodItem(i, PERIODS_NAMES[i]);
            mPeriodComboBox.addItem(item);
            if (resMan != null) {
                item.setTitle(resMan.getString(PERIODS_NAMES[i]));
            }
        }
    }

    /**
     * @return initial or changed Account ID
     */
    public String getAccount() {
        return mAccountComboBox.getSelectedAccount();
    }

    /**
     * @return initial or changed begin date report starts from.
     */
    public String getDateFrom() {
        return mDateSecondPage.getBeginDate();
    }

    /**
     * @return initial or changed date report continues to.
     */
    public String getDateTo() {
        return mDateSecondPage.getEndDate();
    }

    /**
     * Initialisates components of of dialog.
     */
    private void initComponents() {
        getContentPane().setLayout(new RiverLayout());
        ResourceManager resMan = TradeApp.getInst().getResourceManager();
        mButtonPanel = new JPanel();
        mButtonPanel.setLayout(new RiverLayout());
        JPanel cardPanel = new JPanel();
        JPanel[] childOfCardFirst = new JPanel[2];
        childOfCardFirst[0] = new JPanel(new RiverLayout());
        childOfCardFirst[1] = new JPanel(new RiverLayout());
        childOfCardFirst[0].add("br", childOfCardFirst[1]);

        JPanel[][] childsOfCard = new JPanel[mDataPages.length][2];
        for (int i = 0; i < childsOfCard.length; i++) {
            childsOfCard[i][0] = new JPanel(new RiverLayout());
            childsOfCard[i][1] = new JPanel(new RiverLayout());
            childsOfCard[i][0].add("br", childsOfCard[i][1]);
        }
        pack();
        CardLayout cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        //First page
        mFirstPanel = new JPanel();
        mFirstPanel.setLayout(new RiverLayout());
        String s = resMan.getString("IDS_REPORTWIZARD_SELECT_ACCOUNT_AND_PERIOD");
        JLabel titleLabelFirst = UIManager.getInst().createLabel(s);
        JLabel accountLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_ACCOUNT_LABEL"));
        JLabel periodLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_PERIOD_LABEL"));
        mBackButton = UIManager.getInst().createButton(resMan.getString("IDS_REPORTWIZARD_BACK"));
        mBackButton.setEnabled(false);
        mNextFinishButton = UIManager.getInst().createButton(resMan.getString("IDS_REPORTWIZARD_NEXT"));
        JButton cancelButton = UIManager.getInst().createButton(resMan.getString("IDS_REPORTWIZARD_CANCEL"));

        mFirstPanel.add("center", titleLabelFirst);
        mFirstPanel.add("br left", accountLabel);
        mFirstPanel.add("tab hfill", mAccountComboBox);
        mFirstPanel.add("br left", periodLabel);
        mFirstPanel.add("tab hfill", mPeriodComboBox);

        mButtonPanel.add("br center", mBackButton);
        mButtonPanel.add(mNextFinishButton);
        mButtonPanel.add(cancelButton);
        Util.setAllToBiggest(new JComponent[]{mBackButton, mNextFinishButton, cancelButton});

        mBackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                ResourceManager manager = TradeApp.getInst().getResourceManager();
                mBackButton.setEnabled(false);
                mNextFinishButton.setText(manager.getString("IDS_REPORTWIZARD_NEXT"));
                setTitle(getTitle().substring(0, getTitle().length() - 3) + "1/2");
                if (mCurrent != -1) {
                    getContentPane().remove(mDataPages[mCurrent]);
                    getContentPane().remove(mButtonPanel);
                }
                getContentPane().add("hfill vfill", mFirstPanel);
                getContentPane().add("br center", mButtonPanel);
                pack();
                mPage = 0;
                correctNextButtonEnabling();
                mDateSecondPage = null;
                mNextFinishButton.removeActionListener(mFinishListener);
                mNextFinishButton.addActionListener(mNextListener);
            }
        });
        mNextFinishButton.addActionListener(mNextListener);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        //sets for exiting by escape
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        childOfCardFirst[1].add(mFirstPanel, BorderLayout.NORTH);
        cardPanel.add(childOfCardFirst[0], "first");
        for (int i = 0; i < mDataPages.length; i++) {
            childsOfCard[i][1].add(mDataPages[i], BorderLayout.NORTH);
            mDataPages[i].init();
            cardPanel.add(childsOfCard[i][0], "second_" + i);
        }
        getContentPane().add("hfill vfill", mFirstPanel);
        getContentPane().add("br center", mButtonPanel);
        pack();
        cardLayout.show(cardPanel, "first");
        mPage = 0;
        correctNextButtonEnabling();
    }

    /**
     * Sets initial Account ID.
     *
     * @param aAccountID identifier of the account
     */
    public void setAccountID(String aAccountID) {
        if (mAccountComboBox != null) {
            if (aAccountID != null) {
                mAccountComboBox.selectAccount(aAccountID);
            } else {
                mAccountComboBox.setSelectedIndex(0);
            }
        }
    }

    /**
     * Shows dialog at modal state.
     */
    @Override
    public int showModal() {
        mExitCode = JOptionPane.CLOSED_OPTION;
        setModal(true);
        setLocationRelativeTo(getOwner());
        TradeDesk.getInst().addServerTimeListener(this);
        setVisible(true);
        TradeDesk.getInst().removeServerTimeListener(this);
        return mExitCode;
    }

    /**
     * Is called when the Server time is changed.
     *
     * @param aServerTime current server time
     */
    public void timeUpdated(ServerTime aServerTime) {
        mCal.setTime(aServerTime);
        mCalendarComboBegin.setTodayDate(aServerTime);
        mCalendarComboEnd.setTodayDate(aServerTime);
        if (mCal.get(Calendar.YEAR) == mCalendarComboBegin.getEndYear()) {
            return;
        }
        Date curDateBegin = mCalendarComboBegin.getDate();
        Date curDateEnd = mCalendarComboEnd.getDate();
        adjustYear();
        mCalendarComboBegin.setDate(curDateBegin);
        mCalendarComboEnd.setDate(curDateEnd);
    }

    /* Verifies user input. */
    private boolean verify() {
        Calendar instance = Calendar.getInstance();
        instance.roll(Calendar.DATE, true);
        //xxx can go one more day in future for current time period request
        boolean rc = true;
        if (mCalendarComboEnd.getDate().compareTo(mCalendarComboBegin.getDate()) < 0
            || mCalendarComboBegin.getDate().getTime() > instance.getTime().getTime()) {
            ResourceManager resMan = TradeApp.getInst().getResourceManager();
            String title = resMan.getString("IDS_MAINFRAME_SHORT_TITLE");
            String message = resMan.getString("IDS_REPORT_ERROR_MESSAGE_INVALID_PERIOD");
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            if (mDateSecondPage != null) {
                mDateSecondPage.requestFocus();
                rc = false;
            }
        }
        return rc;
    }

    /**
     * Class represents item for differents types of periods
     * (month, year, half-year, etc.)
     */
    private static class PeriodItem {
        /**
         * index of item
         */
        private int mIndex;
        /**
         * title of item will be shown in combo box
         */
        private String mTitle;

        /**
         * Constructor.
         *
         * @param aIndex zero based index of item in check box
         * @param aTitle title of item will be shown into combo box
         */
        PeriodItem(int aIndex, String aTitle) {
            mIndex = aIndex;
            mTitle = aTitle;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         */
        @Override
        public boolean equals(Object aObject) {
            return aObject != null && mTitle.equals(aObject.toString());
        }

        /**
         * @return index of item
         */
        public int getIndex() {
            return mIndex;
        }

        /**
         * @param aIndex index of item
         */
        public void setIndex(int aIndex) {
            mIndex = aIndex;
        }

        /**
         * @return title of item will be shown in combo box
         */
        public String getTitle() {
            return mTitle;
        }

        /**
         * @param aTitle title of item will be shown in combo box
         */
        public void setTitle(String aTitle) {
            mTitle = aTitle;
        }

        /**
         * returns string will be shown in combo box
         */
        @Override
        public String toString() {
            return mTitle;
        }
    }
}
