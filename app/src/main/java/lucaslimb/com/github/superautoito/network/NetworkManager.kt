package lucaslimb.com.github.superautoito.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class NetworkManager(private val context: Context) {

    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    companion object {
        private const val SERVICE_TYPE = "_junjiitopets._tcp"
        private const val SERVICE_NAME = "JunjiItoPetsGame"
        private const val TAG = "NetworkManager"
        const val PORT = 8888
    }

    fun registerService(playerName: String): Flow<NetworkEvent> = callbackFlow {
        try {
            serverSocket = ServerSocket(PORT)

            val serviceInfo = NsdServiceInfo().apply {
                serviceName = "$SERVICE_NAME-$playerName"
                serviceType = SERVICE_TYPE
                port = PORT
            }

            val registrationListener = object : NsdManager.RegistrationListener {
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                    Log.e(TAG, "Service registration failed: $errorCode")
                    trySend(NetworkEvent.Error("Falha ao registrar serviço"))
                }

                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                    Log.e(TAG, "Service unregistration failed: $errorCode")
                }

                override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                    Log.d(TAG, "Service registered: ${serviceInfo?.serviceName}")
                    trySend(NetworkEvent.ServiceRegistered)

                    // Aguardar conexão de cliente
                    Thread {
                        try {
                            trySend(NetworkEvent.WaitingForPlayer)
                            clientSocket = serverSocket?.accept()

                            clientSocket?.let { socket ->
                                writer = PrintWriter(socket.getOutputStream(), true)
                                reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                                trySend(NetworkEvent.Connected)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error accepting connection", e)
                            trySend(NetworkEvent.Error("Erro ao aceitar conexão: ${e.message}"))
                        }
                    }.start()
                }

                override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
                    Log.d(TAG, "Service unregistered")
                }
            }

            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering service", e)
            trySend(NetworkEvent.Error("Erro ao criar servidor: ${e.message}"))
        }

        awaitClose {
            disconnect()
        }
    }

    fun discoverServices(): Flow<NetworkEvent> = callbackFlow {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: $errorCode")
                trySend(NetworkEvent.Error("Falha ao iniciar busca"))
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: $errorCode")
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.d(TAG, "Discovery started")
                trySend(NetworkEvent.Searching)
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.d(TAG, "Discovery stopped")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                Log.d(TAG, "Service found: ${serviceInfo?.serviceName}")
                serviceInfo?.let {
                    trySend(NetworkEvent.ServiceFound(it))
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                Log.d(TAG, "Service lost: ${serviceInfo?.serviceName}")
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping discovery", e)
            }
        }
    }

    fun connectToService(serviceInfo: NsdServiceInfo): Flow<NetworkEvent> = callbackFlow {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.e(TAG, "Resolve failed: $errorCode")
                trySend(NetworkEvent.Error("Falha ao resolver serviço"))
            }

            override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                Log.d(TAG, "Service resolved: ${resolvedInfo?.serviceName}")

                resolvedInfo?.let { info ->
                    Thread {
                        try {
                            clientSocket = Socket(info.host, info.port)

                            clientSocket?.let { socket ->
                                writer = PrintWriter(socket.getOutputStream(), true)
                                reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                                trySend(NetworkEvent.Connected)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error connecting to service", e)
                            trySend(NetworkEvent.Error("Erro ao conectar: ${e.message}"))
                        }
                    }.start()
                }
            }
        }

        nsdManager.resolveService(serviceInfo, resolveListener)

        awaitClose {
            disconnect()
        }
    }

    fun sendMessage(message: String): Boolean {
        return try {
            writer?.println(message)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            false
        }
    }

    fun receiveMessage(): String? {
        return try {
            reader?.readLine()
        } catch (e: Exception) {
            Log.e(TAG, "Error receiving message", e)
            null
        }
    }

    fun disconnect() {
        try {
            writer?.close()
            reader?.close()
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
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