package com.mazbayid.aegisnet.domain.vfs

enum class VfsWorkspace {
    PERSONAL,
    MISSION
}

enum class VfsAccess {
    READ_ONLY,
    READ_WRITE
}

enum class VfsOrigin {
    PLAYER_CREATED,
    MISSION_SOURCE,
    SYSTEM
}

data class VfsFile(
    /** Path relative to the root of its workspace; never an Android filesystem path. */
    val path: String,
    val content: String,
    val access: VfsAccess = VfsAccess.READ_ONLY,
    val origin: VfsOrigin = VfsOrigin.MISSION_SOURCE
)

data class VfsEntryInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val access: VfsAccess?
)

/** Serializable workspace data; paths remain relative to the virtual root. */
data class VfsWorkspaceSnapshot(
    val files: List<VfsFile> = emptyList(),
    val directories: Set<String> = emptySet()
)

sealed interface VfsResult<out T> {
    data class Success<T>(val value: T) : VfsResult<T>
    data class Failure(val code: String, val message: String) : VfsResult<Nothing>
}

/**
 * In-memory, game-controlled virtual file system.
 *
 * Personal and mission workspaces are isolated mounts. The mission mount is
 * bound to one operation ID. No method accesses Android files or the host OS.
 */
class VirtualFileSystem(
    private val operationId: String,
    personalFiles: List<VfsFile> = emptyList(),
    missionFiles: List<VfsFile> = emptyList(),
    personalDirectories: Set<String> = emptySet(),
    missionDirectories: Set<String> = emptySet()
) {
    private val personalFileMap = linkedMapOf<String, VfsFile>()
    private val missionFileMap = linkedMapOf<String, VfsFile>()
    private val personalDirectorySet = linkedSetOf("")
    private val missionDirectorySet = linkedSetOf("")

    var activeWorkspace: VfsWorkspace = VfsWorkspace.PERSONAL
        private set

    private var currentDirectory: String = ""

    init {
        require(operationId.matches(Regex("[A-Za-z0-9_-]+"))) {
            "Operation ID must contain only letters, digits, underscores, and hyphens."
        }
        seedDirectories(personalDirectories, personalDirectorySet, personalFileMap)
        seedDirectories(missionDirectories, missionDirectorySet, missionFileMap)
        seedFiles(personalFiles, personalDirectorySet, personalFileMap)
        seedFiles(missionFiles, missionDirectorySet, missionFileMap)
    }

    fun pwd(): String = absolutePath(currentDirectory)

    /** Exports a deterministic snapshot without changing the active workspace. */
    fun snapshot(workspace: VfsWorkspace): VfsWorkspaceSnapshot {
        val fileMap = when (workspace) {
            VfsWorkspace.PERSONAL -> personalFileMap
            VfsWorkspace.MISSION -> missionFileMap
        }
        val directorySet = when (workspace) {
            VfsWorkspace.PERSONAL -> personalDirectorySet
            VfsWorkspace.MISSION -> missionDirectorySet
        }
        return VfsWorkspaceSnapshot(
            files = fileMap.values.sortedBy { it.path },
            directories = directorySet.filter { it.isNotEmpty() }.toSortedSet()
        )
    }

    companion object {
        fun fromSnapshots(
            operationId: String,
            personal: VfsWorkspaceSnapshot,
            mission: VfsWorkspaceSnapshot
        ): VirtualFileSystem = VirtualFileSystem(
            operationId = operationId,
            personalFiles = personal.files,
            missionFiles = mission.files,
            personalDirectories = personal.directories,
            missionDirectories = mission.directories
        )
    }

    /** Workspace changes are explicit; paths cannot switch mounts implicitly. */
    fun switchWorkspace(workspace: VfsWorkspace): String {
        activeWorkspace = workspace
        currentDirectory = ""
        return pwd()
    }

    fun ls(path: String? = null): VfsResult<List<VfsEntryInfo>> {
        val resolved = resolve(path ?: ".")
        if (resolved is VfsResult.Failure) return resolved
        val directory = (resolved as VfsResult.Success).value
        val directories = directories()
        val files = files()

        if (directory !in directories) {
            return failure("not_a_directory", "The requested directory does not exist.")
        }

        val childDirectories = directories
            .filter { it.isNotEmpty() && parentOf(it) == directory }
            .map {
                VfsEntryInfo(
                    name = nameOf(it),
                    path = absolutePath(it),
                    isDirectory = true,
                    access = null
                )
            }

        val childFiles = files.values
            .filter { parentOf(it.path) == directory }
            .map {
                VfsEntryInfo(
                    name = nameOf(it.path),
                    path = absolutePath(it.path),
                    isDirectory = false,
                    access = it.access
                )
            }

        return VfsResult.Success(
            (childDirectories + childFiles)
                .sortedWith(compareBy<VfsEntryInfo> { !it.isDirectory }.thenBy { it.name.lowercase() })
        )
    }

    fun cd(path: String): VfsResult<String> {
        val resolved = resolve(path)
        if (resolved is VfsResult.Failure) return resolved
        val directory = (resolved as VfsResult.Success).value

        if (directory !in directories()) {
            return failure("not_a_directory", "The requested directory does not exist.")
        }

        currentDirectory = directory
        return VfsResult.Success(pwd())
    }

    fun cat(path: String): VfsResult<String> {
        val resolved = resolve(path)
        if (resolved is VfsResult.Failure) return resolved
        val filePath = (resolved as VfsResult.Success).value

        if (filePath in directories()) {
            return failure("is_a_directory", "The requested path is a directory, not a file.")
        }

        val file = files()[filePath]
            ?: return failure("file_not_found", "The requested file does not exist.")
        return VfsResult.Success(file.content)
    }

    /** Creates a new, player-owned, writable file. Repeated touch is idempotent. */
    fun touch(path: String): VfsResult<VfsFile> {
        val resolved = resolve(path)
        if (resolved is VfsResult.Failure) return resolved
        val filePath = (resolved as VfsResult.Success).value

        if (filePath.isEmpty() || filePath in directories()) {
            return failure("is_a_directory", "A file cannot replace a workspace directory.")
        }

        val fileMap = files()
        fileMap[filePath]?.let { return VfsResult.Success(it) }

        if (parentOf(filePath) !in directories()) {
            return failure("parent_not_found", "Create the parent directory before creating this file.")
        }

        val file = VfsFile(
            path = filePath,
            content = "",
            access = VfsAccess.READ_WRITE,
            origin = VfsOrigin.PLAYER_CREATED
        )
        fileMap[filePath] = file
        return VfsResult.Success(file)
    }

    /** Creates one directory below an existing parent directory. */
    fun mkdir(path: String): VfsResult<String> {
        val resolved = resolve(path)
        if (resolved is VfsResult.Failure) return resolved
        val directory = (resolved as VfsResult.Success).value

        if (directory.isEmpty()) return VfsResult.Success(pwd())
        if (directory in files()) {
            return failure("path_is_file", "A file already exists at this path.")
        }
        if (directory in directories()) {
            return VfsResult.Success(absolutePath(directory))
        }
        if (parentOf(directory) !in directories()) {
            return failure("parent_not_found", "Create the parent directory before creating this directory.")
        }

        directories().add(directory)
        return VfsResult.Success(absolutePath(directory))
    }

    /**
     * Writes only player-writable files. New files are player-owned; mission
     * source records remain immutable even when their content is readable.
     */
    fun writeFile(path: String, content: String): VfsResult<VfsFile> {
        val resolved = resolve(path)
        if (resolved is VfsResult.Failure) return resolved
        val filePath = (resolved as VfsResult.Success).value

        if (filePath.isEmpty() || filePath in directories()) {
            return failure("is_a_directory", "A directory cannot be overwritten as a file.")
        }

        val fileMap = files()
        val existing = fileMap[filePath]
        if (existing != null && existing.access == VfsAccess.READ_ONLY) {
            return failure("read_only", "This file is read-only and cannot be modified.")
        }
        if (existing == null && parentOf(filePath) !in directories()) {
            return failure("parent_not_found", "Create the parent directory before writing this file.")
        }

        val updated = if (existing == null) {
            VfsFile(
                path = filePath,
                content = content,
                access = VfsAccess.READ_WRITE,
                origin = VfsOrigin.PLAYER_CREATED
            )
        } else {
            existing.copy(content = content)
        }
        fileMap[filePath] = updated
        return VfsResult.Success(updated)
    }

    private fun resolve(input: String): VfsResult<String> {
        if (input.isBlank()) {
            return failure("blank_path", "A path is required.")
        }
        if (input.contains('\u0000') || input.contains('\\')) {
            return failure("invalid_path", "The path contains an unsupported character.")
        }

        val root = workspaceRoot()
        val absolute = input.startsWith("/")
        var raw = input
        val segments = mutableListOf<String>()

        if (absolute) {
            when {
                input == root -> raw = ""
                input.startsWith("$root/") -> raw = input.removePrefix(root).removePrefix("/")
                input == "/personal" || input.startsWith("/personal/") -> {
                    if (activeWorkspace != VfsWorkspace.PERSONAL) {
                        return failure("workspace_boundary", "The active workspace cannot access the personal mount.")
                    }
                    raw = input.removePrefix("/personal").removePrefix("/")
                }
                input == "/mission" || input.startsWith("/mission/") -> {
                    if (activeWorkspace != VfsWorkspace.MISSION) {
                        return failure("workspace_boundary", "The active workspace cannot access the mission mount.")
                    }
                    val missionRoot = "/mission/$operationId"
                    if (input == missionRoot) {
                        raw = ""
                    } else if (input.startsWith("$missionRoot/")) {
                        raw = input.removePrefix(missionRoot).removePrefix("/")
                    } else {
                        return failure("workspace_boundary", "The path belongs to another mission workspace.")
                    }
                }
                else -> raw = input.removePrefix("/")
            }
        } else if (currentDirectory.isNotEmpty()) {
            segments += currentDirectory.split("/")
        }

        raw.split("/").forEach { part ->
            when (part) {
                "", "." -> Unit
                ".." -> {
                    if (segments.isEmpty()) {
                        return failure("path_escape", "The path cannot leave the active workspace.")
                    }
                    segments.removeAt(segments.lastIndex)
                }
                else -> segments += part
            }
        }

        return VfsResult.Success(segments.joinToString("/"))
    }

    private fun workspaceRoot(): String = when (activeWorkspace) {
        VfsWorkspace.PERSONAL -> "/personal"
        VfsWorkspace.MISSION -> "/mission/$operationId"
    }

    private fun absolutePath(relativePath: String): String =
        if (relativePath.isEmpty()) workspaceRoot() else "${workspaceRoot()}/$relativePath"

    private fun files(): MutableMap<String, VfsFile> = when (activeWorkspace) {
        VfsWorkspace.PERSONAL -> personalFileMap
        VfsWorkspace.MISSION -> missionFileMap
    }

    private fun directories(): MutableSet<String> = when (activeWorkspace) {
        VfsWorkspace.PERSONAL -> personalDirectorySet
        VfsWorkspace.MISSION -> missionDirectorySet
    }

    private fun seedDirectories(
        paths: Set<String>,
        targetDirectories: MutableSet<String>,
        targetFiles: MutableMap<String, VfsFile>
    ) {
        paths.forEach { raw ->
            val path = normalizeSeedPath(raw)
            require(path !in targetFiles) { "A directory conflicts with a seeded file: $path" }
            addDirectoryAndParents(path, targetDirectories)
        }
    }

    private fun seedFiles(
        seededFiles: List<VfsFile>,
        targetDirectories: MutableSet<String>,
        targetFiles: MutableMap<String, VfsFile>
    ) {
        seededFiles.forEach { file ->
            val path = normalizeSeedPath(file.path)
            require(path !in targetDirectories) { "A file conflicts with a seeded directory: $path" }
            require(targetFiles.putIfAbsent(path, file.copy(path = path)) == null) {
                "Duplicate virtual file path: $path"
            }
            addParentDirectories(path, targetDirectories)
        }
    }

    private fun addDirectoryAndParents(path: String, target: MutableSet<String>) {
        var current = ""
        path.split("/").forEach { segment ->
            current = if (current.isEmpty()) segment else "$current/$segment"
            target += current
        }
    }

    private fun addParentDirectories(path: String, target: MutableSet<String>) {
        val parent = parentOf(path)
        if (parent.isNotEmpty()) addDirectoryAndParents(parent, target)
    }

    private fun normalizeSeedPath(path: String): String {
        require(path.isNotBlank() && !path.startsWith("/") &&
            !path.contains('\u0000') && !path.contains('\\')
        ) { "Seed paths must be non-blank workspace-relative paths." }
        val parts = path.split("/")
        require(parts.none { it.isBlank() || it == "." || it == ".." }) {
            "Seed paths cannot contain empty, current, or parent path segments."
        }
        return parts.joinToString("/")
    }

    private fun parentOf(path: String): String =
        path.substringBeforeLast("/", missingDelimiterValue = "")

    private fun nameOf(path: String): String = path.substringAfterLast("/")

    private fun <T> failure(code: String, message: String) =
        VfsResult.Failure(code, message) as VfsResult<T>
}
