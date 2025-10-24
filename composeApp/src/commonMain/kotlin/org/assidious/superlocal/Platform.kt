package org.assidious.superlocal

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform