# BillingLibraryCoroutines
This library provides simple to use coroutine versions of the Google Billing Library APIs

[![](https://jitpack.io/v/recheej/BillingLibraryCoroutines.svg)](https://jitpack.io/#recheej/BillingLibraryCoroutines)


## How to get library

### Step 1

Add to root build.gradle:

```
allprojects {
   	repositories {
   		maven { url 'https://jitpack.io' }
   	}
   }
```

### Step 2

```
	dependencies {
	        implementation 'com.github.recheej:BillingLibraryCoroutines:1.0'
	}
```

## Example usage:

```kotlin
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
```
