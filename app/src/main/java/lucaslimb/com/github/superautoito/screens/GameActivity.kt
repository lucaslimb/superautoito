package lucaslimb.com.github.superautoito.screens

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.model.Player

class GameActivity : AppCompatActivity() {

    private lateinit var currentPlayer: Player
    private lateinit var opponentPlayer: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentPlayer = intent.getParcelableExtra("CURRENT_PLAYER") ?: return finish()
        opponentPlayer = intent.getParcelableExtra("OPPONENT_PLAYER") ?: return finish()

        val intent = Intent(this, TeamSetupActivity::class.java)
        intent.putExtra("CURRENT_PLAYER", currentPlayer)
        intent.putExtra("OPPONENT_PLAYER", opponentPlayer)
        startActivity(intent)
        finish()
    }
}