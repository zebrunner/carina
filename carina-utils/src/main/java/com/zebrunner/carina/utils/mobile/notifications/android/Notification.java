/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zebrunner.carina.utils.mobile.notifications.android;

public class Notification {

    private String notificationPkg;
    private String tickerText;

    public Notification() {

    }

    public Notification(final String pkg, final String tickerText) {
        this.notificationPkg = pkg;
        this.tickerText = tickerText;
    }

    public String getNotificationPkg() {
        return notificationPkg;
    }

    public String getNotificationText() {
        return tickerText;
    }

    public void setNotificationPkg(String notificationPkg) {
        this.notificationPkg = notificationPkg;
    }

    public void setNotificationText(String tickerText) {
        this.tickerText = tickerText;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((notificationPkg == null) ? 0 : notificationPkg.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Notification other = (Notification) obj;
        if (notificationPkg == null) {
            if (other.notificationPkg != null)
                return false;
        } else if (!notificationPkg.equals(other.notificationPkg))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Notification [notificationPkg=" + notificationPkg + ", notificationText=" + tickerText + "]";
    }

}
