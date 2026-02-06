package lucaslimb.com.github.superautoito.screens

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lucaslimb.com.github.superautoito.MainActivity
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.engine.BattleEngine
import lucaslimb.com.github.superautoito.model.Character
import lucaslimb.com.github.superautoito.model.GameState
import lucaslimb.com.github.superautoito.model.Player
import lucaslimb.com.github.superautoito.network.NetworkManager

class BattleActivity : AppCompatActivity() {

    private lateinit var currentPlayer: Player
    private lateinit var opponentPlayer: Player
    private lateinit var opponentReserve: List<Character>
    private lateinit var layoutGameOverControls: View
    private lateinit var btnExitGame: View
    private lateinit var btnPlayAgain: View
    private lateinit var battleEngine: BattleEngine

    private val playerCardViews = mutableListOf<View>()
    private val enemyCardViews = mutableListOf<View>()
    private lateinit var tvPlayerName: TextView
    private lateinit var tvOpponentName: TextView
    private lateinit var tvBattleLog: TextView
    private var gameState: GameState? = null
    private var isFirstLoad = true
    private var isMultiplayer = false

    // Multiplayer
    private lateinit var networkManager: NetworkManager
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_battle)

        gameState = intent.getParcelableExtra("GAME_STATE")

        if (gameState != null) {
            currentPlayer = Player(
                id = "player",
                name = intent.getStringExtra("PLAYER_NAME")
                    ?: getString(R.string.default_player_name),
                hand = gameState!!.playerTeam
            )
            opponentPlayer = Player(
                id = "opponent",
                name = getString(R.string.cpu_name),
                hand = gameState!!.opponentTeam
            )

            opponentReserve = gameState!!.opponentReserve
            isFirstLoad = false
        } else {
            isMultiplayer = intent.getBooleanExtra("IS_MULTIPLAYER", false)
            currentPlayer = intent.getParcelableExtra("CURRENT_PLAYER") ?: return finish()
            opponentPlayer = intent.getParcelableExtra("OPPONENT_PLAYER") ?: return finish()
            val maxRounds = intent.getIntExtra("MAX_ROUNDS", 10)
            val playerRes = intent.getParcelableArrayListExtra<Character>("PLAYER_RESERVE") ?: emptyList()
            opponentReserve = intent.getParcelableArrayListExtra<Character>("OPPONENT_RESERVE") ?: emptyList()

            gameState = GameState(
                currentRound = 1,
                totalRounds = maxRounds,
                playerTeam = currentPlayer.hand,
                playerReserve = playerRes,
                opponentTeam = opponentPlayer.hand,
                opponentReserve = opponentReserve,
                isMultiplayer = isMultiplayer
            )
            isFirstLoad = true
        }

        if (isMultiplayer) {
            networkManager = NetworkManager(this)
        }

        initViews()
        setupBattleField()

        battleEngine = BattleEngine(
            context = this,
            playerTeam = currentPlayer.hand.toMutableList(),
            enemyTeam = opponentPlayer.hand.toMutableList()
        )

        runEntranceAnimation()

        lifecycleScope.launch {
            delay(1500)
            startBattleSequence()
        }
    }

    private suspend fun startBattleSequence() {
        logBattle(getString(R.string.battle_starting))

        val battlePlayerTeam = currentPlayer.hand.map { it.copy() }.toMutableList()
        val battleEnemyTeam = opponentPlayer.hand.map { it.copy() }.toMutableList()

        battleEngine = BattleEngine(
            context = this,
            playerTeam = battlePlayerTeam,
            enemyTeam = battleEnemyTeam
        )

        battleEngine.executePreBattlePhase(
            onLog = { message -> logBattle(message) },
            onUpdate = {
                refreshAllCards()
                delay(1200)
            }
        )

        delay(1000)
        logBattle(getString(R.string.combat_started))
        delay(1000)

        while (true) {
            if (battleEngine.getPlayerTeam().isNotEmpty() && battleEngine.getEnemyTeam().isNotEmpty()) {

                val pChar = battleEngine.getPlayerTeam()[0]
                val eChar = battleEngine.getEnemyTeam()[0]

                val pSim = pChar.copy(defense = pChar.defense - eChar.attack)
                val eSim = eChar.copy(defense = eChar.defense - pChar.attack)

                animateClash(playerCardViews[0], enemyCardViews[0], pSim, eSim)

                delay(1200)
            }

            val shouldContinue = battleEngine.executeCombatTurn(
                onLog = { message -> logBattle(message) },
                onUpdate = {
                    refreshAllCards()
                    delay(1000)
                }
            )

            if (!shouldContinue) break

            delay(1500)
        }

        delay(1500)
        if (isMultiplayer) {
            announceWinnerMultiplayer()
        } else {
            announceWinnerCPU()
        }
    }

    private fun announceWinnerCPU() {
        val winner = battleEngine.getWinner()
        val bannedIds = battleEngine.getBannedIds()

        val newState = when (winner) {
            1 -> {
                logBattle(getString(R.string.round_victory, gameState!!.currentRound))
                gameState!!.copy(
                    currentRound = gameState!!.currentRound + 1,
                    playerWins = gameState!!.playerWins + 1,
                    playerCanBuyCard = false,
                    lastRoundWinner = 1,
                    playerTeam = currentPlayer.hand.reversed(),
                    playerReserve = gameState!!.playerReserve,
                    nextRoundBannedCharacters = bannedIds,
                    opponentTeam = opponentPlayer.hand.reversed(),
                    opponentReserve = opponentReserve
                )
            }
            -1 -> {
                logBattle(getString(R.string.round_defeat, gameState!!.currentRound))
                gameState!!.copy(
                    currentRound = gameState!!.currentRound + 1,
                    playerLosses = gameState!!.playerLosses + 1,
                    playerCanBuyCard = true,
                    lastRoundWinner = -1,
                    playerTeam = currentPlayer.hand.reversed(),
                    playerReserve = gameState!!.playerReserve,
                    nextRoundBannedCharacters = bannedIds,
                    opponentTeam = opponentPlayer.hand.reversed(),
                    opponentReserve = opponentReserve
                )
            }
            else -> {
                logBattle(getString(R.string.round_draw, gameState!!.currentRound))
                gameState!!.copy(
                    currentRound = gameState!!.currentRound + 1,
                    lastRoundWinner = 0,
                    playerTeam = currentPlayer.hand.reversed(),
                    playerReserve = gameState!!.playerReserve,
                    nextRoundBannedCharacters = bannedIds,
                    opponentTeam = opponentPlayer.hand.reversed(),
                    opponentReserve = opponentReserve
                )
            }
        }

        lifecycleScope.launch {
            delay(1500)

            if (newState.isGameOver()) {
                showFinalResult(newState)
                setupGameOverListeners()
            } else {
                returnToSetup(newState)
            }
        }
    }

    private fun announceWinnerMultiplayer() {
        lifecycleScope.launch {
            val localWinner = battleEngine.getWinner()
            val localBannedIds = battleEngine.getBannedIds()

            logBattle("Sincronizando resultado...")

            try {
                withContext(Dispatchers.IO) {
                    val myResult = mapOf(
                        "winner" to localWinner,
                        "bannedIds" to localBannedIds
                    )

                    val myJson = gson.toJson(myResult)
                    networkManager.sendMessage(myJson)

                    val opponentJson = networkManager.receiveMessage()
                        ?: throw Exception("Não recebeu resultado do oponente")

                    val opponentResult = gson.fromJson(opponentJson, Map::class.java)
                    val opponentWinner = (opponentResult["winner"] as Double).toInt()
                    val opponentBannedIdsJson = gson.toJson(opponentResult["bannedIds"])
                    val opponentBannedIds = gson.fromJson(
                        opponentBannedIdsJson,
                        Array<Int>::class.java
                    ).toList()

                    val expectedOpponentWinner = when (localWinner) {
                        1 -> -1
                        -1 -> 1
                        else -> 0
                    }

                    if (opponentWinner != expectedOpponentWinner) {
                        throw Exception("Dessincronização detectada! Resultados diferentes.")
                    }

                    withContext(Dispatchers.Main) {
                        val allBannedIds = (localBannedIds + opponentBannedIds).distinct()

                        val newState = when (localWinner) {
                            1 -> {
                                logBattle(getString(R.string.round_victory, gameState!!.currentRound))
                                gameState!!.copy(
                                    currentRound = gameState!!.currentRound + 1,
                                    playerWins = gameState!!.playerWins + 1,
                                    playerCanBuyCard = false,
                                    lastRoundWinner = 1,
                                    playerTeam = currentPlayer.hand.reversed(),
                                    playerReserve = gameState!!.playerReserve,
                                    nextRoundBannedCharacters = allBannedIds,
                                    opponentTeam = opponentPlayer.hand.reversed(),
                                    opponentReserve = opponentReserve,
                                    isMultiplayer = true
                                )
                            }
                            -1 -> {
                                logBattle(getString(R.string.round_defeat, gameState!!.currentRound))
                                gameState!!.copy(
                                    currentRound = gameState!!.currentRound + 1,
                                    playerLosses = gameState!!.playerLosses + 1,
                                    playerCanBuyCard = true,
                                    lastRoundWinner = -1,
                                    playerTeam = currentPlayer.hand.reversed(),
                                    playerReserve = gameState!!.playerReserve,
                                    nextRoundBannedCharacters = allBannedIds,
                                    opponentTeam = opponentPlayer.hand.reversed(),
                                    opponentReserve = opponentReserve,
                                    isMultiplayer = true
                                )
                            }
                            else -> {
                                logBattle(getString(R.string.round_draw, gameState!!.currentRound))
                                gameState!!.copy(
                                    currentRound = gameState!!.currentRound + 1,
                                    lastRoundWinner = 0,
                                    playerTeam = currentPlayer.hand.reversed(),
                                    playerReserve = gameState!!.playerReserve,
                                    nextRoundBannedCharacters = allBannedIds,
                                    opponentTeam = opponentPlayer.hand.reversed(),
                                    opponentReserve = opponentReserve,
                                    isMultiplayer = true
                                )
                            }
                        }

                        delay(1500)

                        if (newState.isGameOver()) {
                            showFinalResult(newState)
                            setupGameOverListeners()
                        } else {
                            returnToSetup(newState)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logBattle("Erro de sincronização: ${e.message}")
                    delay(2000)

                    val intent = Intent(this@BattleActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
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

        runOnUiThread {
            layoutGameOverControls.visibility = View.VISIBLE
        }
    }

    private fun returnToSetup(state: GameState) {
        val intent = Intent(this, TeamSetupActivity::class.java)
        intent.putExtra("GAME_STATE", state)
        intent.putExtra("PLAYER_NAME", currentPlayer.name)
        intent.putExtra("IS_MULTIPLAYER", isMultiplayer)
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

            renderTeam(playerCardViews, playerTeam, isPlayer = true)
            renderTeam(enemyCardViews, enemyTeam, isPlayer = false)
        }
    }

    private fun renderTeam(views: List<View>, cards: List<Character>, isPlayer: Boolean) {

        for (i in views.indices) {
            val cardView = views[i]

            cardView.animate().cancel()
            cardView.clearAnimation()

            if (i < cards.size) {
                val character = cards[i]
                val isNewCardInSlot = cardView.tag != character.id

                if (isNewCardInSlot) {
                    cardView.alpha = 0f
                    val direction = if (isPlayer) 1 else -1
                    cardView.translationX = 150f * direction
                    cardView.scaleX = 1f
                    cardView.scaleY = 1f
                }

                val tvAttack = cardView.findViewById<TextView>(R.id.tv_attack)
                val tvDefense = cardView.findViewById<TextView>(R.id.tv_defense)
                val imgCharacter = cardView.findViewById<ImageView>(R.id.img_character)

                tvAttack.text = character.attack.toString()
                tvDefense.text = maxOf(0, character.defense).toString()

                if (character.imageResId != 0) {
                    imgCharacter.setImageResource(character.imageResId)
                } else {
                    imgCharacter.setImageResource(R.drawable.ic_character_placeholder)
                }

                imgCharacter.clearColorFilter()

                if (isNewCardInSlot) {
                    cardView.visibility = View.VISIBLE
                    cardView.tag = character.id

                    cardView.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(350)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                } else {
                    if (cardView.visibility != View.VISIBLE || cardView.alpha < 1f) {
                        cardView.visibility = View.VISIBLE
                        cardView.alpha = 1f
                        cardView.translationX = 0f
                        cardView.scaleX = 1f
                        cardView.scaleY = 1f
                    }
                }

            } else {
                cardView.visibility = View.INVISIBLE
                cardView.tag = null
                cardView.alpha = 1f
                cardView.translationX = 0f
                cardView.scaleX = 1f
                cardView.scaleY = 1f
            }
        }
    }

    private fun animateClash(
        playerView: View,
        enemyView: View,
        playerChar: Character?,
        enemyChar: Character?
    ) {
        playerView.animate()
            .translationX(100f)
            .setDuration(150)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                playerView.animate().translationX(0f).setDuration(150).start()
//                if (playerChar != null && playerChar.defense <= 0) {
//                    animateDeathSequence(playerView)
//                }
            }
            .start()

        enemyView.animate()
            .translationX(-100f)
            .setDuration(150)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                enemyView.animate().translationX(0f).setDuration(150).start()
//                if (enemyChar != null && enemyChar.defense <= 0) {
//                    animateDeathSequence(enemyView)
//                }
            }
            .start()
    }

//    private fun animateDeathSequence(view: View) {
//        deadCharacterIds.add(view.id)
//
//        val img = view.findViewById<ImageView>(R.id.img_character)
//        img.setColorFilter(android.graphics.Color.RED, android.graphics.PorterDuff.Mode.SRC_ATOP)
//
//        val shake = android.view.animation.RotateAnimation(
//            -10f, 10f,
//            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
//            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
//        )
//        shake.duration = 50
//        shake.repeatCount = 5
//        shake.repeatMode = android.view.animation.Animation.REVERSE
//        view.startAnimation(shake)
//
//        view.animate()
//            .alpha(0f)
//            .scaleX(0.9f)
//            .scaleY(0.9f)
//            .setDuration(400)
//            .setStartDelay(200)
//            .start()
//    }

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

        layoutGameOverControls = findViewById(R.id.layout_game_over_controls)
        btnExitGame = findViewById(R.id.btn_exit_game)
        btnPlayAgain = findViewById(R.id.btn_play_again)
    }

    private fun setupBattleField() {
        tvPlayerName.text = currentPlayer.name
        tvOpponentName.text = opponentPlayer.name

        tvBattleLog.text =
            getString(R.string.battle_started, currentPlayer.name, opponentPlayer.name)
    }

    private fun setupGameOverListeners() {
        btnExitGame.setOnClickListener {
            if (isMultiplayer) {
                networkManager.disconnect()
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnPlayAgain.setOnClickListener {
            if (isMultiplayer) {
                networkManager.disconnect()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                val allCharacters = Character.getDefaultCharacters(context = this).shuffled()

                val newPlayerHand = allCharacters.take(8)
                val newCpuHand = allCharacters.drop(8).take(8)

                val freshPlayer = currentPlayer.copy(hand = newPlayerHand)
                val freshOpponent = opponentPlayer.copy(hand = newCpuHand)

                val intent = Intent(this, TeamSetupActivity::class.java)
                intent.putExtra("CURRENT_PLAYER", freshPlayer)
                intent.putExtra("OPPONENT_PLAYER", freshOpponent)
                intent.putExtra("MAX_ROUNDS", gameState?.totalRounds ?: 10)
                intent.putExtra("IS_MULTIPLAYER", false)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun runEntranceAnimation() {
        playerCardViews.forEachIndexed { index, view ->
            view.translationX = -500f
            view.alpha = 0f
            view.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay((index * 100).toLong())
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        enemyCardViews.forEachIndexed { index, view ->
            view.translationX = 500f
            view.alpha = 0f
            view.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay((index * 100).toLong())
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMultiplayer) {
            networkManager.disconnect()
        }
    }
}

