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
 *
 */
package fxts.stations.transport;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Abstract class ALiaisonImpl.<br>
 * <br>
 * The class ALiaisonImpl extends class ALiaison with the methods and properties
 * are hidden for ALiaison client and used by realization.<br>
 * It is responsible for actions which realizations seem as the same in all implementation:
 * <ul>
 * <li> Collecting requests and following sending in separate thread. </li>
 * <li> Managing the status, and dispatching it and all ingoing events to listeners </li>
 * </ul>
 * <br>
 * Creation date (9/4/2003 4:17 PM)
 */
public abstract class ALiaisonImpl extends ALiaison {

    private final Object mutex = new Object();

    /**
     * Internal class Queue.<br>
     * The class does:
     * <ul>
     * <li>queuing requests waiting to be sent;</li>
     * <li>notifying sender thread about it has to work.</li>
     * <br>
     * <br>
     */
    protected class Queue implements IReqCollection {
        /**
         * The collection of requests waiting to be sent
         */
        private Vector<IRequester> mCollection = new Vector<IRequester>();

        /**
         * Adds object to collection using Vector's method.
         *
         * @param aRequester
         */
        public void add(IRequester aRequester) {
            mCollection.add(aRequester);
        }

        /**
         * Removes all elements from queue
         */
        public void clear() {
            mCollection.clear();
        }

        /**
         * Returns current amount of elements in queue
         */
        public int getSize() {
            return mCollection.size();
        }

        /**
         * Returns reference to object from top of queue without removing.
         */
        public IRequester getTop() {
            if (!mCollection.isEmpty()) {
                return mCollection.firstElement();
            }
            return null;
        }

        /**
         * Adds object of argument to collection using its toQueue method.
         *
         * @param aObject
         */
        public void put(IRequester aObject) {
            aObject.toQueue(this);
        }

        /**
         * Extracts and removes object from top of queue.
         */
        public IRequester remove() {
            if (!mCollection.isEmpty()) {
                return mCollection.remove(0);
            }
            return null;
        }

        /**
         * Extracts and removes object from queue.
         */
        public void remove(IRequester aObject) {
            if (!mCollection.isEmpty()) {
                mCollection.remove(aObject);
            }
        }
    }

    /**
     * Internal class SenderThread.<br>
     * <br>
     * It is responsible for:
     * <ul>
     * <li>waiting notification of it has to work;</li>
     * <li>extracting the request from queue and calling its method doIt;</li>
     * <li>terminating self by outgoing command.</li>
     * <br>
     */
    protected class SenderThread extends Thread {
        /**
         * The flag what thread should stop
         */
        private boolean mStop;

        /**
         * It does all work
         */
        public void run() {
            while (!mStop) {
                while (!mStop && mReqQueue.getSize() > 0) {
                    IRequester requester = mReqQueue.getTop();
                    IRequest request = requester.getRequest();
                    IRequester next = requester.getSibling();
                    IRequestSender sender = request.getSender();
                    try {
                        LiaisonStatus status = requester.doIt();
                        if (status != null) {
                            mReqQueue.remove();
                            if (next == null) {
                                sender.onRequestCompleted(request, null);
                            }
                            if (mReqQueue.getSize() == 0) {
                                setStatus(status);
                            }
                        } else {
                            mStop = true;
                        }
                    } catch (LiaisonExceptionExit e) {
                        onCriticalError(e);
                        mStop = true;
                    } catch (LiaisonException e) {
                        e.printStackTrace();
                        mReqQueue.remove();
                        for (IRequester current = next; current != null; current = current.getSibling()) {
                            mReqQueue.remove(current);
                        }
                        if (sender != null) {
                            sender.onRequestFailed(request, e);
                        }
                        if (mReqQueue.getSize() == 0) {
                            setStatus(LiaisonStatus.READY);
                        }
                    }
                }
                if (!mStop) {
                    synchronized (mutex) {
                        try {
                            mutex.wait();
                        } catch (InterruptedException e) {
                            mStop = true;
                        }
                    }
                }
            }
            mStop = false;
        }

