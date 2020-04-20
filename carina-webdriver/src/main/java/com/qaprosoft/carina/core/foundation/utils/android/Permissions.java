/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils.android;

public class Permissions {

    public enum Permission {
        CAMERA_ACCESS("android.permission.CAMERA"),
        READ_EXTERNAL_STORAGE(
                "android.permission.READ_EXTERNAL_STORAGE"),
        WRITE_EXTERNAL_STORAGE(
                "android.permission.WRITE_EXTERNAL_STORAGE"),
        ACCESS_COARSE_LOCATION(
                "android.permission.ACCESS_COARSE_LOCATION"),
        ACCESS_FINE_LOCATION(
                "android.permission.ACCESS_FINE_LOCATION"),
        ACCESS_LOCATION_EXTRA_COMMANDS(
                "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS");

        private String permission;

        private Permission(String permission) {
            this.permission = permission;
        }

        public String getPermission() {
            return this.permission;
        }
    }

    public enum PermissionType {
        DENIED("denied"),
        GRANTED("granted"),
        REQUESTED("requested");

        private String status;

        private PermissionType(String status) {
            this.status = status;
        }

        public String getType() {
            return this.status;
        }
    }

    public enum PermissionAction {
        GRANT("grant"),
        REVOKE("revoke");

        private String action;

        private PermissionAction(String action) {
            this.action = action;
        }

        public String getAction() {
            return this.action;
        }
    }
}