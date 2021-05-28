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
package fxts.stations.ui.help;

import java.util.Vector;

/**
 * History of browsing by help.
 * It`s vector bised class simplifies processing of browing by help.
 */
public class HelpContentHistory extends Vector {
    /* -- Data members -- */
    /**
     * Current step at history.
     */
    private int miCurrent = -1;

    /**
     * Go to back by history.
     *
     * @return new current page
     */
    public String back() {
        if (miCurrent > 0) {
            return (String) get(--miCurrent);
        } else {
            return null;
        }
    }

    /**
     * Go to forvard by history.
     *
     * @return new current page
     */
    public String forward() {
        if (miCurrent < size() - 1) {
            return (String) get(++miCurrent);
        } else {
            return null;
        }
    }

    /**
     * Checking to existing back step.
     *
     * @return true if exists step to back
     */
    public boolean hasBackStep() {
        return miCurrent > 0;
    }

    /**
     * Checking to existing back step.
     *
     * @return true if exists step to back
     */
    public boolean hasForwardStep() {
        return miCurrent < size() - 1;
    }

    /* -- Public methods -- */

    /**
     * Puts new step to history.
     *
     * @param asId identifier of page
     */
    public void put(String asId) {
        //deletes next step if they exists
        for (int i = size() - 1; i > miCurrent; i--) {
            remove(i);
        }

        //adds new step
        add(asId);

        //sets current position
        miCurrent++;
    }
}