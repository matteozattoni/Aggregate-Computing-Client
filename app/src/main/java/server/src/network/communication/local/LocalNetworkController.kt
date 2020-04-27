package server.src.network.communication.local

import communication.Message
import communication.MessageType
import server.src.devices.PeerDevice
import server.src.network.serverSupport
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

const val SERVER_PORT = 2002
val LOOPBACK_ADDRESS: InetAddress = InetAddress.getLoopbackAddress()

class LocalNetworkController(val server: PeerDevice? = serverSupport) {


    // connect to lookup server socket, instance the serverPeer, subscribe to server
    // and call the server Ready, this is called by the client
    fun getRemoteServer(onServerReady: (PeerDevice) -> Unit, onMessage: (Message) -> Unit) {
        val serverPeer = PeerDevice(null, -1, "server")
        val localCommunication = LocalClientCommunication(
            serverPeer, InetSocketAddress(
                LOOPBACK_ADDRESS, SERVER_PORT
            )
        )
        serverPeer.networkDevice = localCommunication
        try {
            localCommunication.connect(onReceive = {
                while (it.isConnected) {
                    val message = localCommunication.extractMessage(it)
                    onMessage(message)
                }
            }) // can trow exception
            onServerReady(serverPeer)
        } catch (error: Exception) {
            // cant connect to server, try again or abort
        }

    }

    // When the network found a neighbour server to send a Join and wait for message
    // the support doesn't add the peer
    fun offerServiceToNeighbourServer(address: InetAddress, port: Int) {
        thread {
            try {
                val connection = Socket(address, port)
                val joinSendMessage = Message(-1, MessageType.Join, serverSupport.uuid)
                val outputStream = ObjectOutputStream(connection.getOutputStream())
                outputStream.writeObject(joinSendMessage)
                val objectInput = ObjectInputStream(connection.getInputStream())
                while (connection.isConnected) {
                    val receiveMessage = objectInput.readObject() as Message
                    server!!.tell(receiveMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


}