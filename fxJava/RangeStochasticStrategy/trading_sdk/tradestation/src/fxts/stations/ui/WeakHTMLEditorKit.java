/* 
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/ui/WeakHTMLEditorKit.java#1 $ 
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
 * $History: $ 
 */
package fxts.stations.ui;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;

/**
 */
public class WeakHTMLEditorKit extends HTMLEditorKit {
    private static WeakReference<StyleSheet> cWeakDefaultStyles;

    public StyleSheet getStyleSheet() {
        if (cWeakDefaultStyles == null || cWeakDefaultStyles.get() == null) {
            cWeakDefaultStyles = new WeakReference<StyleSheet>(new StyleSheet());
            Reader r = null;
            try {
                InputStream is = HTMLEditorKit.class.getResourceAsStream(DEFAULT_CSS);
                r = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
                cWeakDefaultStyles.get().loadRules(r, null);
            } catch (Throwable e) {
                //swallow
            } finally {
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException e) {
                        //swallow
                    }
                }
            }
        }
        return cWeakDefaultStyles.get();
    }

    public void setStyleSheet(StyleSheet aStyleSheet) {
        cWeakDefaultStyles = new WeakReference<StyleSheet>(aStyleSheet);
    }
}
