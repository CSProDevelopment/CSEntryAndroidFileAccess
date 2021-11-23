package gov.census.cspro.csentry.fileaccessexample

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import gov.census.cspro.csentry.fileaccessexample.databinding.FragmentFirstBinding
import java.io.File

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private val localDir get() = mainActivity.localDir

    private var _binding: FragmentFirstBinding? = null
    private var curDir: String
        get() = mainActivity.csEntryCurDir
        set(value) {
            mainActivity.csEntryCurDir = value
        }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mainActivity get() = activity as MainActivity
    private var _listViewAdapter: FileListViewAdapter? = null
    private val listViewAdapter get() = _listViewAdapter!!
    val il = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ma = mainActivity
        _listViewAdapter = FileListViewAdapter(ma, il)

        binding.buttonFirst.setOnClickListener {
            showCreateFolderDialog()
        }

        binding.csentryList.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) as String

            if (element.equals("..")) { //dir up
                val el = curDir.substring(0, curDir.length - 1).split("/")
                curDir = ""
                if (el.count() > 1) {
                    el.subList(0, el.count() - 1).forEach {
                        curDir += "${it}/"
                    }
                }
                readCsentryDir()
            } else if (element.endsWith("/") && listViewAdapter.selIdx == position) { //folder
                curDir += element
                readCsentryDir()
            } else {
                listViewAdapter.selIdx = position
                listViewAdapter.notifyDataSetChanged()
                profileUi()
            }
        }

        binding.buttonCopy.setOnClickListener {
            val selItem = il[listViewAdapter.selIdx]
            if (selItem.endsWith("/")) {
                copySelectedDir(selItem.trimEnd('/'))
            } else {
                copySelectedFile(selItem)
            }
        }

        binding.buttonDelete.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            deleteSelected(il[listViewAdapter.selIdx].trimEnd('/'))
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }

            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
        }

        readCsentryDir()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun profileUi() {
        binding.buttonCopy.isEnabled = listViewAdapter.selIdx >= 0
        binding.buttonCopy.text = "Copy selected to this app's files/${mainActivity.localCurDir}"
        binding.buttonDelete.isEnabled = listViewAdapter.selIdx >= 0

        val toolbar = mainActivity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.title = "csentry/${curDir}"
        }
    }

    private fun readCsentryDir() {
        val ma = mainActivity
        //!!AI calling FileAccessHelper to list files in csentry/ directory or its subdirectories
        ma.fileAccessHelper.listDirectory(curDir, //!!AI subdirectory in csentry/. "" points to root of csentry/
            //!!AI handling callback of FileAccessHelper successful call
            {
                il.clear()
                if (curDir.length > 0)
                    il.add("..")

                il.addAll(it.filter {
                        item -> item.endsWith("/")
                })
                il.addAll(it.filterNot {
                        item -> item.endsWith("/")
                })

                val lv = ma.findViewById<ListView>(R.id.csentryList)
                lv.adapter = listViewAdapter

                listViewAdapter.selIdx = -1
                listViewAdapter.notifyDataSetChanged()
                profileUi()
            },
            //!!AI handling callback of FileAccessHelper unsuccessful call
            {
                Toast.makeText(ma, "Error listing directory: ${it}", Toast.LENGTH_SHORT).show()
            })
    }

    private fun createDirectory(folder: String) {
        val ma = mainActivity
        //!!AI calling FileAccessHelper to make a directory in csentry/ directory or its subdirectories
        ma.fileAccessHelper.makeDirectory(curDir + folder, //!!AI path to directory being created inside csentry/ directory
            //!!AI handling callback of FileAccessHelper successful call
            {
                readCsentryDir()
            },
            //!!AI handling callback of FileAccessHelper unsuccessful call
            {
                Toast.makeText(ma, "Error creating directory: ${it}", Toast.LENGTH_SHORT).show()
            })
    }

    private fun showCreateFolderDialog() {
        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(mainActivity)
        builder.setTitle("Create new folder")

        // Set up the input
        val input = EditText(mainActivity)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setHint("Folder name")
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            // Here you get get input text from the Edittext
            createDirectory(input.text.toString())
        })
        builder.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    private fun copySelectedFile(filename: String) {
        val ma = mainActivity
        localDir?.let {
            val ldir = File(it, ma.localCurDir)

            //!!AI calling FileAccessHelper to pull a file from csentry/ directory or its subdirectories
            ma.fileAccessHelper.pullFiles(
                curDir, //!!AI directory under csentry/ where file(s) are to be pulled from
                filename, //!!AI pattern to filter for files in directory. May contain wildcard characters
                ldir.absolutePath, //!!AI directory where the pulled files are to be deposited
                false, //!!AI if ture, will search for files in subdirectories of the source directory. This will preserve the directory structure
                true, //!!AI if ture, will overwrite existing files in destination deirectory
                //!!AI handling callback of FileAccessHelper successful call
                {
                    Toast.makeText(ma, "File copied successfully", Toast.LENGTH_SHORT).show()
                },
                //!!AI handling callback of FileAccessHelper unsuccessful call
                {
                    Toast.makeText(ma, "Error copying file: ${it}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun copySelectedDir(dirname: String) {
        val ma = mainActivity
        localDir?.let {
            val ldir = File(File(it, ma.localCurDir), dirname)
            val rdir = File(File(curDir), dirname)

            //!!AI calling FileAccessHelper to pull files from csentry/ directory or its subdirectories
            ma.fileAccessHelper.pullFiles(
                rdir.absolutePath, //!!AI directory under csentry/ where file(s) are to be pulled from
                "*", //!!AI pattern to filter for files in directory. May contain wildcard characters
                ldir.absolutePath, //!!AI directory where the pulled files are to be deposited
                true, //!!AI if ture, will search for files in subdirectories of the source directory. This will preserve the directory structure
                true, //!!AI if ture, will overwrite existing files in destination deirectory
                //!!AI handling callback of FileAccessHelper successful call
                {
                    Toast.makeText(ma, "Folder copied successfully", Toast.LENGTH_SHORT).show()
                },
                //!!AI handling callback of FileAccessHelper unsuccessful call
                {
                    Toast.makeText(ma, "Error copying folder: ${it}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun deleteSelected(item: String) {
        val ma = mainActivity
        //!!AI calling FileAccessHelper to delete file or directory from csentry/ directory or its subdirectories
        ma.fileAccessHelper.delete(
            curDir, //!!AI directory under csentry/
            item, //!!AI file or directory name to be deleted
            true, //!!AI if true, files with matching pattern will be deleted
            true, //!!AI if ture, directories with matching pattern will be deleted
            //!!AI handling callback of FileAccessHelper successful call
            {
                readCsentryDir()
                Toast.makeText(ma, "Folder/file deleted successfully", Toast.LENGTH_SHORT)
                    .show()
            },
            //!!AI handling callback of FileAccessHelper unsuccessful call
            {
                Toast.makeText(ma, "Error deleting folder/file: ${it}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}