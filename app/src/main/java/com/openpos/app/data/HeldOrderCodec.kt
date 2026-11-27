package com.openpos.app.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Serializes a held cart's line items to/from a JSON blob stored in
 * [com.openpos.app.data.entity.HeldOrder.linesJson]. A held order is a
 * short-lived snapshot, not data anything else needs to query, so it
 * doesn't get its own line-item table.
 */
data class HeldLineDto(
    val productId: Long?,
    val name: String,
    val quantity: Double,
    val unitPrice: Double,
    val unitCost: Double,
    val taxPercent: Double,
    val lineDiscount: Double
)

object HeldOrderCodec {
    private val gson = Gson()
    private val listType = object : TypeToken<List<HeldLineDto>>() {}.type

    fun encode(lines: List<HeldLineDto>): String = gson.toJson(lines, listType)

    fun decode(json: String): List<HeldLineDto> =
        try {
            gson.fromJson<List<HeldLineDto>>(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
}
