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
package fxts.stations.transport;

import fxts.stations.util.Event;
import fxts.stations.util.EventProcessor;
import fxts.stations.util.IEventRecipient;

/**
 * LiaisonListenerStub class
 * This is stub for ILiaisonListener and IEventRecepient interfaces.
 * It allows post events from background thread to main dispatch thread.
 */
public class LiaisonListenerStub implements ILiaisonListener, IEventRecipient {
    private static final String EXCEPTION = "exception";
    private static final int LIAISON_CRITICAL_ERROR = 4;
    /**
     * Values representing kinds of Liaison notifications
     */
    private static final int LIAISON_LOGIN_COMPLETE = 0;
    private static final int LIAISON_LOGIN_FAILED = 1;
    private static final int LIAISON_LOGOUT_COMPLETE = 2;
    private static final int LIAISON_STATUS = 3;
    /**
     * Event parameters names
     */
    private static final String NOTIFICATION = "notification";
    private static final String STATUS = "status";
    /*Target to perform ILiaisonListener methods of the main ILiaisonListener implementation*/
    private ILiaisonListener mTarget;

    /**
     * Constructor
     */
    public LiaisonListenerStub(ILiaisonListener aListener) {
        mTarget = aListener;
    }

    /**
     * This method is called when critical error occurred. Connection is closed.
     *
     * @param aEx
     */
    public void onCriticalError(LiaisonException aEx) {
        Event evt = new Event();
        evt.setRecipient(this);
        evt.addParameter(NOTIFICATION, new Integer(LIAISON_CRITICAL_ERROR));
        evt.addParameter(EXCEPTION, aEx);
        EventProcessor.getInst().post(evt);
    }

    /**
     * This method is called when the event for this recipient has been post.
     */
    public void onEvent(Event aEvent) {
        Integer notification;
        LiaisonException exception;
        LiaisonStatus status;
        if (aEvent == null) {
            return;
        }
        try {
            notification = (Integer) aEvent.getParameter(NOTIFICATION);
            switch (notification.shortValue()) {
                case LIAISON_LOGIN_COMPLETE:
                    mTarget.onLoginCompleted();
                    break;
                case LIAISON_LOGIN_FAILED:
                    try {
                        exception = (LiaisonException) aEvent.getParameter(EXCEPTION);
                        mTarget.onLoginFailed(exception);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case LIAISON_LOGOUT_COMPLETE:
                    mTarget.onLogoutCompleted();
                    break;
                case LIAISON_STATUS:
                    try {
                        status = (LiaisonStatus) aEvent.getParameter(STATUS);
                        mTarget.onLiaisonStatus(status);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case LIAISON_CRITICAL_ERROR:
                    try {
                        exception = (LiaisonException) aEvent.getParameter(EXCEPTION);
                        mTarget.onCriticalError(exception);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is called when status of liaison has changed.
     *
     * @param aStatus
     */
    public void onLiaisonStatus(LiaisonStatus aStatus) {
        Event evt = new Event();
        evt.setRecipient(this);
        evt.addParameter(NOTIFICATION, new Integer(LIAISON_STATUS));
        evt.addParameter(STATUS, aStatus);
        EventProcessor.getInst().post(evt);
    }

    /**
     * This method is called when initiated login command has completed successfully.
     */
    public void onLoginCompleted() {
        Event evt = new Event();
        evt.setRecipient(this);
        evt.addParameter(NOTIFICATION, new Integer(LIAISON_LOGIN_COMPLETE));
        EventProcessor.getInst().post(evt);
    }

    /**
     * This method is called when initiated login command has failed. aEx
     * contains information about error.
     *
     * @param aEx
     */
    public void onLoginFailed(LiaisonException aEx) {
        Event evt = new Event();
        evt.setRecipient(this);
        evt.addParameter(NOTIFICATION, new Integer(LIAISON_LOGIN_FAILED));
        evt.addParameter(EXCEPTION, aEx);
        EventProcessor.getInst().post(evt);
    }

    /**
     * This method is called when initiated logout command has completed.
     */
    public void onLogoutCompleted() {
        Event evt = new Event();
        evt.setRecipient(this);
        evt.addParameter(NOTIFICATION, new Integer(LIAISON_LOGOUT_COMPLETE));
        EventProcessor.getInst().post(evt);
    }
}