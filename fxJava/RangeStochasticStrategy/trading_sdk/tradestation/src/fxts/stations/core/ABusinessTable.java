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

import fxts.stations.util.SignalVector;

/**
 * Abstract class that is base for all tables
 * containing business data.
 */
public abstract class ABusinessTable extends SignalVector {
    /**
     * This method enables/disables recalculate business data mode for the table.
     * It must be overridden in subclasses.
     */
    public void enableRecalc(boolean abEnable) {
    }
}