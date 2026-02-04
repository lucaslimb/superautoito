package lucaslimb.com.github.superautoito

import android.content.Intent
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lucaslimb.com.github.superautoito.model.Character
import lucaslimb.com.github.superautoito.model.Player
import lucaslimb.com.github.superautoito.network.NetworkEvent
import lucaslimb.com.github.superautoito.network.NetworkManager
import lucaslimb.com.github.superautoito.screens.GameActivity
import lucaslimb.com.github.superautoito.screens.toRoman
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var progressBarMain: ProgressBar
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnStart: Button
    private lateinit var btnCancel: Button

    private lateinit var layoutHost: LinearLayout
    private lateinit var layoutJoin: LinearLayout
    private lateinit var layoutSolo: LinearLayout

    private lateinit var etRoomName: EditText
    private lateinit var etHostRounds: EditText
    private lateinit var etSoloRounds: EditText

    private lateinit var rvRoomList: RecyclerView
    private lateinit var pbSearching: ProgressBar
    private val roomAdapter = RoomAdapter { serviceInfo -> onRoomSelected(serviceInfo) }

    private lateinit var networkManager: NetworkManager
    private var isHost = false
    private val gson = Gson()

    private var selectedServiceInfo: NsdServiceInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkManager = NetworkManager(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tv_status)
        progressBarMain = findViewById(R.id.progress_bar_main)
        radioGroup = findViewById(R.id.radio_group)
        btnStart = findViewById(R.id.btn_start)
        btnCancel = findViewById(R.id.btn_cancel)

        layoutHost = findViewById(R.id.layout_host_options)
        layoutJoin = findViewById(R.id.layout_join_options)
        layoutSolo = findViewById(R.id.layout_solo_options)

        etRoomName = findViewById(R.id.et_room_name)
        etHostRounds = findViewById(R.id.et_host_rounds)
        etSoloRounds = findViewById(R.id.et_solo_rounds)

        rvRoomList = findViewById(R.id.rv_room_list)
        pbSearching = findViewById(R.id.pb_searching)
        rvRoomList.layoutManager = LinearLayoutManager(this)
        rvRoomList.adapter = roomAdapter

        etRoomName.setText("${UUID.randomUUID() .toString().replace("-", "").take(8)}")
    }

    private fun setupListeners() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            updateUIState(checkedId)
        }

        btnStart.setOnClickListener {
            handleStartClick()
        }

        btnCancel.setOnClickListener {
            networkManager.disconnect()
            finish()
        }
    }

    private fun updateUIState(checkedId: Int) {
        layoutHost.visibility = View.GONE
        layoutJoin.visibility = View.GONE
        layoutSolo.visibility = View.GONE

        selectedServiceInfo = null
        roomAdapter.clearSelection()

        btnStart.isEnabled = true
        btnStart.setBackgroundColor(getColor(R.color.bronze))
        btnStart.setTextColor(getColor(R.color.silver))

        when (checkedId) {
            R.id.radio_host -> {
                layoutHost.visibility = View.VISIBLE
                btnStart.text = "CRIAR SALA"
            }
            R.id.radio_join -> {
                layoutJoin.visibility = View.VISIBLE
                btnStart.text = "CONECTAR"
                btnStart.isEnabled = false
                btnStart.setBackgroundColor(getColor(android.R.color.darker_gray))
                startSearchingRooms()
            }
            R.id.radio_solo -> {
                layoutSolo.visibility = View.VISIBLE
                btnStart.text = "JOGAR SOLO"
            }
        }
    }

    private fun handleStartClick() {
        val checkedId = radioGroup.checkedRadioButtonId

        when (checkedId) {
            R.id.radio_host -> {
                val roomName = etRoomName.text.toString()
                val rounds = etHostRounds.text.toString().toIntOrNull() ?: 10

                if (roomName.isBlank()) {
                    Toast.makeText(this, "Digite um nome para a sala", Toast.LENGTH_SHORT).show()
                    return
                }
                isHost = true
                startHosting(roomName, rounds)
            }
            R.id.radio_join -> {
                selectedServiceInfo?.let { service ->
                    isHost = false
                    connectToHost(service)
                }
            }
            R.id.radio_solo -> {
                val rounds = etSoloRounds.text.toString().toIntOrNull() ?: 10
                startSoloMode(rounds)
            }
        }
    }


    private fun startHosting(roomName: String, rounds: Int) {
        setLoadingState("Criando sala '$roomName'...")

        lifecycleScope.launch {
            // Passamos roomName e rounds para serem registrados no NSD
            networkManager.registerService(roomName, rounds).collect { event ->
                handleNetworkEvent(event)
            }
        }
    }

    private fun startSearchingRooms() {
        // Limpa lista anterior
        roomAdapter.submitList(emptyList())

        lifecycleScope.launch {
            networkManager.discoverServices().collect { event ->
                when (event) {
                    is NetworkEvent.ServiceFound -> {
                        withContext(Dispatchers.Main) {
                            // Adiciona à lista do RecyclerView
                            roomAdapter.addService(event.serviceInfo)
                        }
                    }
                    else -> { /* Logs ou outros estados */ }
                }
            }
        }
    }

    private var selectedRounds: Int = 10 // Variável global na Activity para guardar o valor

    private fun onRoomSelected(serviceInfo: NsdServiceInfo) {
        selectedServiceInfo = serviceInfo

        // Ler os rounds dos metadados do serviço selecionado
        val roundsStr = serviceInfo.attributes["rounds"]?.let { String(it) }
        selectedRounds = roundsStr?.toIntOrNull() ?: 10

        btnStart.isEnabled = true
        val roomName = serviceInfo.attributes["room"]?.let { String(it) } ?: serviceInfo.serviceName
        btnStart.text = "CONECTAR: $roomName"
        btnStart.setBackgroundColor(getColor(android.R.color.holo_green_dark))
    }

    private fun connectToHost(serviceInfo: NsdServiceInfo) {
        setLoadingState("Conectando a ${serviceInfo.serviceName}...")

        lifecycleScope.launch {
            networkManager.connectToService(serviceInfo).collect { connectEvent ->
                handleNetworkEvent(connectEvent)
            }
        }
    }

    private fun startSoloMode(rounds: Int) {
        setLoadingState("Iniciando modo Solo ($rounds rounds)...")

        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)

            val allCharacters = Character.Companion.getDefaultCharacters()
            val shuffled = allCharacters.shuffled()

            val player1Cards = shuffled.take(8)
            val player2Cards = shuffled.drop(8).take(8)

            val currentPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = "Você",
                hand = player1Cards,
                isHost = true
            )

            val cpuPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = "CPU",
                hand = player2Cards,
                isHost = false
            )

            navigateToGame(currentPlayer, cpuPlayer, rounds)
        }
    }

    private fun setLoadingState(message: String) {
        layoutHost.visibility = View.GONE
        layoutJoin.visibility = View.GONE
        layoutSolo.visibility = View.GONE
        radioGroup.visibility = View.GONE
        btnStart.visibility = View.GONE

        tvStatus.visibility = View.VISIBLE
        progressBarMain.visibility = View.VISIBLE
        tvStatus.text = message
    }

    //  REDE

    private fun handleNetworkEvent(event: NetworkEvent) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (event) {
                is NetworkEvent.ServiceRegistered -> {
                    tvStatus.text = "Sala criada! Aguardando oponente..."
                }
                is NetworkEvent.WaitingForPlayer -> {
                    tvStatus.text = "Esperando alguém conectar..."
                }
                is NetworkEvent.Connected -> {
                    tvStatus.text = "Conectado! Sincronizando..."
                    progressBarMain.visibility = View.GONE
                    delay(1000)
                    startGameNetworked()
                }
                is NetworkEvent.Error -> {
                    progressBarMain.visibility = View.GONE
                    tvStatus.text = "Erro: ${event.message}"
                    Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_LONG).show()

                    delay(2000)
                    tvStatus.visibility = View.GONE
                    radioGroup.visibility = View.VISIBLE
                    updateUIState(radioGroup.checkedRadioButtonId)
                }
                else -> {}
            }
        }
    }

    private fun startGameNetworked() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allCharacters = Character.getDefaultCharacters()
            val shuffled = allCharacters.shuffled()
            val player1Cards = shuffled.take(8)
            val player2Cards = shuffled.drop(8).take(8)

            val currentPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = "Voce",
                hand = if (isHost) player1Cards else player2Cards,
                isHost = isHost
            )

            var opponentPlayer: Player? = null

            if (isHost) {
                val myJson = gson.toJson(currentPlayer)
                val sent = networkManager.sendMessage(myJson)

                if (sent) {
                    val opponentJson = networkManager.receiveMessage()
                    if (opponentJson != null) {
                        opponentPlayer = gson.fromJson(opponentJson, Player::class.java)
                    }
                }
            } else {
                val hostJson = networkManager.receiveMessage()
                if (hostJson != null) {
                    val hostPlayer = gson.fromJson(hostJson, Player::class.java)
                    opponentPlayer = hostPlayer
                    val myJson = gson.toJson(currentPlayer)
                    networkManager.sendMessage(myJson)
                }
            }

            if (opponentPlayer != null) {
                val finalRounds = if (isHost) {
                    etHostRounds.text.toString().toIntOrNull() ?: 10
                } else {
                    selectedRounds
                }

                navigateToGame(currentPlayer, opponentPlayer!!, finalRounds)
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erro na sincronização dos dados", Toast.LENGTH_LONG).show()
                    networkManager.disconnect()
                    tvStatus.visibility = View.GONE
                    radioGroup.visibility = View.VISIBLE
                    updateUIState(radioGroup.checkedRadioButtonId)
                }
            }
        }
    }

    private suspend fun navigateToGame(player: Player, opponent: Player, rounds: Int = 10) {
        withContext(Dispatchers.Main) {
            val intent = Intent(this@MainActivity, GameActivity::class.java)
            intent.putExtra("CURRENT_PLAYER", player)
            intent.putExtra("OPPONENT_PLAYER", opponent)
            intent.putExtra("MAX_ROUNDS", rounds)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkManager.disconnect()
    }

    inner class RoomAdapter(private val onClick: (NsdServiceInfo) -> Unit) :
        RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

        private val services = mutableListOf<NsdServiceInfo>()
        private var selectedPosition = -1

        fun submitList(list: List<NsdServiceInfo>) {
            services.clear()
            services.addAll(list)
            notifyDataSetChanged()
        }

        fun addService(service: NsdServiceInfo) {
            // Evita duplicados
            if (services.none { it.serviceName == service.serviceName }) {
                services.add(service)
                notifyItemInserted(services.size - 1)
            }
        }

        fun clearSelection() {
            selectedPosition = -1
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return RoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
            val service = services[position]
            val textView = holder.itemView.findViewById<TextView>(android.R.id.text1)

            // DECODIFICANDO OS ATRIBUTOS DO NSD
            // O Android < API 34 usa getAttributes que retorna Map<String, ByteArray>
            // Mas a classe NsdServiceInfo tem helpers diretos em versões novas ou legacy

            val roomName = try {
                service.attributes["room"]?.let { String(it) } ?: service.serviceName
            } catch (e: Exception) { service.serviceName }

            val rounds = try {
                service.attributes["rounds"]?.let { String(it) } ?: "?"
            } catch (e: Exception) { "?" }

            // Exibe: "Arena Suprema (10 Rounds)"
            textView.text = "$roomName ($rounds Rounds)"
            textView.setTextColor(getColor(android.R.color.white))

            holder.itemView.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onClick(service)
            }
        }

        override fun getItemCount() = services.size

        inner class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}