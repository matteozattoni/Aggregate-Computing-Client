package server.devices

import adapters.Adapter
import communication.Message
import communication.MessageType
import devices.interfaces.EmulatedDevice
import server.Execution
import server.network.communication.NetworkCommunication
import server.network.ServerSupport
import java.io.Serializable

class LocalExecutionPeerDevice(
    id: Int,
    name: String,
    adapterBuilder: (EmulatedDevice) -> Adapter = Execution.adapter
) :
    EmulatedDevice(id, name, adapterBuilder, onResult = ::println) {

    private var networkDevice: NetworkCommunication? = null

    fun setPhysicalDevice(networkCommunication: NetworkCommunication) {
        networkDevice = networkCommunication
    }

    override fun showResult(result: Serializable) {
        networkDevice?.send(Message(id, MessageType.Show, result))
    }

    override fun tell(message: Message) {
        super.tell(message)
        when (message.type) {
            MessageType.Result, MessageType.Show -> networkDevice?.send(message)
            MessageType.LeaveLightWeight -> goFullWeight()
            else -> {
            }
        }
    }

    private fun goFullWeight() {
        ServerSupport.replaceHosted(
            this, PeerDevice(
                ServerSupport.uuid, id, name, networkDevice
            )
        )
    }
}