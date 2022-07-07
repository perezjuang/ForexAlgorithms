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
package fxts.stations.core;

import com.fxcm.fix.IFixDefs;
import fxts.stations.transport.ALiaisonListener;
import fxts.stations.transport.LiaisonListenerStub;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A class, which singleton instance is used to manage all trade actions.
 */
public class ActionManager extends ALiaisonListener {
    /**
     * The one and only instance of the trade action manager.
     */
    private static ActionManager cInst;
    /**
     * Actions hashtable. Key is action name.
     */
    private Hashtable mActions = new Hashtable();
    /**
     * Stub for liaison listener. All activity should be performed only in main dispatch thread.
     */
    private LiaisonListenerStub mLiaisonStub;
    /**
     * This variable dependents on the current state of the liaison and determines if a trade action can be performed in this state.
     */
    private boolean mbCanAct;

    /**
     * Private constructor
     */
    private ActionManager() {
        mLiaisonStub = new LiaisonListenerStub(this);
        Liaison.getInstance().addLiaisonListener(mLiaisonStub);
        cInst = this;
    }

    /**
     * Returns enumeration of all actions in the manager.
     */
    public Enumeration actions() {
        return mActions.elements();
    }

    /**
     * Adds new action to the manager.
     */
    public void add(ATradeAction aAction) {
        if (aAction != null && aAction.getName() != null) {
            mActions.put(aAction.getName(), aAction);
        }
    }

    /**
     * Returns true if the application is in appropriate state to perform trade action.
     */
    public boolean canAct() {
        return mbCanAct;
    }

    /**
     * Returns the one and only instance of the trade action manager.
     */
    public static ActionManager getInst() {
        return cInst != null ? cInst : new ActionManager();
    }

    /**
     * This method is called when status of liaison has changed.
     */
    public void onLiaisonStatus(LiaisonStatus aStatus) {
        if (aStatus == LiaisonStatus.READY || aStatus == LiaisonStatus.RECEIVING) {
            if (!mbCanAct && TradingServerSession.getInstance().getUserKind() != IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
                mbCanAct = true;
                synchronized (mActions) {
                    for (Enumeration e = actions(); e.hasMoreElements();) {
                        ((ATradeAction) e.nextElement()).canAct(mbCanAct);
                    }
                }
            }
        } else {
            if (mbCanAct) {
                mbCanAct = false;
                synchronized (mActions) {
                    for (Enumeration e = actions(); e.hasMoreElements();) {
                        ((ATradeAction) e.nextElement()).canAct(mbCanAct);
                    }
                }
            }
        }
    }

    /**
     * Removes the action from the manager.
     */
    public void remove(ATradeAction aAction) {
        if (aAction != null) {
            mActions.remove(aAction.getName());
        }
    }
}
