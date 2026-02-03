package lucaslimb.com.github.superautoito.screens

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.model.Player

class GameActivity : AppCompatActivity() {

    private lateinit var tvGameInfo: TextView
    private lateinit var currentPlayer: Player
    private lateinit var opponentPlayer: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        currentPlayer = intent.getParcelableExtra("CURRENT_PLAYER") ?: return finish()
        opponentPlayer = intent.getParcelableExtra("OPPONENT_PLAYER") ?: return finish()

        tvGameInfo = findViewById(R.id.tv_game_info)

        displayGameInfo()
    }

    private fun displayGameInfo() {
        val info = buildString {
            appendLine("=== JOGO INICIADO ===")
            appendLine()
            appendLine("Você: ${currentPlayer.name}")
            appendLine("Suas cartas: ${currentPlayer.hand.size}")
            currentPlayer.hand.forEachIndexed { index, card ->
                appendLine("  ${index + 1}. ${card.name} (ATK: ${card.attack}, DEF: ${card.defense})")
            }
            appendLine()
            appendLine("Oponente: ${opponentPlayer.name}")
            appendLine("Cartas do oponente: ${opponentPlayer.hand.size}")
            appendLine()
            appendLine("A lógica de batalha será implementada em breve!")
        }

        tvGameInfo.text = info
    }
}