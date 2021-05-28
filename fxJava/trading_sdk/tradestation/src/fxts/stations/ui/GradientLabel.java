/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/ui/GradientLabel.java#1 $
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
 * Created: May 7, 2007 12:14:32 PM
 *
 * $History: $
 * 05/11/2007   Andre Mermegas: refactor
 */
package fxts.stations.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 */
public class GradientLabel extends JLabel {
    private Builder mBuilder;

    private GradientLabel(Builder aBuilder) {
        super(aBuilder.text(), JLabel.CENTER);
        mBuilder = aBuilder;
        setBg(aBuilder.bg());
        setBg2(aBuilder.bg2());
        setOpaque(true);

        if (aBuilder.fg() != null) {
            setForeground(aBuilder.fg());
        }

        if (aBuilder.bg() != null) {
            setBackground(aBuilder.bg());
        }

        if (aBuilder.bg2() != null) {
            setBackground(aBuilder.bg2());
        }

        if (aBuilder.isShowBorder()) {
            setBorder(BorderFactory.createMatteBorder(aBuilder.top(),
                                                      aBuilder.left(),
                                                      aBuilder.bottom(),
                                                      aBuilder.right(),
                                                      Color.DARK_GRAY));
        }

        if (aBuilder.fontSize() >= 0) {
            if (aBuilder.isBold()) {
                setFont(getFont().deriveFont(Font.BOLD, aBuilder.fontSize()));
            } else {
                setFont(getFont().deriveFont(aBuilder.fontSize()));
            }
        }
    }

    public Color getBg() {
        return mBuilder.bg();
    }

    public Color getBg2() {
        return mBuilder.bg2();
    }

    @Override
    public void paintComponent(Graphics aGraphics) {
        Graphics2D g2d = (Graphics2D) aGraphics;
        if (UIManager.getInst().isAAEnabled()) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        super.paintComponent(aGraphics);
    }

    public void setBg(Color aBg) {
        mBuilder.bg(aBg);
        setBackground(aBg);
    }

    public void setBg2(Color aBg2) {
        mBuilder.bg2(aBg2);
        setBackground(aBg2);
    }

    public static class Builder {
        private Color mBg;
        private Color mBg2;
        private boolean mBold;
        private int mBottom;
        private Color mFg;
        private int mFontSize = -1;
        private int mLeft;
        private int mRight;
        private boolean mShowBorder;
        private String mText;
        private int mTop;

        public Color bg() {
            return mBg;
        }

        public Builder bg(Color aBg) {
            mBg = aBg;
            return this;
        }

        public Color bg2() {
            return mBg2;
        }

        public Builder bg2(Color aBg2) {
            mBg2 = aBg2;
            return this;
        }

        public Builder bold(boolean aBold) {
            mBold = aBold;
            return this;
        }

        public int bottom() {
            return mBottom;
        }

        public Builder bottom(int aBottom) {
            mBottom = aBottom;
            return this;
        }

        public GradientLabel build() {
            return new GradientLabel(this);
        }

        public Color fg() {
            return mFg;
        }

        public Builder fg(Color aFg) {
            mFg = aFg;
            return this;
        }

        public int fontSize() {
            return mFontSize;
        }

        public Builder fontSize(int aFontSize) {
            mFontSize = aFontSize;
            return this;
        }

        public boolean isBold() {
            return mBold;
        }

        public boolean isShowBorder() {
            return mShowBorder;
        }

        public int left() {
            return mLeft;
        }

        public Builder left(int aLeft) {
            mLeft = aLeft;
            return this;
        }

        public int right() {
            return mRight;
        }

        public Builder right(int aRight) {
            mRight = aRight;
            return this;
        }

        public Builder showBorder(boolean aShowBorder) {
            mShowBorder = aShowBorder;
            return this;
        }

        public String text() {
            return mText;
        }

        public Builder text(String aText) {
            mText = aText;
            return this;
        }

        public int top() {
            return mTop;
        }

        public Builder top(int aTop) {
            mTop = aTop;
            return this;
        }
    }
}
