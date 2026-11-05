package com.android.car.settings.initial

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.car.settings.R
import com.android.car.settings.common.BaseFragment
import kotlinx.coroutines.launch

/** Implement in the existing Activity using its legacy-screen transition. */
interface InitialSettingsNavigator {
    fun openLegacySettings(preferenceKey: String)
}

/**
 * XML-backed presentation. This class does not initialize AAOS PreferenceControllers.
 * See README before using this as a replacement for the production homepage.
 */
class InitialSettingsFragment : BaseFragment() {
    private lateinit var model: InitialSettingsViewModel
    private var recycler: RecyclerView? = null
    private var manager: PreferenceManager? = null
    private var screen: PreferenceScreen? = null
    private val preferences = linkedMapOf<String, Preference>()
    private var revision = 0L

    override fun getLayoutId(): Int = R.layout.fragment_initial_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[InitialSettingsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navigator = requireActivity() as? InitialSettingsNavigator
            ?: error("Activity must implement InitialSettingsNavigator")

        loadPreferences()
        val adapter = SettingsCardsAdapter(
            iconForKey = { key ->
                val icon = preferences[key]?.icon
                icon?.constantState?.newDrawable(resources)?.mutate() ?: icon
            },
            onClick = { key ->
                if (canOpen(key)) navigator.openLegacySettings(key)
            },
        )
        recycler = view.findViewById<RecyclerView>(R.id.initial_settings_cards).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.cards.collect { adapter.submitList(it) }
            }
        }
    }

    private fun createThemedContext(context: Context): Context {
        val value = TypedValue()
        val found = context.theme.resolveAttribute(
            androidx.preference.R.attr.preferenceTheme, value, true)
        check(found && value.resourceId != 0) { "Activity theme must define preferenceTheme" }
        return ContextThemeWrapper(context, value.resourceId)
    }

    @SuppressLint("RestrictedApi")
    private fun loadPreferences() {
        val context = createThemedContext(requireActivity())
        val preferenceManager = PreferenceManager(context)
        manager = preferenceManager
        screen = preferenceManager.inflateFromResource(context, R.xml.homepage_fragment, null)
        refreshCards()
    }

    /** Call on the main thread after any externally integrated controller updates. */
    @MainThread
    fun refreshCards() {
        val root = screen ?: return
        preferences.clear()
        revision++
        val options = mutableListOf<SettingsOption>()
        fun collect(group: PreferenceGroup, visible: Boolean, enabled: Boolean) {
            for (index in 0 until group.preferenceCount) {
                val pref = group.getPreference(index)
                val isVisible = visible && pref.isVisible
                val isEnabled = enabled && pref.isEnabled
                if (pref is PreferenceGroup && pref !is PreferenceScreen) {
                    collect(pref, isVisible, isEnabled)
                } else {
                    val key = pref.key?.takeIf { it.isNotBlank() } ?: continue
                    check(!preferences.containsKey(key)) { "Duplicate preference key: $key" }
                    preferences[key] = pref
                    options += SettingsOption(key, pref.title?.toString().orEmpty(),
                        isVisible, isEnabled, revision)
                }
            }
        }
        collect(root, root.isVisible, root.isEnabled)
        model.update(options)
    }

    private fun canOpen(key: String): Boolean {
        var preference: Preference? = preferences[key] ?: return false
        while (preference != null) {
            if (!preference.isVisible || !preference.isEnabled) return false
            preference = preference.parent
        }
        return true
    }

    override fun onDestroyView() {
        recycler?.adapter = null
        recycler = null
        preferences.clear()
        screen = null
        manager = null
        model.update(emptyList())
        super.onDestroyView()
    }
}
