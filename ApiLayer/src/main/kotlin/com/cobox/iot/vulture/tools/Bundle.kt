package com.cobox.iot.vulture.tools

@Suppress("unused")
class Bundle {

    private val dataSet: HashMap<String, Any> = hashMapOf()

    fun putBoolean(key: String, value: Boolean): Bundle = putAny(key, value)

    fun putBooleanArray(key: String, value: BooleanArray): Bundle = putAny(key, value)

    fun putByte(key: String, value: Byte): Bundle = putAny(key, value)

    fun putByteArray(key: String, value: ByteArray): Bundle = putAny(key, value)

    fun putChar(key: String, value: Char): Bundle = putAny(key, value)

    fun putCharArray(key: String, value: CharArray): Bundle = putAny(key, value)

    fun putCharSequence(key: String, value: CharSequence): Bundle = putAny(key, value)

    fun putCharSequenceArray(key: String, value: Array<CharSequence>): Bundle = putAny(key, value)

    fun putCharSequenceArrayList(key: String, value: ArrayList<CharSequence>): Bundle = putAny(key, value)

    fun putDouble(key: String, value: Double): Bundle = putAny(key, value)

    fun putDoubleArray(key: String, value: DoubleArray): Bundle = putAny(key, value)

    fun putFloat(key: String, value: Float): Bundle = putAny(key, value)

    fun putFloatArray(key: String, value: FloatArray): Bundle = putAny(key, value)

    fun putInt(key: String, value: Int): Bundle = putAny(key, value)

    fun putIntArray(key: String, value: IntArray): Bundle = putAny(key, value)

    fun putLong(key: String, value: Long): Bundle = putAny(key, value)

    fun putLongArray(key: String, value: LongArray): Bundle = putAny(key, value)

    fun putShort(key: String, value: Short): Bundle = putAny(key, value)

    fun putShortArray(key: String, value: ShortArray): Bundle = putAny(key, value)

    fun putString(key: String, value: String): Bundle = putAny(key, value)

    fun putStringArray(key: String, value: Array<String>): Bundle = putAny(key, value)

    fun putStringArrayList(key: String, value: ArrayList<String>): Bundle = putAny(key, value)

    fun putAny(key: String, value: Any): Bundle = this.apply {
        dataSet[key] = value
    }

    val size: Int
        get() = dataSet.size

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Boolean
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getBooleanArray(key: String, defaultValue: BooleanArray = emptyArray<Boolean>().toBooleanArray()): BooleanArray =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as BooleanArray
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getByte(key: String, defaultValue: Byte): Byte =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Byte
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getByteArray(key: String, defaultValue: ByteArray): ByteArray =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as ByteArray
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getChar(key: String, defaultValue: Char): Char =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Char
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getCharArray(key: String, defaultValue: CharArray): CharArray =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as CharArray
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getCharSequence(key: String, defaultValue: CharSequence): CharSequence =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as CharSequence
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getCharSequenceArray(key: String, defaultValue: Array<CharSequence>): Array<CharSequence> =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Array<CharSequence>
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getCharSequenceArrayList(key: String, defaultValue: ArrayList<CharSequence>): ArrayList<CharSequence> =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as ArrayList<CharSequence>
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getDouble(key: String, defaultValue: Double): Double =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Double
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getDoubleArray(key: String, defaultValue: DoubleArray): DoubleArray =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as DoubleArray
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getFloat(key: String, defaultValue: Float): Float =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Float
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getFloatArray(key: String, defaultValue: FloatArray): FloatArray =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as FloatArray
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getInt(key: String, defaultValue: Int): Int =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Int
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getIntArray(key: String, defaultValue: IntArray): IntArray =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as IntArray
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getLong(key: String, defaultValue: Long): Long =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Long
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getLongArray(key: String, defaultValue: LongArray): LongArray =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as LongArray
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getShort(key: String, defaultValue: Short): Short =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Short
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getShortArray(key: String, defaultValue: ShortArray): ShortArray =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as ShortArray
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getString(key: String, defaultValue: String): String =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as String
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getStringArray(key: String, defaultValue: Array<String>): Array<String> =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as Array<String>
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getStringArrayList(key: String, defaultValue: ArrayList<String>): ArrayList<String> =
        dataSet[key]?.let { value ->
            value.runCatching {
                value as ArrayList<String>
            }.getOrDefault(defaultValue)
        } ?: defaultValue

    fun getAny(key: String, defaultValue: Any): Any =
        dataSet[key] ?: defaultValue

}