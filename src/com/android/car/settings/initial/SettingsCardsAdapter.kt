package com.android.car.settings.initial

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.car.settings.R

class SettingsCardsAdapter(
    private val iconForKey: (String) -> Drawable?,
    private val onClick: (String) -> Unit,
) : ListAdapter<SettingsCard, SettingsCardsAdapter.Holder>(Diff) {
    init {
        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_initial_settings_card, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: Holder) {
        holder.icon.setImageDrawable(null)
        super.onViewRecycled(holder)
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.initial_card_title)
        val icon: ImageView = view.findViewById(R.id.initial_card_icon)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val card = getItem(position)
                    if (card.enabled) onClick(card.key)
                }
            }
        }

        fun bind(card: SettingsCard) {
            title.text = card.title
            icon.setImageDrawable(iconForKey(card.key))
            itemView.isEnabled = card.enabled
            itemView.isClickable = card.enabled
            itemView.alpha = if (card.enabled) 1f else 0.4f
            itemView.contentDescription = card.title
        }
    }

    private object Diff : DiffUtil.ItemCallback<SettingsCard>() {
        override fun areItemsTheSame(old: SettingsCard, new: SettingsCard) = old.key == new.key
        override fun areContentsTheSame(old: SettingsCard, new: SettingsCard) = old == new
    }
}
