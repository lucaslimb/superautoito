package lucaslimb.com.github.superautoito.screens

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import lucaslimb.com.github.superautoito.MainActivity
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.model.Player
import lucaslimb.com.github.superautoito.model.Character
import lucaslimb.com.github.superautoito.model.GameState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import lucaslimb.com.github.superautoito.network.NetworkManager

enum class CardZone {
    MAIN, RESERVE, SHOP
}

class TeamSetupActivity : AppCompatActivity() {

    private lateinit var currentPlayer: Player
    private lateinit var opponentPlayer: Player
    private lateinit var opponentReserve: MutableList<Character>
    private lateinit var etTeamName: EditText
    private lateinit var tvInstructions: TextView
    private lateinit var mainCards: MutableList<Character>
    private lateinit var reserveCards: MutableList<Character>
    private var isMultiplayer: Boolean = false
    private lateinit var shopCard: Character

    private val mainCardViews = mutableListOf<View>()
    private val reserveCardViews = mutableListOf<View>()
    private lateinit var shopCardView: View

    private var selectedIndex: Int? = null
    private var selectedZone: CardZone? = null

    private lateinit var tvCardInfo: TextView
    private lateinit var btnConfirm: Button

    private var lastClickTime: Long = 0
    private var lastClickedIndex: Int = -1
    private var lastClickedZone: CardZone? = null
    private val DOUBLE_CLICK_DELAY = 500L

    private var gameState: GameState? = null
    private var initialTeamSnapshot: List<Int> = emptyList()
    private var maxRounds: Int = 20
    private lateinit var btnExit: Button
    private val gson = Gson()
    private var networkManager: NetworkManager? = null
    private var isReady: Boolean = false
    // Data holding for synchronization
    private var pendingOpponentTeam: List<Character>? = null
    private var pendingOpponentReserve: List<Character>? = null
    private var isOpponentReady: Boolean = false

    private val feedbackHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val resetTextRunnable = Runnable { tvInstructions.text = defaultText }

    private val defaultText by lazy { getString(R.string.default_instruction) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_team_setup)

        gameState = intent.getParcelableExtra("GAME_STATE")
        isMultiplayer = intent.getBooleanExtra("IS_MULTIPLAYER", false) || (gameState?.isMultiplayer == true)
        initViews()

        if (isMultiplayer) {
            networkManager = NetworkManager(this)
            listenForOpponentMessages()
        }

        if (gameState != null) {
            maxRounds = gameState!!.totalRounds
            isMultiplayer = gameState!!.isMultiplayer

            mainCards = gameState!!.playerTeam.toMutableList()
            reserveCards = gameState!!.playerReserve.toMutableList()

            val cpuMain = gameState!!.opponentTeam.toMutableList()
            val cpuRes = gameState!!.opponentReserve.toMutableList()

            if (isMultiplayer) {
                opponentReserve = cpuRes
                opponentPlayer = Player(
                    id = "opponent",
                    name = getString(R.string.opponent_name),
                    hand = cpuMain
                )
            } else {
                val cpuWasDefeated = gameState!!.lastRoundWinner == 1
                val (evolvedCpuTeam, evolvedCpuReserve) = simulateCpuAction(cpuMain, cpuRes, cpuWasDefeated)
                opponentReserve = evolvedCpuReserve
                opponentPlayer = Player(
                    id = "opponent",
                    name = getString(R.string.cpu_name),
                    hand = evolvedCpuTeam
                )
            }

            currentPlayer = Player(
                id = "player",
                name = intent.getStringExtra("PLAYER_NAME")
                    ?: getString(R.string.default_player_name),
                hand = mainCards
            )

            applyBans(gameState!!.nextRoundBannedCharacters)
            initialTeamSnapshot = mainCards.map { it.id }
        } else {
            maxRounds = intent.getIntExtra("MAX_ROUNDS", 10)
            isMultiplayer = intent.getBooleanExtra("IS_MULTIPLAYER", false)
            currentPlayer = intent.getParcelableExtra("CURRENT_PLAYER") ?: return finish()

            val passedOpponent = intent.getParcelableExtra<Player>("OPPONENT_PLAYER") ?: return finish()

            mainCards = currentPlayer.hand.take(6).toMutableList()
            reserveCards = currentPlayer.hand.drop(6).take(2).toMutableList()

            val opponentHand = passedOpponent.hand
            val cpuMain = opponentHand.take(6).toMutableList()
            opponentReserve = opponentHand.drop(6).take(2).toMutableList()

            opponentPlayer = passedOpponent.copy(
                name = if (isMultiplayer) getString(R.string.opponent_name) else getString(R.string.cpu_name),
                hand = cpuMain
            )
        }

