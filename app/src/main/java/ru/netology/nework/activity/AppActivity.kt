package ru.netology.nework.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.ActivityMainBinding
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

//        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.bottomNav.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment

//        val navController = findNavController(R.id.main_nav_host)
         navController = navHostFragment.navController

//        binding.bottomNav.setupWithNavController(navController)

         appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.postsFragment,
                R.id.eventsFragment,
                R.id.usersFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            supportActionBar?.title = destination.label
//        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                    menu.clear()
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                val isAuthenticated = appAuth.authState.value.token != null
                menu.setGroupVisible(R.id.group_authenticated_not, !isAuthenticated)
                menu.setGroupVisible(R.id.group_authenticated, isAuthenticated)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean =
                when (item.itemId) {
                    R.id.menu_login -> {
                        navController.navigate(R.id.action_global_loginFragment)
                        true
                    }

                    R.id.menu_register -> {
                        navController.navigate(R.id.action_global_registerFragment)
                        true
                    }

                    R.id.menu_logout -> {
                        appAuth.removeAuth()
                        true
                    }

                    else -> false
                }
        }, this)

        lifecycleScope.launch {
            appAuth.authState.collect {
                invalidateOptionsMenu()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() ||  super.onSupportNavigateUp()
    }
}