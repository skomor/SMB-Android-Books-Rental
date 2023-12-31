package com.example.smb.booksapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.example.smb.booksapp.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.smb.booksapp.databinding.FragmentBottomSheetBinding
import com.example.smb.booksapp.viewmodels.main.MainViewModel
import com.example.smb.booksapp.viewmodels.main.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.TextView
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModel
import com.example.smb.booksapp.viewmodels.drawerFragments.UserInfoViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var mainViewModel: MainViewModel
    lateinit var userInfoViewModel: UserInfoViewModel

    public override fun onStart() {
        super.onStart()

        if (!mainViewModel.isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)

        mainViewModel = ViewModelProvider(this, MainViewModelFactory( applicationContext))
            .get(MainViewModel::class.java)
        userInfoViewModel = ViewModelProvider(this, UserInfoViewModelFactory())
            .get(UserInfoViewModel::class.java)

        if (!mainViewModel.isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        mainViewModel.setupListenerForSignOut {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        var textvie = this.findViewById<TextView>(R.id.navbarMail)


        val image = navView.getHeaderView(0).findViewById<ImageView>(R.id.profilePicInNavHeader)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_audio, R.id.booked
            ), drawerLayout
        )
        userInfoViewModel.userimage.observe(this, Observer {
            textvie.text = userInfoViewModel.user?.email;
            if (userInfoViewModel.userimage.value != null){
                image.setImageBitmap(null)
                image.setImageBitmap(userInfoViewModel.userimage.value)
            }
        })

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val extras = intent.extras
        if (extras != null)
        {
            val value: Boolean = extras["openAudios"] as Boolean
            if(value){
                navController.navigate(R.id.nav_audio)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.title == "Settings") {
            this.mainViewModel.logout();
            return true;
        }
        return false;
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}