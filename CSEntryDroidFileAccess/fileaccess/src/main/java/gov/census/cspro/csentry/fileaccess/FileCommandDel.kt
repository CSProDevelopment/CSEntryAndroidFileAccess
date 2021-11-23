package gov.census.cspro.csentry.fileaccess

import androidx.appcompat.app.AppCompatActivity

class FileCommandDel (private val context: AppCompatActivity) :
    FileCommand(context) {
    override fun getCommandName(): String {
        return "DEL"
    }
}