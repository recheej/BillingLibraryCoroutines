package com.example.billinglibrarycoroutines

import android.app.Activity
import com.android.billingclient.api.*
import com.example.billinglibrarycoroutines.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.mockito.Mockito

class BillingClientTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private lateinit var billingClient: BillingClient

    private val okResult = BillingResult.newBuilder().setDebugMessage("test").setResponseCode(0).build()
    private val errorResult =
        BillingResult.newBuilder().setDebugMessage("test").setResponseCode(BillingClient.BillingResponseCode.ERROR)
            .build()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        billingClient = Mockito.mock(BillingClient::class.java)
    }

    @Test
    fun `getSkuDetails() returns valid sku details`() {

        val testSkuDetails = Mockito.mock(SkuDetails::class.java)
        Mockito.`when`(testSkuDetails.sku).thenReturn("testSku")

        Mockito.`when`(billingClient.querySkuDetailsAsync(Mockito.any(), Mockito.any())).thenAnswer {
            val listener = it.arguments[1] as SkuDetailsResponseListener
            listener.onSkuDetailsResponse(okResult, listOf(testSkuDetails))

            null
        }

        val params =
            SkuDetailsParams.newBuilder().setType(BillingClient.SkuType.INAPP).setSkusList(listOf("test")).build()

        runBlocking {
            val skuDetails = billingClient.getSkuDetails(params)
            assertEquals(testSkuDetails.sku, skuDetails?.get(0)?.sku)
        }
    }

    @Test
    fun `getSkuDetails() does not throw for null list`() {
        Mockito.`when`(billingClient.querySkuDetailsAsync(Mockito.any(), Mockito.any())).thenAnswer {
            val listener = it.arguments[1] as SkuDetailsResponseListener
            listener.onSkuDetailsResponse(okResult, null)

            null
        }

        val params =
            SkuDetailsParams.newBuilder().setType(BillingClient.SkuType.INAPP).setSkusList(listOf("test")).build()

        runBlocking {
            val skuDetails = billingClient.getSkuDetails(params)
            Assert.assertNull(skuDetails)
        }
    }

    @Test
    fun `getSkuDetails() throws exception for bad billing result`() {
        Mockito.`when`(billingClient.querySkuDetailsAsync(Mockito.any(), Mockito.any())).thenAnswer {
            val listener = it.arguments[1] as SkuDetailsResponseListener
            listener.onSkuDetailsResponse(errorResult, listOf())

            null
        }

        val params =
            SkuDetailsParams.newBuilder().setType(BillingClient.SkuType.INAPP).setSkusList(listOf("test")).build()

        assertBillingException {
            billingClient.getSkuDetails(params)
        }
    }

    private inline fun assertNoException(crossinline block: suspend CoroutineScope.() -> Unit) {
        runBlocking {
            try {
                block()
            } catch (e: Exception) {
                Assert.fail()
            }
        }
    }

    private inline fun assertBillingException(crossinline block: suspend CoroutineScope.() -> Unit) {
        runBlocking {
            try {
                block()
            } catch (e: BillingResultException) {
                assertEquals(errorResult.responseCode, e.billingResult.responseCode)
                assertEquals(errorResult.debugMessage, e.billingResult.debugMessage)
            } catch (e: Exception) {
                Assert.fail()
            }
        }
    }


    @Test
    fun `consume() returns valid purchaseToken`() {

        val testSkuDetails = Mockito.mock(SkuDetails::class.java)
        Mockito.`when`(testSkuDetails.sku).thenReturn("testSku")

        val testToken = "123"

        Mockito.`when`(billingClient.consumeAsync(Mockito.any(), Mockito.any())).thenAnswer {
            val listener = it.arguments[1] as ConsumeResponseListener
            listener.onConsumeResponse(okResult, testToken)

            null
        }

        val params =
            ConsumeParams.newBuilder().setPurchaseToken(testToken).setDeveloperPayload("test").build()

        runBlocking {
            val token = billingClient.consume(params)
            assertEquals(testToken, token)
        }
    }

    @Test
    fun `consume() throws exception for bad billing result`() {
        val testToken = "123"

        Mockito.`when`(billingClient.consumeAsync(Mockito.any(), Mockito.any())).thenAnswer {
            val listener = it.arguments[1] as ConsumeResponseListener
            listener.onConsumeResponse(errorResult, testToken)

            null
        }

        val params =
            ConsumeParams.newBuilder().setPurchaseToken(testToken).setDeveloperPayload("test").build()

        assertBillingException {
            billingClient.consume(params)
        }
    }

    @Test
    fun `launchPriceChange() finishes without error`() {
        Mockito.`when`(billingClient.launchPriceChangeConfirmationFlow(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenAnswer {
                val listener = it.arguments[2] as PriceChangeConfirmationListener
                listener.onPriceChangeConfirmationResult(okResult)

                null
            }

        val testSkuDetails = Mockito.mock(SkuDetails::class.java)
        Mockito.`when`(testSkuDetails.sku).thenReturn("testSku")

        val params =
            PriceChangeFlowParams.newBuilder().setSkuDetails(testSkuDetails).build()

        val mockActivity = Mockito.mock(Activity::class.java)
        Mockito.doNothing().`when`(mockActivity).startActivity(Mockito.any(), Mockito.any())

        assertNoException {
            billingClient.launchPriceChange(mockActivity, params)
        }
    }

    @Test
    fun `launchPriceChange() throws exception for bad billing result`() {
        Mockito.`when`(billingClient.launchPriceChangeConfirmationFlow(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenAnswer {
                val listener = it.arguments[2] as PriceChangeConfirmationListener
                listener.onPriceChangeConfirmationResult(errorResult)

                null
            }

        val testSkuDetails = Mockito.mock(SkuDetails::class.java)
        Mockito.`when`(testSkuDetails.sku).thenReturn("testSku")

        val params =
            PriceChangeFlowParams.newBuilder().setSkuDetails(testSkuDetails).build()

        val mockActivity = Mockito.mock(Activity::class.java)
        Mockito.doNothing().`when`(mockActivity).startActivity(Mockito.any(), Mockito.any())

        assertBillingException {
            billingClient.launchPriceChange(mockActivity, params)
        }
    }

    @Test
    fun `loadRewardedSku() finishes without error`() {
        Mockito.`when`(billingClient.loadRewardedSku(Mockito.any(), Mockito.any()))
            .thenAnswer {
                val listener = it.arguments[1] as RewardResponseListener
                listener.onRewardResponse(okResult)

                null
            }

        val testSkuDetails = Mockito.mock(SkuDetails::class.java)
        Mockito.`when`(testSkuDetails.sku).thenReturn("testSku")

        val params =
            RewardLoadParams.newBuilder().setSkuDetails(testSkuDetails).build()

        assertNoException {
            billingClient.loadRewardedSku(params)
        }
    }

    @Test
    fun `loadRewardedSku() throws exception for bad billing result`() {
        Mockito.`when`(billingClient.loadRewardedSku(Mockito.any(), Mockito.any()))
            .thenAnswer {
                val listener = it.arguments[1] as RewardResponseListener
                listener.onRewardResponse(errorResult)

                null
            }

        val testSkuDetails = Mockito.mock(SkuDetails::class.java)
        Mockito.`when`(testSkuDetails.sku).thenReturn("testSku")

        val params =
            RewardLoadParams.newBuilder().setSkuDetails(testSkuDetails).build()

        assertBillingException {
            billingClient.loadRewardedSku(params)
        }
    }

    @Test
    fun `getPurchaseHistory() returns valid purchase history`() {
        val testHistoryRecord = Mockito.mock(PurchaseHistoryRecord::class.java)
        Mockito.`when`(testHistoryRecord.sku).thenReturn("testSku")

        Mockito.`when`(billingClient.queryPurchaseHistoryAsync(Mockito.any(), Mockito.any())).thenAnswer {
            val listener = it.arguments[1] as PurchaseHistoryResponseListener
            listener.onPurchaseHistoryResponse(okResult, listOf(testHistoryRecord))

            null
        }

        runBlocking {
            val historyRecords = billingClient.getPurchaseHistory(BillingClient.SkuType.INAPP)
            assertEquals(testHistoryRecord.sku, historyRecords?.get(0)?.sku)
        }
    }

    @Test
    fun `getPurchaseHistory() does not throw for null list`() {
        Mockito.`when`(billingClient.queryPurchaseHistoryAsync(Mockito.any(), Mockito.any())).thenAnswer {
            val listener = it.arguments[1] as PurchaseHistoryResponseListener
            listener.onPurchaseHistoryResponse(okResult, null)

            null
        }

        runBlocking {
            val historyRecords = billingClient.getPurchaseHistory(BillingClient.SkuType.INAPP)
            Assert.assertNull(historyRecords)
        }
    }

    @Test
    fun `getPurchaseHistory() throws exception for bad billing result`() {
        Mockito.`when`(billingClient.queryPurchaseHistoryAsync(Mockito.any(), Mockito.any())).thenAnswer {
            val listener = it.arguments[1] as PurchaseHistoryResponseListener
            listener.onPurchaseHistoryResponse(errorResult, listOf())

            null
        }

        assertBillingException {
            billingClient.getPurchaseHistory(BillingClient.SkuType.INAPP)
        }
    }


    @Test
    fun `acknowledgePurchase() finishes without error`() {
        Mockito.`when`(billingClient.acknowledgePurchase(Mockito.any(), Mockito.any()))
            .thenAnswer {
                val listener = it.arguments[1] as AcknowledgePurchaseResponseListener
                listener.onAcknowledgePurchaseResponse(okResult)

                null
            }

        val params =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken("123").build()

        assertNoException {
            billingClient.acknowledgePurchase(params)
        }
    }

    @Test
    fun `acknowledgePurchase() throws exception for bad billing result`() {
        Mockito.`when`(billingClient.acknowledgePurchase(Mockito.any(), Mockito.any()))
            .thenAnswer {
                val listener = it.arguments[1] as AcknowledgePurchaseResponseListener
                listener.onAcknowledgePurchaseResponse(errorResult)

                null
            }

        val params =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken("123").build()

        assertBillingException {
            billingClient.acknowledgePurchase(params)
        }
    }
}