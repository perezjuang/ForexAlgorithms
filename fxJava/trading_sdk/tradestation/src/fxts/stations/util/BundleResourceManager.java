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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
/*                                                                              Comment by USHIK 9/23/2003
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
*/

/**
 * BundleResourceManager class
 * Concrete implementation of the ResourceManager
 */
public class BundleResourceManager extends ResourceManager {
    /**
     * Resource bundle for the current locale. It's used to get localized resources.
     */
    private ResourceBundle mBundle;
    /**
     * Resource bundle for the default locale. It's used to get localized resources.
     */
    private ResourceBundle mDefaultBundle;
    /**
     * Base bundle name (see ResourceBundle).
     */
    private String msBaseBundleName;

    /**
     * This method is called when locale is changed.
     *
     * @param aLocale new locale.
     *
     * @return true if succeeded, false otherwise.
     */
    protected boolean changeLocale(Locale aLocale) {
        mBundle = ResourceBundle.getBundle(msBaseBundleName, aLocale);
        return true;
    }

    /**
     * Returns URL of the resource specified by the id. It's assumed that caller then can load the resource with class loader or other methods. Resource can be icon now.
     *
     * @param asKey id of the resource which URL is required.
     *
     * @return URL of the specified resource or null if URL not found
     */
    public URL getResource(String asKey) {
        String sValue;
        ClassLoader classLoader;
        URL url = null;
        try {
            sValue = mBundle.getString(asKey);
        } catch (MissingResourceException ex1) {
            try {
                sValue = mDefaultBundle.getString(asKey);
            } catch (MissingResourceException ex2) {
                return null;
            }
        }
        classLoader = getClass().getClassLoader();
        url = classLoader.getResource(sValue);
        if (url != null) {
            return url;
        }
        try {
            url = new URL(sValue);
        } catch (MalformedURLException ex) {
            return null;
        }
        return url;
    }

    /**
     * Returns localized string with specified id.
     *
     * @param asKey id of required string.
     *
     * @return localized string if it is found, null otherwise.
     */
    public String getString(String asKey) {
        String sValue;
        try {
            sValue = mBundle.getString(asKey);
        } catch (MissingResourceException ex1) {
            try {
                sValue = mDefaultBundle.getString(asKey);
            } catch (MissingResourceException ex2) {
                return null;
            }
        }
        return sValue;
    }

    /**
     * Returns localized string with specified id.
     *
     * @param asKey     id of required string.
     * @param asDefault string to be returned in case of asKey not found
     *
     * @return localized string if it is found, asDefault otherwise.
     */
    public String getString(String asKey, String asDefault) {
        String sValue;
        try {
            sValue = mBundle.getString(asKey);
        } catch (MissingResourceException ex1) {
            try {
                sValue = mDefaultBundle.getString(asKey);
            } catch (MissingResourceException ex2) {
                return asDefault;
            }
        }
        return sValue;
    }

    /*                                                                              Comment by USHIK 9/23/2003 
    //    /**The method is called when resource manager is created.
         * @param aDescriptorRoot Node object that represents root of the XML file.
    //     
        protected void init(Node aDescriptorRoot) 
                throws java.lang.Exception {
    
            Enumeration bundleResourceNodes;
            Enumeration baseNameNodes;
            Enumeration names;
            Node bundleResourceNode;
            Node baseNameNode;
            Node name;
    
            bundleResourceNodes = Util.findChildElement("BundleResourceManager", aDescriptorRoot);
            if (bundleResourceNodes.hasMoreElements()) {
                bundleResourceNode = (Node)(bundleResourceNodes.nextElement());
                baseNameNodes = Util.findChildElement("BundleBaseName", bundleResourceNode);
                if (baseNameNodes.hasMoreElements()) {
                    baseNameNode = (Node)(baseNameNodes.nextElement());
                    msBaseBundleName = Util.getChildText(baseNameNode);
                    if ("".equals(msBaseBundleName)) {
                        throw new Exception(INCORRECT_XML_FILE + "\"BundleBaseName\" node should contain text node");
                    }
                    mDefaultBundle = ResourceBundle.getBundle(msBaseBundleName, getDefaultLocale());
                } else {
                    throw new Exception(INCORRECT_XML_FILE + "file should have \"BundleBaseName\" node");
                }
            } else {
                throw new Exception(INCORRECT_XML_FILE + "file should have \"BundleResourceManager\" node");
            }
            
        }
    */

    /**
     * The method is called when resource manager is created.
     *
     * @param aDescriptorRoot Node object that represents root of the XML file.
     */
    protected void init(Properties aResourceIni)
            throws Exception {
        msBaseBundleName = aResourceIni.getProperty(RESOURCEMANAGER_BUNDLEBASENAME_KEY);
        if (msBaseBundleName == null) {
            throw new Exception(INCORRECT_INI_FILE + RESOURCEMANAGER_BUNDLEBASENAME_KEY + " should be in");
        }
        mDefaultBundle = ResourceBundle.getBundle(msBaseBundleName, getDefaultLocale());
    }
}