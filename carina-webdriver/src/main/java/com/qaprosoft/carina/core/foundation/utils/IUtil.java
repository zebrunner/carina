package com.qaprosoft.carina.core.foundation.utils;

import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public interface IUtil {

    static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);

    static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    static final int MINIMUM_TIMEOUT = 2;

    static final int DEFAULT_SWIPE_TIMEOUT = 1000;

    enum Direction {
        LEFT, RIGHT, UP, DOWN, VERTICAL, VERTICAL_DOWN_FIRST, HORIZONTAL, HORIZONTAL_RIGHT_FIRST
    }

    enum JSDirection {
        UP("up"), DOWN("down");

        String directionName;

        JSDirection(String directionName) {
            this.directionName = directionName;
        }

        public String getName() {
            return directionName;
        }

    }

}
