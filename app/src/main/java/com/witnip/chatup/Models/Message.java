package com.witnip.chatup.Models;

public class Message {
    private String messageID, message, senderID;
    private long timestamp;
    private int feeling = -1;

    public Message() {
    }

    public Message(String messageID, String message, String senderID, long timestamp, int feeling) {
        this.messageID = messageID;
        this.message = message;
        this.senderID = senderID;
        this.timestamp = timestamp;
        this.feeling = feeling;
    }

    public Message(String message, String senderID, long timestamp) {
        this.message = message;
        this.senderID = senderID;
        this.timestamp = timestamp;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }
}
