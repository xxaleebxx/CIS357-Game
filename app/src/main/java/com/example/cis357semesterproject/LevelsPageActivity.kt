package com.example.cis357semesterproject

import android.content.Intent
import android.media.Rating
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController

class LevelsPageActivity : AppCompatActivity() {
    // variables to carry information from the LevelsActivity
    private var levelOneTime: String? = null
    private var levelOneRating: Rating? = null
    private var levelOneStarted: Boolean = false
    var navCtrl : NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels_page)

        val   mainMenuButton  = findViewById<Button>(R.id.mainmenu_button)
        val   levelOneButton  = findViewById<ImageButton>(R.id.levelOne_button)


        mainMenuButton.setOnClickListener {
            val mainMenuIntent = Intent(this@LevelsPageActivity, MainActivity::class.java)
            startActivity(mainMenuIntent)
        }
        levelOneButton.setOnClickListener {
            val levelOneIntent = Intent(this@LevelsPageActivity, LevelActivity::class.java)
            startActivity(levelOneIntent)
        }

    }


}