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
package fxts.stations.util;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

/**
 * ResourceManager class
 * This class is used to simplify i18n usage in java.
 * It provides general interface for working with localized resources
 * and support of different locales.
 * The next files should be present in resource directory pointed
 * by path specified by parameter asResourceDescriptor of the method getManager
 * filename.ini, where filename is a last word of asResourceDescriptor
 * one ore more resource bundle file described in  description of java.lang.Classloader
 * format of filename.ini file:
 * <pre>
 * # the name of ResourceManager implementation class
 * ResourceManager.class=fxts.stations.util.BundleResourceManager
 * # the amount of supported locales (the directory should contain exactly this amount
 * # of files with localization bundlename_ln_CN.proprties files)
 * ResourceManager.AvailableLocales.count=7
 * # 2 line below characterizes US Engilsh locale,
 * # meaning that file  bundlename_en_US.properties is in the directory
 * ResourceManager.AvailableLocales.Locale.0.language=en
 * ResourceManager.AvailableLocales.Locale.0.country=US
 * # next line means that US Engilsh locale is default
 * ResourceManager.AvailableLocales.Locale.0.default=true
 * The next 12 lines define 6 another locales
 * ResourceManager.AvailableLocales.Locale.1.language=es
 * ResourceManager.AvailableLocales.Locale.1.country=ES
 * ResourceManager.AvailableLocales.Locale.2.language=fr
 * ResourceManager.AvailableLocales.Locale.2.country=FR
 * ResourceManager.AvailableLocales.Locale.3.language=ja
 * ResourceManager.AvailableLocales.Locale.3.country=JP
 * ResourceManager.AvailableLocales.Locale.4.language=ru
 * ResourceManager.AvailableLocales.Locale.4.country=RU
 * ResourceManager.AvailableLocales.Locale.5.language=zh
 * ResourceManager.AvailableLocales.Locale.5.country=CN
 * ResourceManager.AvailableLocales.Locale.6.language=zh
 * ResourceManager.AvailableLocales.Locale.6.country=TW
 * # If no locale have default attribute the first locale will be assumed as defult
 * # The next line defines package path to bungle with prefix before _ln_CN.proprties
 * ResourceManager.BundleBaseName=com.fxcm.fxtrade.trader.resources.FXTraderRes
 * </pre>
 */
public abstract class ResourceManager {
    protected static final String RESOURCEMANAGER_CLASS_KEY = "ResourceManager.class";
    protected static final String RESOURCEMANAGER_AVAILABLELOCALES_COUNT_KEY = "ResourceManager.AvailableLocales.count";
    protected static final String RESOURCEMANAGER_AVAILABLELOCALES_LOCALE_KEY = "ResourceManager.AvailableLocales.Locale.";
    protected static final String LANGUAGE_KEY = "language";
    protected static final String COUNTRY_KEY = "country";
    protected static final String DEFAULT_KEY = "default";
    protected static final String RESOURCEMANAGER_BUNDLEBASENAME_KEY = "ResourceManager.BundleBaseName";
    protected static Vector cManagers = new Vector();
    protected static Locale cPrimaryLocale;
    protected static final String INCORRECT_INI_FILE = "Incorrect resource.ini file format: ";
    protected Locale mDefaultLocale;
    protected Locale mLocale;
    protected Vector<ILocaleListener> mLocaleListeners = new Vector<ILocaleListener>();
    protected Vector mLocales = new Vector();
    protected String msResourceDescriptor;

    /**
     * Adds locale listener to the manager.
     *
     * @param aListener locale listener to be added.
     */
    public void addLocaleListener(ILocaleListener aListener) {
        if (aListener != null) {
            mLocaleListeners.add(aListener);
        }
    }

    /**
     * This method is called when locale is changed.
     *
     * @param aLocale new locale.
     *
     * @return true if succeeded, false otherwise.
     */
    protected abstract boolean changeLocale(Locale aLocale);

    /**
     * Returns enumeration of all locales that are supported by the manager.
     *
     * @return enumeration of all supported locales
     */
    public Enumeration getAvailableLocales() {
        return mLocales.elements();
    }

    /**
     * Returns default locale for the manager.
     * This locale should be used when there is no resource in the
     * current locale or locale not supported by the manager.
     *
     * @return default locale for the manager.
     */
    public Locale getDefaultLocale() {
        return mDefaultLocale;
    }

    /**
     * Returns current locale of the manager.
     *
     * @return current locale of the manager.
     */
    public Locale getLocale() {
        return mLocale;
    }

