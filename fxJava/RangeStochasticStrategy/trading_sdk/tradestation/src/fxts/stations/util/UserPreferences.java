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
 * 07/18/2006   Andre Mermegas: performance update
 */
package fxts.stations.util;

import fxts.stations.core.TradeDesk;
import fxts.stations.util.preferences.IUserPreferencesListener;
import fxts.stations.util.preferences.PrefProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.Vector;

/**
 * Singleton class that is responsible for saving user
 * preferencies of ui.
 */
public class UserPreferences extends Observable implements IUserPreferencesListener {
    private static final Log LOG = LogFactory.getLog(UserPreferences.class);
    private static UserPreferences cInstance;
    private Map mColorCache = new HashMap();
    private boolean mDefault = true;
    private Properties mDefaultProperties;
    private Map mFontCache = new HashMap();
    private String mUserName;

    /**
     * Protected constructor.
     */
    protected UserPreferences(String aUserName) throws Exception {
        //sets name of user
        mUserName = aUserName;
        //creates default properties
        mDefaultProperties = new Properties();
        //loads default properties
        loadDefaultProrerties();
    }

    public static String clipFontFamily(String aFamily) {
        String tempName = aFamily;
        int pos = tempName.indexOf(".");
        if (pos >= 0) {
            String rest = tempName.substring(pos + 1);
            if (rest.contains("bold")
                || rest.contains("italic")
                || rest.contains("BOLD")
                || rest.contains("ITALIC")) {
                tempName = tempName.substring(0, pos);
            }
        }
        return tempName;
    }

