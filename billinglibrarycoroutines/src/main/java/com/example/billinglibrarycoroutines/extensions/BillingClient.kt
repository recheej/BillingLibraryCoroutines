package com.example.billinglibrarycoroutines.extensions

import android.app.Activity
import com.android.billingclient.api.*
import com.example.billinglibrarycoroutines.BillingResultException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Throws(BillingResultException::class)
suspend fun BillingClient.getSkuDetails(params: SkuDetailsParams): List<SkuDetails>? = suspendOnMainThread {
    querySkuDetailsAsync(params) { billingResult: BillingResult, skuDetails: List<SkuDetails>? ->
        if (billingResult.isOk()) {
            it.resume(skuDetails)
        } else {
            it.resumeWithException(billingResult.toBillingException())
        }
    }
}

@Throws(BillingResultException::class)
suspend fun BillingClient.acknowledgePurchase(params: AcknowledgePurchaseParams) =
    suspendOnMainThread<Unit> { continuation ->
        acknowledgePurchase(params) {
            if (it.isOk()) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(it.toBillingException())
            }
        }
    }

@Throws(BillingResultException::class)
suspend fun BillingClient.consume(params: ConsumeParams) = suspendOnMainThread<String> {
    consumeAsync(params) { billingResult, purchaseToken ->
        if (billingResult.isOk()) {
            it.resume(purchaseToken)
        } else {
            it.resumeWithException(billingResult.toBillingException())
        }
    }
}

@Throws(BillingResultException::class)
suspend fun BillingClient.launchPriceChange(activity: Activity, params: PriceChangeFlowParams) =
    suspendOnMainThread<Unit> { continuation ->
        launchPriceChangeConfirmationFlow(activity, params) {
            if (it.isOk()) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(it.toBillingException())
            }
        }
    }

@Throws(BillingResultException::class)
suspend fun BillingClient.loadRewardedSku(params: RewardLoadParams) =
    suspendOnMainThread<Unit> { continuation ->
        loadRewardedSku(params) {
            if (it.isOk()) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(it.toBillingException())
            }
        }
    }

@Throws(BillingResultException::class)
suspend fun BillingClient.getPurchaseHistory(skuType: String) =
    suspendCancellableCoroutine<List<PurchaseHistoryRecord>?> {
        queryPurchaseHistoryAsync(skuType) { billingResult, purchaseHistoryRecordList ->
            if (billingResult.isOk()) {
                it.resume(purchaseHistoryRecordList)
            } else {
                it.resumeWithException(billingResult.toBillingException())
            }
        }
    }

private suspend inline fun <T> suspendOnMainThread(crossinline block: (CancellableContinuation<T>) -> Unit): T =
    withContext(Dispatchers.Main) {
        suspendCancellableCoroutine(block)
    }
