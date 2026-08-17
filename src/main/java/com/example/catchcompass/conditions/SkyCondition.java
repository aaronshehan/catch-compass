package com.example.catchcompass.conditions;

/**
 * Your README asks for "general sky conditions" without naming the values, so
 * these are a starting set. Adding one means a new Flyway migration to widen
 * the CHECK constraint, which is the intended friction: the database should
 * not silently accept a value the application does not know about.
 */
public enum SkyCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    OVERCAST,
    RAIN,
    SNOW,
    FOG
}
