package com.rodemtree.chatservice.constant;

public class MessageType {
    public static final String FETCH_USER_INVITE_CODE_REQUEST = "FETCH_USER_INVITE_CODE_REQUEST";
    public static final String FETCH_USER_INVITE_CODE_RESPONSE = "FETCH_USER_INVITE_CODE_RESPONSE";
    public static final String FETCH_CONNECTIONS_REQUEST = "FETCH_CONNECTIONS_REQUEST";
    public static final String FETCH_CONNECTIONS_RESPONSE = "FETCH_CONNECTIONS_RESPONSE";
    public static final String INVITE_REQUEST = "INVITE_REQUEST";
    public static final String INVITE_RESPONSE = "INVITE_RESPONSE";
    public static final String ACCEPT_INVITE_REQUEST = "ACCEPT_INVITE_REQUEST";
    public static final String ACCEPT_INVITE_RESPONSE = "ACCEPT_INVITE_RESPONSE";
    public static final String REJECT_INVITE_REQUEST = "REJECT_INVITE_REQUEST";
    public static final String REJECT_INVITE_RESPONSE = "REJECT_INVITE_RESPONSE";
    public static final String DISCONNECT_REQUEST = "DISCONNECT_REQUEST";
    public static final String DISCONNECT_RESPONSE = "DISCONNECT_RESPONSE";
    public static final String CREATE_CHANNEL_REQUEST = "CREATE_CHANNEL_REQUEST";
    public static final String CREATE_CHANNEL_RESPONSE = "CREATE_CHANNEL_RESPONSE";
    public static final String ENTER_CHANNEL_REQUEST = "ENTER_CHANNEL_REQUEST";
    public static final String ENTER_CHANNEL_RESPONSE = "ENTER_CHANNEL_RESPONSE";
    public static final String WRITE_MESSAGE = "WRITE_MESSAGE";

    // Notification
    public static final String ASK_INVITE = "ASK_INVITE";
    public static final String NOTIFY_ACCEPT_INVITE = "NOTIFY_ACCEPT_INVITE";
    public static final String NOTIFY_JOIN_CHANNEL = "NOTIFY_JOIN_CHANNEL";
    public static final String NOTIFY_MESSAGE = "NOTIFY_MESSAGE";
    public static final String KEEP_ALIVE = "KEEP_ALIVE";
    public static final String ERROR = "ERROR";
}
