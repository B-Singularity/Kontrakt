package adapter.file

import exception.KontraktException
import execution.domain.vo.result.TestResultEvent
import execution.domain.vo.result.TestStatus
import execution.port.outgoing.TestResultPublisher
import infrastructure.json.toJson
import reporting.adapter.config.ReportingDirectives
import java.io.BufferedWriter
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [Adapter] JSON File Reporter (Final Observability Edition)
 *
 * Implements **ADR-017 (Worker-Based Isolation)** via component delegation.
 * This class acts as a thin orchestrator, ensuring thread-safety and lifecycle management.
 *
 * **Architecture Guarantees:**
 * 1. **Isolation:** Worker-based sharding for lock-free performance in normal operation.
 * 2. **Reality Defense:** Dedicated locks protect against unexpected thread interleaving.
 * 3. **Resource Safety:** Unconditional best-effort closure prevents file handle leaks.
 * 4. **Forensic Merge:** Scans disk to recover ALL data from the current run.
 * 5. **Atomic Commit:** Consumers only see the final, complete report.
 */
class JsonReporter(
    config: ReportingDirectives,
) : TestResultPublisher {
    private val reportDir = config.baseReportDir
    private val runId = UUID.randomUUID().toString()

    // Collaborators (Specialists)
    private val mapper = TestResultMapper()
    private val shardManager = ShardManager(reportDir, runId)
    private val assembler = ReportAssembler(reportDir, runId)
    private val committer = AtomicCommitter(reportDir)

    private val isClosed = AtomicBoolean(false)

    init {
        // [Fail-Fast] Ensure base directory exists
        if (!Files.exists(reportDir)) {
            Files.createDirectories(reportDir)
        }
    }

    override fun publish(event: TestResultEvent) {
        if (isClosed.get()) return

        try {
            // 1. Map (CPU Bound) - Safe encoding
            val json = mapper.map(event)

            // 2. Write (I/O Bound) - Delegated to ShardManager
            shardManager.write(event.workerId.value, json)
        } catch (e: Exception) {
            // Last-resort safety net
            System.err.println("[JsonReporter] Failed to publish event '${event.testName}': ${e.message}")
        }
    }

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                // 1. Close all active shards (Flush buffers)
                shardManager.closeAll()

                // 2. Merge ALL shards for this run (Forensic Merge)
                val tempFinalFile = assembler.assemble()

                // 3. Atomic Commit (Rename temp -> final)
                val committed = committer.commit(tempFinalFile, "test-results.json")

                // 4. Cleanup (Delete shards)
                // Policy: Only delete shards if the final report is successfully committed.
                if (committed) {
                    shardManager.cleanup()
                } else {
                    System.err.println("[JsonReporter] Commit failed. Preserving shards in .shards/$runId for forensics.")
                }
            } catch (e: Exception) {
                System.err.println("[JsonReporter] Critical failure during close: ${e.message}")
            }
        }
    }
}

// ========================================================================================
// Internal Specialists (Hidden Complexity)
// ========================================================================================

/**
 * [Specialist] Maps Domain Events to JSON-safe Strings.
 */
private class TestResultMapper {
    fun map(event: TestResultEvent): String =
        runCatching {
            // Happy Path
            eventToMap(event).toJson()
        }.getOrElse { e ->
            // Double-Fallback Safety
            runCatching {
                mapOf(
                    "status" to "REPORTING_ERROR",
                    "error" to
                            mapOf(
                                "message" to "Serialization failed",
                                "testName" to event.testName,
                                "type" to e::class.java.name,
                            ),
                ).toJson()
            }.getOrElse {
                // Triple-Fallback: Absolute Constant
                """{"status":"REPORTING_ERROR_CRITICAL","error":{"message":"Mapper double-fallback failed"}}"""
            }
        }

    private fun eventToMap(event: TestResultEvent): Map<String, Any?> {
        val base =
            mutableMapOf<String, Any?>(
                "runId" to event.runId,
                "testName" to event.testName,
                "workerId" to event.workerId.value,
                "seed" to event.seed,
                "status" to event.status::class.simpleName,
                "durationMs" to event.durationMs,
                "timestamp" to event.timestamp,
            )

        when (val status = event.status) {
            is TestStatus.AssertionFailed -> {
                base["failure"] =
                    mapOf(
                        "message" to status.message,
                        "expected" to status.expected.toString(),
                        "actual" to status.actual.toString(),
                    )
            }

            is TestStatus.ExecutionError -> {
                val cause = status.cause
                val errorMap =
                    mutableMapOf<String, Any?>(
                        "type" to cause::class.qualifiedName,
                        "message" to cause.message,
                    )
                if (cause is KontraktException) {
                    errorMap["details"] = cause.details
                }
                base["error"] = errorMap
            }

            is TestStatus.Aborted -> {
                base["reason"] = status.reason
            }

            else -> {}
        }
        return base
    }
}

/**
 * [Specialist] Manages Shard Files and Writers.
 */
