package com.kct.tplusmcscenter.utils.exception

import java.io.IOException

class NoInternetException : IOException() {
    override val message: String
        get() = "The network is not connected"
}