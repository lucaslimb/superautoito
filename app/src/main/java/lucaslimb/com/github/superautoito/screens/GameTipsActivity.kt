package lucaslimb.com.github.superautoito.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import lucaslimb.com.github.superautoito.R

class GameTipsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tips)

        val btnBack = findViewById<Button>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }
    }
}