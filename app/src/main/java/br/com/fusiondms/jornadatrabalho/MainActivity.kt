package br.com.fusiondms.jornadatrabalho

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import br.com.fusiondms.core.common.statusBarIconColor
import br.com.fusiondms.jornadatrabalho.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var splashScreen: SplashScreen
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usarAnimacaoDeSaidaCustomizada()

//        window.apply {
//            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            statusBarColor = Color.TRANSPARENT
//        }
        statusBarIconColor(this, Color.WHITE)
    }

    private fun usarAnimacaoDeSaidaCustomizada() {
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashScreenView = splashScreenViewProvider.view
            val scX = splashScreenView.width / 2
            val scY = splashScreenView.height / 2

            val finalRadius = Math.hypot(scX.toDouble(), scY.toDouble()).toFloat()

            val mover = ObjectAnimator.ofFloat(splashScreenView, View.TRANSLATION_Y, 0f, splashScreenView.height.toFloat())
            mover.interpolator = AnticipateInterpolator()
            mover.duration = 700L
            mover.start()
            val reveal = ViewAnimationUtils.createCircularReveal(splashScreenView, scX, scY, finalRadius, finalRadius / 5)
            reveal.interpolator = DecelerateInterpolator(3f)
            reveal.startDelay = 200L
            reveal.duration = 700L
            reveal.doOnEnd {
                splashScreenViewProvider.remove()
            }
            reveal.start()

//            val set = AnimatorSet()
//            set.duration = 700L
//            set.playTogether(mover, reveal)
//            set.doOnEnd {
//                splashScreenViewProvider.remove()
//            }
//            set.start()
        }
    }

    private fun manterSplashScreenIndefinitamente() {
        splashScreen.setKeepOnScreenCondition { true }
    }

    private fun manterSplashScreenPor5Segundos() {
        binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                Thread.sleep(5000)
                binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}