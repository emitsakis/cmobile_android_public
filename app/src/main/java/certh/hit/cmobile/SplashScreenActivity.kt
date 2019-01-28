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
import android.content.Intent
import android.os.Handler
import android.support.v4.os.HandlerCompat.postDelayed
import certh.hit.cmobile.model.DataFactory


/**
 * Created by anmpout on 26/01/2019
 */
class SplashScreenActivity:AppCompatActivity(){
    private val mWaitHandler = Handler()
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
                //
            }

            override fun onAnimationCancel(p0: Animator?) {
                //
            }

            override fun onAnimationStart(p0: Animator?) {
                //
            }


            override fun onAnimationEnd(animation: Animator) {
                progressBar.visibility = View.VISIBLE
                mWaitHandler.postDelayed(Runnable {
                    //The following code will execute after the 5 seconds.

                    try {

                        //Go to next page i.e, start the next activity.
                        val intent = Intent(applicationContext, HomeActivity::class.java)
                        startActivity(intent)

                        //Let's Finish Splash Activity since we don't want to show this when user press back button.
                        finish()
                    } catch (ignored: Exception) {
                        ignored.printStackTrace()
                    }
                }, 2000)  // Give a 5 seconds delay.
            }


        })
    }




}
