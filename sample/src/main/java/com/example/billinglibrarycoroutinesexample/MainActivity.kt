package com.example.billinglibrarycoroutinesexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.billingclient.api.*
import com.example.billinglibrarycoroutines.BillingResultException
import com.example.billinglibrarycoroutines.extensions.consume
import com.example.billinglibrarycoroutines.extensions.getSkuDetails
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        billingClient = BillingClient.newBuilder(this).setListener(this).build()

        // start connection api coming soon.
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // you shouldn't actually use GlobalScope. This is just for example.
                GlobalScope.launch {
                    val purchaseResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                    purchaseResult.purchasesList?.forEach {
                        try {
                            billingClient.consume(ConsumeParams.newBuilder().setPurchaseToken(it.purchaseToken).build())
                        } catch (e: BillingResultException) {
                            val responseCode = e.billingResult.responseCode
                            // handle response code
                        }
                    }
                }

            }

            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
