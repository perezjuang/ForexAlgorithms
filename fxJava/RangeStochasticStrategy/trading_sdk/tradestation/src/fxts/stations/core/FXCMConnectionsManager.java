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

import com.fxcm.util.Util;
import fxts.stations.util.UserPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Andre Mermegas
 *         Date: Jan 16, 2006
 *         Time: 2:41:56 PM
 */
public class FXCMConnectionsManager {
    private static Map<String, FXCMConnection> cConnectionMap = new TreeMap<String, FXCMConnection>();
    private static List<IConnectionManagerListener> cListenerList = new ArrayList<IConnectionManagerListener>();
    private static UserPreferences cPreferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
    private static final String EMPTY_CODE = "-";

    private static String convertToString() {
        StringBuilder sb = new StringBuilder();
        String[] keys = cConnectionMap.keySet().toArray(new String[cConnectionMap.size()]);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            FXCMConnection conn = cConnectionMap.get(key);
            String username = conn.getUsername();
            sb.append(username == null || "".equals(username.trim()) ? EMPTY_CODE : username)
                    .append(";")
                    .append(conn.getTerminal())
                    .append(";")
                    .append(conn.getUrl());
            if (i + 1 != keys.length) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    public static FXCMConnection getConnection(String aTerminal) {
        return cConnectionMap.get(aTerminal);
    }

    public static String[] getTerminals() {
        return cConnectionMap.keySet().toArray(new String[cConnectionMap.size()]);
    }

    private static void notifyListeners() {
        for (IConnectionManagerListener listener : cListenerList) {
            listener.updated();
        }
    }

    public static void register(IConnectionManagerListener aConnectionManagerPanel) {
        cListenerList.add(aConnectionManagerPanel);
    }

    public static void unregister(IConnectionManagerListener aConnectionManagerPanel) {
        cListenerList.remove(aConnectionManagerPanel);
    }

    public static void remove(String aTerminal) {
        if (aTerminal != null) {
            cConnectionMap.remove(aTerminal);
        }
        cPreferences.set("Server.Connections", convertToString());
        notifyListeners();
    }

    public static void setConnections(String aConnections) {
        // format example
        // "andre;CW;http://devmw2.dev.fxcm.com:9999/Hosts.jsp|andmer;Demo;http://fxcorporate.com/Phosts.jsp"
        if (aConnections != null) {
            String[] pipes = Util.splitToArray(aConnections, "|");
            for (String pipe : pipes) {
                String[] pair = Util.splitToArray(pipe, ";");
                String username = pair[0];
                String terminal = pair[1];
                String url = pair[2];
                FXCMConnection conn = new FXCMConnection(EMPTY_CODE.equals(username) ? "" : username, terminal, url);
                cConnectionMap.put(terminal, conn);
            }
        }
        cPreferences.set("Server.Connections", aConnections);
        notifyListeners();
    }

    public static void updateAddConnection(String aTerminal, FXCMConnection aFXCMConnection) {
        if (aTerminal != null) {
            cConnectionMap.remove(aTerminal);
        }
        cConnectionMap.put(aFXCMConnection.getTerminal(), aFXCMConnection);
        cPreferences.set("Server.Connections", convertToString());
        notifyListeners();
    }
}