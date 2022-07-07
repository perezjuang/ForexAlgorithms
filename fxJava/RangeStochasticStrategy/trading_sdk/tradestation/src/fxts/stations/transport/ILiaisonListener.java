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
 * interface ILiaisonListener.<br>
 * <br>
 * This interface should be implemented by classes that wants to receive
 * notifications from the liaison about change of status and completing of
 * login/logout operations.<br>
 * <br>
 *
 * @Creation date (9/3/2003 6:20 PM)
 */
public interface ILiaisonListener {
    /**
     * This method is called when critical error occurred. Connection is closed.
     *
     * @param aEx
     */
    void onCriticalError(LiaisonException aEx);

    /**
     * This method is called when status of liaison has changed.
     *
     * @param aStatus
     */
    void onLiaisonStatus(LiaisonStatus aStatus);

    /**
     * This method is called when initiated login command has completed successfully.
     */
    void onLoginCompleted();

    /**
     * This method is called when initiated login command has failed. aEx
     * contains information about error.
     *
     * @param aEx
     */
    void onLoginFailed(LiaisonException aEx);

    /**
     * This method is called when initiated logout command has completed.
     */
    void onLogoutCompleted();
}