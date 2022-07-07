/* 
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/ui/WeakPropertyChangeListener.java#1 $ 
 * Copyright (c) 2008 FXCM, LLC. 
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 */
public class WeakPropertyChangeListener implements PropertyChangeListener {
    private WeakReference<PropertyChangeListener> mReference;
    private Object mSource;

    public WeakPropertyChangeListener(PropertyChangeListener aListener, Object aSource) {
        mReference = new WeakReference<PropertyChangeListener>(aListener);
        mSource = aSource;
    }

    public void propertyChange(PropertyChangeEvent aEvent) {
        PropertyChangeListener listener = mReference.get();
        if (listener == null) {
            removeListener();
        } else {
            try {
                listener.propertyChange(aEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeListener() {
        try {
            Method method = mSource.getClass().getMethod("removePropertyChangeListener", PropertyChangeListener.class);
            method.invoke(mSource, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
