package com.example.billinglibrarycoroutines

import com.android.billingclient.api.BillingResult

class BillingResultException(val billingResult: BillingResult) : Exception(billingResult.debugMessage)