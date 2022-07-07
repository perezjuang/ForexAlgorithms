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

import java.util.Hashtable;
import java.util.Vector;

public class PropertiesSheet {
    private static Hashtable cTypesTable = new Hashtable();
    /* -- Data members -- */
    //    private PrefProperty mProperty;
    private Vector mProperties;

    /* Constructor */
    public PropertiesSheet() {
        mProperties = new Vector();
    }

    public void add(PrefProperty aProperty) {
        mProperties.addElement(aProperty);
    }

    public PrefProperty add(String asPropertyID,
                            String asNameResID,
                            Object aValue) throws ClassNotFoundException,
                                                  InstantiationException,
                                                  IllegalAccessException {
        if (asPropertyID == null || asNameResID == null) {
            return null;
        }
        String className = aValue.getClass().getName();
        int pos = className.lastIndexOf('.');
        if (pos < 0) {
            pos -= 1;
        }
        className = className.substring(++pos) + "PropertyType";
        String packageName = APropertyType.class.getPackage().getName();
        className = packageName + "." + className;
        APropertyType type = (APropertyType) cTypesTable.get(className);
        if (type == null) {
            type = (APropertyType) Class.forName(className).newInstance();
            cTypesTable.put(className, type);
        }
        PrefProperty property = new PrefProperty(asPropertyID, asNameResID, type, aValue);
        add(property);
        return property;
    }

    public void clear() {
        mProperties.clear();
    }

    public PrefProperty get(int aiIndex) {
        if (mProperties.isEmpty()) {
            return null;
        }
        return (PrefProperty) mProperties.get(aiIndex);
    }

    public PrefProperty get(String asNameResID) {
        if (mProperties.isEmpty()) {
            return null;
        }
        PrefProperty moObject;
        for (int i = 0; i < mProperties.size(); i++) {
            moObject = (PrefProperty) mProperties.get(i);
            if (moObject.getName().equals(asNameResID)) {
                return moObject;
            }
        }
        return null;
    }

    public static APropertyType getType(Object aValue) {
        String className = aValue.getClass().getName();
        int pos = className.lastIndexOf('.');
        if (pos < 0) {
            pos -= 1;
        }
        className = className.substring(++pos) + "PropertyType";
        String packageName = APropertyType.class.getPackage().getName();
        className = packageName + "." + className;
        APropertyType type = (APropertyType) cTypesTable.get(className);
        return type;
    }

    public void remove(String asName) {
        PrefProperty mPrefProperty;
        for (int i = 0; i < mProperties.size(); i++) {
            mPrefProperty = (PrefProperty) mProperties.get(i);
            if (mPrefProperty.getName().equals(asName)) {
                mProperties.remove(i);
                break;
            }
        }
    }

    public void remove(PrefProperty aProperty) {
        mProperties.remove(aProperty);
    }

    public int size() {
        return mProperties.size();
    }
}