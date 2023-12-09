package com.example.cis357semesterproject

import android.annotation.SuppressLint
import android.content.Intent
import android.media.Rating
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class LevelActivity : AppCompatActivity() {
    private var levelOneTime: String? = null //this time is the final time
    private var levelOneRunningTime: String? = null //this is the current time
    private var levelOneRating: Rating? = null
    private var levelOneStarted: Boolean = false


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level)


        val   playButton  = findViewById<Button>(R.id.play_button)

        playButton.setOnClickListener {
            val playIntent = Intent(this@LevelActivity, TiltMazeActivity::class.java)
            levelOneStarted = true
            startActivity(playIntent)
        }

        val   levelsButton  = findViewById<Button>(R.id.levels_button)

        levelsButton.setOnClickListener {
            val levelsIntent = Intent(this@LevelActivity, LevelsPageActivity::class.java)
            startActivity(levelsIntent)
        }



        if (!levelOneStarted && levelOneTime.isNullOrEmpty()) {
                playButton.text = "Play"
        } else if (levelOneStarted && levelOneTime.isNullOrEmpty()){
            playButton.text = "Resume"
        } else { playButton.text = "Restart"}
    }
}