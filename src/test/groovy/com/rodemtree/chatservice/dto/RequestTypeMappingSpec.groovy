package com.rodemtree.chatservice.dto


import com.rodemtree.chatservice.constant.UserConnectionStatus
import com.rodemtree.chatservice.dto.kafka.*
import com.rodemtree.chatservice.util.JsonUtil
import spock.lang.Specification

class RequestTypeMappingSpec extends Specification {

    JsonUtil jsonUtil = new JsonUtil();

    def "DTO 형식의 JSON 문자열을 해당 타입의 DTO로 변환할 수 있다."() {
        given:
        String jsonBody = payload

        when:
        RecordInterface record = jsonUtil.fromJson(jsonBody, RecordInterface).get()

        then:
        record.getClass() == expectedClass
        validate(record)

        where:
        payload                                                               | expectedClass                    | validate
        '{"type": "FETCH_USER_INVITE_CODE_REQUEST"}'                          | FetchUserInviteCodeRequestRecord | { req -> (req as FetchUserInviteCodeRequestRecord).type() == 'FETCH_USER_INVITE_CODE_REQUEST' }
        '{"type": "FETCH_CONNECTIONS_REQUEST", "status": "PENDING"}'          | FetchConnectionsRequestRecord    | { req -> (req as FetchConnectionsRequestRecord).status() == UserConnectionStatus.PENDING }
        '{"type": "INVITE_REQUEST", "userInviteCode": "TestInviteCode123"}'   | InviteRequestRecord              | { req -> (req as InviteRequestRecord).userInviteCode().code() == 'TestInviteCode123' }
        '{"type": "ACCEPT_INVITE_REQUEST", "username": "testuser"}'           | AcceptInviteRequestRecord        | { req -> (req as AcceptInviteRequestRecord).username() == 'testuser' }
        '{"type": "REJECT_INVITE_REQUEST", "username": "testuser"}'           | RejectInviteRequestRecord        | { req -> (req as RejectInviteRequestRecord).username() == 'testuser' }
        '{"type": "DISCONNECT_REQUEST", "username": "testuser"}'              | DisconnectRequestRecord          | { req -> (req as DisconnectRequestRecord).username() == 'testuser' }
        '{"type": "WRITE_MESSAGE", "channelId": 1, "content":"test message"}' | WriteMessageRecord               | { req -> (req as WriteMessageRecord).content() == 'test message' }
    }
}
