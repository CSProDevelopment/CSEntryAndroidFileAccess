package gov.census.cspro.csentry.fileaccess

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import java.io.File
import java.util.*

abstract class FileCommand(private val context: AppCompatActivity) {
    protected abstract fun getCommandName(): String
    protected open fun preProc(args: Array<String>): Pair<String?, Array<String>> {
        return null to args
    }

    protected open fun postProc(args: Array<String>?) { }

    private fun getIntent(args: Array<String>, sharedUri: String? = null): Intent? {
        val packageName = FileAccessHelper.getPackageName()
        val activityName = FileAccessHelper.getActivityName()

        val intent = Intent()
        intent.setPackage(packageName)
        val pm = context.packageManager
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(pm))
        if (resolveInfos.size > 0) {
            val launchable = resolveInfos[0]
            val activity = launchable.activityInfo
            val name = ComponentName(
                activity.applicationInfo.packageName,
                activityName
            )
            val i = Intent(Intent.ACTION_DEFAULT)
            i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
            i.component = name

            if (!sharedUri.isNullOrBlank()) {
                val f = File(sharedUri)
                val uri = FileProvider.getUriForFile(
                    context,
                    "gov.census.cspro.fileaccess.fileprovider",
                    f
                )

                i.data = uri
            }

            i.putExtra("COMMAND", getCommandName())
            if (args.count() > 0) {
                i.putExtra("ARGS", args)
            }

            return i
        }

        return null
    }

    private val getContent = context.registerForActivityResult(StartActivityForResult()) { result ->
        postProc(result.data?.getStringArrayExtra("RES"))

        if (result.data?.hasExtra("ERR") == true)
            result.data?.getStringExtra("ERR")?.let { onErrorHandler(it) }
        else
            result.data?.getStringArrayExtra("RES")?.let { onCompleteHandler(it) }
    }

    lateinit private var onCompleteHandler: (res: Array<String>) -> Unit
    lateinit private var onErrorHandler: (err: String) -> Unit

    fun runCommand(args: Array<String>, onComplete: (res: Array<String>) -> Unit, onError:(err: String) -> Unit) {
        //command preproc
        val (uri, intentArgs) = preProc(args)

        if (uri.equals("stop")) {
            onError("Command stopped before calling server")
            return
        }

        //start activity for result
        val i = getIntent(intentArgs, uri)
            ?: throw ActivityNotFoundException(
                Resources.getSystem().getString(R.string.SERVER_ACTIVITY_NAME)
            )

        onCompleteHandler = onComplete
        onErrorHandler = onError

        getContent.launch(i)
    }
}