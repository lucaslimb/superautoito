package lucaslimb.com.github.superautoito.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

class NetworkManager(private val context: Context) {

    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    companion object {
        // Nome do serviço deve ser único e seguir padrão DNS-SD
        private const val SERVICE_TYPE = "_superautoito._tcp"
        private const val TAG = "NetworkManager"
    }

    // AGORA RECEBE roomName e rounds
    fun registerService(roomName: String, rounds: Int): Flow<NetworkEvent> = callbackFlow {
        try {
            // 0 = O Sistema Operacional escolhe uma porta livre automaticamente
            serverSocket = ServerSocket(0)
            val assignedPort = serverSocket!!.localPort

            val serviceInfo = NsdServiceInfo().apply {
                // Nome do serviço na rede (deve ser único, o android pode adicionar sufixos se repetir)
                serviceName = "Ito-$roomName"
                serviceType = SERVICE_TYPE
                port = assignedPort

                // ATRIBUTOS EXTRAS (A mágica acontece aqui)
                // Enviamos o nome da sala e rounds via TXT Record do DNS-SD
                setAttribute("room", roomName)
                setAttribute("rounds", rounds.toString())
            }

            val registrationListener = object : NsdManager.RegistrationListener {
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                    trySend(NetworkEvent.Error("Falha registro NSD: $errorCode"))
                }

                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                    // Ignorar erros de unregister ao fechar app
                }

                override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                    trySend(NetworkEvent.ServiceRegistered)
                    Log.d(TAG, "Serviço registrado na porta $assignedPort")

                    // Thread dedicada para aceitar conexão (Blocking Call)
                    Thread {
                        try {
                            trySend(NetworkEvent.WaitingForPlayer)
                            // Bloqueia até alguém conectar
                            val socket = serverSocket?.accept()

                            if (socket != null) {
                                clientSocket = socket
                                setupStreams(socket)
                                trySend(NetworkEvent.Connected)
                            }
                        } catch (e: Exception) {
                            if (!channel.isClosedForSend) {
                                trySend(NetworkEvent.Error("Erro socket host: ${e.message}"))
                            }
                        }
                    }.start()
                }

                override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {}
            }

            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)

        } catch (e: Exception) {
            trySend(NetworkEvent.Error("Erro server socket: ${e.message}"))
        }

        awaitClose { disconnect() }
    }

    fun discoverServices(): Flow<NetworkEvent> = callbackFlow {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                trySend(NetworkEvent.Error("Falha discovery: $errorCode"))
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {}
            override fun onDiscoveryStarted(serviceType: String?) { trySend(NetworkEvent.Searching) }
            override fun onDiscoveryStopped(serviceType: String?) {}

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                // Filtra para garantir que é o nosso jogo
                if (serviceInfo?.serviceType?.contains("_superautoito") == true) {
                    // Precisamos RESOLVER o serviço para pegar os Atributos (Rounds/RoomName)
                    // Discovery puro só dá o nome e tipo.
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            Log.e(TAG, "Resolve falhou na busca: $errorCode")
                        }

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                            resolvedInfo?.let {
                                trySend(NetworkEvent.ServiceFound(it))
                            }
                        }
                    })
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {}
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        awaitClose {
            try { nsdManager.stopServiceDiscovery(discoveryListener) } catch (e: Exception) {}
        }
    }

    fun connectToService(serviceInfo: NsdServiceInfo): Flow<NetworkEvent> = callbackFlow {
        Thread {
            try {
                // Usa o host e porta resolvidos pelo NSD
                val socket = Socket(serviceInfo.host, serviceInfo.port)
                clientSocket = socket
                setupStreams(socket)
                trySend(NetworkEvent.Connected)
            } catch (e: Exception) {
                trySend(NetworkEvent.Error("Falha conexão cliente: ${e.message}"))
            }
        }.start()

        awaitClose { disconnect() }
    }

    private fun setupStreams(socket: Socket) {
        // AutoFlush = true é crucial para enviar imediatamente
        writer = PrintWriter(socket.getOutputStream(), true)
        reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
    }

    // TRANSFORMADO EM SUSPEND para rodar em IO
    suspend fun sendMessage(message: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (writer != null && !clientSocket!!.isClosed) {
                writer?.println(message)
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Erro envio", e)
            return@withContext false
        }
    }

    // TRANSFORMADO EM SUSPEND e trata nulos
    suspend fun receiveMessage(): String? = withContext(Dispatchers.IO) {
        try {
            return@withContext reader?.readLine()
        } catch (e: Exception) {
            Log.e(TAG, "Erro recebimento", e)
            return@withContext null
        }
    }

    fun disconnect() {
        try {
            nsdManager.unregisterService(object : NsdManager.RegistrationListener { // Dummy listener para unregister
                override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {}
                override fun onUnregistrationFailed(p0: NsdServiceInfo?, p1: Int) {}
                override fun onServiceRegistered(p0: NsdServiceInfo?) {}
                override fun onServiceUnregistered(p0: NsdServiceInfo?) {}
            })
        } catch (e: Exception) { /* Já estava sem registro */ }

        try {
            writer?.close()
            reader?.close()
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) { }
    }
}

sealed class NetworkEvent {
    object ServiceRegistered : NetworkEvent()
    object WaitingForPlayer : NetworkEvent()
    object Searching : NetworkEvent()
    data class ServiceFound(val serviceInfo: NsdServiceInfo) : NetworkEvent()
    object Connected : NetworkEvent()
    data class Error(val message: String) : NetworkEvent()
}