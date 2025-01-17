package com.nytimes.android.sample

import com.nytimes.android.external.store3.base.Clearable
import com.nytimes.android.external.store3.base.Fetcher
import com.nytimes.android.external.store3.base.Persister
import com.nytimes.android.external.store3.base.impl.BarCode
import com.nytimes.android.external.store3.base.impl.StoreBuilder
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


val barcode1 = BarCode("type1", "key1")
val barcode2 = BarCode("type2", "key2")

@RunWith(MockitoJUnitRunner::class)
class ClearStoreTest {
    private val persister = ClearingPersister()
    private val networkCalls = AtomicInteger(0)
    private val store= StoreBuilder.barcode<Int>()
            .fetcher(object : Fetcher<Int, BarCode> {
                override suspend fun fetch(key: BarCode): Int {
                    return networkCalls.incrementAndGet()
                }

            })
            .persister(persister)
            .open()

    @Test
    fun testClearSingleBarCode() = runBlocking<Unit> {
        // one request should produce one call
        val barcode = BarCode("type", "key")

        store.get(barcode)
        assertThat(networkCalls.toInt()).isEqualTo(1)

        // after clearing the memory another call should be made
        store.clear(barcode)
        store.get(barcode)
        assertThat(persister.isClear).isTrue()
        assertThat(networkCalls.toInt()).isEqualTo(2)
    }

    @Test
    fun testClearAllBarCodes() = runBlocking<Unit> {
        // each request should produce one call
        val result = store.get(barcode1)
        store.get(barcode2)
        assertThat(networkCalls.toInt()).isEqualTo(2)

        store.clear(barcode1)
        store.clear(barcode2)

        // after everything is cleared each request should produce another 2 calls
        store.get(barcode1)
        store.get(barcode2)
        assertThat(networkCalls.toInt()).isEqualTo(4)
    }
}

//everything will be mocked
class ClearingPersister : Persister<Int, BarCode>, Clearable<BarCode> {
    var isClear = false
    private val barcode1Responses = LinkedList(mutableListOf(null, 1, null, 1))
    private val barcode2Responses = LinkedList(mutableListOf(null, 1, null, 1))

    override fun clear(key: BarCode) {
        isClear = true
    }

    override suspend fun read(key: BarCode): Int? {
        val diskValue = if (key == barcode1) barcode1Responses else barcode2Responses
        return diskValue.remove()
    }

    override suspend fun write(key: BarCode, raw: Int): Boolean {
        return when (raw) {
            in 1..5 -> true
            else -> throw RuntimeException("no good")
        }
    }
}
