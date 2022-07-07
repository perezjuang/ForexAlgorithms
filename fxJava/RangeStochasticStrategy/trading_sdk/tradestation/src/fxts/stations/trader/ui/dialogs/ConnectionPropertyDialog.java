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
 * 05/18/2006   Andre Mermegas: extends ABaseDialog
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.ConnectionPropertyPanel;
import fxts.stations.core.FXCMConnection;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.util.ResourceManager;

import java.awt.Frame;
import java.awt.HeadlessException;

/**
 * @author Andre Mermegas
 *         Date: Jan 16, 2006
 *         Time: 4:11:58 PM
 */
public class ConnectionPropertyDialog extends ABaseDialog {
    /**
     * @param aFXCMConnection FXCM Connection
     * @param aOwner Frame Owner
     *
     * @throws HeadlessException
     */
    public ConnectionPropertyDialog(FXCMConnection aFXCMConnection, Frame aOwner) throws HeadlessException {
        super(aOwner);
        ResourceManager rm = TradeApp.getInst().getResourceManager();
        ConnectionPropertyPanel contentPane = new ConnectionPropertyPanel(this, aFXCMConnection);
        setTitle(rm.getString("IDS_CONNECTION_PROPERTIES"));
        setContentPane(contentPane);
        pack();
        setModal(true);
        setLocationRelativeTo(getOwner());
    }

    public int showModal() {
        setVisible(true);
        return 0;
    }
}
