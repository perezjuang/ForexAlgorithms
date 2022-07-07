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

import java.lang.reflect.Method;

/**
 * Simple wrapper for ResizeParameter class.
 * Class support all necessary method, which needed
 * for correct usage of ResizeParameter class.
 * If no internal object present class do nothing.
 */
public class ResizeParameterWrapper {
    private Method mInitMethod;
    private Object mResizeParameter;
    private Method mSetToConstraintsMethod;

    /**
     * Default constructor.
     * Create wrapper around dummy object.
     * No action will be performed when calling
     * any methods of class.
     */
    public ResizeParameterWrapper() {
        mResizeParameter = null;
    }

    /**
     * Construct wrapper object and get all necessary methods.
     *
     * @param aResizeParameter wrapped object.
     */
    public ResizeParameterWrapper(Object aResizeParameter) throws Exception {
        mResizeParameter = aResizeParameter;
        mInitMethod = aResizeParameter.getClass().getMethod("init", Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);
        mSetToConstraintsMethod = aResizeParameter.getClass().getMethod("setToConstraints", Object.class);
    }

    /**
     * Invoke "init" method of internal object
     */
    public void init(double aLeft, double aTop, double aRight, double aBottom) {
        if (mResizeParameter != null) {
            try {
                mInitMethod.invoke(mResizeParameter, aLeft, aTop, aRight, aBottom);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Invoke "setToConstraints" method of internal object
     */
    public void setToConstraints(Object aObject) {
        if (mResizeParameter != null) {
            try {
                mSetToConstraintsMethod.invoke(mResizeParameter, aObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}