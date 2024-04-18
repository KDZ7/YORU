import android.util.Log
import com.example.yoru.ValueStatic
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import java.util.UUID
import java.util.concurrent.TimeUnit

class Mqtt5Handler(
    broker: String,
    port: Int,
    username: String,
    password: String,
) {

    private val mqtt5Client: Mqtt5AsyncClient
    private val TAG = "MQTT"

    init {
        mqtt5Client = MqttClient.builder()
            .useMqttVersion5()
            .identifier(UUID.randomUUID().toString())
            .serverHost(broker)
            .serverPort(port)
            .sslWithDefaultConfig()
            .automaticReconnect()
            .initialDelay(500, TimeUnit.MILLISECONDS)
            .maxDelay(1, TimeUnit.MINUTES)
            .applyAutomaticReconnect()
            .simpleAuth()
            .username(username)
            .password(password.toByteArray())
            .applySimpleAuth()
            .buildAsync()

        connect()
    }

    private fun connect() {
        mqtt5Client.connect()
            .whenComplete { ack, throwable ->
                if (throwable != null) {
                    Log.d(TAG, "Connection failed: ${throwable.message}")
                } else {
                    Log.d(TAG, "Connected to MQTT broker")
                    subscribe(ValueStatic.topic)
                }
            }
    }

    fun sendMessage(topic: String, message: String) {
        val messagePublish = Mqtt5Publish.builder()
            .topic(topic)
            .payload(message.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .build()

        mqtt5Client.publish(messagePublish).whenComplete { ack, throwable ->
            if (throwable != null) {
                Log.d(TAG, "Failed to send message: ${throwable.message}")
            } else {
                Log.d(TAG, "Message sent to ${topic}")
            }
        }
    }

    fun subscribe(topic: String = ValueStatic.topic) {
        mqtt5Client.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { publish ->
                Log.d(
                    TAG,
                    "Received message: ${String(publish.payloadAsBytes)} on topic: ${publish.topic}"
                )
                ValueStatic.tmpInfo += String(publish.payloadAsBytes) + "\n"
                if (ValueStatic.tmpInfo.length > 5000) {
                    ValueStatic.tmpInfo = ""
                }
            }
            .send()
            .whenComplete { ack, throwable ->
                if (throwable != null) {
                    Log.d(TAG, "Failed to subscribe: ${throwable.message}")
                } else {
                    Log.d(TAG, "Subscribed to ${topic}")
                }
            }
    }
}
