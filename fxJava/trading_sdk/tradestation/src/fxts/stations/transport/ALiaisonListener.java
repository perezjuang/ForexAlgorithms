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
package fxts.stations.transport;

/**
 * ALiaisonListener class
 * This is default implementation of ILiaisonListener interface.
 */
public abstract class ALiaisonListener implements ILiaisonListener {
    /**
     * This method is called when critical error occurred. Connection is closed.
     *
     * @param aEx
     */
    public void onCriticalError(LiaisonException aEx) {
    }

    /**
     * This method is called when status of liaison has changed.
     *
     * @param aStatus
     */
    public void onLiaisonStatus(LiaisonStatus aStatus) {
    }

    /**
     * This method is called when initiated login command has completed successfully.
     */
    public void onLoginCompleted() {
    }

    /**
     * This method is called when initiated login command has failed. aEx
     * contains information about error.
     *
     * @param aEx
     */
    public void onLoginFailed(LiaisonException aEx) {
    }

    /**
     * This method is called when initiated logout command has completed.
     */
    public void onLogoutCompleted() {
    }
}