package com.alarm.app.data.mapper

import com.alarm.app.data.db.AlarmEntity
import com.alarm.app.domain.model.Alarm

/**
 * Extension function to map an [AlarmEntity] to a domain [Alarm] model.
 *
 * @return The mapped [Alarm] instance.
 */
fun AlarmEntity.toDomain(): Alarm {
    return Alarm(
        id = id,
        hour = hour,
        minute = minute,
        label = label,
        isEnabled = isEnabled,
        repeatDays = repeatDays,
        qrCodeId = qrCodeId,
        qrCodeValue = qrCodeValue,
        ringtoneUri = ringtoneUri,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extension function to map a domain [Alarm] model to an [AlarmEntity].
 *
 * @return The mapped [AlarmEntity] instance.
 */
fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        hour = hour,
        minute = minute,
        label = label,
        isEnabled = isEnabled,
        repeatDays = repeatDays,
        qrCodeId = qrCodeId,
        qrCodeValue = qrCodeValue,
        ringtoneUri = ringtoneUri,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
