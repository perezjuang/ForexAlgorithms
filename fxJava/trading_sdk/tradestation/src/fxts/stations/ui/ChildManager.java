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
package fxts.stations.ui;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager of child frames.
 */
public class ChildManager extends JDesktopPane {
    private Map<String, JInternalFrame> mFrameMap = new HashMap<String, JInternalFrame>();

    /**
     * Looks for the internal frame with the specified name.
     *
     * @param asName name of the component
     *
     * @return internal frame if found else returns null.
     */
    public JInternalFrame findFrameByName(String asName) {
        JInternalFrame internalFrame = mFrameMap.get(asName);
        if (internalFrame == null) {
            JInternalFrame[] frames = getAllFrames();
            for (JInternalFrame frame : frames) {
                if (asName.equals(frame.getName())) {
                    frame.addInternalFrameListener(new InternalFrameAdapter() {
                        public void internalFrameClosed(InternalFrameEvent aEvent) {
                            mFrameMap.remove(aEvent.getInternalFrame().getName());
                        }
                    });
                    mFrameMap.put(asName, frame);
                    return frame;
                }
            }
        } else {
            return internalFrame;
        }
        return null;
    }
}