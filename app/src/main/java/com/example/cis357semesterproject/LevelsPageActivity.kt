package com.example.cis357semesterproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class LevelsPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels_page)

        val   mainMenuButton  = findViewById<Button>(R.id.mainmenu_button)
        mainMenuButton.setOnClickListener {
            val mainMenuIntent = Intent(this@LevelsPageActivity, MainActivity::class.java)
            startActivity(mainMenuIntent)
        }


        val   levelOneButton  = findViewById<ImageButton>(R.id.levelOne_button)
        levelOneButton.setOnClickListener {
            val levelOneIntent = Intent(this@LevelsPageActivity, LevelActivity::class.java)
            startActivity(levelOneIntent)
        }
    }
}