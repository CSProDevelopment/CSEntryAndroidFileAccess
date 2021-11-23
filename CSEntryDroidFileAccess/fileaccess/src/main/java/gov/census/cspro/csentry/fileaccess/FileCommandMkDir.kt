package gov.census.cspro.csentry.fileaccess

import androidx.appcompat.app.AppCompatActivity

class FileCommandMkDir(private val context: AppCompatActivity) :
    FileCommand(context) {
    override fun getCommandName(): String {
        return "MK_DIR"
    }
}