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

import java.util.Date;

/**
 * PersistentStorage class
 * Persistent storage is used for store some values at client side.
 * It allows to store named values of predefined basic types.
 * Stored values can be retrieved in next session (next start up of application).
 * This is abstract class for store support.
 */
public abstract class PersistentStorage {
    /**
     * persistent storage class name
     */
    private static final String PERSISTENT_STORAGE_CLASS =
            "fxts.persistent_storage_class";
    /**
     * default persistent storage class name
     */
    private static final String DEFAULT_PERSISTENT_STORAGE_CLASS =
            "fxts.stations.util.PropertiesPersistentStorage";
    /**
     * The one and only instance of the storage
     */
    private static PersistentStorage cStorage;

    /**
     * This method flushes all changes to the persistent location.
     * Should be called before application exit to ensure
     * that in the next session the same values will be retrieved.
     */
    public abstract void flush()
            throws Exception;

    public abstract boolean getBoolean(String asKey, boolean abDefault)
            throws NullPointerException;

    public abstract Date getDate(String asKey, Date adtDefault)
            throws NullPointerException;

    public abstract double getDouble(String asKey, double adblDefault)
            throws NullPointerException;

    public abstract int getInt(String asKey, int aiDefault)
            throws NullPointerException;

    /**
     * Gets the only instance of the storage.
     */
    public static PersistentStorage getStorage()
            throws Exception {
        String persistentStorageClassName;
        Class persistentStorageClass;
        if (cStorage != null) {
            return cStorage;
        }

        // getting persistent storage class name from system
        persistentStorageClassName = System.getProperty(PERSISTENT_STORAGE_CLASS);
        if (persistentStorageClassName == null) {
            persistentStorageClassName = DEFAULT_PERSISTENT_STORAGE_CLASS;
        }

        // creating new instance of the class
        try {
            persistentStorageClass = Class.forName(persistentStorageClassName);//throws ClassNotFoundException
        } catch (Throwable e) {
            throw new Exception("Error is thrown :" + e.getMessage());
        }
        cStorage = (PersistentStorage) persistentStorageClass.newInstance();//throws InstantiationException, IllegalAccessException
        return cStorage;
    }

    /**
     * Gets named value of the specific type from the storage.
     * Depending on implementation can throw types mismatch exception.
     */
    public abstract String getString(String asKey, String asDefault)
            throws NullPointerException;

    /**
     * Removes the specified entry.
     */
    public abstract void remove(String asKey);

    /**
     * Sets the value with specified name.
     */
    public abstract void set(String asKey, String asValue);

    public abstract void set(String asKey, int aiValue);

    public abstract void set(String asKey, double adblValue);

    public abstract void set(String asKey, Date adtValue);

    public abstract void set(String asKey, boolean abValue);
}
