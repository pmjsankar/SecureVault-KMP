package com.pmj.securevault

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform