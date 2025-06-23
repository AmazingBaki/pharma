package com.pharmacy.app.utils.oauthInterceptor

interface TimestampService {
    val timestampInSeconds: String
    val nonce: String
}