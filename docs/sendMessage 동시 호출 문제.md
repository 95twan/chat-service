# 단체 채팅 전환 시 `session.sendMessage()` 에러 발생 원인과 해결

## 1. 배경
기존 시스템은 1:1 채팅 구조로, 하나의 메시지는 단일 상대 WebSocket 세션에만 전송되었다.  
이 구조에서는 동일 세션에 대해 동시에 `sendMessage()`가 호출될 가능성이 거의 없었다.

그룹 채팅으로 전환하면서, 하나의 메시지를 여러 참여자 세션에 브로드캐스트하도록 구조가 변경되었다.  
그 결과, `session.sendMessage()` 호출 빈도와 동시성이 이전보다 크게 증가하였다.

---

## 2. `session.sendMessage()` 에러 발생 원인

### 2-1. 동일 세션에 대한 동시 send 호출 문제
Spring WebSocket의 `WebSocketSession`은 **동일 세션에 대해 여러 스레드가 동시에 `sendMessage()`를 호출하는 상황을 안전하게 보장하지 않는다.**

그룹 채팅 환경에서 다음과 같은 흐름이 발생한다.

- A, B, C 클라이언트가 거의 동시에 메시지를 전송
- 서버는 각 메시지를 모든 참여자에게 브로드캐스트
- 특정 참여자 세션에 대해 `sendMessage()` 호출이 짧은 시간 간격으로 겹침
- WebSocket 내부 송신 상태가 충돌하며 전송 실패 발생

---

## 3. 테스트를 통한 문제 재현

그룹 채팅 전환 이후, 여러 클라이언트가 거의 동시에 메시지를 전송하는 시나리오 테스트를 진행했다.

테스트 실행 중, 다음과 같은 에러가 간헐적으로 발생했다.

    Failed to send message to 5708232f-00ae-ffa3-29e4-89b1fec360aa
    error: The remote endpoint was in state [TEXT_PARTIAL_WRITING]
    which is an invalid state for called method

- 여러 클라이언트가 거의 동시에 메시지를 전송하는 구간에서
  일부 `session.sendMessage()` 호출이 정상적으로 처리되지 않음
- 그 결과, 특정 클라이언트가 기대한 메시지 개수를 수신하지 못해
  수신 큐의 `poll()` 결과가 `null`이 되는 경우가 발생

**동시 송신 상황에서 WebSocketSession의 송신 안정성이 보장되지 않는 문제**임을 확인했다.

---

## 4. 해결 방법

동일 WebSocketSession에 대해 동시에 `sendMessage()`가 호출되는 구조가 문제기 때문에,  
해결책은 **세션 단위로 송신을 직렬화**할 수 있어야 했다.

이를 위해 연결 수립 시, 원본 `WebSocketSession`을 `ConcurrentWebSocketSessionDecorator`로 감싸도록 변경하였다.

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        ConcurrentWebSocketSessionDecorator decorated =
                new ConcurrentWebSocketSessionDecorator(session, 5000, 100 * 1024);

        webSocketSessionManager.storeSession(decorated);
    }

`ConcurrentWebSocketSessionDecorator`는 내부적으로 다음을 보장한다.

- 동일 세션에 대한 `sendMessage()` 호출을 **세션 단위로 직렬화**
- send 중인 상태에서 추가 send 요청이 발생하면 내부 큐에 적재 후 순차 처리
- 결과적으로 send 호출 간 충돌 가능성 제거

즉, **“동시 send → 순차 send” 구조로 변경함으로써**
WebSocket 송신 과정에서 발생하던 불안정성을 제거할 수 있었다.

---

## 5. 적용 결과
- `session.sendMessage()` 단계에서 발생하던 간헐적 전송 실패 해소
- 그룹 채팅 브로드캐스트 시 메시지 누락 현상 제거
- 전체 WebSocket 세션 안정성 향상
