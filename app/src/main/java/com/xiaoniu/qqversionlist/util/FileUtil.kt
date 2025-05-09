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

package com.xiaoniu.qqversionlist.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import org.apache.commons.compress.archivers.zip.ZipFile as ApacheZipFile

object FileUtil {
    class ZipFileCompat(file: File) : AutoCloseable {
        private var javaZipFile: ZipFile? = null
        private var apacheCommonsZipFile: ApacheZipFile? = null

        init {
            runCatching {
                javaZipFile = ZipFile(file)
            }.onFailure {
                apacheCommonsZipFile = ApacheZipFile.Builder().setFile(file).get()
            }
        }

        fun getEntry(name: String): ZipEntry? {
            return javaZipFile?.getEntry(name) ?: apacheCommonsZipFile?.getEntry(name)
        }

        fun getInputStream(entry: ZipEntry): InputStream {
            return javaZipFile?.getInputStream(entry) ?: run {
                if (entry is ZipArchiveEntry) {
                    apacheCommonsZipFile?.getInputStream(entry)
                        ?: throw Exception("未找到 InputStream。")
                } else {
                    val archiveEntry = apacheCommonsZipFile?.getEntry(entry.name)
                    archiveEntry?.let { apacheCommonsZipFile?.getInputStream(it) }
                        ?: throw Exception("未找到 InputStream。")
                }
            }
        }

        override fun close() {
            javaZipFile?.close()
            apacheCommonsZipFile?.close()
        }
    }

    fun downloadFile(context: Context, url: String, fileName: String? = null) {
        if (DataStoreUtil.getBooleanKV(
                "downloadOnSystemManager", false
            )
        ) {
            val requestDownload =
                DownloadManager.Request(Uri.parse(url))
            requestDownload.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                if (fileName != null) fileName else url.substringAfterLast('/')
            )
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(requestDownload)
        } else {
            // 这里不用 Chrome Custom Tab 的原因是 Chrome 不知道咋回事有概率卡在“等待下载”状态
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            browserIntent.apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    /**
     * 获取文件大小
     *
     * **需在后台线程调用**
     *
     * @param url 文件下载地址
     * @return 单位为 MB
     */
    fun getFileSize(url: String): String? {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(url).head().build()
        val response = okHttpClient.newCall(request).execute()
        return if (!response.isSuccessful) null else "%.2f".format(
            response.header("Content-Length")?.toDoubleOrNull()
                ?.div(1024 * 1024)
        )
    }
}