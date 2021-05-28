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

import fxts.stations.datatypes.IKey;
import fxts.stations.util.signals.AddSignal;
import fxts.stations.util.signals.ChangeSignal;
import fxts.stations.util.signals.RemoveSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SignalVector class
 * This is a list with signaling support.
 * For complete description of the methods see java.util.List.
 */
public class SignalVector extends Signaler {
    private Comparator mComparator;
    private Map mKeyMap = new HashMap();
    private final List mData = new ArrayList();

    /**
     * Appends the specified element to the end of this SignalVector.
     */
    public void add(Object aObj) {
        int index = mData.size();
        if (mComparator != null) {
            // Search for the object to determine its insertion point
            index = Collections.binarySearch(mData, aObj, mComparator);
            if (index < 0) {
                index = -index - 1;
            }
        }
        mData.add(index, aObj);
        if (aObj instanceof IKey) {
            IKey key = (IKey) aObj;
            mKeyMap.put(key.getKey(), key);
        }
        notify(new AddSignal(index, aObj));
    }

    /**
     * Removes all of the elements from this SignalVector.
     * Sends REMOVE signal for each element.
     *
     * @see List#clear()
     */
    public void clear() {
        mKeyMap.clear();
        mData.clear();
    }

    /**
     * Sends CHANGE signal. Should be called when some element was changed directly (not with set() method call).
     */
    public void elementChanged(int aIndex) {
        try {
            Object obj = get(aIndex);
            notify(new ChangeSignal(aIndex, obj, null));
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
    }

    /**
     * Returns an enumeration of the components of this SignalVector.
     *
     * @see List#elements()
     */
    public Enumeration elements() {
        return Collections.enumeration(mData);
    }

    /**
     * Returns the element at the specified position in this SignalVector.
     *
     * @throws ArrayIndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()).
     * @see List#get(int)
     */
    public Object get(int aIndex) {
        try {
            return mData.get(aIndex);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param aKey
     *
     * @return
     */
    public Object get(String aKey) {
        return mKeyMap.get(aKey);
    }

    /**
     * Searches for the first occurence of the given argument,
     * testing for equality using the equals method.
     *
     * @return -1 if the object is not found.
     *
     * @see List#indexOf(Object)
     */
    public int indexOf(Object aObj) {
        return mData.indexOf(aObj);
    }

    /**
     * Tests if this vector has no components.
     *
     * @see List#isEmpty()
     */
    public boolean isEmpty() {
        return mData.isEmpty();
    }

    /**
     * Removes the first occurrence of the specified element in this SignalVector.
     * If the Vector does not contain the element, it is unchanged.
     * Sends REMOVE signal.
     *
     * @see List#remove(Object)
     */
    public boolean remove(Object aObj) {
        int index = indexOf(aObj);
        if (index == -1) {
            return false;
        } else {
            try {
                remove(index);
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
            return true;
        }
    }

    /**
     * Removes the element at the specified position in this SignalVector.
     * Sends REMOVE signal.
     *
     * @throws ArrayIndexOutOfBoundsException if the index was invalid.
     * @see List#remove(int)
     */
    public Object remove(int aIndex) {
        Object removedObj = mData.remove(aIndex);
        if (removedObj instanceof IKey) {
            IKey key = (IKey) removedObj;
            mKeyMap.remove(key.getKey());
        }
        notify(new RemoveSignal(aIndex, removedObj));
        return removedObj;
    }

    /**
     * Replaces the element at the specified position in this Vector with the specified element.
     * Sends CHANGE signal.
     *
     * @throws ArrayIndexOutOfBoundsException if the index out of range (index < 0 || index >= size()).
     * @see List#set(int,Object)
     */
    public Object set(int aIndex, Object aObj) {
        Object retObject = mData.set(aIndex, aObj);
        if (retObject instanceof IKey && aObj instanceof IKey) {
            IKey outgoing = (IKey) retObject;
            IKey incoming = (IKey) aObj;
            if (!outgoing.getKey().equals(incoming.getKey())) {
                mKeyMap.remove(outgoing.getKey());
            }
        }
        notify(new ChangeSignal(aIndex, aObj, retObject));
        return retObject;
    }

    public void setComparator(Comparator aComparator) {
        mComparator = aComparator;
        sort();
    }

    /**
     * Returns the number of components in this SignalVector.
     *
     * @see List#size()
     */
    public int size() {
        return mData.size();
    }

    private void sort() {
        if (mComparator != null) {
            Collections.sort(mData, mComparator);
        }
    }
}
