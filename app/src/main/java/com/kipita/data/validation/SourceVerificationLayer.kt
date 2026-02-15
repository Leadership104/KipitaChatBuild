package com.kipita.data.validation

import java.net.URI

class SourceVerificationLayer(
    private val allowedDomains: Set<String>
) {
    fun isVerifiedSource(url: String): Boolean {
        return runCatching {
            val host = URI(url).host.orEmpty().lowercase()
            allowedDomains.any { host == it || host.endsWith(".$it") }
        }.getOrDefault(false)
    }
}
