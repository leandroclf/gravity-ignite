package br.com.trustsystems.gravity.server.httpserver.transform;

import br.com.trustsystems.gravity.core.worker.state.messages.*;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.server.transform.MqttIOTTransformer;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpIOTTransformerImpl implements MqttIOTTransformer<FullHttpMessage> {

    private static final Logger log = LoggerFactory.getLogger(HttpIOTTransformerImpl.class);

    @Override
    public IOTMessage toIOTMessage(FullHttpMessage serverMessage) {


        if (serverMessage instanceof FullHttpRequest) {

            FullHttpRequest request = (FullHttpRequest) serverMessage;
            final String content = request.content().toString(CharsetUtil.UTF_8);

            //add jsonSchema to validate request

            final JSONObject json = new JSONObject(content);

            log.debug(" toIOTMessage : received content {} ", content);


            final String path = request.uri().toUpperCase();

            switch (path) {

                case "/CONNECT":

                    boolean isAnnonymousConnect = (!json.has("username") && !json.has("password"));


                    return ConnectMessage.from(
                            false, 1, false,
                            "MQTT", 4, false, isAnnonymousConnect, json.getString("clientId"),
                            json.has("username") ? json.getString("username") : "",
                            json.has("password") ? json.getString("password") : "",
                            0, "");

                case "/PUBLISH":

                    ByteBuffer byteBuffer = ByteBuffer.wrap(json.getString("payload").getBytes());

                    PublishMessage publishMessage = PublishMessage.from(json.getLong("messageId"), false, 1,
                            json.getBoolean("retain"), json.getString("topic"), byteBuffer, true);

                    publishMessage.setSessionId(json.getString("sessionId"));
//                    publishMessage.setPartition(json.getString("partition"));
                    publishMessage.setAuthKey(json.getString("authKey"));
                    return publishMessage;


                case "/SUBSCRIBE":


                    SubscribeMessage subscribeMessage = SubscribeMessage.from(1, false, 1, false);

                    JSONArray jsonTopicQosList = json.getJSONArray("topicQosList");
                    for (int i = 0; i < jsonTopicQosList.length(); i++) {
                        JSONObject topicQos = jsonTopicQosList.getJSONObject(i);

                        String topic = topicQos.keys().next();
                        int qos = topicQos.getInt(topic);

                        Map.Entry<String, Integer> entry =
                                new AbstractMap.SimpleEntry<>(topic, qos);
                        subscribeMessage.getTopicFilterList().add(entry);
                    }
                    subscribeMessage.setReceptionUrl(json.getString("recipientUrl"));
                    subscribeMessage.setSessionId(json.getString("sessionId"));
                    subscribeMessage.setAuthKey(json.getString("authKey"));


                    return subscribeMessage;

                case "/UNSUBSCRIBE":

                    List<String> topicList = new ArrayList<>();
                    JSONArray jsonTopicList = json.getJSONArray("topicQosList");
                    for (int i = 0; i < jsonTopicList.length(); i++) {
                        String topic = jsonTopicList.getString(i);

                        topicList.add(topic);
                    }

                    UnSubscribeMessage unSubscribeMessage = UnSubscribeMessage.from(1, false, 1, false, topicList);
                    unSubscribeMessage.setSessionId(json.getString("sessionId"));
                    unSubscribeMessage.setAuthKey(json.getString("authKey"));

                case "/DISCONNECT":

                    DisconnectMessage disconMessage = DisconnectMessage.from(false);
                    disconMessage.setSessionId(json.getString("sessionId"));
                    disconMessage.setAuthKey(json.getString("authKey"));

                    return disconMessage;

                default:
                    return null;
            }


        }

        return null;
    }


}
