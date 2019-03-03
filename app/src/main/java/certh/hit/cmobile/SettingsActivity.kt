package certh.hit.cmobile

import android.annotation.TargetApi
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.util.AndroidException
import android.widget.Switch
import certh.hit.cmobile.utils.PreferencesHelper


class SettingsActivity : AppCompatActivity() {
    var preferencesHelper :PreferencesHelper? = null
    var switch :Switch? =  null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initializeActionBar()
        preferencesHelper = PreferencesHelper(this)
        switch = findViewById(R.id.data_collection)
        switch!!.isChecked = preferencesHelper!!.dataCollection
        switch!!.setOnCheckedChangeListener { buttonView, isChecked ->
        preferencesHelper!!.dataCollection = isChecked

        }
    }

    /**
     * Add title to action bar
     */
    @TargetApi(21)
    private fun initializeActionBar() {
        var upArrow = resources.getDrawable(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material,theme);
        upArrow.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        supportActionBar!!.setHomeAsUpIndicator(upArrow)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportActionBar!!.title = "Settings"
        val text = SpannableString(supportActionBar!!.title)
        text.setSpan(ForegroundColorSpan(Color.WHITE), 0, text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        supportActionBar!!.title = text
        supportActionBar!!.setBackgroundDrawable( ColorDrawable(getResources()
            .getColor(R.color.dark_blue))
        );


    }
}
