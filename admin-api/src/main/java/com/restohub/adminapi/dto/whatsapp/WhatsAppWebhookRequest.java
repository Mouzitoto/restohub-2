package com.restohub.adminapi.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookRequest {
    
    // Green API формат
    private String typeWebhook; // incomingMessageReceived, outgoingMessageStatus, etc.
    private IncomingMessage incomingMessage;
    private OutgoingMessageStatus outgoingMessageStatus;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IncomingMessage {
        private String typeMessage; // textMessage, buttonMessage, etc.
        private String idMessage;
        private Long timestamp;
        private String typeData;
        private TextMessageData textMessageData;
        private ButtonMessageData buttonMessageData;
        private String chatId; // номер телефона отправителя
        private String senderId; // номер телефона отправителя
        private String senderName;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextMessageData {
        private String textMessage;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonMessageData {
        private String selectedButtonId; // ID выбранной кнопки
        private String selectedButtonText; // текст выбранной кнопки
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutgoingMessageStatus {
        private String idMessage;
        private String status; // sent, delivered, read, etc.
        private Long timestamp;
        private String chatId;
    }
    
    // Twilio формат (если будет использоваться)
    private String MessageSid;
    private String From; // номер отправителя
    private String To; // номер получателя
    private String Body; // текст сообщения
    private String ButtonText; // текст кнопки (для callback)
    private String ButtonPayload; // payload кнопки
    
    // Meta WhatsApp Business API формат
    private String object; // "whatsapp_business_account"
    private java.util.List<Entry> entry;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String id;
        private java.util.List<Change> changes;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private Value value;
        private String field; // "messages"
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        private String messaging_product; // "whatsapp"
        private java.util.List<MetaMessage> messages;
        private java.util.List<Status> statuses;
        private java.util.List<Contact> contacts;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaMessage {
        private String from; // номер отправителя
        private String id; // ID сообщения
        private Long timestamp;
        private Text text;
        private Interactive interactive;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Text {
        private String body;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interactive {
        @JsonProperty("button_reply")
        private ButtonReply buttonReply;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonReply {
        private String id; // ID кнопки
        private String title; // текст кнопки
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String id; // ID сообщения
        private String status; // sent, delivered, read, failed
        private String timestamp;
        private String recipient_id;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        private Profile profile;
        private String wa_id; // номер телефона
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String name;
    }
}

