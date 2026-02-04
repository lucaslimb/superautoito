package lucaslimb.com.github.superautoito.screens

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.engine.BattleEngine
import lucaslimb.com.github.superautoito.model.Character
import lucaslimb.com.github.superautoito.model.GameState
import lucaslimb.com.github.superautoito.model.Player

class BattleActivity : AppCompatActivity() {

    private lateinit var currentPlayer: Player
    private lateinit var opponentPlayer: Player
    private lateinit var battleEngine: BattleEngine

    private val playerCardViews = mutableListOf<View>()
    private val enemyCardViews = mutableListOf<View>()

    private lateinit var tvPlayerName: TextView
    private lateinit var tvOpponentName: TextView
    private lateinit var tvBattleLog: TextView
    private var gameState: GameState? = null
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_battle)

        gameState = intent.getParcelableExtra("GAME_STATE")

        if (gameState != null) {
            currentPlayer = Player(
                id = "player",
                name = intent.getStringExtra("PLAYER_NAME") ?: "Jogador",
                hand = gameState!!.playerTeam
            )
            opponentPlayer = Player(
                id = "opponent",
                name = "CPU",
                hand = gameState!!.opponentTeam
            )
            isFirstLoad = false
        } else {
            currentPlayer = intent.getParcelableExtra("CURRENT_PLAYER") ?: return finish()
            opponentPlayer = intent.getParcelableExtra("OPPONENT_PLAYER") ?: return finish()

            gameState = GameState(
                currentRound = 1,
                playerTeam = currentPlayer.hand,
                playerReserve = intent.getParcelableArrayListExtra("PLAYER_RESERVE") ?: emptyList(),
                opponentTeam = opponentPlayer.hand,
                opponentReserve = emptyList()
            )
            isFirstLoad = true
        }

        balanceTeams()

        initViews()
        setupBattleField()

        battleEngine = BattleEngine(
            playerTeam = currentPlayer.hand.toMutableList(),
            enemyTeam = opponentPlayer.hand.toMutableList()
        )

        lifecycleScope.launch {
            delay(1500)
            startBattleSequence()
        }
    }

    private fun balanceTeams() {
        if (opponentPlayer.hand.size > 6) {
            opponentPlayer = opponentPlayer.copy(
                hand = opponentPlayer.hand.take(6)
            )
        }
    }

    private suspend fun startBattleSequence() {
        logBattle("Preparando para a batalha...")
        delay(1000)

        battleEngine.executePreBattlePhase(
            onLog = { message -> logBattle(message) },
            onUpdate = {
                refreshAllCards()
                delay(800)
            }
        )

        delay(1000)
        logBattle("INÍCIO DO COMBATE")
        delay(1000)

        while (true) {
            if (playerCardViews.isNotEmpty() && enemyCardViews.isNotEmpty()) {
                animateClash(playerCardViews[0], enemyCardViews[0])
                delay(600)
            }

            val shouldContinue = battleEngine.executeCombatTurn(
                onLog = { message -> logBattle(message) },
                onUpdate = {
                    refreshAllCards()
                    delay(600)
                }
            )

            if (!shouldContinue) break

            delay(1000)
        }

        delay(1000)
        announceWinner()
    }

    private fun announceWinner() {
        val winner = battleEngine.getWinner()

        val newState = when (winner) {
            1 -> {
                logBattle("VITÓRIA! Round ${gameState!!.currentRound}")
                gameState!!.copy(
                    currentRound = gameState!!.currentRound + 1,
                    playerWins = gameState!!.playerWins + 1,
                    playerCanBuyCard = false,
                    lastRoundWinner = 1,
                    playerTeam = battleEngine.getPlayerTeam(),
                    opponentTeam = battleEngine.getEnemyTeam()
                )
            }
            -1 -> {
                logBattle("DERROTA! Round ${gameState!!.currentRound}")
                gameState!!.copy(
                    currentRound = gameState!!.currentRound + 1,
                    playerLosses = gameState!!.playerLosses + 1,
                    playerCanBuyCard = true, // Pode comprar na loja!
                    lastRoundWinner = -1,
                    playerTeam = battleEngine.getPlayerTeam(),
                    opponentTeam = battleEngine.getEnemyTeam()
                )
            }
            else -> {
                logBattle("EMPATE! Round ${gameState!!.currentRound}")
                gameState!!.copy(
                    currentRound = gameState!!.currentRound + 1,
                    lastRoundWinner = 0,
                    playerTeam = battleEngine.getPlayerTeam(),
                    opponentTeam = battleEngine.getEnemyTeam()
                )
            }
        }

        lifecycleScope.launch {
            delay(2000)

            if (newState.isGameOver()) {
                // Jogo terminou!
                showFinalResult(newState)
            } else {
                // Voltar para TeamSetup
                returnToSetup(newState)
            }
        }
    }

    private fun showFinalResult(state: GameState) {
        val finalWinner = state.getFinalWinner()
        val message = when (finalWinner) {
            1 -> "🏆 VITÓRIA FINAL! ${state.playerWins} x ${state.playerLosses}"
            -1 -> "💀 DERROTA FINAL! ${state.playerWins} x ${state.playerLosses}"
            else -> "⚔️ EMPATE FINAL! ${state.playerWins} x ${state.playerLosses}"
        }

        logBattle(message)

        // Permanecer na tela mostrando resultado final
        // Opcional: adicionar botão "Jogar Novamente"
    }

    private fun returnToSetup(state: GameState) {
        val intent = Intent(this, TeamSetupActivity::class.java)
        intent.putExtra("GAME_STATE", state)
        intent.putExtra("PLAYER_NAME", currentPlayer.name)
        startActivity(intent)
        finish()
    }

    private fun logBattle(message: String) {
        runOnUiThread {
            tvBattleLog.text = message
        }
    }

    private fun refreshAllCards() {
        runOnUiThread {
            val playerTeam = battleEngine.getPlayerTeam()
            val enemyTeam = battleEngine.getEnemyTeam()

            renderTeam(playerCardViews, playerTeam)
            renderTeam(enemyCardViews, enemyTeam)
        }
    }

    private fun renderTeam(views: List<View>, cards: List<Character>) {
        for (i in views.indices) {
            val cardView = views[i]
            if (i < cards.size) {
                val character = cards[i]
                cardView.visibility = View.VISIBLE

                val tvAttack = cardView.findViewById<TextView>(R.id.tv_attack)
                val tvDefense = cardView.findViewById<TextView>(R.id.tv_defense)
                val imgCharacter = cardView.findViewById<ImageView>(R.id.img_character)

                tvAttack.text = character.attack.toString()
                tvDefense.text = character.defense.toString()

                if (character.imageResId != 0) {
                    imgCharacter.setImageResource(character.imageResId)
                } else {
                    imgCharacter.setImageResource(R.drawable.ic_character_placeholder)
                }

                // Destacar carta com vida baixa
                if (character.defense <= 2) {
                    cardView.alpha = 0.6f
                } else {
                    cardView.alpha = 1.0f
                }
            } else {
                cardView.visibility = View.INVISIBLE
            }
        }
    }

    private fun animateClash(view1: View, view2: View) {
        view1.animate()
            .translationX(80f)
            .setDuration(150)
            .withEndAction {
                view1.animate().translationX(0f).setDuration(150).start()
            }
            .start()

        view2.animate()
            .translationX(-80f)
            .setDuration(150)
            .withEndAction {
                view2.animate().translationX(0f).setDuration(150).start()
            }
            .start()
    }

    private fun initViews() {
        tvPlayerName = findViewById(R.id.tv_player_name)
        tvOpponentName = findViewById(R.id.tv_opponent_name)
        tvBattleLog = findViewById(R.id.tv_battle_log)

        playerCardViews.add(findViewById(R.id.player_card_1))
        playerCardViews.add(findViewById(R.id.player_card_2))
        playerCardViews.add(findViewById(R.id.player_card_3))
        playerCardViews.add(findViewById(R.id.player_card_4))
        playerCardViews.add(findViewById(R.id.player_card_5))
        playerCardViews.add(findViewById(R.id.player_card_6))

        enemyCardViews.add(findViewById(R.id.enemy_card_1))
        enemyCardViews.add(findViewById(R.id.enemy_card_2))
        enemyCardViews.add(findViewById(R.id.enemy_card_3))
        enemyCardViews.add(findViewById(R.id.enemy_card_4))
        enemyCardViews.add(findViewById(R.id.enemy_card_5))
        enemyCardViews.add(findViewById(R.id.enemy_card_6))
    }

    private fun setupBattleField() {
        tvPlayerName.text = currentPlayer.name
        tvOpponentName.text = opponentPlayer.name

        tvBattleLog.text = "Batalha Iniciada! ${currentPlayer.name} vs ${opponentPlayer.name}"
    }
}

