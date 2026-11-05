package com.android.car.settings.initial

data class SettingsOption(
    val key: String,
    val title: String,
    val visible: Boolean,
    val enabled: Boolean,
    val revision: Long,
)

data class SettingsCard(
    val key: String,
    val title: String,
    val enabled: Boolean,
    val revision: Long,
)

/** Pure transformation; retains XML order and contains no Android references. */
class GetSettingsCardsUseCase {
    operator fun invoke(options: List<SettingsOption>): List<SettingsCard> =
        options.filter { it.visible }.map {
            SettingsCard(it.key, it.title, it.enabled, it.revision)
        }
}
