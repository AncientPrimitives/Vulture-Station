package com.cobox.utilites.unit

fun Int.bit() = StorageUnit.Bit.bytesToUnitedVolume(this)
fun Int.byte() = StorageUnit.Byte.bytesToUnitedVolume(this)
fun Int.KB() = StorageUnit.KB.bytesToUnitedVolume(this)
fun Int.MB() = StorageUnit.MB.bytesToUnitedVolume(this)
fun Int.GB() = StorageUnit.GB.bytesToUnitedVolume(this)
fun Int.TB() = StorageUnit.TB.bytesToUnitedVolume(this)
fun Int.PB() = StorageUnit.PB.bytesToUnitedVolume(this)

fun Long.bit() = StorageUnit.Bit.bytesToUnitedVolume(this)
fun Long.byte() = StorageUnit.Byte.bytesToUnitedVolume(this)
fun Long.KB() = StorageUnit.KB.bytesToUnitedVolume(this)
fun Long.MB() = StorageUnit.MB.bytesToUnitedVolume(this)
fun Long.GB() = StorageUnit.GB.bytesToUnitedVolume(this)
fun Long.TB() = StorageUnit.TB.bytesToUnitedVolume(this)
fun Long.PB() = StorageUnit.PB.bytesToUnitedVolume(this)

fun Float.bit() = StorageUnit.Bit.bytesToUnitedVolume(this)
fun Float.byte() = StorageUnit.Byte.bytesToUnitedVolume(this)
fun Float.KB() = StorageUnit.KB.bytesToUnitedVolume(this)
fun Float.MB() = StorageUnit.MB.bytesToUnitedVolume(this)
fun Float.GB() = StorageUnit.GB.bytesToUnitedVolume(this)
fun Float.TB() = StorageUnit.TB.bytesToUnitedVolume(this)
fun Float.PB() = StorageUnit.PB.bytesToUnitedVolume(this)

fun Double.bit() = StorageUnit.Bit.bytesToUnitedVolume(this)
fun Double.byte() = StorageUnit.Byte.bytesToUnitedVolume(this)
fun Double.KB() = StorageUnit.KB.bytesToUnitedVolume(this)
fun Double.MB() = StorageUnit.MB.bytesToUnitedVolume(this)
fun Double.GB() = StorageUnit.GB.bytesToUnitedVolume(this)
fun Double.TB() = StorageUnit.TB.bytesToUnitedVolume(this)
fun Double.PB() = StorageUnit.PB.bytesToUnitedVolume(this)

fun String.toStorageUnit(): StorageUnit =
    when (this.toLowerCase()) {
        "bit" -> StorageUnit.Bit
        "bits" -> StorageUnit.Bit
        "byte" -> StorageUnit.Byte
        "bytes" -> StorageUnit.Byte
        "kb" -> StorageUnit.KB
        "mb" -> StorageUnit.MB
        "gb" -> StorageUnit.GB
        "tb" -> StorageUnit.TB
        "pb" -> StorageUnit.PB
        else -> StorageUnit.Unknown
    }

fun CharSequence.toStorageUnit(): StorageUnit =
    this.toString().toStorageUnit()

enum class StorageUnit {
    Unknown,
    Bit, Byte, KB, MB, GB, TB, PB;

    fun bytesToUnitedVolume(value: Long): Double =
        when (this) {
            Bit -> value * 4.0
            Byte -> value.toDouble()
            KB -> value / 1024.0
            MB -> value / 1024.0 / 1024.0
            GB -> value / 1024.0 / 1024.0 / 1024.0
            TB -> value / 1024.0 / 1024.0 / 1024.0 / 1024.0
            PB -> value / 1024.0 / 1024.0 / 1024.0 / 1024.0 / 1024.0
            else -> value.toDouble()
        }

    fun bytesToUnitedVolume(value: Int): Double = bytesToUnitedVolume(value.toLong())

    fun bytesToUnitedVolume(value: Double): Double = bytesToUnitedVolume(value.toLong())

    fun bytesToUnitedVolume(value: Float): Double = bytesToUnitedVolume(value.toLong())

    fun calcBytes(value: Double): Long =
        when (this) {
            Bit -> (value / 4.0).toLong()
            Byte -> value.toLong()
            KB -> (value * 1024).toLong()
            MB -> (value * 1024 * 1024).toLong()
            GB -> (value * 1024 * 1024 * 1024).toLong()
            TB -> (value * 1024 * 1024 * 1024 * 1024).toLong()
            PB -> (value * 1024 * 1024 * 1024 * 1024 * 1024).toLong()
            else -> value.toLong()
        }

    fun calcBytes(value: Float) = calcBytes(value.toDouble())
    
    fun calcBytes(value: Long) = calcBytes(value.toDouble())
    
    fun calcBytes(value: Int) = calcBytes(value.toDouble())
}