    /**
     * Get boolean from UserPreferences.
     */
    public boolean getBoolean(String aKey) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        //gets default value
        String sResult = mDefaultProperties.getProperty(aKey, null);
        boolean bResult = false;
        if (sResult != null) {
            bResult = sResult.equals(String.valueOf(true));
        }
        if (storage != null) {
            boolean tmp = bResult;
            bResult = storage.getBoolean(getFullKey(aKey), bResult);
            if (bResult ^ tmp) {
                mDefault = false;
            }
        }
        return bResult;
    }

    /**
     * Get color from UserPreferences.
     */
    public Color getColor(String aKey) {
        String sColor = getString(aKey);
        if (sColor == null) {
            LOG.debug("Color not found!");
            return Color.BLACK;
        }
        int nRed;
        int nGreen;
        int nBlue;
        try {
            //creating of Color
            int nCommaFirstPos = sColor.indexOf(',', 0);
            nRed = Integer.parseInt(sColor.substring(0, nCommaFirstPos).trim());
            int nCommaSecondPos = sColor.indexOf(',', nCommaFirstPos + 1);
            nGreen = Integer.parseInt(sColor.substring(nCommaFirstPos + 1, nCommaSecondPos).trim());
            nBlue = Integer.parseInt(sColor.substring(nCommaSecondPos + 1).trim());
        } catch (NumberFormatException ex) {
            LOG.debug("NumberFormatException at PersistanceStorage.getColor().");
            return Color.BLACK;
        }
        nRed = nRed > 255 ? 255 : nRed;
        nGreen = nGreen > 255 ? 255 : nGreen;
        nBlue = nBlue > 255 ? 255 : nBlue;
        String key = String.valueOf(nRed) + String.valueOf(nGreen) + String.valueOf(nBlue);
        if (mColorCache.containsKey(key)) {
            return (Color) mColorCache.get(key);
        } else {
            Color color = new Color(nRed, nGreen, nBlue);
            mColorCache.put(key, color);
            return color;
        }
    }

    /**
     * Get double from UserPreferences.
     */
    public double getDouble(String aKey) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        //gets default value
        String sResult = mDefaultProperties.getProperty(aKey, null);
        double dResult = 0;
        if (sResult != null) {
            try {
                dResult = Util.parseDouble(sResult);
            } catch (NumberFormatException ex) {
                LOG.debug("Default value for user preference with key = "
                          + aKey
                          + " and value = \""
                          + sResult
                          + "\" not converted to double!");
            }
        }
        if (storage != null) {
            double tmp = dResult;
            dResult = storage.getDouble(getFullKey(aKey), dResult);
            if (dResult != tmp) {
                mDefault = false;
            }
        }
        return dResult;
    }

    /**
     * Get font from UserPreferences.
     */
    public Font getFont(String aKey) {
        //gets from storage
        String sFont = getString(aKey);
        if (sFont == null) {
            return new Font("dialog", 0, 12);
        }
        int nSize;
        String sName;
        int nStyle;
        try {
            //creating of Color
            int nCommaFirstPos = sFont.indexOf(',', 0);
            sName = sFont.substring(0, nCommaFirstPos).trim();
            int nCommaSecondPos = sFont.indexOf(',', nCommaFirstPos + 1);
            nStyle = Integer.parseInt(sFont.substring(nCommaFirstPos + 1, nCommaSecondPos).trim());
            nSize = Integer.parseInt(sFont.substring(nCommaSecondPos + 1).trim());
        } catch (NumberFormatException ex) {
            LOG.debug("NumberFormatException at PersistanceStorage.getFont().");
            return null;
        }
        String key = sName + nStyle + nSize;
        //creates font
        if (mFontCache.containsKey(key)) {
            return (Font) mFontCache.get(key);
        } else {
            Font font = new Font(sName, nStyle, nSize);
            mFontCache.put(key, font);
            return font;
        }
    }

    public Font getFont(String aKey, Font aDefault) {
        Font font = getFont(aKey);
        if (font == null) {
            return aDefault;
        } else {
            return font;
        }
    }

    private String getFullKey(String aKey) {
        StringBuffer sb = new StringBuffer();
        if (aKey.startsWith("Server") || aKey.startsWith("Proxy")) {
            sb.append("preferences..").append(aKey);
        } else {
            sb.append("preferences.").append(mUserName).append(".").append(aKey);
        }
        return sb.toString();
    }

    /**
     * Get int from UserPreferences.
     */
    public int getInt(String aKey) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        //gets default value
        String sResult = mDefaultProperties.getProperty(aKey, null);
        int nResult = 0;
        if (sResult != null) {
            try {
                nResult = Integer.parseInt(sResult);
            } catch (NumberFormatException ex) {
                LOG.debug("Default value for user preference with key = "
                          + aKey + " and value = \"" + sResult + "\" not converted to int!");
            }
        }
        if (storage != null) {
            int tmp = nResult;
            nResult = storage.getInt(getFullKey(aKey), nResult);
            if (nResult != tmp) {
                mDefault = false;
            }
        }
        return nResult;
    }

    private PersistentStorage getStorage() {
        try {
            return PersistentStorage.getStorage();
        } catch (Exception ex) {
            LOG.debug("Persistent storage not created.");
            return null;
        }
    }

    /**
     * Get string from UserPreferences.
     */
    public String getString(String aKey) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        //gets default value
        String sResult = mDefaultProperties.getProperty(aKey, null);
        if (storage != null) {
            // returns default value
            String tmp = sResult;
            sResult = storage.getString(getFullKey(aKey), sResult);
            if (tmp != null && !tmp.equals(sResult)) {
                mDefault = false;
            }
        }
        return sResult;
    }

    public static String getStringValue(String aValue) {
        return aValue;
    }

    public static String getStringValue(Password aValue) {
        return aValue.toString();
    }

    public static String getStringValue(Color aValue) {
        return aValue.getRed() + "," + aValue.getGreen() + "," + aValue.getBlue();
    }

    public static String getStringValue(boolean aValue) {
        return getStringValue(aValue ? Boolean.TRUE : Boolean.FALSE);
    }

    public static String getStringValue(Boolean aValue) {
        return aValue.toString();
    }

    public static String getStringValue(Double aValue) {
        return aValue.toString();
    }

    public static String getStringValue(double aValue) {
        return getStringValue(Double.valueOf(aValue));
    }

    public static String getStringValue(Long aValue) {
        return aValue.toString();
    }

    public static String getStringValue(long aValue) {
        return getStringValue(Long.valueOf(aValue));
    }

    public static String getStringValue(int aValue) {
        return getStringValue(Integer.valueOf(aValue));
    }

    public static String getStringValue(Integer aValue) {
        return aValue.toString();
    }

    public static String getStringValue(Font aValue) {
        return clipFontFamily(aValue.getFamily()) + "," + aValue.getStyle() + ","
               + aValue.getSize();
    }

    /**
     * Returns current user name
     */
    public String getUserName() {
        return mUserName;
    }

    /**
     * Sets current user name
     */
    public void setUserName(String aUserName) {
        mUserName = aUserName;
    }

    /**
     * Get instance of UserPreferences. Really constructor
     */
    public static UserPreferences getUserPreferences(String aUserName) {
        //creates instance if it`s null or user name not equals to specified
        if (cInstance == null || !aUserName.equals(cInstance.getUserName())) {
            try {
                cInstance = new UserPreferences(aUserName);
            } catch (Exception e) {
                cInstance = null;
                LOG.error("Not loaded properties file with default values of user settings!");
            }
        }
        return cInstance;
    }

    /**
     * Get instance of UserPreferences. Really constructor
     */
    public static UserPreferences getUserPreferences() {
        return getUserPreferences(TradeDesk.getInst().getUserName());
    }

    public Object getValue(String aKey, Object aOldValue) {
        if (aOldValue instanceof Color) {
            return getColor(aKey);
        }
        if (aOldValue instanceof Font) {
            return getFont(aKey);
        }
        if (aOldValue instanceof String) {
            return getString(aKey);
        }
        if (aOldValue instanceof Integer) {
            return getInt(aKey);
        }
        if (aOldValue instanceof Double) {
            return getDouble(aKey);
        }
        if (aOldValue instanceof Boolean) {
            return getBoolean(aKey) ? Boolean.TRUE : Boolean.FALSE;
        }
        return aOldValue;
    }

    public boolean isDefault() {
        return mDefault;
    }

    /* -- Private methods -- */

    /**
     * Loads default properties from properties file.
     */
    private void loadDefaultProrerties() throws Exception {
        ClassLoader loader = UserPreferences.class.getClassLoader();
        URL url = loader.getResource("fxts/stations/util/UserPreferences.properties");
        if (url == null) {
            throw new Exception("Not found UserPreferences.properties");
        }
        try {
            InputStream istream = url.openStream();
            //loads from input stream
            mDefaultProperties.load(istream);
            istream.close();
        } catch (IOException e) {
            throw new Exception("Not loaded properties from UserPreferences.properties!");
        }
    }

    public void preferencesUpdated(Vector aChangings) {
        for (Object change : aChangings) {
            PrefProperty property = (PrefProperty) change;
            set(property.getPropertyID(), property.getType().toString(property.getValue()));
            ((PrefProperty) change).getType().toString(((PrefProperty) change).getValue());
        }
    }

    public void remove(String aKey) {
        PersistentStorage storage = getStorage();
        if (storage == null) {
            return;
        }
        storage.remove(getFullKey(aKey));
    }

    public void resetToDefault() {
        mDefault = true;
    }

    /**
     * Save color to UserPreferences.
     */
    public void set(String aKey, Color aColor) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        if (storage != null) {
            storage.set(getFullKey(aKey), getStringValue(aColor));
            setChanged();
            notifyObservers(getFullKey(aKey));
            mDefault = false;
        }
    }

    /**
     * Save font to UserPreferences.
     */
    public void set(String aKey, Font aFont) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        if (storage != null) {
            storage.set(getFullKey(aKey), getStringValue(aFont));
            setChanged();
            notifyObservers(getFullKey(aKey));
            mDefault = false;
        }
    }

    /**
     * Save string to UserPreferences.
     */
    public void set(String aKey, String aValue) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        if (storage != null) {
            storage.set(getFullKey(aKey), getStringValue(aValue));
            setChanged();
            notifyObservers(getFullKey(aKey));
            mDefault = false;
        }
    }

    /**
     * Save int to UserPreferences.
     */
    public void set(String aKey, int aValue) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        if (storage != null) {
            storage.set(getFullKey(aKey), getStringValue(aValue));
            setChanged();
            notifyObservers(getFullKey(aKey));
            mDefault = false;
        }
    }

    /**
     * Save double to UserPreferences.
     */
    public void set(String aKey, double aValue) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        if (storage != null) {
            storage.set(getFullKey(aKey), getStringValue(aValue));
            setChanged();
            notifyObservers(getFullKey(aKey));
            mDefault = false;
        }
    }

    /**
     * Save boolean to UserPreferences.
     */
    public void set(String aKey, boolean aValue) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        if (storage != null) {
            storage.set(getFullKey(aKey), getStringValue(aValue));
            setChanged();
            notifyObservers(getFullKey(aKey));
            mDefault = false;
        }
    }

    /**
     * Save date to UserPreferences.
     */
    public void set(String aKey, Date aValue) {
        //Get PersistenceStorage
        PersistentStorage storage = getStorage();
        if (storage != null) {
            storage.set(getFullKey(aKey), aValue);
            setChanged();
            notifyObservers(getFullKey(aKey));
            mDefault = false;
        }
    }
}
