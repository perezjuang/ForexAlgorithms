/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/util/Signaler.java#1 $
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
 * $History: $
 */
package fxts.stations.util;

/**
 * Signaler class
 * This class is used by objects
 * that can send signals when their state is changed.
 */
public class Signaler {
    private final WeakListener<ISignalListener> mAddListeners = new WeakListener<ISignalListener>();
    private final WeakListener<ISignalListener> mChangeListeners = new WeakListener<ISignalListener>();
    private final WeakListener<ISignalListener> mRemoveListeners = new WeakListener<ISignalListener>();

    /**
     * This methods notifies all listeners that are subscribed for the signal type the same as aSignal.
     * @param aSignal signal
     */
    public void notify(ISignal aSignal) {
        try {
            if (aSignal != null) {
                if (SignalType.ADD.equals(aSignal.getType())) {
                    synchronized (mAddListeners) {
                        for (ISignalListener listener : mAddListeners) {
                            listener.onSignal(this, aSignal);
                        }
                    }
                } else if (SignalType.CHANGE.equals(aSignal.getType())) {
                    synchronized (mChangeListeners) {
                        for (ISignalListener listener : mChangeListeners) {
                            listener.onSignal(this, aSignal);
                        }
                    }
                } else if (SignalType.REMOVE.equals(aSignal.getType())) {
                    synchronized (mRemoveListeners) {
                        for (ISignalListener listener : mRemoveListeners) {
                            listener.onSignal(this, aSignal);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method subscribes the listener for receive signals of the specified type.
     * NOTE: for each subscribe should be unsubscribe
     * @param aListener listener
     * @param aSignalType type
     */
    public void subscribe(ISignalListener aListener, SignalType aSignalType) {
        if (aListener != null && aSignalType != null) {
            if (aSignalType == SignalType.ADD) {
                synchronized (mAddListeners) {
                    mAddListeners.add(aListener);
                }
            } else if (aSignalType == SignalType.CHANGE) {
                synchronized (mChangeListeners) {
                    mChangeListeners.add(aListener);
                }
            } else if (aSignalType == SignalType.REMOVE) {
                synchronized (mRemoveListeners) {
                    mRemoveListeners.add(aListener);
                }
            }
        }
    }

    /**
     * This method unsubscribes the listener from receive signals of the specified type.
     * NOTE: for each subscribe should be unsubscribe
     * @param aListener listener
     * @param aSignalType type
     */
    public void unsubscribe(ISignalListener aListener, SignalType aSignalType) {
        if (aListener != null && aSignalType != null) {
            if (aSignalType == SignalType.ADD) {
                synchronized (mAddListeners) {
                    mAddListeners.remove(aListener);
                }
            } else if (aSignalType == SignalType.CHANGE) {
                synchronized (mChangeListeners) {
                    mChangeListeners.remove(aListener);
                }
            } else if (aSignalType == SignalType.REMOVE) {
                synchronized (mRemoveListeners) {
                    mRemoveListeners.remove(aListener);
                }
            }
        }
    }
}