        /**
         * Receives terminating command
         */
        public void terminate() {
            mStop = true;
            interrupt();
            try {
                join();
            } catch (InterruptedException e) {
                //
            }
            mStop = false;
        }
    }

    /**
     * The collection of requests waiting to be sent
     */
    protected Queue mReqQueue = new Queue();
    /**
     * The refernce to sender thread for starting and terminating
     */
    private SenderThread mSenderThread;

    /**
     * It is a protected constructor. Does nothing.
     */
    protected ALiaisonImpl() {
    }

    /**
     * Dispatch LoginCompleted states to listeners. Should be overridden by extension for
     * access from realization package.
     */
    public void dispatchLoginCompleted() {
        if (mListeners != null) {
            synchronized (mListeners) {
                for (Enumeration<ILiaisonListener> e = mListeners.elements(); e.hasMoreElements();) {
                    e.nextElement().onLoginCompleted();
                }
            }
        }
    }

    /**
     * Dispatch LoginFailed states to listeners. Should be overridden by extension for
     * access from realization package.
     *
     * @param aEx
     */
    protected void dispatchLoginFailed(LiaisonException aEx) {
        if (mListeners != null) {
            synchronized (mListeners) {
                for (Enumeration<ILiaisonListener> e = mListeners.elements(); e.hasMoreElements();) {
                    e.nextElement().onLoginFailed(aEx);
                }
            }
        }
    }

    /**
     * Dispatch LogoutCompleted states to listeners. Should be overridden by extension for
     * access from realization package.
     */
    protected void dispatchLogoutCompleted() {
        if (mListeners != null) {
            synchronized (mListeners) {
                for (Enumeration<ILiaisonListener> e = mListeners.elements(); e.hasMoreElements();) {
                    e.nextElement().onLogoutCompleted();
                }
            }
        }
    }

    public abstract boolean isMarketClosed();

    /**
     * Dispatch Error to listeners. Should be overridden by extension for
     * access from realization package.
     *
     * @param aEx
     */
    public void onCriticalError(LiaisonException aEx) {
        if (mListeners != null) {
            synchronized (mListeners) {
                for (Enumeration<ILiaisonListener> e = mListeners.elements(); e.hasMoreElements();) {
                    e.nextElement().onCriticalError(aEx);
                }
            }
        }
    }

    /**
     * Peeks the request to requests queue.
     *
     * @param aRequest
     */
    public void sendRequest(IRequest aRequest) throws LiaisonException {
        try {
            if (mSenderThread == null) {
                throw new LiaisonException(null, "IDS_YOU_ARE_NOT_CONNECTED");
            }
            setStatus(LiaisonStatus.SENDING);
            mReqQueue.put((IRequester) aRequest);
            synchronized (mutex) {
                mutex.notifyAll();
            }
        } catch (Exception e) {
            onCriticalError(new LiaisonException(e, "IDS_INVALID_TYPE_OF_REQUEST"));
        }
    }

    /**
     * Changes state of connection. Dispatches status to the listeners.
     * Should be overridden by extension for access from realization package.
     *
     * @param aLiaisonStatus
     */
    public synchronized void setStatus(LiaisonStatus aLiaisonStatus) {
        if (mReqQueue.getSize() != 0) {
            mStatus = LiaisonStatus.SENDING;
        } else {
            mStatus = aLiaisonStatus;
        }
        if (mListeners != null) {
            synchronized (mListeners) {
                for (Enumeration<ILiaisonListener> e = mListeners.elements(); e.hasMoreElements();) {
                    e.nextElement().onLiaisonStatus(mStatus);
                }
            }
        }
    }

    /**
     * Starts sender thread
     */
    public void start() {
        mSenderThread = new SenderThread();
        mSenderThread.start();
    }

    /**
     * Stops sender thread
     */
    protected void stop() {
        mReqQueue.clear();
        if (mSenderThread != null) {
            mSenderThread.terminate();
            mSenderThread = null;
        }
    }
}