    /**
     * This is factory method that is used to get resource manager for the specified resource descriptor.
     *
     * @param asResourceDescriptor resource descriptor of manager to get
     *
     * @return resource manager or null, if asResourceDescriptor is null
     *
     * @throws Exception
     */
    public static ResourceManager getManager(String asResourceDescriptor) throws Exception {
        try {
            Properties resourceIni = new Properties();
            if (asResourceDescriptor == null) {
                return null;
            }

            // looking required manager up in the global vector of ResourceManagers
            for (Object manager : cManagers) {
                ResourceManager rmng = (ResourceManager) manager;
                String sResourceDescriptor = rmng.getResourceDescriptor();
                if (asResourceDescriptor.equals(sResourceDescriptor)) {
                    return rmng;
                }
            }
            String sResourceName = asResourceDescriptor.replace('.', '/');
            sResourceName = sResourceName.concat(".ini");
            ClassLoader classLoader = ResourceManager.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(sResourceName);

            // load the .ini file
            try {
                resourceIni.load(inputStream);
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    //
                }
            }
            String sClassName = resourceIni.getProperty(RESOURCEMANAGER_CLASS_KEY);
            if (sClassName == null || sClassName.length() == 0) {
                throw new Exception(INCORRECT_INI_FILE + RESOURCEMANAGER_CLASS_KEY + " key should be in");
            }
            Class resourceManagerClass = Class.forName(sClassName);

            // creating new resource manager
            ResourceManager newResourceManager = (ResourceManager) resourceManagerClass.newInstance();
            newResourceManager.msResourceDescriptor = asResourceDescriptor;

            // initalize available locales of newResourceManager
            newResourceManager.setAvailableLocales(resourceIni);
            newResourceManager.init(resourceIni);

            // adding created manager to the global vector of ResourceManagers
            cManagers.add(newResourceManager);

            // setting current locale
            if (cPrimaryLocale != null) {
                newResourceManager.setLocale(cPrimaryLocale);
            } else {
                newResourceManager.setLocale(newResourceManager.mDefaultLocale);
            }
            newResourceManager.changeLocale(newResourceManager.mLocale);
            return newResourceManager;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Gets primary locale.
     *
     * @return primary locale if one exists, null otherwise.
     */
    public static Locale getPrimaryLocale() {
        return cPrimaryLocale;
    }

    /**
     * Returns URL of the resource specified by the id. It's assumed then caller then can load the resource with
     * class loader or other methods. Resource can be icon now.
     *
     * @param asKey of the resource which URL is required.
     *
     * @return URL of the specified resource
     */
    public abstract URL getResource(String asKey);

    /**
     * Returns resource descriptor for the manager.
     *
     * @return resource descriptor.
     */
    public String getResourceDescriptor() {
        return msResourceDescriptor;
    }

    /**
     * Returns localized String with specified id.
     *
     * @param asKey id of required String.
     *
     * @return localized String if it is found, empty String "" otherwise.
     */
    public abstract String getString(String asKey);

    /**
     * Returns localized String with specified id.
     *
     * @param asKey     id of required String.
     * @param asDefault String to be returned in case of asKey not found
     *
     * @return localized String if it is found, asDefault otherwise.
     */
    public abstract String getString(String asKey, String asDefault);

    /**
     * The method is called when resource manager is created.
     *
     * @param aResourceIni property loaded from resource.ini
     *
     * @throws Exception
     */
    protected abstract void init(Properties aResourceIni) throws Exception;

    /**
     * Removes locale listener from the manager.
     *
     * @param aListener locale listener to be removed.
     */
    public void removeLocaleListener(ILocaleListener aListener) {
        mLocaleListeners.remove(aListener);
    }

    /**
     * Sets available locales of the ResourceManager from the root node of the resource.ini file
     *
     * @param aResourceIni resource properties
     *
     * @throws Exception
     */
    private void setAvailableLocales(Properties aResourceIni) throws Exception {
        int availableLocalesCount = 0;
        try {
            String prop = aResourceIni.getProperty(RESOURCEMANAGER_AVAILABLELOCALES_COUNT_KEY, "");
            availableLocalesCount = Integer.parseInt(prop);
        } catch (NumberFormatException e) {
            //
        }
        if (availableLocalesCount <= 0) {
            throw new Exception(INCORRECT_INI_FILE
                                + RESOURCEMANAGER_AVAILABLELOCALES_COUNT_KEY
                                + " key is absent or has invalid value");
        }
        for (int i = 0; i < availableLocalesCount; i++) {
            String sKey = RESOURCEMANAGER_AVAILABLELOCALES_LOCALE_KEY + i + "." + LANGUAGE_KEY;
            String sLanguage = aResourceIni.getProperty(sKey);
            if (sLanguage == null || sLanguage.length() == 0) {
                throw new Exception(INCORRECT_INI_FILE + sKey + " should be in");
            }
            sKey = RESOURCEMANAGER_AVAILABLELOCALES_LOCALE_KEY + i + "." + COUNTRY_KEY;
            String sCounrty = aResourceIni.getProperty(sKey);
            if (sCounrty == null || sCounrty.length() == 0) {
                throw new Exception(INCORRECT_INI_FILE + sKey + " should be in");
            }
            Locale locale = new Locale(sLanguage, sCounrty);
            mLocales.add(locale);
            sKey = RESOURCEMANAGER_AVAILABLELOCALES_LOCALE_KEY + i + "." + DEFAULT_KEY;
            if (i == 0 || "true".equals(aResourceIni.getProperty(sKey))) {
                mDefaultLocale = locale;
            }
        }
    }

    /**
     * Sets current locale of the manager.
     *
     * @param aLocale locale to be set.
     *
     * @return true if the locale is supported, false otherwise.
     */
    public boolean setLocale(Locale aLocale) {
        boolean bRet = false;
        if (aLocale != null && aLocale.equals(mLocale)) {
            return true;
        }
        if (mLocales.contains(aLocale)) {
            mLocale = aLocale;
            bRet = changeLocale(mLocale);
            // notifying all listeners
            for (Object localeListener : mLocaleListeners) {
                ILocaleListener listener = (ILocaleListener) localeListener;
                listener.onChangeLocale(this);
            }
        }
        return bRet;
    }

    /**
     * Sets primary locale.
     *
     * @param aLocale primary locale
     */
    public static void setPrimaryLocale(Locale aLocale) {
        if (cPrimaryLocale == aLocale) {
            return;
        }
        cPrimaryLocale = aLocale;
        for (Object manager : cManagers) {
            ((ResourceManager) manager).setLocale(cPrimaryLocale);
        }
    }
}