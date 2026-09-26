package br.com.mrserluiz.terra2.core;

/** State observed before Terra 2.0 is allowed to attach a generator. */
public enum WorldState {
    MISSING,
    EXISTING_UNMANAGED,
    EXISTING_MANAGED_BY_TERRA2
}
