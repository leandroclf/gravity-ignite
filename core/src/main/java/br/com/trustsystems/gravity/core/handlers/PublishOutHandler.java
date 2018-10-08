package br.com.trustsystems.gravity.core.handlers;


import br.com.trustsystems.gravity.core.worker.state.messages.AcknowledgeMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import io.netty.handler.codec.mqtt.MqttQoS;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class PublishOutHandler extends RequestHandler<PublishMessage> {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private String protocalData;

    public PublishOutHandler(PublishMessage message, String protocalData) {
        super(message);
        this.protocalData = protocalData;
    }

    @Override
    public void handle() throws RetriableException, UnRetriableException {

        log.debug(" handle : outbound message {} being processed", getMessage());

        if (getMessage().getProtocol().isPersistent()) {

            //We need to generate a publish message to start this conversation.
            pushToServer(getMessage());

        } else {
            switch (getMessage().getProtocol()) {

                case HTTP:
                    httpPushToUrl(protocalData, getMessage());
                    break;
                default:
                    log.error(" handle : outbound message {} using none implemented protocal");
            }
        }
    }

    private void httpPushToUrl(String url, PublishMessage publishMessage) {


       ByteBuffer payloadBuffer = ByteBuffer.wrap((byte[]) publishMessage.getPayload());

        String payload = UTF8.decode(payloadBuffer).toString();


        MultipartBody httpMessage = Unirest.post(url)
                .header("accept", "application/json")
                .field("topic", publishMessage.getTopic())
                .field("message", payload);

        if (MqttQoS.AT_LEAST_ONCE.value() == publishMessage.getQos()) {

            httpMessage.asJsonAsync(new Callback<JsonNode>() {

                public void failed(UnirestException e) {
                    log.info(" httpPushToUrl failed : problems calling service", e);
                }

                public void completed(HttpResponse<JsonNode> response) {
                    int code = response.getStatus();

                    JsonNode responseBody = response.getBody();
                    log.info(" httpPushToUrl completed : external server responded with {}", responseBody);
                    if (200 == code) {

                        AcknowledgeMessage ackMessage = AcknowledgeMessage.from(publishMessage.getMessageId());
                        ackMessage.copyBase(publishMessage);

                        PublishAcknowledgeHandler publishAcknowledgeHandler = new PublishAcknowledgeHandler(ackMessage);
                        try {
                            publishAcknowledgeHandler.handle();
                        } catch (RetriableException | UnRetriableException e) {
                            log.warn(" httpPushToUrl completed : problem closing connection. ");
                        }
                    }
                }

                public void cancelled() {
                    log.info(" httpPushToUrl cancelled : request cancelled.");
                }

            });
        } else {
            httpMessage.asJsonAsync();
        }

    }
}
