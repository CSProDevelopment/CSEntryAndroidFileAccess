package gov.census.cspro.csentry.fileaccessexample

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import gov.census.cspro.csentry.fileaccessexample.databinding.ActivityMainBinding
import gov.census.cspro.csentry.fileaccess.FileAccessHelper
import java.io.File

class MainActivity : AppCompatActivity() {

    //!!AI application local directory is set to application's external storage files directory
    // change this property to copy files to to/from other directories
    val localDir get() = this.getExternalFilesDir(null)

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    val fileAccessHelper = FileAccessHelper(this)

    var csEntryCurDir = ""
    var localCurDir = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener{
            if (mainMenu!=null) {
                mainMenu.findItem(R.id.action_csentry).isVisible = false
                mainMenu.findItem(R.id.action_local).isVisible = true
            }

            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    var _mainMenu:Menu? = null
    val mainMenu get() = _mainMenu!!

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_csentry).isVisible = false
        _mainMenu = menu

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (R.id.action_local == item.itemId) {
            if (mainMenu!=null) {
                mainMenu.findItem(R.id.action_csentry).isVisible = true
                mainMenu.findItem(R.id.action_local).isVisible = false
            }


            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.action_FirstFragment_to_SecondFragment)
            return true
        } else if (R.id.action_csentry == item.itemId) {
            if (mainMenu!=null) {
                mainMenu.findItem(R.id.action_csentry).isVisible = false
                mainMenu.findItem(R.id.action_local).isVisible = true
            }

            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.action_SecondFragment_to_FirstFragment)
            return true
        }

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}