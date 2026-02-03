    package lucaslimb.com.github.superautoito.screens

    import android.content.Intent
    import android.content.pm.ActivityInfo
    import android.os.Bundle
    import android.view.View
    import android.widget.Button
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import lucaslimb.com.github.superautoito.R
    import lucaslimb.com.github.superautoito.model.Player
    import lucaslimb.com.github.superautoito.model.Character

    class TeamSetupActivity : AppCompatActivity() {

        private lateinit var currentPlayer: Player
        private lateinit var opponentPlayer: Player

        private lateinit var mainCards: MutableList<Character>

        private lateinit var reserveCards: MutableList<Character>

        private val mainCardViews = mutableListOf<View>()

        private val reserveCardViews = mutableListOf<View>()

        private var selectedCardIndex: Int? = null
        private var selectedFromReserve: Boolean = false
        private lateinit var tvCardInfo: TextView
        private lateinit var btnConfirm: Button
        private lateinit var tvInstructions: TextView

        private var lastClickTime: Long = 0
        private var lastClickedPosition: Int = -1
        private var lastClickedIsReserve: Boolean = false
        private val DOUBLE_CLICK_DELAY = 500L // 500ms para considerar duplo

        private val defaultText =
            "Monte seu time! Toque para selecionar, toque novamente para trocar/reordenar."

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            setContentView(R.layout.activity_team_setup)

            currentPlayer = intent.getParcelableExtra("CURRENT_PLAYER") ?: return finish()
            opponentPlayer = intent.getParcelableExtra("OPPONENT_PLAYER") ?: return finish()
            mainCards = currentPlayer.hand.take(6).toMutableList()
            reserveCards = currentPlayer.hand.drop(6).take(2).toMutableList()

            initViews()
            setupCards()
            setupListeners()
        }

        private fun initViews() {
            tvInstructions = findViewById(R.id.tv_instructions)
            btnConfirm = findViewById(R.id.btn_confirm)
            tvCardInfo = findViewById(R.id.tv_card_info)

            mainCardViews.add(findViewById(R.id.card_main_1))
            mainCardViews.add(findViewById(R.id.card_main_2))
            mainCardViews.add(findViewById(R.id.card_main_3))
            mainCardViews.add(findViewById(R.id.card_main_4))
            mainCardViews.add(findViewById(R.id.card_main_5))
            mainCardViews.add(findViewById(R.id.card_main_6))

            reserveCardViews.add(findViewById(R.id.card_reserve_1))
            reserveCardViews.add(findViewById(R.id.card_reserve_2))
        }

        private fun setupCards() {
            mainCards.forEachIndexed { index, character ->
                setupCardView(mainCardViews[index], character, index, false)
            }

            reserveCards.forEachIndexed { index, character ->
                setupCardView(reserveCardViews[index], character, index, true)
            }
        }

        private fun setupCardView(cardView: View, character: Character, index: Int, isReserve: Boolean) {
            val imgCharacter = cardView.findViewById<ImageView>(R.id.img_character)
            val tvAttack = cardView.findViewById<TextView>(R.id.tv_attack)
            val tvDefense = cardView.findViewById<TextView>(R.id.tv_defense)

            if (character.imageResId != 0) {
                imgCharacter.setImageResource(character.imageResId)
            } else {
                imgCharacter.setImageResource(R.drawable.ic_character_placeholder)
            }

            tvAttack.text = character.attack.toString()
            tvDefense.text = character.defense.toString()

            cardView.setOnClickListener {
                onCardClicked(index, isReserve)
            }

            resetCardSelection(cardView)
        }

        private fun setupListeners() {
            btnConfirm.setOnClickListener {
                val updatedPlayer = currentPlayer.copy(hand = mainCards.reversed())

                val intent = Intent(this, BattleActivity::class.java)
                intent.putExtra("CURRENT_PLAYER", updatedPlayer)
                intent.putExtra("OPPONENT_PLAYER", opponentPlayer)
                startActivity(intent)
                finish()
            }
        }

        private fun onCardClicked(index: Int, isReserve: Boolean) {
            val currentTime = System.currentTimeMillis()
            val character = if (isReserve) reserveCards[index] else mainCards[index]

            showPowerDescription(character)

            if (index == lastClickedPosition &&
                isReserve == lastClickedIsReserve &&
                (currentTime - lastClickTime) < DOUBLE_CLICK_DELAY) {

                executeCardSelection(index, isReserve)

                lastClickTime = 0
            } else {
                lastClickTime = currentTime
                lastClickedPosition = index
                lastClickedIsReserve = isReserve
            }
        }

        private fun executeCardSelection(index: Int, isReserve: Boolean) {
            if (selectedCardIndex == null) {
                selectedCardIndex = index
                selectedFromReserve = isReserve

                val cardView = if (isReserve) reserveCardViews[index] else mainCardViews[index]
                highlightCard(cardView)

                val cardName = if (isReserve) reserveCards[index].name else mainCards[index].name

                if (isReserve) {
                    tvInstructions.text = "$cardName. Double click no time para trocar."
                } else {
                    tvInstructions.text = "$cardName. Double click para reordenar/trocar."
                }

            } else {
                if (selectedFromReserve && isReserve) {
                    resetAllSelections()

                    tvInstructions.text = defaultText

                } else if (!selectedFromReserve && !isReserve) {
                    reorderMainCards(selectedCardIndex!!, index)

                } else {
                    val mainIndex = if (selectedFromReserve) index else selectedCardIndex!!
                    val reserveIndex = if (selectedFromReserve) selectedCardIndex!! else index

                    swapCards(mainIndex, reserveIndex)
                }
            }
        }

        private fun reorderMainCards(fromIndex: Int, toIndex: Int) {
            if (fromIndex == toIndex) {
                resetAllSelections()
                tvInstructions.text = defaultText
                return
            }

            val cardToMove = mainCards.removeAt(fromIndex)
            mainCards.add(toIndex, cardToMove)

            mainCards.forEachIndexed { index, character ->
                setupCardView(mainCardViews[index], character, index, false)
            }

            resetAllSelections()
            tvInstructions.text = defaultText
        }

        private fun swapCards(mainIndex: Int, reserveIndex: Int) {
            val temp = mainCards[mainIndex]
            mainCards[mainIndex] = reserveCards[reserveIndex]
            reserveCards[reserveIndex] = temp

            setupCardView(mainCardViews[mainIndex], mainCards[mainIndex], mainIndex, false)
            setupCardView(reserveCardViews[reserveIndex], reserveCards[reserveIndex], reserveIndex, true)

            resetAllSelections()
            tvInstructions.text = defaultText
        }

        private fun showPowerDescription(character: Character) {
            val powerText = character.power.ifEmpty {
                "Nenhum"
            }

            val infoText = buildString {
                appendLine(character.id.toRoman())
                appendLine("${character.name} é ${character.types.joinToString(" e ") { it.desc }}")
                appendLine("Seu poder $powerText ativa sempre ${character.trigger.desc}")
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
            selectedCardIndex = null
            selectedFromReserve = false

            mainCardViews.forEach { resetCardSelection(it) }
            reserveCardViews.forEach { resetCardSelection(it) }
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