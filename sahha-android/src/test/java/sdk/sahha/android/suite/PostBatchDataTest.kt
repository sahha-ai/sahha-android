package sdk.sahha.android.suite

import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import sdk.sahha.android.data.manager.PostChunkManagerImpl
import sdk.sahha.android.data.repository.MockAuthRepoImpl
import sdk.sahha.android.data.repository.MockBatchedDataRepoImpl
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.domain.use_case.CalculateBatchLimit
import sdk.sahha.android.domain.use_case.post.PostBatchData
import kotlin.coroutines.resume

internal class PostBatchDataTest {
    val postBatchData = PostBatchData(
        context = mockk(),
        api = AppModule.mockSahhaApi,
        chunkManager = PostChunkManagerImpl(
            Mutex(),
        ),
        authRepo = MockAuthRepoImpl(),
        batchRepo = MockBatchedDataRepoImpl(),
        sahhaErrorLogger = mockk(),
        calculateBatchLimit = CalculateBatchLimit(MockBatchedDataRepoImpl()),
        filterActivityOverlaps = AppModule.filterOverlaps,
        addMetadata = AppModule.addMetadata,
        connectionStateManager = AppModule.mockConnectionStateManager,
        dataLogTransformer = AppModule.mockDataLogTransformer

//        addPostDateMetadata = mockk(),
//        postedDataRepository = mockk()
    )

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Ignore("ConnectionStateManager causing test issue")
    @Test
    fun post() = runTest {
        suspendCancellableCoroutine { cont ->

            scope.launch {

                postBatchData(
                    batchedData = listOf()
                ) { error, successful ->

                    println(error)
                    Assert.assertEquals(true, successful)
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }
    }
}