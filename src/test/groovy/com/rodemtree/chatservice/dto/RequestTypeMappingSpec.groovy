package com.rodemtree.chatservice.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.rodemtree.chatservice.constant.UserConnectionStatus
import com.rodemtree.chatservice.dto.websocket.inbound.AcceptRequest
import com.rodemtree.chatservice.dto.websocket.inbound.BaseRequest
import com.rodemtree.chatservice.dto.websocket.inbound.DisconnectRequest
import com.rodemtree.chatservice.dto.websocket.inbound.FetchConnectionsRequest
import com.rodemtree.chatservice.dto.websocket.inbound.FetchUserInviteCodeRequest
import com.rodemtree.chatservice.dto.websocket.inbound.InviteRequest
import com.rodemtree.chatservice.dto.websocket.inbound.KeepAliveRequest
import com.rodemtree.chatservice.dto.websocket.inbound.RejectInviteRequest
import com.rodemtree.chatservice.dto.websocket.inbound.WriteMessageRequest
import com.rodemtree.chatservice.util.JsonUtil
import spock.lang.Specification


class RequestTypeMappingSpec extends Specification {

    JsonUtil jsonUtil = new JsonUtil(new ObjectMapper());

    def "DTO 형식의 JSON 문자열을 해당 타입의 DTO로 변환할 수 있다."() {
        given:
        String jsonBody = payload

        when:
        BaseRequest request = jsonUtil.fromJson(jsonBody, BaseRequest).get()

        then:
        request.getClass() == expectedClass
        validate(request)

        where:
        payload                                                                       | expectedClass              | validate
        '{"type": "FETCH_USER_INVITE_CODE_REQUEST"}'                                  | FetchUserInviteCodeRequest | { req -> (req as FetchUserInviteCodeRequest).getType() == 'FETCH_USER_INVITE_CODE_REQUEST' }
        '{"type": "FETCH_CONNECTIONS_REQUEST", "status": "PENDING"}'                  | FetchConnectionsRequest    | { req -> (req as FetchConnectionsRequest).status == UserConnectionStatus.PENDING }
        '{"type": "INVITE_REQUEST", "userInviteCode": "TestInviteCode123"}'           | InviteRequest              | { req -> (req as InviteRequest).userInviteCode.code() == 'TestInviteCode123' }
        '{"type": "ACCEPT_REQUEST", "username": "testuser"}'                          | AcceptRequest              | { req -> (req as AcceptRequest).username == 'testuser' }
        '{"type": "REJECT_INVITE_REQUEST", "username": "testuser"}'                   | RejectInviteRequest        | { req -> (req as RejectInviteRequest).username == 'testuser' }
        '{"type": "DISCONNECT_REQUEST", "username": "testuser"}'                      | DisconnectRequest          | { req -> (req as DisconnectRequest).username == 'testuser' }
        '{"type": "WRITE_MESSAGE", "username": "testUser", "content":"test message"}' | WriteMessageRequest        | { req -> (req as WriteMessageRequest).content == 'test message' }
        '{"type": "KEEP_ALIVE"}'                                                      | KeepAliveRequest           | { req -> (req as KeepAliveRequest).getType() == 'KEEP_ALIVE' }
    }
}
