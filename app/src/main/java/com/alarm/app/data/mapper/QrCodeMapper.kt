package com.alarm.app.data.mapper

import com.alarm.app.data.db.QrCodeEntity
import com.alarm.app.domain.model.QrCode

/**
 * Extension function to map a [QrCodeEntity] to a domain [QrCode] model.
 *
 * @return The mapped [QrCode] instance.
 */
fun QrCodeEntity.toDomain(): QrCode {
    return QrCode(
        id = id,
        name = name,
        value = value,
        isGenerated = isGenerated,
        createdAt = createdAt
    )
}

/**
 * Extension function to map a domain [QrCode] model to a [QrCodeEntity].
 *
 * @return The mapped [QrCodeEntity] instance.
 */
fun QrCode.toEntity(): QrCodeEntity {
    return QrCodeEntity(
        id = id,
        name = name,
        value = value,
        isGenerated = isGenerated,
        createdAt = createdAt
    )
}
