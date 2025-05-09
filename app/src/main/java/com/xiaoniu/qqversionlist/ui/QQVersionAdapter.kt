// SPDX-License-Identifier: AGPL-3.0-or-later

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

package com.xiaoniu.qqversionlist.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.databinding.ItemQqVersionBinding
import com.xiaoniu.qqversionlist.databinding.ItemQqVersionDetailBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.Extensions.dpToPx
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat

class QQVersionAdapter :
    ListAdapter<QQVersionBean, RecyclerView.ViewHolder>(QQVersionDiffCallback()) {
    private var getProgressSize = DataStoreUtil.getBooleanKV("progressSize", false)
    private var getProgressSizeText = DataStoreUtil.getBooleanKV("progressSizeText", false)
    private var getVersionTCloud = DataStoreUtil.getBooleanKV("versionTCloud", true)
    private var getVersionTCloudThickness =
        DataStoreUtil.getStringKV("versionTCloudThickness", "System")
    private var getShowUnrealEngineTag = DataStoreUtil.getBooleanKV("unrealEngineTag", false)
    private var getShowKuiklyTag = DataStoreUtil.getBooleanKV("kuiklyTag", true)

    class ViewHolder(val binding: ItemQqVersionBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root)

    class ViewHolderDetail(val binding: ItemQqVersionDetailBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return currentList[position].displayType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> { // 卡片收起态
                ViewHolder(
                    ItemQqVersionBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), parent.context
                ).apply {
                    binding.ibExpand.setOnClickListener {
                        currentList[bindingAdapterPosition].displayType = 1
                        notifyItemChanged(bindingAdapterPosition)
                    }
                    binding.cardAll.setOnLongClickListener {
                        longPressCard(bindingAdapterPosition, it)
                        true
                    }
                }
            }

            else -> {
                ViewHolderDetail(
                    ItemQqVersionDetailBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), parent.context
                ).apply {
                    binding.ibCollapse.setOnClickListener {
                        currentList[bindingAdapterPosition].displayType = 0
                        notifyItemChanged(bindingAdapterPosition)
                    }
                    binding.cardAllDetail.setOnLongClickListener {
                        longPressCard(bindingAdapterPosition, it)
                        true
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bean = currentList[position]
        when (holder) {
            is ViewHolder -> {
                holder.binding.apply {
                    tvVersion.text = bean.versionNumber
                    tvSize.text = bean.size + " MB"
                    bindProgress(
                        listProgressLine, null, tvPerSizeText, tvPerSizeCard, tvSizeCard, bean
                    )
                    bindDisplayInstall(tvInstall, tvInstallCard, bean)
                    bindVersionTCloud(tvVersion, holder.context)
                    bindAccessibilityTag(accessibilityTag, holder.context, bean)
                    bindQQNTTag(qqntTag, bean)
                    bindUnrealEngineTag(ueTag, bean)
                    bindKuiklyTag(kuiklyTag, bean)
                }
            }

            is ViewHolderDetail -> {
                holder.binding.apply {
                    linearImages.removeAllViews()
                    bean.imgs.forEachIndexed { index, s ->
                        val iv = ImageView(holder.itemView.context).apply {
                            setPadding(0, 0, if (index == bean.imgs.size - 1) 0 else 4.dpToPx, 0)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, 150.dpToPx
                            )
                        }
                        linearImages.addView(iv)
                        iv.load(s) {
                            crossfade(true)
                            transformations(RoundedCornersTransformation(2.dpToPx.toFloat()))
                        }
                    }
                    tvOldVersion.text = bean.versionNumber
                    tvOldSize.text = bean.size + " MB"
                    tvDetailVersion.text =
                        holder.itemView.context.getString(R.string.version) + bean.versionNumber
                    tvDetailSize.text =
                        holder.itemView.context.getString(R.string.reatedFileSize) + bean.size + " MB"
                    tvTitle.text = bean.featureTitle
                    tvDesc.text = bean.summary.joinToString(separator = "\n- ", prefix = "- ")

                    tvTitle.isVisible = tvTitle.text != ""

                    bindDisplayInstall(tvOldInstall, tvOldInstallCard, bean)
                    bindVersionTCloud(tvOldVersion, holder.context)
                    bindAccessibilityTag(accessibilityOldTag, holder.context, bean)
                    bindQQNTTag(qqntOldTag, bean)
                    bindUnrealEngineTag(ueOldTag, bean)
                    bindKuiklyTag(kuiklyOldTag, bean)

                    bindProgress(
                        listDetailProgressLine,
                        tvPerSize,
                        tvOldPerSizeText,
                        tvOldPerSizeCard,
                        tvOldSizeCard,
                        bean
                    )
                }
            }
        }
    }

    private fun longPressCard(bindingAdapterPosition: Int, it: View) {
        if (DataStoreUtil.getBooleanKV("longPressCard", true)) showDialog(
            it.context, currentList[bindingAdapterPosition].jsonString.toPrettyFormat()
        ) else showToast(R.string.longPressToViewSourceDetailsIsDisabled)
    }

    @SuppressLint("SetTextI18n")
    private fun bindProgress(
        listProgressLine: LinearProgressIndicator,
        tvPerSize: TextView?,
        tvPerSizeText: TextView,
        tvPerSizeCard: MaterialCardView,
        tvSizeCard: MaterialCardView,
        bean: QQVersionBean,
    ) {
        tvPerSize?.isVisible = (getProgressSize || getProgressSizeText)
        listProgressLine.isVisible = getProgressSize
        tvPerSizeCard.isVisible = getProgressSizeText

        val layoutParams = tvSizeCard.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        layoutParams.marginEnd = if (getProgressSizeText) 6.dpToPx else 0
        tvSizeCard.layoutParams = layoutParams

        if (getProgressSize || getProgressSizeText) {
            val listMaxSize = currentList.maxByOrNull { it.size.toFloat() }?.size?.toFloat() ?: 0f

            tvPerSize?.text = "${
                tvPerSizeCard.context.getString(
                    R.string.currentSizeVsLargestHistoricalPackage, "$listMaxSize MB"
                )
            }${
                ("%.2f".format(bean.size.toFloat() / listMaxSize * 100)).replace(
                    "100.00", "100"
                )
            }%"
            tvPerSizeText.text = "${
                ("%.2f".format(bean.size.toFloat() / listMaxSize * 100)).replace(
                    "100.00", "100"
                )
            }%"

            // 进度条这里不能直接用 listMaxSize
            // 因为鬼知道 Material 上游的什么 Bug，导致这里要故意拖点时间让 Material 进度指示条视图加载完毕才能正确显示进度更新，否则会显示异常
            listProgressLine.apply {
                max = ((currentList.maxByOrNull { it.size.toFloat() }?.size?.toFloat()
                    ?: 0f) * 100).toInt()
                progress = (bean.size.toFloat() * 100).toInt()
            }
        }

    }

    private fun bindDisplayInstall(
        tvInstall: TextView, tvInstallCard: MaterialCardView, bean: QQVersionBean
    ) {
        if (bean.displayInstall) {
            tvInstallCard.isVisible = true
            tvInstall.text = tvInstall.context.getString(R.string.installed)
            val marginLayoutParams = tvInstallCard.layoutParams as ViewGroup.MarginLayoutParams
            marginLayoutParams.marginStart =
                if (bean.isAccessibility || bean.isQQNTFramework || (getShowKuiklyTag && bean.isKuiklyInside)) 3.dpToPx else 6.dpToPx
            tvInstallCard.layoutParams = marginLayoutParams
        } else tvInstallCard.isVisible = false
    }

    private fun bindAccessibilityTag(
        accessibilityTag: ImageView, context: Context, bean: QQVersionBean
    ) {
        if (bean.isAccessibility) {
            accessibilityTag.contentDescription = String(
                Base64.decode(
                    context.getString(R.string.accessibilityTag), Base64.NO_WRAP
                ), Charsets.UTF_8
            )
            accessibilityTag.isVisible = true
        } else accessibilityTag.isVisible = false
    }

    private fun bindQQNTTag(qqntTag: ImageView, bean: QQVersionBean) {
        qqntTag.isVisible = bean.isQQNTFramework
    }

    private fun bindUnrealEngineTag(ueTag: ImageView, bean: QQVersionBean) {
        ueTag.isVisible = (getShowUnrealEngineTag && bean.isUnrealEngine)
    }

    private fun bindKuiklyTag(kuikly: ImageView, bean: QQVersionBean) {
        kuikly.isVisible = (getShowKuiklyTag && bean.isKuiklyInside)
    }

    private fun bindVersionTCloud(tvVersion: TextView, context: Context) {
        if (getVersionTCloud) {
            val TCloudFont: Typeface = when (getVersionTCloudThickness) {
                "Light" -> ResourcesCompat.getFont(context, R.font.tcloud_number_light)!!
                "Regular" -> ResourcesCompat.getFont(context, R.font.tcloud_number_regular)!!
                "Bold" -> ResourcesCompat.getFont(context, R.font.tcloud_number_bold)!!
                else -> ResourcesCompat.getFont(context, R.font.tcloud_number_vf)!!
            }
            tvVersion.typeface = TCloudFont
        } else tvVersion.setTypeface(null, Typeface.NORMAL)
    }

    private fun showDialog(context: Context, s: String) {
        val tv = TextView(context).apply {
            text = s
            setTextIsSelectable(true)
            setPadding(96, 48, 96, 96)
        }
        MaterialAlertDialogBuilder(context).setView(tv).setTitle(R.string.jsonDetails)
            .setIcon(R.drawable.braces_line).show()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else {
            val bean = currentList[position]
            when (payloads[0]) {
                "displayType" -> onBindViewHolder(holder, position)

                "displayInstall" -> if (holder is ViewHolder) bindDisplayInstall(
                    holder.binding.tvInstall, holder.binding.tvInstallCard, bean
                ) else if (holder is ViewHolderDetail) bindDisplayInstall(
                    holder.binding.tvOldInstall, holder.binding.tvOldInstallCard, bean
                )

                "isShowProgressSize", "isShowProgressSizeText" -> if (holder is ViewHolder) bindProgress(
                    holder.binding.listProgressLine,
                    null,
                    holder.binding.tvPerSizeText,
                    holder.binding.tvPerSizeCard,
                    holder.binding.tvSizeCard,
                    bean
                ) else if (holder is ViewHolderDetail) bindProgress(
                    holder.binding.listDetailProgressLine,
                    holder.binding.tvPerSize,
                    holder.binding.tvOldPerSizeText,
                    holder.binding.tvOldPerSizeCard,
                    holder.binding.tvOldSizeCard,
                    bean
                )

                "isTCloud" -> if (holder is ViewHolder) bindVersionTCloud(
                    holder.binding.tvVersion, holder.context
                ) else if (holder is ViewHolderDetail) bindVersionTCloud(
                    holder.binding.tvOldVersion, holder.context
                )

                "isShowUnrealEngineTag" -> if (holder is ViewHolder) {
                    bindUnrealEngineTag(holder.binding.ueTag, bean)
                    bindDisplayInstall(holder.binding.tvInstall, holder.binding.tvInstallCard, bean)
                } else if (holder is ViewHolderDetail) {
                    bindUnrealEngineTag(holder.binding.ueOldTag, bean)
                    bindDisplayInstall(
                        holder.binding.tvOldInstall, holder.binding.tvOldInstallCard, bean
                    )
                }

                "isShowKuiklyTag" -> if (holder is ViewHolder) {
                    bindKuiklyTag(holder.binding.kuiklyTag, bean)
                    bindDisplayInstall(holder.binding.tvInstall, holder.binding.tvInstallCard, bean)
                } else if (holder is ViewHolderDetail) {
                    bindKuiklyTag(holder.binding.kuiklyOldTag, bean)
                    bindDisplayInstall(
                        holder.binding.tvOldInstall, holder.binding.tvOldInstallCard, bean
                    )
                }
            }
        }
    }

    fun updateItemProperty(payloads: Any?) {
        when (payloads) {
            "isShowProgressSize" -> {
                getProgressSize = DataStoreUtil.getBooleanKV("progressSize", false)
                notifyItemRangeChanged(0, currentList.size, "isShowProgressSize")
            }

            "isShowProgressSizeText" -> {
                getProgressSizeText = DataStoreUtil.getBooleanKV("progressSizeText", false)
                notifyItemRangeChanged(0, currentList.size, "isShowProgressSizeText")
            }

            "isTCloud" -> {
                getVersionTCloud = DataStoreUtil.getBooleanKV("versionTCloud", true)
                getVersionTCloudThickness =
                    DataStoreUtil.getStringKV("versionTCloudThickness", "System")
                notifyItemRangeChanged(0, currentList.size, "isTCloud")
            }

            "isShowUnrealEngineTag" -> {
                getShowUnrealEngineTag = DataStoreUtil.getBooleanKV("unrealEngineTag", false)
                notifyItemRangeChanged(0, currentList.size, "isShowUnrealEngineTag")
            }

            "isShowKuiklyTag" -> {
                getShowKuiklyTag = DataStoreUtil.getBooleanKV("kuiklyTag", true)
                notifyItemRangeChanged(0, currentList.size, "isShowKuiklyTag")
            }
        }
    }

    class QQVersionDiffCallback : DiffUtil.ItemCallback<QQVersionBean>() {
        override fun areItemsTheSame(
            oldItem: QQVersionBean, newItem: QQVersionBean
        ): Boolean {
            return oldItem.jsonString == newItem.jsonString
        }

        override fun areContentsTheSame(
            oldItem: QQVersionBean, newItem: QQVersionBean
        ): Boolean {
            return oldItem.displayType == newItem.displayType && oldItem.displayInstall == newItem.displayInstall
        }

        override fun getChangePayload(
            oldItem: QQVersionBean, newItem: QQVersionBean
        ): Any? {
            return when {
                oldItem.displayType != newItem.displayType -> "displayType"
                oldItem.displayInstall != newItem.displayInstall -> "displayInstall"
                else -> null
            }
        }
    }
}

