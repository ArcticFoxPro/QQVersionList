/*
    Qverbow Util
    Copyright (C) 2023 klxiaoniu

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.xiaoniu.qqversionlist.ui.components.cell

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.CellBottomSwitchBinding

@SuppressLint("CustomViewStyleable")
class CellBottomSwitch @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding: CellBottomSwitchBinding =
        CellBottomSwitchBinding.inflate(LayoutInflater.from(context), this, true)
    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Cell, defStyleAttr, 0)
        val title = a.getString(R.styleable.Cell_cellTitle)
        val iconResId = a.getResourceId(R.styleable.Cell_cellIcon, 0)
        val description = a.getString(R.styleable.Cell_cellDescription)
        a.recycle()
        if (title == null) throw IllegalArgumentException("Title and type are required attributes for CellSwitch.")

        binding.title.text = title

        if (iconResId != 0) binding.icon.apply {
            setImageResource(iconResId)
            isVisible = true
        } else binding.icon.isVisible = false

        if (description != null) binding.description.apply {
            text = description
            isVisible = true
        } else binding.description.isVisible = false

        setOnClickListener { v ->
            toggleSwitch()
            onClick?.invoke(v)
        }
    }

    var onClick: ((View) -> Unit)? = null

    fun setCellTitle(title: String) {
        binding.title.text = title
    }

    fun setCellDescription(description: String?) {
        if (description == null) binding.description.isVisible = false
        else binding.description.apply {
            isVisible = true
            text = description
        }

    }

    fun setCellIcon(iconResId: Int?) {
        if (iconResId == null)
            binding.icon.isVisible = false
        else binding.icon.apply {
            isVisible = true
            setImageResource(iconResId)
        }
    }

    var switchChecked: Boolean
        get() = binding.switchCompat.isChecked
        set(value) {
            binding.switchCompat.isChecked = value
        }

    var switchEnabled: Boolean
        get() = binding.switchCompat.isEnabled
        set(value) {
            binding.switchCompat.isEnabled = value
        }

    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        onCheckedChangeListener = listener
    }

    private fun toggleSwitch() {
        val isChecked = !binding.switchCompat.isChecked
        val isEnabled = binding.switchCompat.isEnabled
        if (isEnabled) {
            binding.switchCompat.isChecked = isChecked
            onCheckedChangeListener?.invoke(isChecked)
        }
    }
}
