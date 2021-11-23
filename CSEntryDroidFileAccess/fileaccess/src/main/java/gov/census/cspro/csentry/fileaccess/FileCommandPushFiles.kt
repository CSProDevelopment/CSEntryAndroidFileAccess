package gov.census.cspro.csentry.fileaccess

import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.util.UUID;
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileCommandPushFiles(private val context: AppCompatActivity) :
    FileCommand(context) {
    override fun getCommandName(): String {
        return "PUSH_FILES"
    }

    override fun preProc(args: Array<String>): Pair<String?, Array<String>> {
        val localSourceDirectory = File(args[0])
        val pattern = args[1]
        val remoteDestinationDirectory = File(args[2])
        val includeSubdirectories = args[3].equals("true", true)
        val overwirteExistingFiles = args[4].equals("true", true)

        val regex = Regex(wildcardToRegex(pattern))
        val files = mutableListOf<String>()

        if (includeSubdirectories) {
            localSourceDirectory.walk().filter {
                it.isFile && regex.matches(it.name)
            }.forEach {
                files.add(it.absolutePath)
            }
        } else {
            localSourceDirectory.listFiles().filter {
                it.isFile && regex.matches(it.name)
            }.forEach {
                files.add(it.absolutePath)
            }
        }

        //creating a zip file and saving it to cache folder
        if (files.count() > 0) {
            val id = UUID.randomUUID().toString()
            val tempFile = File(context.getExternalCacheDir(), "${id}.zip")
            FileOutputStream(tempFile).use { fos ->
                val zos = ZipOutputStream(BufferedOutputStream(fos))
                files.forEach {
                    var fi = FileInputStream(it)
                    var origin = BufferedInputStream(fi)

                    //get entry name
                    val entryNameLocal = it.substring(localSourceDirectory.absolutePath.length + 1)
                    val entryName =
                        File(remoteDestinationDirectory, entryNameLocal).absolutePath.trimStart('/')

                    var entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    origin.copyTo(zos, 1024)
                    origin.close()
                }
                zos.close()
            }

            return Pair(
                tempFile.absolutePath,
                arrayOf(tempFile.absolutePath, overwirteExistingFiles.toString())
            )
        }

        return Pair("stop", args)
    }

    override fun postProc(args: Array<String>?) {
        //deleting temp file
        if (args != null) {
            File(args[0]).delete()
        }
    }

    fun wildcardToRegex(wildcard: String): String {
        val s = StringBuffer(wildcard.length)
        s.append('^')
        var i = 0
        val `is` = wildcard.length
        while (i < `is`) {
            val c = wildcard[i]
            when (c) {
                '*' -> s.append(".*")
                '?' -> s.append(".")
                '^' -> s.append("\\")
                '(', ')', '[', ']', '$', '.', '{', '}', '|', '\\' -> {
                    s.append("\\")
                    s.append(c)
                }
                else -> s.append("[${c.lowercase()}${c.uppercase()}]")
            }
            i++
        }
        s.append('$')
        return s.toString()
    }

}