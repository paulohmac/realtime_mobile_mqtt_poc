package br.com.mqtttest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment()  {
    private lateinit var mqttAndroidClient: MqttAndroidClient



    companion object {
        const val TAG = "AndroidMqttClient"
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.connectBtn).setOnClickListener {
            this.context?.let { it1 -> connect(it1) }
        }

        view.findViewById<Button>(R.id.topicBtn).setOnClickListener {
            subscribe( view?.findViewById<EditText>(R.id.topicTxt).text.toString()  as String)
            receiveMessages()
        }

        view.findViewById<Button>(R.id.sendBtn).setOnClickListener {
            publish(view?.findViewById<EditText>(R.id.topicTxt).text.toString(), view?.findViewById<EditText>(R.id.enviarTxt).text.toString())
        }


    }



    fun connect(applicationContext : Context) {
        mqttAndroidClient = MqttAndroidClient (context?.applicationContext,"tcp://172.28.144.1:1883","javali" )
        try {
            val token = mqttAndroidClient.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken)                        {
                    val e = Log.e("Connection", "success ")
                    Toast.makeText(context, "Connectedo!", Toast.LENGTH_SHORT).show()
                    //connectionStatus = true
                    // Give your callback on connection established here
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //connectionStatus = false
                    Toast.makeText(context, "Erro de conexão!", Toast.LENGTH_SHORT).show()
                    Log.e("Connection", "failure")
                    // Give your callback on connection failure here
                    exception.printStackTrace()
                }
            }
        } catch (e: MqttException) {
            // Give your callback on connection failure here
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String) {
        val qos = 2 // Mention your qos value
        try {
            mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // Give your callback on Subscription here
                    Toast.makeText(context, "Topico assinado!", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                ) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: MqttException) {
            // Give your subscription failure callback here
        }

        fun unSubscribe(topic: String) {
            try {
                val unsubToken = mqttAndroidClient.unsubscribe(topic)
                unsubToken.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        // Give your callback on unsubscribing here
                        Toast.makeText(context, "Removido assinatura com sucesso", Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Toast.makeText(context, "Erro ao remover assinatura", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: MqttException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()

                // Give your callback on failure here
            }
        }
    }


    fun receiveMessages() {
        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                //connectionStatus = false
                // Give your callback on failure here
            }
            override fun messageArrived(topic: String, message: MqttMessage) {
                try {
                    val data = String(message.payload, charset("UTF-8"))
                    view?.findViewById<TextView>(R.id.receivedTxt)?.setText(data)
                    Log.e("asda", data)
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                // Acknowledgement on delivery complete
            }
        })
    }

    fun publish(topic: String, data: String) {
        val encodedPayload : ByteArray
        try {
            encodedPayload = data.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            message.qos = 2
            message.isRetained = false
            mqttAndroidClient.publish(topic, message)
            Toast.makeText(context, "Mensagem enviada com sucesso", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            // Give Callback on error here
        } catch (e: MqttException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            // Give Callback on error here
        }


        fun disconnect() {
            try {
                val disconToken = mqttAndroidClient.disconnect()
                disconToken.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        //connectionStatus = false
                        // Give Callback on disconnection here
                    }
                    override fun onFailure(
                            asyncActionToken: IMqttToken,
                            exception: Throwable
                    ) {
                        Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: MqttException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


}


