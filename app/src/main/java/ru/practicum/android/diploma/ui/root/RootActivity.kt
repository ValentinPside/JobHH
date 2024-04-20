package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ActivityRootBinding
import ru.practicum.android.diploma.presentation.general.fragment.GeneralFragment

class RootActivity : AppCompatActivity() {

    private var _binding: ActivityRootBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val navController = navFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.generalFragment,
                R.id.favoritesFragment,
                R.id.teamFragment -> showBottomNav()

                else -> hideBottomNav()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.container)
            val myFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
            myFragment?.let {
                if (it is GeneralFragment) {
                    it.setCoords(ev.x, ev.y)
                }
            }
        }
        //myFragment.doSomething()
        return super.dispatchTouchEvent(ev)
    }


    private fun hideBottomNav() {
        binding.bottomNavigation.visibility = View.GONE
    }

    private fun showBottomNav() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

}
