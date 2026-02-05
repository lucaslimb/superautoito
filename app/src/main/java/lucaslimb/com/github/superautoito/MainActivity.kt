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
import lucaslimb.com.github.superautoito.screens.GameTipsActivity
import lucaslimb.com.github.superautoito.screens.TeamSetupActivity
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
    private lateinit var btnConfigs: Button
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

        btnConfigs = findViewById(R.id.btn_configs)

        etRoomName.setText("${UUID.randomUUID() .toString().replace("-", "").take(8)}")
    }

    private fun setupListeners() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            updateUIState(checkedId)
        }

        btnStart.setOnClickListener {
            handleStartClick()
        }

        btnConfigs.setOnClickListener {
            val intent = Intent(this, GameTipsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateUIState(checkedId: Int) {
        layoutHost.visibility = View.GONE
        layoutJoin.visibility = View.GONE
        layoutSolo.visibility = View.GONE

        selectedServiceInfo = null
        roomAdapter.clearSelection()

        btnStart.isEnabled = true
        btnStart.setBackgroundColor(getColor(R.color.dark_red))
        btnStart.setTextColor(getColor(R.color.silver))

        when (checkedId) {
            R.id.radio_host -> {
                layoutHost.visibility = View.VISIBLE
                btnStart.text = getString(R.string.create_room)
            }
            R.id.radio_join -> {
                layoutJoin.visibility = View.VISIBLE
                btnStart.text = getString(R.string.connect)
                btnStart.isEnabled = false
                btnStart.setBackgroundColor(getColor(android.R.color.darker_gray))
                startSearchingRooms()
            }
            R.id.radio_solo -> {
                layoutSolo.visibility = View.VISIBLE
                btnStart.text = getString(R.string.play_solo)
            }
        }
    }

    private fun handleStartClick() {
        val checkedId = radioGroup.checkedRadioButtonId

        when (checkedId) {
            R.id.radio_host -> {
                val roomName = etRoomName.text.toString()
                val rounds = etHostRounds.text.toString().toIntOrNull() ?: 10

                if (rounds !in 1..20) {
                    Toast.makeText(this, getString(R.string.toast_invalid_rounds), Toast.LENGTH_SHORT).show()
                    return
                }

                if (roomName.isBlank()) {
                    Toast.makeText(this, getString(R.string.toast_enter_room_name), Toast.LENGTH_SHORT).show()
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
                var rounds = etSoloRounds.text.toString().toIntOrNull() ?: 10
                if (rounds !in 1..20) {
                    Toast.makeText(this, getString(R.string.toast_invalid_rounds), Toast.LENGTH_SHORT).show()
                    return
                }

                startSoloMode(rounds)
            }
        }
    }


    private fun startHosting(roomName: String, rounds: Int) {
        setLoadingState(getString(R.string.creating_room, roomName))

        lifecycleScope.launch {
            networkManager.registerService(roomName, rounds).collect { event ->
                handleNetworkEvent(event)
            }
        }
    }

    private fun startSearchingRooms() {
        roomAdapter.submitList(emptyList())

        lifecycleScope.launch {
            networkManager.discoverServices().collect { event ->
                when (event) {
                    is NetworkEvent.ServiceFound -> {
                        withContext(Dispatchers.Main) {
                            roomAdapter.addService(event.serviceInfo)
                        }
                    }
                    else -> { /* Logs ou outros estados */ }
                }
            }
        }
    }

    private var selectedRounds: Int = 10

    private fun onRoomSelected(serviceInfo: NsdServiceInfo) {
        selectedServiceInfo = serviceInfo
        val roundsStr = serviceInfo.attributes["rounds"]?.let { String(it) }
        selectedRounds = roundsStr?.toIntOrNull() ?: 10

        btnStart.isEnabled = true
        val roomName = serviceInfo.attributes["room"]?.let { String(it) } ?: serviceInfo.serviceName
        btnStart.text = getString(R.string.connect_room_format, roomName)
        btnStart.setBackgroundColor(getColor(android.R.color.holo_green_dark))
    }

    private fun connectToHost(serviceInfo: NsdServiceInfo) {
        setLoadingState(getString(R.string.connecting_to, serviceInfo.serviceName))

        lifecycleScope.launch {
            networkManager.connectToService(serviceInfo).collect { connectEvent ->
                handleNetworkEvent(connectEvent)
            }
        }
    }

    private fun startSoloMode(rounds: Int) {
        setLoadingState(getString(R.string.starting_solo, rounds))

        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)

            val allCharacters = Character.Companion.getDefaultCharacters(context = this@MainActivity)
            val shuffled = allCharacters.shuffled()

            val player1Cards = shuffled.take(8)
            val player2Cards = shuffled.drop(8).take(8)

            val currentPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = getString(R.string.player_you),
                hand = player1Cards,
                isHost = true
            )

            val cpuPlayer = Player(
                id = UUID.randomUUID().toString(),
                name = getString(R.string.player_cpu),
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
                    tvStatus.text = getString(R.string.room_created_waiting)
                }
                is NetworkEvent.WaitingForPlayer -> {
                    tvStatus.text = getString(R.string.waiting_for_player)
                }
                is NetworkEvent.Connected -> {
                    tvStatus.text = getString(R.string.connected_sync)
                    progressBarMain.visibility = View.GONE
                    delay(1000)
                    startGameNetworked()
                }
                is NetworkEvent.Error -> {
                    progressBarMain.visibility = View.GONE
                    tvStatus.text = getString(R.string.error_prefix, event.message)
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
            try {
                kotlinx.coroutines.withTimeout(20000L) {

                    if (isHost) {
                        // ===== HOST: CONTROLA TUDO =====
                        val allCharacters = Character.getDefaultCharacters(context = this@MainActivity)
                        val shuffled = allCharacters.shuffled()

                        val hostCards = shuffled.take(8)
                        val clientCards = shuffled.drop(8).take(8)

                        val hostPlayer = Player(
                            id = "host",
                            name = getString(R.string.player_you_no_accent),
                            hand = hostCards,
                            isHost = true
                        )

                        val clientPlayer = Player(
                            id = "client",
                            name = getString(R.string.opponent_name),
                            hand = clientCards,
                            isHost = false
                        )

                        val initialData = mapOf(
                            "myCards" to clientCards,
                            "opponentCards" to hostCards,
                            "rounds" to (etHostRounds.text.toString().toIntOrNull() ?: 10)
                        )

                        val dataJson = gson.toJson(initialData)
                        val sent = networkManager.sendMessage(dataJson)

                        if (!sent) throw Exception("Falha ao enviar dados iniciais")

                        val confirm = networkManager.receiveMessage()
                            ?: throw Exception("Cliente não confirmou")

                        val finalRounds = etHostRounds.text.toString().toIntOrNull() ?: 10
                        navigateToGame(hostPlayer, clientPlayer, finalRounds, isMultiplayer = true)

                    } else {
                        val dataJson = networkManager.receiveMessage()
                            ?: throw Exception("Não recebeu dados do host")

                        val data = gson.fromJson(dataJson, Map::class.java)
                        val myCardsJson = gson.toJson(data["myCards"])
                        val opponentCardsJson = gson.toJson(data["opponentCards"])
                        val rounds = (data["rounds"] as Double).toInt()

                        val myCards = gson.fromJson(myCardsJson, Array<Character>::class.java).toList()
                        val opponentCards = gson.fromJson(opponentCardsJson, Array<Character>::class.java).toList()

                        val myPlayer = Player(
                            id = "client",
                            name = getString(R.string.player_you_no_accent),
                            hand = myCards,
                            isHost = false
                        )

                        val hostPlayer = Player(
                            id = "host",
                            name = getString(R.string.opponent_name),
                            hand = opponentCards,
                            isHost = true
                        )

                        networkManager.sendMessage(gson.toJson(mapOf("status" to "ready")))

                        navigateToGame(myPlayer, hostPlayer, rounds, isMultiplayer = true)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = when(e) {
                        is kotlinx.coroutines.TimeoutCancellationException -> getString(R.string.error_timeout)
                        else -> getString(R.string.sync_error) + ": ${e.message}"
                    }

                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()

                    networkManager.disconnect()
                    tvStatus.visibility = View.GONE
                    radioGroup.visibility = View.VISIBLE
                    updateUIState(radioGroup.checkedRadioButtonId)
                }
            }
        }
    }

    private suspend fun navigateToGame(
        player: Player,
        opponent: Player,
        rounds: Int = 10,
        isMultiplayer: Boolean = false
    ) {
        withContext(Dispatchers.Main) {
            val intent = Intent(this@MainActivity, TeamSetupActivity::class.java)
            intent.putExtra("CURRENT_PLAYER", player)
            intent.putExtra("OPPONENT_PLAYER", opponent)
            intent.putExtra("MAX_ROUNDS", rounds)
            intent.putExtra("IS_MULTIPLAYER", isMultiplayer)
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

            val roomName = try {
                service.attributes["room"]?.let { String(it) } ?: service.serviceName
            } catch (e: Exception) { service.serviceName }

            val rounds = try {
                service.attributes["rounds"]?.let { String(it) } ?: "?"
            } catch (e: Exception) { "?" }

            textView.text = getString(R.string.room_list_item, roomName, rounds)
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
