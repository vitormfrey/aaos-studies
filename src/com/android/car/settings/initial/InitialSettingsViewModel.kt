package com.android.car.settings.initial

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Fragment-scoped. Stores snapshots only, never Preferences, Drawables or Contexts. */
class InitialSettingsViewModel : ViewModel() {
    private val getCards = GetSettingsCardsUseCase()
    private val mutableCards = MutableStateFlow<List<SettingsCard>>(emptyList())
    val cards: StateFlow<List<SettingsCard>> = mutableCards.asStateFlow()

    fun update(options: List<SettingsOption>) {
        mutableCards.value = getCards(options)
    }
}
