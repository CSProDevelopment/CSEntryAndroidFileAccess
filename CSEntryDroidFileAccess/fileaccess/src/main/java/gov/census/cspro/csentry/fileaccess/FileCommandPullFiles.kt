package gov.census.cspro.csentry.fileaccess

import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.collections.ArrayList

class FileCommandPullFiles(private val context: AppCompatActivity) :
    FileCommand(context) {
    override fun getCommandName(): String {
        return "PULL_FILES"
    }

    override fun preProc(args: Array<String>): Pair<String?, Array<String>> {
        //building a temp file for accepting remote data
        val id = UUID.randomUUID().toString()
        val tempFile = File(context.getExternalCacheDir(),"${id}.zip")

        val newArgs = ArrayList<String>()
        newArgs.add(tempFile.absolutePath)
        newArgs.addAll(args)
        val na = newArgs.toTypedArray()

        return Pair(tempFile.absolutePath, na)
    }

    override fun postProc(args: Array<String>?) {
        if (args!=null) {
            val tempFile = args.get(0)
            val localDestinationDirectory = File(args.get(3))
            val overwirteExistingFiles = args.get(5).equals("true", true)

            //creating directory if it doesn't exist
            localDestinationDirectory.mkdirs()

            //unzipping to local dir
            FileInputStream(tempFile).let {
                if (it != null) {
                    val zis = ZipInputStream(BufferedInputStream(it))

                    var entry: ZipEntry?

                    // Read each entry from the ZipInputStream until no
                    // more entry found indicated by a null return value
                    // of the getNextEntry() method.
                    while (zis.nextEntry.also { entry = it } != null) {
                        val ef = File(localDestinationDirectory, entry?.name)
                        val fileExists = ef.exists()
                        if (!overwirteExistingFiles && fileExists)
                            continue

                        //deleting the old file and/or creating directory
                        ef.parentFile.mkdirs()
                        if (fileExists)
                            ef.delete()

                        var size: Int
                        val buffer = ByteArray(2048)
                        FileOutputStream(ef).use { fos ->
                            BufferedOutputStream(fos, buffer.size).use { bos ->
                                while (zis.read(buffer, 0, buffer.size).also { size = it } != -1) {
                                    bos.write(buffer, 0, size)
                                }
                                bos.close()
                            }
                        }
                    }

                    zis.close()
                }
            }

            //deleting temp file
            File(tempFile).delete()
        }
    }
}