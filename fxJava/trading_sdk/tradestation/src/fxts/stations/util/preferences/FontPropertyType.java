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
package fxts.stations.util.preferences;

import fxts.stations.util.UserPreferences;

import java.awt.*;

public class FontPropertyType extends APropertyType {
    public FontPropertyType() {
        setEditor(new FontEditor());
    }

    public AValueEditorPanel getRenderer(Object oValue) {
//      getEditor().setValue(oValue);
        return new FontEditorPanel(oValue, this);
    }

    public String toString(Object aValue) {
        return UserPreferences.getStringValue((Font) aValue);
    }

    public String toStringValue(Object aValue) {
        String name;
        String style;
        String size;
        switch (((Font) aValue).getStyle()) {
            case Font.PLAIN:
                style = "Plain";
                break;
            case Font.BOLD:
                style = "Bold";
                break;
            case Font.ITALIC:
                style = "Italic";
                break;
            case Font.BOLD + Font.ITALIC:
                style = "Italic + Bold";
                break;
            default:
                style = "Plain";
        }
        name = UserPreferences.clipFontFamily(((Font) aValue).getFamily());
//        System.out.println(" aValue=" + aValue +" ((Font)aValue).getFamily() = " + " ((Font)aValue).getFamily()=" +((Font)aValue).getFamily() );
        size = Integer.toString(((Font) aValue).getSize());
        return name + "," + style + "," + size;
    }
}