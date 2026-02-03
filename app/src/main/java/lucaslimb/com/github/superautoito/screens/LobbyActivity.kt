package lucaslimb.com.github.superautoito.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lucaslimb.com.github.superautoito.R
import lucaslimb.com.github.superautoito.model.Character
import lucaslimb.com.github.superautoito.model.Player
import lucaslimb.com.github.superautoito.network.NetworkEvent
import lucaslimb.com.github.superautoito.network.NetworkManager
import java.util.UUID

class LobbyActivity : AppCompatActivity() {

    private lateinit var tvPlayerName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioHost: RadioButton
    private lateinit var radioJoin: RadioButton
    private lateinit var radioSolo: RadioButton
    private lateinit var btnStart: Button
    private lateinit var btnCancel: Button

    private lateinit var networkManager: NetworkManager
    private lateinit var playerName: String
    private var isHost = false
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        playerName = intent.getStringExtra("PLAYER_NAME") ?: "Jogador"
        networkManager = NetworkManager(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tvPlayerName = findViewById(R.id.tv_player_name)
        tvStatus = findViewById(R.id.tv_status)
        progressBar = findViewById(R.id.progress_bar)
        radioGroup = findViewById(R.id.radio_group)
        radioHost = findViewById(R.id.radio_host)
        radioJoin = findViewById(R.id.radio_join)
        radioSolo = findViewById(R.id.radio_solo)
        btnStart = findViewById(R.id.btn_start)
        btnCancel = findViewById(R.id.btn_cancel)

        tvPlayerName.text = "Bem-vindo, $playerName!"
        progressBar.visibility = View.GONE
    }

    private fun setupListeners() {
        btnStart.setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                R.id.radio_host -> {
                    isHost = true
                    startHosting()
                }
                R.id.radio_join -> {
                    isHost = false
                    startSearching()
                }
                R.id.radio_solo -> {
                    startSoloMode()
                }
                else -> {
                    Toast.makeText(this, "Selecione uma opção", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCancel.setOnClickListener {
            networkManager.disconnect()
            finish()
        }
    }

    private fun startHosting() {
        radioGroup.visibility = View.GONE
        btnStart.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        tvStatus.text = "Criando sala..."

        lifecycleScope.launch {
            networkManager.registerService(playerName).collect { event ->
                handleNetworkEvent(event)
            }
        }
    }

    private fun startSearching() {
        radioGroup.visibility = View.GONE
        btnStart.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        tvStatus.text = "Procurando salas..."

        lifecycleScope.launch {
            networkManager.discoverServices().collect { event ->
                when (event) {
                    is NetworkEvent.ServiceFound -> {
                        withContext(Dispatchers.Main) {
                            showJoinDialog(event.serviceInfo.serviceName) {
                                connectToHost(event)
                            }
                        }
                    }
                    else -> handleNetworkEvent(event)
                }
            }
        }
    }

    private fun showJoinDialog(hostName: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Sala encontrada")
            .setMessage("Conectar à sala de $hostName?")
            .setPositiveButton("Conectar") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun connectToHost(event: NetworkEvent.ServiceFound) {
        tvStatus.text = "Conectando..."

        lifecycleScope.launch {
            networkManager.connectToService(event.serviceInfo).collect { connectEvent ->
                handleNetworkEvent(connectEvent)
            }
        }
    }

    private fun handleNetworkEvent(event: NetworkEvent) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (event) {
                is NetworkEvent.ServiceRegistered -> {
                    tvStatus.text = "Sala criada! Aguardando jogador..."
                }
                is NetworkEvent.WaitingForPlayer -> {
                    tvStatus.text = "Aguardando conexão..."
                }
                is NetworkEvent.Searching -> {
                    tvStatus.text = "Procurando salas disponíveis..."
                }
                is NetworkEvent.Connected -> {
                    tvStatus.text = "Conectado! Preparando partida..."
                    progressBar.visibility = View.GONE

                    kotlinx.coroutines.delay(1000)
                    startGame()
                }
                is NetworkEvent.Error -> {
                    progressBar.visibility = View.GONE
                    tvStatus.text = "Erro: ${event.message}"
                    Toast.makeText(this@LobbyActivity, event.message, Toast.LENGTH_LONG).show()

                    kotlinx.coroutines.delay(2000)
                    radioGroup.visibility = View.VISIBLE
                    btnStart.visibility = View.VISIBLE
                    tvStatus.text = "Escolha uma opção:"
                }
                else -> {}
            }
        }
    }

    private fun startGame() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allCharacters = Character.getDefaultCharacters()
            val shuffled = allCharacters.shuffled()

            val player1Cards = shuffled.take(8)
            val player2Cards = shuffled.drop(8).take(8)

            val currentPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = playerName,
                hand = if (isHost) player1Cards else player2Cards,
                isHost = isHost
            )

            val opponentPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = "Oponente",
                hand = if (isHost) player2Cards else player1Cards,
                isHost = !isHost
            )

            if (isHost) {
                val playerJson = gson.toJson(currentPlayer)
                networkManager.sendMessage(playerJson)

                val opponentJson = networkManager.receiveMessage()
                opponentJson?.let {
                    val opponent = gson.fromJson(it, Player::class.java)
                    navigateToGame(currentPlayer, opponent)
                }
            } else {
                val hostJson = networkManager.receiveMessage()
                hostJson?.let {
                    val host = gson.fromJson(it, Player::class.java)

                    val playerJson = gson.toJson(currentPlayer)
                    networkManager.sendMessage(playerJson)

                    navigateToGame(currentPlayer, host)
                }
            }
        }
    }

    private suspend fun navigateToGame(player: Player, opponent: Player) {
        withContext(Dispatchers.Main) {
            val intent = Intent(this@LobbyActivity, GameActivity::class.java)
            intent.putExtra("CURRENT_PLAYER", player)
            intent.putExtra("OPPONENT_PLAYER", opponent)
            startActivity(intent)
            finish()
        }
    }

    private fun startSoloMode() {
        radioGroup.visibility = View.GONE
        btnStart.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        tvStatus.text = "Preparando modo solo..."
        tvStatus.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(1000)

            val allCharacters = Character.getDefaultCharacters()
            val shuffled = allCharacters.shuffled()

            val player1Cards = shuffled.take(8)
            val player2Cards = shuffled.drop(8).take(8)

            val currentPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = playerName,
                hand = player1Cards,
                isHost = true
            )

            val cpuPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = "CPU (Debug)",
                hand = player2Cards,
                isHost = false
            )

            navigateToGame(currentPlayer, cpuPlayer)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkManager.disconnect()
    }
}
