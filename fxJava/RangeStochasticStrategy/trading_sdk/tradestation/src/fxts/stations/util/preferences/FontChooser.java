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
 * 05/17/2007   Andre Mermegas: init
 *
 */
package fxts.stations.util.preferences;

import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ChangeableBoolean;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;

import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

/**
 * FontChooser
 * Creation date (01.12.2003 13:09)
 */
public class FontChooser extends JPanel implements ListSelectionListener, ILocaleListener {
    private static Font[] cSystemFonts;
    private TitledBorder mCommonBorder;
    private JTextField mNameCurrentTextField;
    private JList mNameList;
    private String[] mNamesSystemFonts;
    private JLabel mNameTitleLabel;
    private ChangeableBoolean mPlNoReaction = new ChangeableBoolean(true);
    private TitledBorder mPreviewBorder;
    private JLabel mPreviewFontLabel;
    private ResourceManager mResMan;
    private JTextField mSizeCurrentTextField;
    private JList mSizeList;
    private DefaultListModel mSizeListModel;
    private JLabel mSizeTitleLabel;
    private JTextField mStyleCurrentTextField;
    private JList mStyleList;
    private DefaultListModel mStyleListModel;
    private JLabel mStyleTitleLabel;
    private Font mValue;

    public FontChooser() {
        try {
            mResMan = ResourceManager.getManager("fxts.stations.util.preferences.resources.Resources");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cSystemFonts == null) {
            init();
        }
        //construct the window
        mNameTitleLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_FONT_NAME"));
        mNameTitleLabel.setMaximumSize(new Dimension(170, 20));
        mNameTitleLabel.setMinimumSize(new Dimension(170, 20));
        mNameTitleLabel.setPreferredSize(new Dimension(170, 20));

        mStyleTitleLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_FONT_STYLE"));
        mStyleTitleLabel.setMaximumSize(new Dimension(130, 20));
        mStyleTitleLabel.setMinimumSize(new Dimension(130, 20));
        mStyleTitleLabel.setPreferredSize(new Dimension(130, 20));

        mSizeTitleLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_FONT_SIZE"));
        mSizeTitleLabel.setPreferredSize(new Dimension(65, 20));
        mSizeTitleLabel.setMaximumSize(new Dimension(65, 20));
        mSizeTitleLabel.setMinimumSize(new Dimension(65, 20));

        mNameCurrentTextField = UIManager.getInst().createTextField();
        mNameCurrentTextField.setMaximumSize(new Dimension(170, 20));
        mNameCurrentTextField.setMinimumSize(new Dimension(170, 20));
        mNameCurrentTextField.setPreferredSize(new Dimension(170, 20));

        mStyleCurrentTextField = UIManager.getInst().createTextField();
        mStyleCurrentTextField.setMaximumSize(new Dimension(130, 20));
        mStyleCurrentTextField.setMinimumSize(new Dimension(130, 20));
        mStyleCurrentTextField.setPreferredSize(new Dimension(130, 20));

        mSizeCurrentTextField = new SizeFontField();
        mSizeCurrentTextField.setPreferredSize(new Dimension(65, 20));
        mSizeCurrentTextField.setMaximumSize(new Dimension(65, 20));
        mSizeCurrentTextField.setMinimumSize(new Dimension(65, 20));

        JScrollPane nameScrollPane = new JScrollPane();
        nameScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollPane styleScrollPane = new JScrollPane();
        styleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        JScrollPane sizeScrollPane = new JScrollPane();
        sizeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel previewPanel = new JPanel(new RiverLayout());
        mPreviewFontLabel = UIManager.getInst().createLabel();
        mCommonBorder = new TitledBorder(null,
                                         mResMan.getString("IDS_FONT_TITLE"),
                                         TitledBorder.LEFT,
                                         TitledBorder.DEFAULT_POSITION);
        setBorder(mCommonBorder);
        mSizeCurrentTextField.setEditable(true);
        mNameCurrentTextField.setEditable(false);
        mNameCurrentTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent aEvent) {
                mSizeCurrentTextField.requestFocus();
            }
        });
        mNameCurrentTextField.setRequestFocusEnabled(false);
        mStyleCurrentTextField.setEditable(false);
        mStyleCurrentTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent aEvent) {
                mSizeCurrentTextField.requestFocus();
            }
        });
        mStyleCurrentTextField.setRequestFocusEnabled(false);

        mSizeCurrentTextField.setInputVerifier(new InputVerifier() {
            public boolean verify(JComponent aInput) {
                if (aInput instanceof JTextComponent) {
                    ((JTextComponent) aInput).setText(((JTextComponent) aInput).getText());
                }
                return true;
            }
        });

        mNamesSystemFonts = fillNamesSystemFonts();

        DefaultListModel nameListModel = new DefaultListModel();
        //Add getting array to list
        for (String systemFont : mNamesSystemFonts) {
            nameListModel.addElement(systemFont);
        }
        mNameList = new JList(nameListModel) {
            public String toString() {
                return "NameList";
            }
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };

        mNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mNameList.addListSelectionListener(this);
        nameScrollPane.setMaximumSize(new Dimension(170, 150));
        nameScrollPane.setMinimumSize(new Dimension(170, 150));
        nameScrollPane.setPreferredSize(new Dimension(170, 150));
        nameScrollPane.setRequestFocusEnabled(false);
        nameScrollPane.setViewportView(mNameList);
        mStyleListModel = new DefaultListModel();
        mStyleList = new JList(mStyleListModel) {
            public String toString() {
                return "StyleList";
            }
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
        mStyleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mNameList.setNextFocusableComponent(mStyleList);
        mStyleList.addListSelectionListener(this);
        styleScrollPane.setMaximumSize(new Dimension(130, 150));
        styleScrollPane.setMinimumSize(new Dimension(130, 150));
        styleScrollPane.setPreferredSize(new Dimension(130, 150));
        styleScrollPane.setViewportView(mStyleList);
        mSizeListModel = new DefaultListModel();
        mSizeListModel.addElement("5");
        mSizeListModel.addElement("8");
        mSizeListModel.addElement("10");
        mSizeListModel.addElement("12");
        mSizeListModel.addElement("14");
        mSizeListModel.addElement("18");
        mSizeListModel.addElement("24");
        mSizeListModel.addElement("36");
        mSizeListModel.addElement("48");
        mSizeListModel.addElement("72");
        mSizeList = new JList(mSizeListModel) {
            public String toString() {
                return "SizeList";
            }
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
        mSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mSizeList.addListSelectionListener(this);
        sizeScrollPane.setPreferredSize(new Dimension(65, 150));
        sizeScrollPane.setMaximumSize(new Dimension(65, 150));
        sizeScrollPane.setMinimumSize(new Dimension(65, 150));
        sizeScrollPane.setViewportView(mSizeList);
        mPreviewBorder = new TitledBorder(null,
                                          mResMan.getString("IDS_PREVIEW"),
                                          TitledBorder.LEFT,
                                          TitledBorder.DEFAULT_POSITION,
                                          new Font("Dialog", 0, 12));
        previewPanel.setBorder(mPreviewBorder);
        mPreviewFontLabel.setText("Example");
        mPreviewFontLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mPreviewFontLabel.setPreferredSize(new Dimension(220, 80));
        mPreviewFontLabel.setMinimumSize(new Dimension(220, 80));
        mPreviewFontLabel.setMaximumSize(new Dimension(220, 80));
        previewPanel.add("hfill vfill", mPreviewFontLabel);

        setLayout(new RiverLayout());

        add("left", mNameTitleLabel);
        add("tab", mStyleTitleLabel);
        add("tab hfill", mSizeTitleLabel);

        add("br left", mNameCurrentTextField);
        add("tab", mStyleCurrentTextField);
        add("tab hfill", mSizeCurrentTextField);

        add("br left", nameScrollPane);
        add("tab", styleScrollPane);
        add("tab hfill", sizeScrollPane);

        add("br hfill vfill", previewPanel);

        //adds FontChooser to array of LocaleListeners
        mResMan.addLocaleListener(this);
    }

    private String[] fillNamesSystemFonts() {
        Vector outNames = new Vector();
        for (Font systemFont : cSystemFonts) {
            String tempName = UserPreferences.clipFontFamily(systemFont.getFamily());
            if (outNames.indexOf(tempName) < 0) {
                outNames.add(tempName);
            }
        }
        String[] array = new String[outNames.size()];
        return (String[]) outNames.toArray(array);
    }

    private void fillStyleList(String aName, int aStyle) {
        mStyleListModel.removeAllElements();
        int currentStyle = -1;
        //provide minimum one style (some fonts have no styles)
        int k = 0;
        for (String systemFont : mNamesSystemFonts) {
            if (systemFont.equals(aName)) {
                for (int j = 0; j < 4; j++) {
                    Font f = new Font(aName, j, 12);
                    if (f.getStyle() == j) {
                        String styleName;
                        switch (j) {
                            case Font.PLAIN:
                                styleName = mResMan.getString("IDS_FONT_PLAIN");
                                break;
                            case Font.BOLD:
                                styleName = mResMan.getString("IDS_FONT_BOLD");
                                break;
                            case Font.ITALIC:
                                styleName = mResMan.getString("IDS_FONT_ITALIC");
                                break;
                            default:
                                styleName = mResMan.getString("IDS_FONT_BOLD_ITALIC");
                        }
                        StyleWrapper sw = new StyleWrapper(j, styleName);
                        mStyleListModel.addElement(sw);
                        if (j == aStyle) {
                            currentStyle = k;
                        }
                        k++;
                    }
                }
            }
            mStyleList.setSelectedIndex(currentStyle);
        }
    }

    private void fillStyleList(String aName) {
        fillStyleList(aName, -1);
    }

    private void fillStyleTextField() {
        switch (mStyleList.getSelectedIndex()) {
            case 0:
                mStyleCurrentTextField.setText(mResMan.getString("IDS_FONT_PLAIN"));
                break;
            case 1:
                mStyleCurrentTextField.setText(mResMan.getString("IDS_FONT_BOLD"));
                break;
            case 2:
                mStyleCurrentTextField.setText(mResMan.getString("IDS_FONT_ITALIC"));
                break;
            case 3:
                mStyleCurrentTextField.setText(mResMan.getString("IDS_FONT_BOLD_ITALIC"));
                break;
            default:
                mStyleCurrentTextField.setText(mResMan.getString("IDS_FONT_PLAIN"));
        }
    }

    public Font getCurrentFont() {
        return mValue;
    }

    private int getCurrentIndex(String aName) {
        int index;
        for (index = 0; index < mNamesSystemFonts.length; index++) {
            if (mNamesSystemFonts[index].equals(aName)) {
                break;
            }
        }
        return index;
    }

    /**
     * Just decoder of array
     */
    private int getCurrentIndex(int aSize) {
        switch (aSize) {
            case 5:
                return 0;
            case 8:
                return 1;
            case 10:
                return 2;
            case 12:
                return 3;
            case 14:
                return 4;
            case 18:
                return 5;
            case 24:
                return 6;
            case 36:
                return 7;
            case 48:
                return 8;
            case 72:
                return 9;
            default:
                return 2;
        }
    }

    private int getSelectedSize() {
        int rc = -1;
        String listValue = (String) mSizeList.getSelectedValue();
        if (listValue != null) {
            try {
                rc = Integer.parseInt(listValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return rc;
    }

    public static void init() {
        cSystemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        Arrays.sort(cSystemFonts, new Comparator() {
            public int compare(Object aO1, Object aO2) {
                if (aO1 == null) {
                    return aO2 != null ? -1 : 0;
                } else if (aO2 == null) {
                    return 1;
                } else {
                    int res = ((Font) aO1).getFamily().compareTo(((Font) aO2).getFamily());
                    if (res == 0) {
                        res = ((Font) aO1).getFontName().compareTo(((Font) aO2).getFontName());
                    }
                    return res;
                }
            }

            public boolean equals(Object aObj) {
                return aObj == this;
            }
        });
    }

    /**
     * This method is called when current locale of the aMan is changed and becomes aLocale.
     *
     * @param aMan resource manager.
     */
    public void onChangeLocale(ResourceManager aMan) {
        mNameTitleLabel.setText(aMan.getString("IDS_FONT_NAME"));
        mStyleTitleLabel.setText(aMan.getString("IDS_FONT_STYLE"));
        mSizeTitleLabel.setText(aMan.getString("IDS_FONT_SIZE"));
        mPreviewBorder.setTitle(aMan.getString("IDS_PREVIEW"));
        mCommonBorder.setTitle(aMan.getString("IDS_FONT_TITLE"));
    }

    public void setCurrentFont(Font aValue) {
        mValue = aValue;
        //Lock listener while first filling values.
        mPlNoReaction.setValue(true);
        mNameList.setSelectedIndex(getCurrentIndex(UserPreferences.clipFontFamily(mValue.getFamily())));
        mNameCurrentTextField.setText(UserPreferences.clipFontFamily(mValue.getFamily()));
        fillStyleList(UserPreferences.clipFontFamily(mValue.getFamily()), mValue.getStyle());
        fillStyleTextField();
        mSizeList.setSelectedIndex(getCurrentIndex(mValue.getSize()));
        mSizeCurrentTextField.setText(Integer.toString(mValue.getSize()));
        mPlNoReaction.setValue(false);
        mPreviewFontLabel.setText(mNameCurrentTextField.getText()
                                  + ","
                                  + mStyleCurrentTextField.getText()
                                  + ","
                                  + mSizeCurrentTextField.getText());
    }

    public void setNextFocusedComp(Component aComp) {
        mSizeList.setNextFocusableComponent(aComp);
    }

    public void valueChanged(ListSelectionEvent aEvent) {
        //When selected not changing or start filled of dialog
        if (aEvent.getValueIsAdjusting()) {
            return;
        }
        if (!mPlNoReaction.wasSet(true)) {
            String selectedName = mNamesSystemFonts[mNameList.getSelectedIndex()];
            mNameCurrentTextField.setText(selectedName);
            if (mNameList.equals(aEvent.getSource())) {
                fillStyleList(selectedName);
                mStyleList.setSelectedIndex(0);
            }
            if (mStyleList.equals(aEvent.getSource())) {
                fillStyleTextField();
            }
            int size = getSelectedSize();
            if (size >= 0) {
                mSizeCurrentTextField.setText(Integer.toString(size));
            }
            try {
                StyleWrapper wrapper = (StyleWrapper) mStyleList.getModel().getElementAt(mStyleList.getSelectedIndex());
                mValue = new Font(selectedName, wrapper.getStyle(), Integer.parseInt(mSizeCurrentTextField.getText()));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            mPreviewFontLabel.setFont(mValue);
            mPreviewFontLabel.setText(mNameCurrentTextField.getText()
                                      + ","
                                      + mStyleCurrentTextField.getText()
                                      + ","
                                      + mSizeCurrentTextField.getText());
            mPlNoReaction.setValue(false);
        }
    }

    private class NumericDocument extends PlainDocument {
        private int mCurrentVal;
        private final int mMax = 72;
        private final int mMin = 5;
        private boolean mValidate = true;

        public int getIntegerValue() {
            return mCurrentVal;
        }

        public int getValue() {
            return mCurrentVal;
        }

        public void insertString(int aOffs, String aStr, AttributeSet aAttr) throws BadLocationException {
            if (mValidate) {
                super.insertString(aOffs, aStr, aAttr);
                return;
            }
            if (aStr == null) {
                return;
            }
            String proposedResult;
            if (getLength() == 0) {
                proposedResult = aStr;
            } else {
                StringBuilder currentBuffer = new StringBuilder(getText(0, getLength()));
                currentBuffer.insert(aOffs, aStr);
                proposedResult = currentBuffer.toString();
            }
            try {
                mCurrentVal = parse(proposedResult, mCurrentVal);
                super.insertString(aOffs, aStr, aAttr);
                updateValue(mCurrentVal);
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        public void noValidate(boolean aValidateFlag) {
            mValidate = aValidateFlag;
        }

        public int parse(String aProposedResult, int aDefValue) throws NumberFormatException {
            int value = aDefValue;
            if (aProposedResult.length() != 0) {
                value = Integer.parseInt(aProposedResult);
                if (value < mMin) {
                    value = aDefValue;
                } else if (value > mMax) {
                    value = mMax;
                }
            }
            return value;
        }

        public void remove(int aOffs, int aLen) throws BadLocationException {
            if (mValidate) {
                super.remove(aOffs, aLen);
                return;
            }
            String currentText = getText(0, getLength());
            String beforeOffset = currentText.substring(0, aOffs);
            String afterOffset = currentText.substring(aLen + aOffs, currentText.length());
            String proposedResult = beforeOffset + afterOffset;
            try {
                mCurrentVal = parse(proposedResult, mCurrentVal);
                super.remove(aOffs, aLen);
                updateValue(mCurrentVal);
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        public void setIntegerValue(int aValue) {
            mCurrentVal = aValue;
            updateValue(mCurrentVal);
        }

        private void updateValue(int aValue) {
            try {
                String value = Integer.toString(aValue);
                String selectedName = mNamesSystemFonts[mNameList.getSelectedIndex()];
                StyleWrapper wrapper = (StyleWrapper) mStyleList.getModel().getElementAt(mStyleList.getSelectedIndex());
                mValue = new Font(selectedName, wrapper.getStyle(), aValue);
                mPreviewFontLabel.setFont(mValue);
                mPreviewFontLabel.setText(mNameCurrentTextField.getText()
                                          + ","
                                          + mStyleCurrentTextField.getText()
                                          + ","
                                          + value);
                int index = mSizeListModel.indexOf(value);
                mPlNoReaction.setValue(true);
                if (index < 0) {
                    mSizeList.clearSelection();
                } else {
                    mSizeList.setSelectedIndex(index);
                    mSizeList.ensureIndexIsVisible(index);
                }
                mPlNoReaction.setValue(false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class SizeFontField extends JTextField {
        private NumericDocument mNumericDocument = new NumericDocument();

        private SizeFontField() {
            setDocument(mNumericDocument);
        }

        protected void paintComponent(Graphics aGraphics) {
            if (UIManager.getInst().isAAEnabled()) {
                Graphics2D g2d = (Graphics2D) aGraphics;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            super.paintComponent(aGraphics);
        }
        public String getText() {
            return Integer.toString(mNumericDocument.getIntegerValue());
        }

        public void setText(String aText) {
            mNumericDocument.noValidate(true);
            super.setText(aText);
            mNumericDocument.noValidate(false);
            try {
                mNumericDocument.setIntegerValue(Integer.parseInt(aText));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private static class StyleWrapper {
        private int mStyle;
        private String mStyleName;

        private StyleWrapper(int aStyle, String aStyleName) {
            mStyle = aStyle;
            mStyleName = aStyleName;
        }

        public int getStyle() {
            return mStyle;
        }

        public String toString() {
            return mStyleName;
        }
    }
}
