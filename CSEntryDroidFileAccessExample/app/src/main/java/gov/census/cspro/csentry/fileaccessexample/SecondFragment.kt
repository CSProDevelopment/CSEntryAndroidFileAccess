package gov.census.cspro.csentry.fileaccessexample

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import gov.census.cspro.csentry.fileaccessexample.databinding.FragmentSecondBinding
import java.io.File

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private val localDir get() = mainActivity.localDir

    private var _binding: FragmentSecondBinding? = null
    private var curDir: String
        get() = mainActivity.localCurDir
        set(value) {
            mainActivity.localCurDir = value
        }

    private val mainActivity get() = activity as MainActivity
    private var _listViewAdapter: FileListViewAdapter? = null
    private val listViewAdapter get() = _listViewAdapter!!
    val il = arrayListOf<String>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ma = mainActivity
        _listViewAdapter = FileListViewAdapter(ma, il)

        binding.buttonSecond.setOnClickListener {
            //findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            val selItem = il[listViewAdapter.selIdx]
            if (selItem.endsWith("/")) {
                copySelectedDir(selItem.trimEnd('/'))
            } else {
                copySelectedFile(selItem)
            }
        }

        binding.localList.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) as String

            if (element.equals("..")) { //dir up
                val el = curDir.substring(0, curDir.length - 1).split("/")
                curDir = ""
                if (el.count() > 1) {
                    el.subList(0, el.count() - 1).forEach {
                        curDir += "${it}/"
                    }
                }
                readLocalDir()
            } else if (element.endsWith("/") && listViewAdapter.selIdx == position) { //folder
                curDir += element
                readLocalDir()
            } else {
                listViewAdapter.selIdx = position
                listViewAdapter.notifyDataSetChanged()
                profileUi()
            }
        }

        readLocalDir()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun profileUi() {
        binding.buttonSecond.isEnabled = listViewAdapter.selIdx >= 0
        binding.buttonSecond.text = "Copy selected to csentry/${mainActivity.csEntryCurDir}"

        val toolbar = mainActivity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.title="app:files/${curDir}"
        }
    }

    private fun readLocalDir() {
        val ma = mainActivity

        var dir = localDir
        if (curDir.length > 0) {
            dir = File(dir, curDir)
        }


        if (dir!=null) {
            il.clear()
            if (curDir.length > 0)
                il.add("..")

            val ilUnsorted = ArrayList<String>()
            dir.list().forEach {
                var fName = it
                if (File(dir, it).isDirectory){
                    fName += "/"
                }
                ilUnsorted.add(fName)
            }

            il.addAll(ilUnsorted.filter {
                    item -> item.endsWith("/")
            })
            il.addAll(ilUnsorted.filterNot {
                    item -> item.endsWith("/")
            })

            val lv = ma.findViewById<ListView>(R.id.localList)
            lv.adapter = listViewAdapter

            listViewAdapter.selIdx = -1
            listViewAdapter.notifyDataSetChanged()
            profileUi()
        }
    }

    private fun copySelectedFile(filename: String) {
        val ma = mainActivity
        localDir?.let {
            val ldir = File(it, ma.localCurDir)

            //!!AI calling FileAccessHelper to push a file to csentry/ directory or its subdirectories
            ma.fileAccessHelper.pushFiles(
                ldir.absolutePath, //!!AI source directory for the file to be pushed to csentry/
                filename, //!!AI pattern to filter for files in directory. May contain wildcard characters
                mainActivity.csEntryCurDir, //!!AI destination directory under csentry/
                false, //!!AI if ture, will search for files in subdirectories of the source directory. This will preserve the directory structure
                true, //!!AI if ture, will overwrite existing files in destination deirectory
                //!!AI handling callback of FileAccessHelper successful call
                {
                    Toast.makeText(ma, "File copied successfully", Toast.LENGTH_SHORT ).show()
                },
                //!!AI handling callback of FileAccessHelper unsuccessful call
                {
                    Toast.makeText(ma, "Error copying file: ${it}", Toast.LENGTH_SHORT ).show()
                }
            )
        }
    }

    private fun copySelectedDir(dirName: String) {
        val ma = mainActivity
        localDir?.let {
            val ldir = File(File(it, ma.localCurDir), dirName)
            val rdir = File(File(mainActivity.csEntryCurDir), dirName)

            //!!AI calling FileAccessHelper to push a directory to csentry/ directory or its subdirectories
            ma.fileAccessHelper.pushFiles(
                ldir.absolutePath, //!!AI source directory for the file to be pushed to csentry/
                "*", //!!AI pattern to filter for files in directory. May contain wildcard characters
                rdir.absolutePath, //!!AI destination directory under csentry/
                true, //!!AI if ture, will search for files in subdirectories of the source directory. This will preserve the directory structure
                true, //!!AI if ture, will overwrite existing files in destination deirectory
                //!!AI handling callback of FileAccessHelper successful call
                {
                    Toast.makeText(ma, "File copied successfully", Toast.LENGTH_SHORT ).show()
                },
                //!!AI handling callback of FileAccessHelper unsuccessful call
                {
                    Toast.makeText(ma, "Error copying file: ${it}", Toast.LENGTH_SHORT ).show()
                }
            )
        }
    }
}