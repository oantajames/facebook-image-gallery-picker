package com.imagepicker.facebook

/**
 * @author james on 10/11/17.
 */

abstract class BaseGraphRequest : FacebookCallFactory.BaseCallback {

    internal abstract fun onExecute()

    override fun onError(exception: Exception) {
        //todo send error broadcast
    }

    override fun onCancel() {
        //todo send cancel broadcast
    }

}
