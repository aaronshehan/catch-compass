package com.example.catchcompass.conditions;

public enum TideState {
    HIGH,
    LOW,
    RISING,
    FALLING,
    UNKNOWN,

    /** Inland water, where tide is not a meaningful concept. */
    NOT_APPLICABLE
}
