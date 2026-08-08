package com.taotao.healthtracker.ui.screens

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CsvImportReaderTest {
    @Test
    fun readsInputBelowLimit(): Unit {
        val csvBytes: ByteArray = "Date,SBP\n2026-08-08,120\n".toByteArray()
        val result: String? = readUtf8WithLimit(
            inputStream = ByteArrayInputStream(csvBytes),
            maxBytes = csvBytes.size.toLong() + 1L
        )

        assertEquals(csvBytes.toString(Charsets.UTF_8), result)
    }

    @Test
    fun readsInputAtExactLimit(): Unit {
        val csvBytes: ByteArray = "Date,SBP\n".toByteArray()
        val result: String? = readUtf8WithLimit(
            inputStream = ByteArrayInputStream(csvBytes),
            maxBytes = csvBytes.size.toLong()
        )

        assertEquals(csvBytes.toString(Charsets.UTF_8), result)
    }

    @Test
    fun rejectsInputAboveLimitEvenWhenMetadataCouldClaimItIsSmaller(): Unit {
        val csvBytes: ByteArray = "Date,SBP\n2026-08-08,120\n".toByteArray()
        val result: String? = readUtf8WithLimit(
            inputStream = ByteArrayInputStream(csvBytes),
            maxBytes = csvBytes.size.toLong() - 1L
        )

        assertNull(result)
    }
}
