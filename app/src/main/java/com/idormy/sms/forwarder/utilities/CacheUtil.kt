package com.idormy.sms.forwarder.utilities

import android.content.Context
import android.os.Environment
import java.io.File
import java.math.BigDecimal

object CacheUtil {
    /**
     * 获取缓存大小
     *
     * @param context 上下文
     * @return 缓存大小
     */
    fun getTotalCacheSize(context: Context): String {
        var cacheSize = getFolderSize(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            cacheSize += getFolderSize(context.externalCacheDir)
        }
        return getFormatSize(cacheSize.toDouble())
    }

    /***
     * 清理所有缓存
     * @param context 上下文
     */
    fun clearAllCache(context: Context) {
        deleteDir(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            deleteDir(context.externalCacheDir)
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()!!
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) {
                    return false
                }
            }
        }
        assert(dir != null)
        return dir!!.delete()
    }

    // 获取文件
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    fun getFolderSize(file: File?): Long {
        var size: Long = 0
        try {
            val fileList = file!!.listFiles()!!
            for (value in fileList) {
                // 如果下面还有文件
                size = if (value.isDirectory) {
                    size + getFolderSize(value)
                } else {
                    size + value.length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 格式化单位
     *
     * @param size 文件大小
     * @return 结果
     */
    fun getFormatSize(size: Double): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return "0KB"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(java.lang.Double.toString(kiloByte))
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(java.lang.Double.toString(megaByte))
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(java.lang.Double.toString(gigaByte))
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
    }
}