package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MqttWeatherActor extends AbstractBehavior<MqttWeatherActor.MqttCommand> {

    public interface MqttCommand {
    }

    public static class MessageReceived implements MqttCommand {
        public final String topic;
        public final String message;

        public MessageReceived(String topic, String message) {
            this.topic = topic;
            this.message = message;
        }
    }

    private final String broker = "tcp://10.0.40.161:1883";
    private final List<String> topics = Arrays.asList("weather/temperature", "weather/condition");
    private MqttClient client;
    private final ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> envController;


    public static Behavior<MqttCommand> create(ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> envController) {
        return Behaviors.setup(context -> new MqttWeatherActor(context, envController));
    }

    private MqttWeatherActor(ActorContext<MqttCommand> context,
                           ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> envController) {
        super(context);
        this.envController = envController;
        startMqttClient();
    }

    private void startMqttClient() {
        try {
            client = new MqttClient(broker,"environmentActor");

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    getContext().getLog().error("MQTT Connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    String payload = new String(mqttMessage.getPayload());
                    getContext().getSelf().tell(new MessageReceived(topic, payload));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not needed for subscribers
                }
            });

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            // Connect with verification
            IMqttToken connectToken = client.connectWithResult(options);
            connectToken.waitForCompletion();
            if (connectToken.getException() != null) {
                throw connectToken.getException();
            }
            getContext().getLog().info("Verified connection to broker: {}", broker);

            for (String topic : topics) {
                IMqttToken subToken = client.subscribeWithResponse(topic);
                subToken.waitForCompletion();
                if (subToken.getException() != null) {
                    getContext().getLog().error("Failed to subscribe to {}", topic, subToken.getException());
                } else {
                    getContext().getLog().info("Verified subscription to {}", topic);
                }
            }
        } catch (MqttException e) {
            getContext().getLog().error("Error starting MQTT client", e);
        }
    }

    @Override
    public Receive<MqttCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MessageReceived.class, this::onMessageReceived)
                .build();
    }

    private Behavior<MqttCommand> onMessageReceived(MessageReceived message) {
        switch (message.topic) {
            case "weather/temperature":
                JSONObject jsonMessage = new JSONObject(message.message);
                double temperature = Double.parseDouble(jsonMessage.getString("temperature"));
//                envController.tell(new WeatherEnvironmentActor.ExternalTemperatureChanged(temperature));
                break;

            case "weather/condition":
                WeatherTypes weatherType = parseWeather(message.message);
                envController.tell(new WeatherEnvironmentActor.ExternalWeatherUpdate(weatherType));
                break;

            default:
                getContext().getLog().info("unrecognized message to topic '{}' received: {}", message.topic, message.message);
        }
        return this;
    }

    private WeatherTypes parseWeather(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            String conditionStr = json.getString("condition").toUpperCase();
            return WeatherTypes.valueOf(conditionStr);
        } catch (Exception e) {
            getContext().getLog().error("Failed to parse weather condition from MQTT payload: {}", payload, e);
            return null;
        }
    }

}