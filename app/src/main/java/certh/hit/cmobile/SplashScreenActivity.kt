package certh.hit.cmobile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.view.View
import android.widget.ProgressBar


/**
 * Created by anmpout on 26/01/2019
 */
class SplashScreenActivity:AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val image: ImageView = findViewById(R.id.logoView)
        val progressBar:ProgressBar = findViewById(R.id.progressBar)
        val scaleDownX = ObjectAnimator.ofFloat(image, "scaleX", 0.7f)
        val scaleDownY = ObjectAnimator.ofFloat(image, "scaleY", 0.7f)
        scaleDownX.duration = 1000
        scaleDownY.duration = 1000
        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownX).with(scaleDownY).after(2000)

        scaleDown.start()
        scaleDown.addListener(object : AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }


            override fun onAnimationEnd(animation: Animator) {
                progressBar.visibility = View.VISIBLE
            }


        })
    }




}
