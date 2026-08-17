package com.example.catchcompass.conditions;

/**
 * How a conditions record came to exist. Worth recording because a value the
 * angler typed and a value an API guessed are different kinds of evidence when
 * you later ask which conditions produce the most catches.
 */
public enum ConditionsSource {

    /** Typed by the angler. */
    MANUAL,

    /** Retrieved from a provider and saved unchanged. */
    WEATHER_API,

    /** Retrieved from a provider, then corrected by the angler before saving. */
    WEATHER_API_EDITED
}
