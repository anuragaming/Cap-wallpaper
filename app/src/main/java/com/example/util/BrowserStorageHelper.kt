package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import com.example.model.BrowserTab
import org.json.JSONArray
import org.json.JSONObject

/**
 * Helper to persist open tabs, active tab ID, and manage CookieManager synchronization.
 * Like Google Chrome, non-incognito tabs are preserved across app restarts.
 */
object BrowserStorageHelper {

    private const val PREFS_NAME = "browser_tabs_storage"
    private const val KEY_SAVED_TABS = "saved_browser_tabs"
    private const val KEY_ACTIVE_TAB_ID = "active_tab_id"

    fun saveTabs(context: Context, tabs: List<BrowserTab>, activeTabId: String) {
        // Only persist regular (non-incognito) tabs, just like Chrome!
        val nonIncognitoTabs = tabs.filterNot { it.isIncognito }
        if (nonIncognitoTabs.isEmpty()) return

        val jsonArray = JSONArray()
        for (tab in nonIncognitoTabs) {
            val obj = JSONObject().apply {
                put("id", tab.id)
                put("url", tab.url)
                put("title", tab.title)
                put("isIncognito", false)
                put("lastAccessed", tab.lastAccessed)
            }
            jsonArray.put(obj)
        }

        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SAVED_TABS, jsonArray.toString())
            .putString(KEY_ACTIVE_TAB_ID, activeTabId)
            .apply()
    }

    fun loadSavedTabs(context: Context): Pair<List<BrowserTab>, String?> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString(KEY_SAVED_TABS, null) ?: return Pair(emptyList(), null)
        val activeId = prefs.getString(KEY_ACTIVE_TAB_ID, null)

        val result = mutableListOf<BrowserTab>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    BrowserTab(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        url = obj.optString("url", "https://anhdev.io.vn"),
                        title = obj.optString("title", "Nhật Anh DEV"),
                        isIncognito = false,
                        lastAccessed = obj.optLong("lastAccessed", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(result, activeId)
    }

    fun syncCookies() {
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
