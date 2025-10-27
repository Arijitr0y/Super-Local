package org.assidious.superlocal.feature.auth

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

private const val KEY_AT = "sb.at"
private const val KEY_RT = "sb.rt"

object SessionStore {
    private val settings by lazy { Settings() }

    fun save(accessToken: String, refreshToken: String) {
        settings[KEY_AT] = accessToken
        settings[KEY_RT] = refreshToken
    }

    fun load(): Pair<String, String>? {
        val at = settings.getStringOrNull(KEY_AT) ?: return null
        val rt = settings.getStringOrNull(KEY_RT) ?: return null
        return at to rt
    }

    fun clear() {
        settings.remove(KEY_AT)
        settings.remove(KEY_RT)
    }
}
