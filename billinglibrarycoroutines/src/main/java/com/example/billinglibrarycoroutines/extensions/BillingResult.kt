package com.example.billinglibrarycoroutines.extensions

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.example.billinglibrarycoroutines.BillingResultException

internal fun BillingResult.toBillingException() = BillingResultException(this)

fun BillingResult.isOk() = responseCode == BillingClient.BillingResponseCode.OK