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
package fxts.stations.ui.colorchooser;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A helper class to make computing synthetic images a little easier.
 */
abstract class SyntheticImage implements ImageProducer {
    static final ColorModel cm = ColorModel.getRGBdefault();
    public static final int pixMask = 0xFF;
    protected volatile boolean aborted = false;
    private SyntheticImageGenerator root;
    private Thread runner;
    protected int width = 10, height = 100;

    protected SyntheticImage() {
    }

    protected SyntheticImage(int w, int h) {
        width = w;
        height = h;
    }

    public synchronized void addConsumer(ImageConsumer ic) {
        for (SyntheticImageGenerator ics = root; ics != null; ics = ics.next) {
            if (ics.ic == ic) {
                return;
            }
        }
        root = new SyntheticImageGenerator(ic, root, this);
    }

    protected void computeRow(int y, int[] row) {
        int p = 255 - 255 * y / (height - 1);
        p = pixMask << 24 | p << 16 | p << 8 | p;
        for (int i = row.length; --i >= 0;) {
            row[i] = p;
        }
    }

    public synchronized boolean isConsumer(ImageConsumer ic) {
        for (SyntheticImageGenerator ics = root; ics != null; ics = ics.next) {
            if (ics.ic == ic) {
                return true;
            }
        }
        return false;
    }

    protected boolean isStatic() {
        return true;
    }

    public void nextFrame(int param) {
    }//Override if !isStatic

    public synchronized void removeConsumer(ImageConsumer ic) {
        SyntheticImageGenerator prev = null;
        for (SyntheticImageGenerator ics = root; ics != null; ics = ics.next) {
            if (ics.ic == ic) {
                ics.useful = false;
                if (prev != null) {
                    prev.next = ics.next;
                } else {
                    root = ics.next;
                }
                return;
            }
        }
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    public synchronized void startProduction(ImageConsumer ic) {
        addConsumer(ic);
        for (SyntheticImageGenerator ics = root; ics != null; ics = ics.next) {
            if (ics.useful && !ics.isAlive()) {
                ics.start();
            }
        }
    }
}

class SyntheticImageGenerator extends Thread {
    ImageConsumer ic;
    SyntheticImageGenerator next;
    SyntheticImage parent;
    boolean useful;

    SyntheticImageGenerator(ImageConsumer ic, SyntheticImageGenerator next,
                            SyntheticImage parent) {
        super("SyntheticImageGenerator");
        this.ic = ic;
        this.next = next;
        this.parent = parent;
        useful = true;
        setDaemon(true);
    }

    private static void doPrivileged(final Runnable doRun) {
        AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        doRun.run();
                        return null;
                    }
                }
        );
    }

    public void run() {
        ImageConsumer ic = this.ic;
        int w = parent.width;
        int h = parent.height;
        int hints = ic.SINGLEPASS | ic.COMPLETESCANLINES | ic.TOPDOWNLEFTRIGHT;
        if (parent.isStatic()) {
            hints |= ic.SINGLEFRAME;
        }
        ic.setHints(hints);
        ic.setDimensions(w, h);
        ic.setProperties(null);
        ic.setColorModel(parent.cm);
        if (useful) {
            int[] row = new int[w];
            doPrivileged(new Runnable() {
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                }
            });
            do {
                for (int y = 0; y < h && useful; y++) {
                    parent.computeRow(y, row);
                    if (parent.aborted) {
                        ic.imageComplete(ic.IMAGEABORTED);
                        return;
                    }
                    ic.setPixels(0, y, w, 1, parent.cm, row, 0, w);
                }
                ic.imageComplete(parent.isStatic() ? ic.STATICIMAGEDONE
                                 : ic.SINGLEFRAMEDONE);
            } while (!parent.isStatic() && useful);
        }
    }
}
