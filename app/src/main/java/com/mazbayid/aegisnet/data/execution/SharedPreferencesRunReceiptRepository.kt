package com.mazbayid.aegisnet.data.execution

import android.content.Context
import com.mazbayid.aegisnet.domain.execution.RunReceiptRepository
import com.mazbayid.aegisnet.domain.mission.MissionRunReceipt
import org.json.JSONObject

class SharedPreferencesRunReceiptRepository(
    context: Context
) : RunReceiptRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun load(operationId: String): MissionRunReceipt? {
        val key = keyFor(operationId)
        val serialized = preferences.getString(key, null) ?: return null
        return try {
            val json = JSONObject(serialized)
            MissionRunReceipt(
                operationId = json.getString("operationId"),
                sourceHash = json.getString("sourceHash"),
                successful = json.getBoolean("successful")
            ).also { receipt ->
                require(receipt.operationId == operationId)
                require(receipt.sourceHash.matches(SHA256_PATTERN))
            }
        } catch (error: Exception) {
            throw IllegalStateException(
                "Saved RUN receipt is corrupted; the original record was preserved.",
                error
            )
        }
    }

    override fun save(receipt: MissionRunReceipt): Boolean {
        val key = keyFor(receipt.operationId)
        require(receipt.sourceHash.matches(SHA256_PATTERN)) {
            "RUN receipt must contain a valid SHA-256 source hash."
        }
        val serialized = JSONObject()
            .put("operationId", receipt.operationId)
            .put("sourceHash", receipt.sourceHash)
            .put("successful", receipt.successful)
            .toString()
        return preferences.edit().putString(key, serialized).commit()
    }

    private fun keyFor(operationId: String): String {
        require(operationId.matches(Regex("[A-Za-z0-9_-]+"))) {
            "Operation ID must contain only letters, digits, underscores, and hyphens."
        }
        return "$KEY_PREFIX$operationId"
    }

    private companion object {
        const val PREFERENCES_NAME = "aegis_run_receipts"
        const val KEY_PREFIX = "latest_run_v1_"
        val SHA256_PATTERN = Regex("[0-9a-f]{64}")
    }
}
