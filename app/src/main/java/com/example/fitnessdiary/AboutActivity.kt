package com.example.fitnessdiary

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_THEME = "extra_theme"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val appNameText = findViewById<TextView>(R.id.appNameText)
        appNameText.text = getString(R.string.about_app_name)

        val theme = intent.getStringExtra(EXTRA_THEME)
            ?: getString(R.string.about_theme)

        val themeText = findViewById<TextView>(R.id.themeText)
        themeText.text = getString(R.string.about_theme_label) + ": " + theme
    }
}