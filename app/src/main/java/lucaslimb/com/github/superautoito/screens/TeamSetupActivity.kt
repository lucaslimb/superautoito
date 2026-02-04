package lucaslimb.com.github.superautoito.screens

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.model.Player
import lucaslimb.com.github.superautoito.model.Character
import lucaslimb.com.github.superautoito.model.GameState

enum class CardZone {
    MAIN, RESERVE, SHOP
}

class TeamSetupActivity : AppCompatActivity() {

    private lateinit var currentPlayer: Player
    private lateinit var opponentPlayer: Player

    private lateinit var etTeamName: EditText
    private lateinit var mainCards: MutableList<Character>
    private lateinit var reserveCards: MutableList<Character>

    private lateinit var shopCard: Character

    private val mainCardViews = mutableListOf<View>()
    private val reserveCardViews = mutableListOf<View>()
    private lateinit var shopCardView: View

    private var selectedIndex: Int? = null
    private var selectedZone: CardZone? = null

    private lateinit var tvCardInfo: TextView
    private lateinit var btnConfirm: Button
    private lateinit var tvInstructions: TextView

    private var lastClickTime: Long = 0
    private var lastClickedIndex: Int = -1
    private var lastClickedZone: CardZone? = null
    private val DOUBLE_CLICK_DELAY = 500L

    private var gameState: GameState? = null
    private var mustSwapCount = 0
    private val REQUIRED_SWAPS = 2

    private val feedbackHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val resetTextRunnable = Runnable { tvInstructions.text = defaultText }

    private val defaultText = "Monte seu time!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_team_setup)

        gameState = intent.getParcelableExtra("GAME_STATE")

        if (gameState != null) {
            mainCards = gameState!!.playerTeam.toMutableList()
            reserveCards = gameState!!.playerReserve.toMutableList()

            opponentPlayer = Player(
                id = "opponent",
                name = "CPU",
                hand = generateOpponentTeam()
            )

            currentPlayer = Player(
                id = "player",
                name = intent.getStringExtra("PLAYER_NAME") ?: "Jogador",
                hand = mainCards
            )
        } else {
            currentPlayer = intent.getParcelableExtra("CURRENT_PLAYER") ?: return finish()
            opponentPlayer = intent.getParcelableExtra("OPPONENT_PLAYER") ?: return finish()

            mainCards = currentPlayer.hand.take(6).toMutableList()
            reserveCards = currentPlayer.hand.drop(6).take(2).toMutableList()
        }


        shopCard = generateRandomShopCard()

        initViews()
        updateStatsDisplay()
        setupCards()
        setupListeners()

