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
package fxts.stations.trader.ui;

import fxts.stations.transport.LiaisonStatus;

import javax.swing.*;
import java.awt.*;

/**
 * This is JPanel-based pane that shows current liaison
 * state as color indicator.
 */
public class LiaisonStatusIndicator extends JPanel {
    /**
     * Color of state LiaisonStatus.DISCONNECTED.
     */
    private static final Color DISCONNECTED_COLOR = Color.RED;
    /**
     * Color of state LiaisonStatus.READY.
     */
    private static final Color READY_COLOR = Color.GREEN;
    /**
     * Color of state LiaisonStatus.SENDING.
     */
    private static final Color SENDING_COLOR = Color.YELLOW;
    /**
     * Color of state LiaisonStatus.RECEIVING.
     */
    private static final Color RECEIVING_COLOR = Color.BLUE;

    /* -- Constructors -- */

    /**
     * Constructor.
     */
    public LiaisonStatusIndicator() {
        //set disconected indicator by default
        setBackground(DISCONNECTED_COLOR);
    }

    /* -- Public methods -- */

    /**
     * Overrides JPanel's method.
     */
    public Dimension getPreferredSize() {
        return new Dimension(13, 13);
    }

    /**
     * This method is called when status of the liaison is changed.
     */
    public void onLiaisonStatus(LiaisonStatus aStatus) {
        if (aStatus.equals(LiaisonStatus.DISCONNECTED) || aStatus.equals(LiaisonStatus.CONNECTING) ||
            aStatus.equals(LiaisonStatus.RECONNECTING) || aStatus.equals(LiaisonStatus.DISCONNECTING)) {
            setBackground(DISCONNECTED_COLOR);
        } else if (aStatus.equals(LiaisonStatus.READY)) {
            setBackground(READY_COLOR);
        } else if (aStatus.equals(LiaisonStatus.SENDING)) {
            setBackground(SENDING_COLOR);
        } else if (aStatus.equals(LiaisonStatus.RECEIVING)) {
            setBackground(RECEIVING_COLOR);
        }
    }
}