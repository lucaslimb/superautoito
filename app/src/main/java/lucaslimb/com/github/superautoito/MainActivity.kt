package lucaslimb.com.github.superautoito

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import lucaslimb.com.github.superautoito.screens.LobbyActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editPlayerName: EditText
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editPlayerName = findViewById(R.id.edit_player_name)
        btnConfirm = findViewById(R.id.btn_confirm)

        btnConfirm.setOnClickListener {
            val playerName = editPlayerName.text.toString().trim()

            if (playerName.isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu nome", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, LobbyActivity::class.java)
            intent.putExtra("PLAYER_NAME", playerName)
            startActivity(intent)
        }
    }
}