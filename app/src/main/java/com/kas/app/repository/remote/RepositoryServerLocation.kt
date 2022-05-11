package com.kas.app.repository.remote

import okhttp3.Callback

interface RepositoryServerLocation {
    fun post(callbackHttp: Callback, data: String)

    fun postData(
        provider: String, callbackTask: Callback, data: String?,
    )
}