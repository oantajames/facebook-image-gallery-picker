package com.imagepicker.facebook

/**
 * @author james on 10/11/17.
 */

abstract class BaseGraphRequest<T : FacebookCallFactory.BaseCallback>
internal constructor(private var mCallback: T?) {

    internal abstract fun onExecute()

    internal fun onError(exception: Exception) {
        if (mCallback != null) mCallback!!.onError(exception)
    }

    internal fun onCancel() {
        if (mCallback != null) mCallback!!.onCancel()
    }

}
