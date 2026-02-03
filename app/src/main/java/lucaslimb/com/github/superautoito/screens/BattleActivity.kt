package lucaslimb.com.github.superautoito.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.model.Player

class BattleActivity : AppCompatActivity() {

    private lateinit var currentPlayer: Player
    private lateinit var opponentPlayer: Player
    private lateinit var tvBattleInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContentView(R.layout.activity_battle)

        currentPlayer = intent.getParcelableExtra("CURRENT_PLAYER") ?: return finish()
        opponentPlayer = intent.getParcelableExtra("OPPONENT_PLAYER") ?: return finish()

        tvBattleInfo = findViewById(R.id.tv_battle_info)

        displayBattleInfo()
    }

    private fun displayBattleInfo() {
        val info = buildString {
            appendLine("=== BATALHA ===")
            appendLine()
            appendLine("${currentPlayer.name} VS ${opponentPlayer.name}")
            appendLine()
            appendLine("SEU TIME (${currentPlayer.hand.size} cartas):")
            currentPlayer.hand.forEachIndexed { index, card ->
                appendLine("${index + 1}. ${card.name} (ATK: ${card.attack}, DEF: ${card.defense})")
            }
            appendLine()
            appendLine("TIME OPONENTE (${opponentPlayer.hand.size} cartas):")
            opponentPlayer.hand.forEachIndexed { index, card ->
                appendLine("${index + 1}. ${card.name} (ATK: ${card.attack}, DEF: ${card.defense})")
            }
            appendLine()
            appendLine("A lógica de batalha será implementada a seguir!")
        }

        tvBattleInfo.text = info
    }
}