        shopCard = generateRandomShopCard()

        updateStatsDisplay()
        setupCards()
        setupListeners()

        if (gameState?.playerCanBuyCard == true) {
            showFeedbackMessage(getString(R.string.defeat_shop_available))
        }
    }

    private fun listenForOpponentMessages() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                val message = networkManager?.receiveMessage()
                if (message != null) {
                    handleNetworkMessage(message)
                }
            }
        }
    }

    private suspend fun handleNetworkMessage(json: String) {
        try {
            val dataMap = gson.fromJson(json, Map::class.java)

            if (dataMap["type"] == "READY") {
                val opponentTeamJson = gson.toJson(dataMap["team"])
                val opponentReserveJson = gson.toJson(dataMap["reserve"])

                val newOppTeam = gson.fromJson(opponentTeamJson, Array<Character>::class.java).toList()
                val newOppReserve = gson.fromJson(opponentReserveJson, Array<Character>::class.java).toList()

                pendingOpponentTeam = newOppTeam
                pendingOpponentReserve = newOppReserve
                isOpponentReady = true

                withContext(Dispatchers.Main) {
                    checkStartCondition()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendReadySignal(teamName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val readyPayload = mapOf(
                    "type" to "READY",
                    "team" to mainCards.reversed(),
                    "reserve" to reserveCards,
                    "name" to teamName
                )
                val json = gson.toJson(readyPayload)
                networkManager?.sendMessage(json)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TeamSetupActivity, "Error sending ready: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkStartCondition() {
        if (isMultiplayer) {
            if (isReady && isOpponentReady) {
                // Update opponent data with what we received
                pendingOpponentTeam?.let { opponentPlayer = opponentPlayer.copy(hand = it) }
                pendingOpponentReserve?.let { opponentReserve = it.toMutableList() }

                // Get current name from Edit Text
                val rawTeamName = etTeamName.text.toString().trim()
                val finalTeamName = rawTeamName.ifEmpty { currentPlayer.name }

                launchBattle(finalTeamName)
            } else if (isReady && !isOpponentReady) {
                // I am waiting for opponent
                showFeedbackMessage(getString(R.string.waiting_opponent_ready))
                btnConfirm.text = getString(R.string.waiting_ellipses)
                btnConfirm.isEnabled = false
                btnConfirm.setBackgroundColor(Color.DKGRAY)
            } else if (!isReady && isOpponentReady) {
                // Opponent is waiting for me
                showFeedbackMessage(getString(R.string.opponent_is_ready))
            }
        }
    }

    private fun simulateCpuAction(
        team: MutableList<Character>,
        reserve: MutableList<Character>,
        wasDefeated: Boolean
    ): Pair<MutableList<Character>, MutableList<Character>> {

        if (reserve.isEmpty() || team.isEmpty()) return Pair(team, reserve)

        // 1. Simulação padrão: Oponente faz algumas mudanças táticas (2 trocas)
        repeat(2) {
            val mainIndex = (0 until team.size).random()
            val reserveIndex = (0 until reserve.size).random()

            val temp = team[mainIndex]
            team[mainIndex] = reserve[reserveIndex]
            reserve[reserveIndex] = temp
        }

        // 2. Lógica de Compra em caso de Derrota
        if (wasDefeated) {
            if (Math.random() < 0.5) {

                val allCharacters = Character.getDefaultCharacters(this)
                val usedIds = (team + reserve).map { it.id }
                val available = allCharacters.filter { it.id !in usedIds }

                if (available.isNotEmpty()) {
                    val newChar = available.random()

                    val randomReserveIndex = reserve.indices.random()
                    reserve[randomReserveIndex] = newChar

                    if (Math.random() < 0.25) {
                        val randomTeamIndex = team.indices.random()

                        val temp = team[randomTeamIndex]
                        team[randomTeamIndex] = reserve[randomReserveIndex]
                        reserve[randomReserveIndex] = temp
                    }
                }
            }
        }

        return Pair(team, reserve)
    }

    private fun updateStatsDisplay() {
        val current = gameState?.currentRound ?: 1
        val total = gameState?.totalRounds ?: maxRounds
        val wins = gameState?.playerWins ?: 0
        val losses = gameState?.playerLosses ?: 0

        findViewById<TextView>(R.id.tv_round_info).text =
            getString(R.string.round_info_format, current, total)

        findViewById<TextView>(R.id.tv_win_loss).text =
            getString(R.string.win_loss_format, wins, losses)
    }

    private fun generateRandomShopCard(): Character {
        val allCharacters = Character.getDefaultCharacters(context = this)

        val playerIds = (mainCards + reserveCards).map { it.id }
        val opponentIds = (opponentPlayer.hand + opponentReserve).map { it.id }

        val usedIds = playerIds + opponentIds
        val available = allCharacters.filter { it.id !in usedIds }

        return if (available.isNotEmpty()) {
            available.random()
        } else {
            allCharacters.random()
        }
    }

    private fun handleShopTransaction(currentIndex: Int, currentZone: CardZone) {
        if (gameState?.playerCanBuyCard != true) {
            showFeedbackMessage(getString(R.string.shop_only_after_defeat))
            resetAllSelections()
            return
        }

        val reserveIndex = if (currentZone == CardZone.RESERVE) currentIndex else selectedIndex!!

        reserveCards[reserveIndex] = shopCard
        shopCard = generateRandomShopCard()

        setupCardView(reserveCardViews[reserveIndex], reserveCards[reserveIndex], reserveIndex, CardZone.RESERVE)
        setupCardView(shopCardView, shopCard, 0, CardZone.SHOP)

        gameState = gameState!!.copy(playerCanBuyCard = false)

        resetAllSelections()
        showPowerDescription(reserveCards[reserveIndex])
    }

    private fun swapMainAndReserve(mainIndex: Int, reserveIndex: Int) {
        val cardFromReserve = reserveCards[reserveIndex]

        if (isCharacterBanned(cardFromReserve)) {
            showFeedbackMessage( getString(R.string.banned_character, cardFromReserve.name))
            resetAllSelections()
            return
        }

        val temp = mainCards[mainIndex]
        mainCards[mainIndex] = reserveCards[reserveIndex]
        reserveCards[reserveIndex] = temp

        setupCardView(mainCardViews[mainIndex], mainCards[mainIndex], mainIndex, CardZone.MAIN)
        setupCardView(reserveCardViews[reserveIndex], reserveCards[reserveIndex], reserveIndex, CardZone.RESERVE)

        resetAllSelections()

        tvInstructions.text = defaultText
    }

    private fun initViews() {
        tvInstructions = findViewById(R.id.tv_instructions)
        btnConfirm = findViewById(R.id.btn_confirm)
        tvCardInfo = findViewById(R.id.tv_card_info)
        etTeamName = findViewById(R.id.et_team_name)
        btnExit = findViewById(R.id.btn_exit)

        mainCardViews.add(findViewById(R.id.card_main_1))
        mainCardViews.add(findViewById(R.id.card_main_2))
        mainCardViews.add(findViewById(R.id.card_main_3))
        mainCardViews.add(findViewById(R.id.card_main_4))
        mainCardViews.add(findViewById(R.id.card_main_5))
        mainCardViews.add(findViewById(R.id.card_main_6))
        reserveCardViews.add(findViewById(R.id.card_reserve_1))
        reserveCardViews.add(findViewById(R.id.card_reserve_2))
        shopCardView = findViewById(R.id.card_reserve_3)
    }

    private fun setupCards() {
        mainCards.forEachIndexed { index, character ->
            setupCardView(mainCardViews[index], character, index, CardZone.MAIN)
        }

        reserveCards.forEachIndexed { index, character ->
            setupCardView(reserveCardViews[index], character, index, CardZone.RESERVE)
        }

        setupCardView(shopCardView, shopCard, 0, CardZone.SHOP)
    }

    private fun setupCardView(cardView: View, character: Character, index: Int, zone: CardZone) {
        val imgCharacter = cardView.findViewById<ImageView>(R.id.img_character)
        val tvAttack = cardView.findViewById<TextView>(R.id.tv_attack)
        val tvDefense = cardView.findViewById<TextView>(R.id.tv_defense)

        if (zone == CardZone.SHOP) {
            imgCharacter.setImageResource(R.drawable.card)
            tvAttack.text = "?"
            tvDefense.text = "?"
            imgCharacter.clearColorFilter()
            cardView.alpha = 1.0f
        } else {
            if (character.imageResId != 0) {
                imgCharacter.setImageResource(character.imageResId)
            } else {
                imgCharacter.setImageResource(R.drawable.ic_character_placeholder)
            }
            tvAttack.text = character.attack.toString()
            tvDefense.text = character.defense.toString()

            if (isCharacterBanned(character)) {
                imgCharacter.setColorFilter(
                    android.graphics.Color.parseColor("#99000000"),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )

                cardView.alpha = 0.8f
            } else {
                imgCharacter.clearColorFilter()
                cardView.alpha = 1.0f
            }
        }


        cardView.setOnClickListener {
            onCardClicked(index, zone)
        }

        resetCardSelection(cardView)
    }

    private fun isCharacterBanned(character: Character): Boolean {
        return gameState?.nextRoundBannedCharacters?.contains(character.id) == true
    }

    private fun setupListeners() {
        btnConfirm.setOnClickListener {
            if (gameState != null && gameState!!.currentRound > 1) {
                val currentTeamSnapshot = mainCards.map { it.id }
                if (currentTeamSnapshot == initialTeamSnapshot) {
                    showFeedbackMessage(getString(R.string.swap_required))
                    return@setOnClickListener
                }
            }

            val rawTeamName = etTeamName.text.toString().trim()
            val finalTeamName = rawTeamName.ifEmpty { currentPlayer.name }

            if (isMultiplayer) {
                lifecycleScope.launch {
                    isReady = true
                    sendReadySignal(finalTeamName)
                    checkStartCondition()
                }
            } else {
                launchBattle(finalTeamName)
            }
        }
        btnExit.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun launchBattle(teamName: String) {
        val intent = Intent(this, BattleActivity::class.java)

        if (gameState != null) {
            val updatedState = gameState!!.copy(
                playerTeam = mainCards.reversed(),
                playerReserve = reserveCards,
                opponentTeam = opponentPlayer.hand,
                opponentReserve = opponentReserve,
                isMultiplayer = isMultiplayer
            )
            intent.putExtra("GAME_STATE", updatedState)
            intent.putExtra("PLAYER_NAME", teamName)
        } else {
            val updatedPlayer = currentPlayer.copy(
                name = teamName,
                hand = mainCards.reversed()
            )
            intent.putExtra("CURRENT_PLAYER", updatedPlayer)
            intent.putExtra("OPPONENT_PLAYER", opponentPlayer)
            intent.putParcelableArrayListExtra("PLAYER_RESERVE", ArrayList(reserveCards))
            intent.putParcelableArrayListExtra("OPPONENT_RESERVE", ArrayList(opponentReserve))
            intent.putExtra("MAX_ROUNDS", maxRounds)
            intent.putExtra("IS_MULTIPLAYER", isMultiplayer)
        }

        startActivity(intent)
        finish()
    }

    private fun onCardClicked(index: Int, zone: CardZone) {
        val currentTime = System.currentTimeMillis()

        val character = when(zone) {
            CardZone.MAIN -> mainCards[index]
            CardZone.RESERVE -> reserveCards[index]
            CardZone.SHOP -> shopCard
        }

        if (zone != CardZone.SHOP) {
            showPowerDescription(character)
        } else {
            tvCardInfo.text = getString(R.string.shop_card_info)
        }

        if (index == lastClickedIndex && zone == lastClickedZone && (currentTime - lastClickTime) < DOUBLE_CLICK_DELAY) {
            executeCardSelection(index, zone)
            lastClickTime = 0
        } else {
            lastClickTime = currentTime
            lastClickedIndex = index
            lastClickedZone = zone
        }
    }

    private fun executeCardSelection(index: Int, zone: CardZone) {
        if (selectedIndex == null) {
            selectedIndex = index
            selectedZone = zone

            val cardView = when(zone) {
                CardZone.MAIN -> mainCardViews[index]
                CardZone.RESERVE -> reserveCardViews[index]
                CardZone.SHOP -> shopCardView
            }
            highlightCard(cardView)

            val cardName = if(zone == CardZone.SHOP) "Loja" else
                (if (zone == CardZone.RESERVE) reserveCards[index].name else mainCards[index].name)

            tvInstructions.text = getString(R.string.double_click_destination, cardName)

        } else {

            if (selectedIndex == index && selectedZone == zone) {
                resetAllSelections()
                tvInstructions.text = defaultText
                return
            }

            if (isShopSwap(selectedZone!!, zone)) {
                handleShopTransaction(index, zone)
            } else if (selectedZone == CardZone.SHOP || zone == CardZone.SHOP) {
                resetAllSelections()
                showFeedbackMessage(getString(R.string.shop_only_with_reserve))
            } else if (selectedZone == CardZone.MAIN && zone == CardZone.MAIN) {
                reorderMainCards(selectedIndex!!, index)
            } else if (selectedZone == CardZone.RESERVE && zone == CardZone.RESERVE) {
                swapReserveSlots(selectedIndex!!, index)
            } else {
                val mainIndex = if (selectedZone == CardZone.MAIN) selectedIndex!! else index
                val reserveIndex = if (selectedZone == CardZone.RESERVE) selectedIndex!! else index

                swapMainAndReserve(mainIndex, reserveIndex)
            }
        }
    }

    private fun applyBans(bannedIds: List<Int>) {
        if (bannedIds.isEmpty()) return

        val bannedNames = mutableListOf<String>()

        for (i in mainCards.indices.reversed()) {
            val character = mainCards[i]

            if (character.id in bannedIds) {
                bannedNames.add(character.name)

                if (reserveCards.isNotEmpty()) {
                    val cardFromReserve = reserveCards[0]
                    reserveCards[0] = character
                    mainCards[i] = cardFromReserve
                } else {
                    reserveCards.add(character)
                    mainCards.removeAt(i)
                }
            }
        }

        if (bannedNames.isNotEmpty()) {
            val msg = getString(
                R.string.banned_moved_to_reserve,
                bannedNames.joinToString(" e "))
            showFeedbackMessage(msg)
        }
    }

    private fun isShopSwap(zone1: CardZone, zone2: CardZone): Boolean {
        return (zone1 == CardZone.SHOP && zone2 == CardZone.RESERVE) ||
                (zone1 == CardZone.RESERVE && zone2 == CardZone.SHOP)
    }

    private fun reorderMainCards(fromIndex: Int, toIndex: Int) {
        val cardToMove = mainCards.removeAt(fromIndex)
        mainCards.add(toIndex, cardToMove)

        mainCards.forEachIndexed { idx, char ->
            setupCardView(mainCardViews[idx], char, idx, CardZone.MAIN)
        }

        resetAllSelections()
        tvInstructions.text = defaultText
    }

    private fun swapReserveSlots(index1: Int, index2: Int) {
        val temp = reserveCards[index1]
        reserveCards[index1] = reserveCards[index2]
        reserveCards[index2] = temp

        setupCardView(reserveCardViews[index1], reserveCards[index1], index1, CardZone.RESERVE)
        setupCardView(reserveCardViews[index2], reserveCards[index2], index2, CardZone.RESERVE)

        resetAllSelections()
        tvInstructions.text = defaultText
    }

    private fun showPowerDescription(character: Character) {
        val grayColor = Color.parseColor("#aaaaaa") // Cor para os argumentos 1 e 2
        val powerText = character.power.ifEmpty { getString(R.string.no_power) }

        val builder = SpannableStringBuilder()

        builder.append(character.id.toRoman()).append("\n")

        val typeText = formatSpannable(
            getString(
                R.string.character_type_format,
                character.name,
                character.types.joinToString(" e ") { getString(it.desc) }
            ),
            grayColor,
            character.types.joinToString(" e ") { getString(it.desc) }
        )
        builder.append(typeText).append("\n")

        // 3. Poder
        builder.append(formatSpannable(getString(R.string.power_label, powerText), grayColor, powerText)).append("\n")

        // 4. Trigger
        val triggerDesc = getString(character.trigger.desc)
        builder.append(formatSpannable(getString(R.string.trigger_label, triggerDesc), grayColor, triggerDesc))

        tvCardInfo.text = builder
    }

    private fun formatSpannable(fullText: String, color: Int, vararg targets: String): SpannableString {
        val spannable = SpannableString(fullText)
        for (target in targets) {
            val start = fullText.indexOf(target)
            if (start != -1) {
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    start + target.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannable
    }

    private fun highlightCard(cardView: View) {
        cardView.findViewById<View>(R.id.card_border)?.background =
            ContextCompat.getDrawable(this, R.drawable.card_border_selected)
    }

    private fun resetCardSelection(cardView: View) {
        cardView.findViewById<View>(R.id.card_border)?.background =
            ContextCompat.getDrawable(this, R.drawable.card_border_normal)
    }

    private fun resetAllSelections() {
        selectedIndex = null
        selectedZone = null

        mainCardViews.forEach { resetCardSelection(it) }
        reserveCardViews.forEach { resetCardSelection(it) }
        resetCardSelection(shopCardView)
    }

    private fun showFeedbackMessage(message: String) {
        feedbackHandler.removeCallbacks(resetTextRunnable)

        tvInstructions.text = message

        feedbackHandler.postDelayed(resetTextRunnable, 6000)
    }

    override fun onDestroy() {
        super.onDestroy()
        feedbackHandler.removeCallbacks(resetTextRunnable)
    }
}

fun Int.toRoman(): String {
    if (this < 1) return this.toString()
    val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val symbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    var number = this
    return buildString {
        for (i in values.indices) {
            while (number >= values[i]) {
                append(symbols[i])
                number -= values[i]
            }
        }
    }
}