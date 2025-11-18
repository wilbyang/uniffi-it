import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
// 模拟获取的数据
data class UserProfile(val userId: String, val name: String)
data class Transaction(val id: Int, val amount: Double)

// UI 状态的密封类
sealed class SyncState {
    data object Loading : SyncState()
    data class Success(val profile: UserProfile, val transactions: List<Transaction>) : SyncState()
    data class Error(val message: String) : SyncState()
}

// 模拟 Repository - 耗时操作必须是 suspend 函数
class DataRepository {
    // 模拟快速网络请求
    suspend fun fetchProfile(): UserProfile {
        kotlinx.coroutines.delay(500L) // 0.5秒
        return UserProfile("A001", "Alex Smith")
    }
    
    // 模拟慢速数据库/大量数据请求
    suspend fun fetchHistory(): List<Transaction> {
        // 模拟可能发生的错误
        if (System.currentTimeMillis() % 2 == 0L) {
             kotlinx.coroutines.delay(2000L) // 2.0秒
             return List(5) { Transaction(it, 100.0 * (it + 1)) }
        } else {
             // 模拟失败
             kotlinx.coroutines.delay(100L) 
             throw IllegalStateException("Transaction API Failed")
        }
    }
}


// 这是一个服务类，通常在 Android 中是 ViewModel
class DataSyncService(
    // 外部传入 CoroutineScope (例如 ViewModelScope)，用于生命周期管理
    private val scope: CoroutineScope, 
    private val repository: DataRepository
) {
    // StateFlow: 暴露 UI 状态，确保 UI 拿到最新且唯一的 State
    private val _uiState = MutableStateFlow<SyncState>(SyncState.Loading)
    val uiState: StateFlow<SyncState> = _uiState.asStateFlow()

    // 核心函数：用于启动整个并发流程
    fun loadAllData() {
        // 启动协程：在 IO 线程启动，处理网络和计算任务
        scope.launch(Dispatchers.IO) { 
            // 立即设置为加载中
            _uiState.value = SyncState.Loading 
            
            try {
                // 1. Fan-out (扇出): 使用 async 启动两个并发任务
                val profileDeferred = async { repository.fetchProfile() }
                val historyDeferred = async { repository.fetchHistory() }
                
                // 2. Fan-in (扇入): 使用 await 等待所有结果
                // 如果任何一个 await 抛出异常，整个 try 块都会被中断
                val profile = profileDeferred.await()
                val history = historyDeferred.await()
                
                // 3. 上下文切换: 使用 withContext 确保在 Main 线程更新 UI
                withContext(Dispatchers.Main) { 
                    _uiState.value = SyncState.Success(profile, history)
                }

            } catch (e: Exception) {
                // 4. 异常处理: 捕获任何一个任务的失败，并安全切换回 Main 线程通知 UI
                withContext(Dispatchers.Main) {
                    _uiState.value = SyncState.Error("加载失败: ${e.message}")
                }
            }
        }
    }
}

// Demo main function to run the coroutine example
fun main() = runBlocking {
    println("=== Kotlin Coroutines DataSync Demo ===")
    println("Demonstrating concurrent data loading with StateFlow\n")
    
    // Create repository and service
    val repository = DataRepository()
    val scope = CoroutineScope(Dispatchers.Default + Job())
    val service = DataSyncService(scope, repository)
    
    // Collect state changes
    val job = launch {
        service.uiState.collect { state ->
            when (state) {
                is SyncState.Loading -> {
                    println("[State] Loading...")
                }
                is SyncState.Success -> {
                    println("[State] Success!")
                    println("  Profile: ${state.profile}")
                    println("  Transactions: ${state.transactions}")
                }
                is SyncState.Error -> {
                    println("[State] Error: ${state.message}")
                }
            }
        }
    }
    
    // Start loading data
    service.loadAllData()
    
    // Wait for completion
    delay(3000L)
    job.cancel()
    scope.cancel()
    
    println("\n=== Demo completed ===")
}