        if (gameState?.playerCanBuyCard == true) {
            showFeedbackMessage("DERROTA! Você pode comprar 1 carta da loja!")
        }
    }

    private fun generateOpponentTeam(): List<Character> {
        return Character.getDefaultCharacters().shuffled().take(6)
    }

    private fun updateStatsDisplay() {
        val state = gameState ?: return

        findViewById<TextView>(R.id.tv_round_info).text =
            "Round ${state.currentRound}/${state.totalRounds}"

        findViewById<TextView>(R.id.tv_win_loss).text =
            "V: ${state.playerWins} | D: ${state.playerLosses}"
    }

    private fun generateRandomShopCard(): Character {
        val allCharacters = Character.getDefaultCharacters()

        val usedIds = (mainCards + reserveCards).map { it.id }

        val available = allCharacters.filter { it.id !in usedIds }

        return if (available.isNotEmpty()) available.random() else allCharacters.random()
    }

    private fun handleShopTransaction(currentIndex: Int, currentZone: CardZone) {
        if (gameState?.playerCanBuyCard != true) {
            showFeedbackMessage("Loja disponível apenas após DERROTA!")
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

    private fun swapTeamCards(currentIndex: Int, currentZone: CardZone) {
        val mainIdx = if (selectedZone == CardZone.MAIN) selectedIndex!! else currentIndex
        val reserveIdx = if (selectedZone == CardZone.RESERVE) selectedIndex!! else currentIndex

        val temp = mainCards[mainIdx]
        mainCards[mainIdx] = reserveCards[reserveIdx]
        reserveCards[reserveIdx] = temp

        setupCardView(mainCardViews[mainIdx], mainCards[mainIdx], mainIdx, CardZone.MAIN)
        setupCardView(reserveCardViews[reserveIdx], reserveCards[reserveIdx], reserveIdx, CardZone.RESERVE)

        // Incrementar contador de trocas
        mustSwapCount++

        resetAllSelections()

        if(gameState?.currentRound != 1) {
            if (mustSwapCount < REQUIRED_SWAPS) {
                showFeedbackMessage("Troca ${mustSwapCount}/$REQUIRED_SWAPS completa!")
            } else {
                showFeedbackMessage("Trocas obrigatórias concluídas!")
            }
        }
    }

    private fun initViews() {
        tvInstructions = findViewById(R.id.tv_instructions)
        btnConfirm = findViewById(R.id.btn_confirm)
        tvCardInfo = findViewById(R.id.tv_card_info)
        etTeamName = findViewById(R.id.et_team_name)

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
        } else {
            if (character.imageResId != 0) {
                imgCharacter.setImageResource(character.imageResId)
            } else {
                imgCharacter.setImageResource(R.drawable.ic_character_placeholder)
            }
            tvAttack.text = character.attack.toString()
            tvDefense.text = character.defense.toString()
        }

        cardView.setOnClickListener {
            onCardClicked(index, zone)
        }

        resetCardSelection(cardView)
    }

    private fun setupListeners() {
        btnConfirm.setOnClickListener {
            if (gameState != null && gameState!!.currentRound > 1 && mustSwapCount < REQUIRED_SWAPS) {
                showFeedbackMessage(
                    "Você DEVE fazer $REQUIRED_SWAPS trocas! (${mustSwapCount}/$REQUIRED_SWAPS)")
                return@setOnClickListener
            }

            val rawTeamName = etTeamName.text.toString().trim()
            val finalTeamName = rawTeamName.ifEmpty { currentPlayer.name }

            val intent = Intent(this, BattleActivity::class.java)

            if (gameState != null) {
                val updatedState = gameState!!.copy(
                    playerTeam = mainCards.reversed(),
                    playerReserve = reserveCards
                )
                intent.putExtra("GAME_STATE", updatedState)
                intent.putExtra("PLAYER_NAME", finalTeamName)
            } else {
                val updatedPlayer = currentPlayer.copy(
                    name = finalTeamName,
                    hand = mainCards.reversed()
                )
                intent.putExtra("CURRENT_PLAYER", updatedPlayer)
                intent.putExtra("OPPONENT_PLAYER", opponentPlayer)
                intent.putParcelableArrayListExtra("PLAYER_RESERVE", ArrayList(reserveCards))
            }

            startActivity(intent)
            finish()
        }
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
            tvCardInfo.text = "Carta Misteriosa da Loja.\nTroque com uma reserva para revelar.\nA Loja permite comprar uma carta por derrota."
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

            tvInstructions.text = "$cardName. Double click no destino."

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
                showFeedbackMessage("A Loja só troca com a Reserva!")
            } else if (selectedZone == CardZone.MAIN && zone == CardZone.MAIN) {
                reorderMainCards(selectedIndex!!, index)
            } else {
                swapTeamCards(index, zone)
            }
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

    private fun showPowerDescription(character: Character) {
        val powerText = character.power.ifEmpty { "Nenhum" }
        val infoText = buildString {
            appendLine(character.id.toRoman())
            appendLine("${character.name} é ${character.types.joinToString(" e ") { it.desc }}")
            appendLine("Poder: $powerText")
            appendLine("Quando ativa: ${character.trigger.desc}")
        }
        tvCardInfo.text = infoText
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