private class ShardManager(
    private val reportDir: Path,
    private val runId: String,
) {
    private sealed class Shard {
        abstract fun writeLine(json: String)

        abstract fun close()

        class FileShard(
            private val writer: BufferedWriter,
        ) : Shard() {
            @Volatile
            private var isBroken: Boolean = false
            private var buffered = 0
            private val FLUSH_EVERY = 200

            // Dedicated Lock Object for unambiguous synchronization
            private val lock = Any()

            override fun writeLine(json: String) {
                if (isBroken) return

                synchronized(lock) {
                    if (isBroken) return
                    try {
                        writer.write(json)
                        writer.newLine()
                        if (++buffered >= FLUSH_EVERY) {
                            writer.flush()
                            buffered = 0
                        }
                    } catch (e: Exception) {
                        isBroken = true
                        System.err.println("[JsonReporter] Shard write failed. Closing shard: ${e.message}")
                        runCatching { writer.close() }
                    }
                }
            }

            override fun close() {
                // Unconditional Best-Effort Close
                synchronized(lock) {
                    runCatching { writer.flush() }
                    runCatching { writer.close() }
                }
            }
        }

        object BrokenShard : Shard() {
            override fun writeLine(json: String) { // No-op
            }

            override fun close() { // No-op
            }
        }
    }

    private val shards = ConcurrentHashMap<Int, Shard>()
    private val shardDir = reportDir.resolve(".shards").resolve(runId)

    fun write(
        workerId: Int,
        json: String,
    ) {
        val shard = shards.computeIfAbsent(workerId) { id -> createShard(id) }
        shard.writeLine(json)
    }

    private fun createShard(workerId: Int): Shard =
        try {
            if (!Files.exists(shardDir)) {
                Files.createDirectories(shardDir)
            }
            val shardPath = shardDir.resolve("worker-$workerId.jsonl")

            val writer =
                Files.newBufferedWriter(
                    shardPath,
                    Charsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE,
                )
            Shard.FileShard(writer)
        } catch (e: Exception) {
            System.err.println("[JsonReporter] Failed to create shard for Worker-$workerId: ${e.message}")
            Shard.BrokenShard
        }

    fun closeAll() = shards.values.forEach { it.close() }

    fun cleanup() {
        if (!Files.exists(shardDir)) return
        runCatching {
            // Use try-with-resources (use) for the Stream to prevent directory lock on Windows
            Files.walk(shardDir).use { stream ->
                stream
                    .sorted(Comparator.reverseOrder())
                    .forEach { Files.deleteIfExists(it) }
            }
        }.onFailure {
            // [Feature] Forensic Log
            System.err.println("[JsonReporter] Cleanup failed. Preserving shards in $shardDir")
        }
    }
}

/**
 * [Specialist] Merges Shards into a single JSON Array.
 */
private class ReportAssembler(
    private val reportDir: Path,
    private val runId: String,
) {
    private val shardDir = reportDir.resolve(".shards").resolve(runId)

    fun assemble(): Path {
        val tempFinalFile = reportDir.resolve("test-results.json.$runId.tmp")

        Files.newBufferedWriter(tempFinalFile, Charsets.UTF_8).use { writer ->
            writer.write("[")
            var isFirstGlobal = true

            if (Files.exists(shardDir)) {
                Files.list(shardDir).use { stream ->
                    val validShards =
                        stream
                            .filter { Files.isRegularFile(it) }
                            .filter {
                                it.fileName.toString().let { name ->
                                    name.startsWith("worker-") && name.endsWith(".jsonl")
                                }
                            }
                            // Numeric Sort: worker-2 before worker-10
                            .sorted(
                                Comparator.comparingInt { path ->
                                    path.fileName
                                        .toString()
                                        .removePrefix("worker-")
                                        .removeSuffix(".jsonl")
                                        .toIntOrNull() ?: Int.MAX_VALUE
                                },
                            ).toList()

                    validShards.forEach { shardPath ->
                        processShard(shardPath, writer) { hasContent ->
                            if (hasContent && !isFirstGlobal) writer.write(",")
                            if (hasContent) isFirstGlobal = false
                        }
                    }
                }
            }
            writer.write("]")
        }
        return tempFinalFile
    }

    private fun processShard(
        shardPath: Path,
        writer: BufferedWriter,
        onBeforeWrite: (Boolean) -> Unit,
    ) {
        try {
            Files.newBufferedReader(shardPath, Charsets.UTF_8).use { reader ->
                reader.forEachLine { line ->
                    val trimmed = line.trim()
                    if (isValidJsonLine(trimmed)) {
                        onBeforeWrite(true)
                        writer.write(trimmed)
                    }
                }
            }
        } catch (e: Exception) {
            onBeforeWrite(true)
            val errorJson =
                mapOf(
                    "status" to "REPORTING_ERROR",
                    "error" to mapOf("message" to "Shard corrupted: ${e.message}"),
                ).toJson()
            writer.write(errorJson)
        }
    }

    // Sweet-spot Heuristic
    private fun isValidJsonLine(line: String): Boolean {
        if (line.length < 2) return false
        if (line.first() != '{' || line.last() != '}') return false
        if (!line.contains(':')) return false
        return true
    }
}

/**
 * [Specialist] Performs Atomic File System Operations.
 */
private class AtomicCommitter(
    private val reportDir: Path,
) {
    fun commit(
        tempFile: Path,
        targetName: String,
    ): Boolean {
        val targetPath = reportDir.resolve(targetName)
        return try {
            try {
                Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
            true
        } catch (e: Exception) {
            // [Feature] Forensic Log
            System.err.println("[JsonReporter] Failed to commit report (temp=$tempFile, target=$targetPath): ${e.message}")
            false
        }
    }
}
