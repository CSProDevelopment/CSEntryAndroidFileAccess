package gov.census.cspro.csentry.fileaccess

import androidx.appcompat.app.AppCompatActivity

class FileAccessHelper(context: AppCompatActivity) {
    private val commands: MutableMap<String, FileCommand> = mutableMapOf()

    init {
        commands.clear()
        commands["MK_DIR"] = FileCommandMkDir(context)
        commands["LIST_DIR"] = FileCommandListDir(context)
        commands["PUSH_FILES"] = FileCommandPushFiles(context)
        commands["PULL_FILES"] = FileCommandPullFiles(context)
        commands["DEL"] = FileCommandDel(context)
    }

    companion object {
        @JvmStatic
        fun getPackageName(): String {
            return "gov.census.cspro.csentry"
        }

        @JvmStatic
        fun getActivityName(): String {
            return "gov.census.cspro.fileshare.FileShareActivity"
        }
    }

    fun makeDirectory(
        directory: String,
        onComplete: ((res: Array<String>) -> Unit)? = null,
        onError: ((err: String) -> Unit)? = null
    ) {
        commands["MK_DIR"]?.runCommand(arrayOf(directory), {
            if (onComplete != null) {
                onComplete(it)
            }
        }, {
            if (onError != null) {
                onError(it)
            }
        })
    }

    fun listDirectory(
        parentDirectory: String = "",
        onComplete: ((res: Array<String>) -> Unit)? = null,
        onError: ((err: String) -> Unit)? = null
    ) {
        commands["LIST_DIR"]?.runCommand(arrayOf(parentDirectory), {
            if (onComplete != null) {
                onComplete(it)
            }
        }, {
            if (onError != null) {
                onError(it)
            }
        })
    }

    fun pushFiles(
        localSourceDirectory: String,
        pattern: String,
        remoteDestinationDirectory: String,
        includeSubdirectories: Boolean,
        overwirteExistingFiles: Boolean,
        onComplete: ((res: Array<String>) -> Unit)? = null,
        onError: ((err: String) -> Unit)? = null
    ) {
        commands["PUSH_FILES"]?.runCommand(
            arrayOf(
                localSourceDirectory,
                pattern,
                remoteDestinationDirectory,
                includeSubdirectories.toString(),
                overwirteExistingFiles.toString()
            ), {
                if (onComplete != null) {
                    onComplete(it)
                }
            }, {
                if (onError != null) {
                    onError(it)
                }
            })
    }

    fun pullFiles(
        remoteSourceDirectory: String,
        pattern: String,
        localDestinationDirectory: String,
        includeSubdirectories: Boolean,
        overwirteExistingFiles: Boolean,
        onComplete: ((res: Array<String>) -> Unit)? = null,
        onError: ((err: String) -> Unit)? = null
    ) {
        commands["PULL_FILES"]?.runCommand(
            arrayOf(
                remoteSourceDirectory,
                pattern,
                localDestinationDirectory,
                includeSubdirectories.toString(),
                overwirteExistingFiles.toString()
            ), {
                if (onComplete != null) {
                    onComplete(it)
                }
            }, {
                if (onError != null) {
                    onError(it)
                }
            })
    }

    fun delete(
        parentDirectory: String,
        pattern: String,
        deleteFiles: Boolean,
        deleteDirectories: Boolean,
        onComplete: ((res: Array<String>) -> Unit)? = null,
        onError: ((err: String) -> Unit)? = null
    ) {
        commands["DEL"]?.runCommand(
            arrayOf(
                parentDirectory,
                pattern,
                deleteFiles.toString(),
                deleteDirectories.toString()
            ), {
                if (onComplete != null) {
                    onComplete(it)
                }
            }, {
                if (onError != null) {
                    onError(it)
                }
            })
    }
}