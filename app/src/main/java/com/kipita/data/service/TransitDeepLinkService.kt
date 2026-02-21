package com.kipita.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// TransitDeepLinkService
//
// Handles ride-booking deep-links to Uber and Lyft, passing destination
// coordinates sourced directly from Yelp search results. This is the
// "Travel tab transit deep-linking" requirement from the SOW.
//
// Architecture:
//   • When the target app (Uber / Lyft) is installed, the native app URI
//     scheme is used (uber:// or lyft://) for the best UX.
//   • When the app is not installed, we fall back to the mobile web URL so
//     the user can still complete the ride request in-browser.
//   • Both URI formats follow official deep-link specs, so dropping in the
//     real Uber Rides SDK / Lyft Intent SDK later is a no-op swap.
//
// Security:
//   • No API keys are embedded here — Uber/Lyft authentication happens inside
//     their apps after handoff.
//   • Any partner API credentials (e.g. Uber client_id) are stored via
//     KeystoreManager and read at build time, not hard-coded.
// ---------------------------------------------------------------------------

@Singleton
class TransitDeepLinkService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // -----------------------------------------------------------------------
    // Uber
    // -----------------------------------------------------------------------

    /**
     * Deep-link to Uber with pickup and destination coordinates.
     *
     * Native URI:  uber://?action=setPickup&pickup[latitude]=...
     * Web fallback: https://m.uber.com/ul/?action=setPickup&dropoff[...]
     *
     * @param originLat  User's current latitude  (from GPS / last known)
     * @param originLng  User's current longitude (from GPS / last known)
     * @param destLat    Yelp business latitude
     * @param destLng    Yelp business longitude
     * @param destName   Yelp business display name (shown in Uber app)
     */
    fun requestUberRide(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        destName: String
    ) {
        val name = Uri.encode(destName)
        val appUri = Uri.parse(
            "uber://?action=setPickup" +
                "&pickup[latitude]=$originLat&pickup[longitude]=$originLng" +
                "&dropoff[latitude]=$destLat&dropoff[longitude]=$destLng" +
                "&dropoff[nickname]=$name"
        )
        val webUri = Uri.parse(
            "https://m.uber.com/ul/?action=setPickup" +
                "&dropoff%5Blatitude%5D=$destLat&dropoff%5Blongitude%5D=$destLng" +
                "&dropoff%5Bnickname%5D=$name"
        )
        openDeepLink(appUri, webUri)
    }

    // -----------------------------------------------------------------------
    // Lyft
    // -----------------------------------------------------------------------

    /**
     * Deep-link to Lyft with pickup and destination coordinates.
     *
     * Native URI:  lyft://ridetype?id=lyft&pickup[latitude]=...
     * Web fallback: https://lyft.com/ride?pickup[lat]=...
     *
     * @param originLat  User's current latitude
     * @param originLng  User's current longitude
     * @param destLat    Yelp business latitude
     * @param destLng    Yelp business longitude
     * @param destName   Yelp business display name (shown in Lyft app)
     */
    fun requestLyftRide(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        destName: String
    ) {
        val appUri = Uri.parse(
            "lyft://ridetype?id=lyft" +
                "&pickup[latitude]=$originLat&pickup[longitude]=$originLng" +
                "&destination[latitude]=$destLat&destination[longitude]=$destLng"
        )
        val webUri = Uri.parse(
            "https://lyft.com/ride" +
                "?pickup%5Blat%5D=$originLat&pickup%5Blng%5D=$originLng" +
                "&destination%5Blat%5D=$destLat&destination%5Blng%5D=$destLng"
        )
        openDeepLink(appUri, webUri)
    }

    // -----------------------------------------------------------------------
    // App-detection helpers (used to customize UI label e.g. "Open Uber" vs "Book Uber")
    // -----------------------------------------------------------------------

    /** Returns true if the Uber app is installed on this device. */
    fun isUberInstalled(): Boolean = isPackageInstalled(UBER_PACKAGE_ID)

    /** Returns true if the Lyft app is installed on this device. */
    fun isLyftInstalled(): Boolean = isPackageInstalled(LYFT_PACKAGE_ID)

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private fun openDeepLink(appUri: Uri, webFallback: Uri) {
        val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (appIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(appIntent)
        } else {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, webFallback).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean = runCatching {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    }.getOrDefault(false)

    companion object {
        const val UBER_PACKAGE_ID = "com.ubercab"
        const val LYFT_PACKAGE_ID = "me.lyft.android"
    }
}
