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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * EventProcessor class
 * This is singleton class that allows to post events from any thread to main dispatch thread.
 */
public class EventProcessor {
    /**
     * The one and only instance of the event processor
     */
    private static final EventProcessor INST = new EventProcessor();

    /**
     * Events that should be processed
     */
    protected final List mProcessingEvents = new ArrayList();
    /**
     * This is instance of inner class implementing Runnable interface.
     * Its run method is called in context of main dispatch thread
     * and it process all waiting events by calling event's recipient
     * onEvent() method and remove all processed events from the queue.
     */
    private Runnable mWorker = new Worker();

    /**
     * Empty constructor
     */
    private EventProcessor() {
    }

    /**
     * Returns the one and only instance of the processor.
     */
    public static EventProcessor getInst() {
        return INST;
    }

    /**
     * Posts the event.
     */
    public void post(Event aEvt) {
        if (aEvt == null) {
            return;
        }
        mProcessingEvents.add(aEvt);
        EventQueue.invokeLater(mWorker);
    }

    /**
     * Worker class
     * This class is to process all waiting events
     */
    private class Worker implements Runnable {
        public void run() {
            while (!mProcessingEvents.isEmpty()) {
                Event evt = (Event) mProcessingEvents.remove(0);
                if (evt != null) {
                    IEventRecipient rcp = evt.getRecipient();
                    if (rcp != null) {
                        rcp.onEvent(evt);
                    }
                }
            }
        }
    }
}
