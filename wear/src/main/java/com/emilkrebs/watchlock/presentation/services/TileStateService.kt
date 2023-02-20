package com.emilkrebs.watchlock.presentation.services

// needed, else the variables reset itself when the tile is updating

// before requesting the lock status, the lock status is unknown
private var _lockStatus: LockStatus = LockStatus.UNKNOWN

// when first requesting the lock status, the app is loading
private var _isLoading: Boolean = false

/**
 * Gets the lock status
 * @return the lock status
 */
public fun getLockStatus(): LockStatus {
    return _lockStatus
}

/**
 * Sets the lock status
 * @param lockStatus the new lock status
 */
public fun setLockStatus(lockStatus: LockStatus) {
    _lockStatus = lockStatus
}

/**
 * Sets the loading status
 * @param isLoading the new loading status
 */
public fun getLoading(): Boolean {
    return _isLoading
}

/**
 * Sets the loading status
 * @param isLoading the new loading status
 */
public fun setLoading(isLoading: Boolean) {
    _isLoading = isLoading
}
