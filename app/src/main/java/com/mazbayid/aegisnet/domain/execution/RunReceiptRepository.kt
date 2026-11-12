package com.mazbayid.aegisnet.domain.execution

import com.mazbayid.aegisnet.domain.mission.MissionRunReceipt

/** Stores the latest operation-bound RUN receipt for later SUBMIT evaluation. */
interface RunReceiptRepository {
    fun load(operationId: String): MissionRunReceipt?
    fun save(receipt: MissionRunReceipt): Boolean
}
