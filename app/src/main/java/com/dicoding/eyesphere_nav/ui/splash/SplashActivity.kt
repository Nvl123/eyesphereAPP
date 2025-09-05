package com.dicoding.eyesphere_nav.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.eyesphere_nav.MainActivity
import com.dicoding.eyesphere_nav.R
import com.dicoding.eyesphere_nav.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val logo = binding.imgLogo
        val background = binding.imgBg

        supportActionBar?.hide()

        // Animasi background slide up
        val backgroundAnimator = ObjectAnimator.ofFloat(background, "translationY", 1000f, 0f)
        backgroundAnimator.duration = 1500

        // Animasi logo zoom in (dari kecil ke normal)
        val logoAnimatorX = ObjectAnimator.ofFloat(logo, "scaleX", 0.3f, 1.2f, 1f)
        val logoAnimatorY = ObjectAnimator.ofFloat(logo, "scaleY", 0.3f, 1.2f, 1f)

        // Animasi fade in untuk logo
        val logoFadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)

        logoAnimatorX.duration = 1500
        logoAnimatorY.duration = 1500
        logoFadeIn.duration = 1000

        // Animasi zoom in untuk seluruh container (opsional)
        val containerZoomX = ObjectAnimator.ofFloat(binding.root, "scaleX", 0.8f, 1f)
        val containerZoomY = ObjectAnimator.ofFloat(binding.root, "scaleY", 0.8f, 1f)
        containerZoomX.duration = 1200
        containerZoomY.duration = 1200

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            backgroundAnimator,
            logoAnimatorX,
            logoAnimatorY,
            logoFadeIn,
            containerZoomX,
            containerZoomY
        )
        animatorSet.start()

        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Tambahkan custom transition saat pindah activity
            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
            finish()
        }
    }
}
