/* 
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/ui/WeakActionPropertyChangeListener.java#1 $ 
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
 * $History: $ 
 */
package fxts.stations.ui;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 */
public class WeakActionPropertyChangeListener implements PropertyChangeListener {
    private static ReferenceQueue cQueue;
    private Action mAction;
    private OwnedWeakReference mWeakRef;

    public WeakActionPropertyChangeListener(JComponent aTarget, Action aAction) {
        configureQueue();
        OwnedWeakReference ref;
        while ((ref = (OwnedWeakReference) cQueue.poll()) != null) {
            WeakActionPropertyChangeListener pcl = ref.getOwner();
            Action a = pcl.getAction();
            if (a != null) {
                pcl.removePropertyChangeListener(a);
            }
        }
        mWeakRef = new OwnedWeakReference(aTarget, cQueue, this);
        mAction = aAction;
    }

    private static void configureQueue() {
        synchronized (WeakActionPropertyChangeListener.class) {
            if (cQueue == null) {
                cQueue = new ReferenceQueue();
            }
        }
    }

    protected void removePropertyChangeListener(Object aObj) {
        ((Action) aObj).removePropertyChangeListener(this);
    }

    public Action getAction() {
        return mAction;
    }

    public final void propertyChange(PropertyChangeEvent aPropertychangeEvent) {
        if (getTarget() == null) {
            Object obj = aPropertychangeEvent.getSource();
            removePropertyChangeListener(obj);
        } else {
            processPropertyChange(aPropertychangeEvent);
        }
    }

    public JComponent getTarget() {
        return (JComponent) mWeakRef.get();
    }

    protected void processPropertyChange(PropertyChangeEvent aPropertychangeEvent) {
        JComponent target = getTarget();
        if (target == null) {
            Action action = (Action) aPropertychangeEvent.getSource();
            action.removePropertyChangeListener(this);
        } else {
            if (target instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) target;
                if (Action.MNEMONIC_KEY.equals(aPropertychangeEvent.getPropertyName())) {
                    Integer integer = (Integer) aPropertychangeEvent.getNewValue();
                    button.setMnemonic(integer);
                    button.invalidate();
                    button.repaint();
                } else if (Action.NAME.equals(aPropertychangeEvent.getPropertyName())) {
                    button.setText(aPropertychangeEvent.getNewValue().toString());
                    button.repaint();
                } else if (Action.SHORT_DESCRIPTION.equals(aPropertychangeEvent.getPropertyName())) {
                    button.setToolTipText(aPropertychangeEvent.getNewValue().toString());
                    button.invalidate();
                    button.repaint();
                } else if (Action.SMALL_ICON.equals(aPropertychangeEvent.getPropertyName())) {
                    Icon icon = (Icon) aPropertychangeEvent.getNewValue();
                    button.setIcon(icon);
                    button.invalidate();
                    button.repaint();
                } else if (Action.ACTION_COMMAND_KEY.equals(aPropertychangeEvent.getNewValue())) {
                    button.setActionCommand(aPropertychangeEvent.getNewValue().toString());
                    button.invalidate();
                    button.repaint();
                } else if (Action.LONG_DESCRIPTION.equals(aPropertychangeEvent.getNewValue())) {
                    button.getAccessibleContext()
                            .setAccessibleDescription(aPropertychangeEvent.getNewValue().toString());
                    button.invalidate();
                    button.repaint();
                } else if ("enabled".equals(aPropertychangeEvent.getPropertyName())) {
                    Boolean enabled = (Boolean) aPropertychangeEvent.getNewValue();
                    button.setEnabled(enabled.booleanValue());
                    button.invalidate();
                    button.repaint();
                }
            } else if (target instanceof JComboBox) {
                String propertyName = aPropertychangeEvent.getPropertyName();
                JComboBox comboBox = (JComboBox) target;
                if (aPropertychangeEvent.getPropertyName().equals(Action.SHORT_DESCRIPTION)) {
                    String text = (String) aPropertychangeEvent.getNewValue();
                    comboBox.setToolTipText(text);
                    comboBox.invalidate();
                    comboBox.repaint();
                } else if (propertyName.equals("enabled")) {
                    Boolean enabledState = (Boolean) aPropertychangeEvent.getNewValue();
                    comboBox.setEnabled(enabledState.booleanValue());
                    comboBox.invalidate();
                    comboBox.repaint();
                }
            }
        }
    }

    private static class OwnedWeakReference extends WeakReference {
        private WeakActionPropertyChangeListener mReference;

        OwnedWeakReference(JComponent aTarget,
                           ReferenceQueue aReferenceQueue,
                           WeakActionPropertyChangeListener aReference) {
            super(aTarget, aReferenceQueue);
            mReference = aReference;
        }

        public WeakActionPropertyChangeListener getOwner() {
            return mReference;
        }
    }
}
