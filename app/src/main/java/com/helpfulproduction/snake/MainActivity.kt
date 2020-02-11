package com.helpfulproduction.snake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private var playButton: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playButton = findViewById(R.id.play)
        playButton?.setOnClickListener {
            startActivity(getGameIntent())
        }
    }

    private fun getGameIntent() = Intent(this, GameActivity::class.java)
}
