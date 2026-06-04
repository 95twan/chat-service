package com.rodemtree.chatservice.integration

import com.rodemtree.chatservice.ChatApplication
import com.rodemtree.chatservice.constant.UserConnectionStatus
import com.rodemtree.chatservice.dto.domain.UserId
import com.rodemtree.chatservice.entity.UserConnectionId
import com.rodemtree.chatservice.repository.UserConnectionRepository
import com.rodemtree.chatservice.repository.UserRepository
import com.rodemtree.chatservice.service.UserConnectionLimitService
import com.rodemtree.chatservice.service.UserConnectionService
import com.rodemtree.chatservice.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest(classes = ChatApplication)
class UserConnectionServiceSpec extends Specification {

    @Autowired
    UserService userService

    @Autowired
    UserConnectionService userConnectionService

    @Autowired
    UserConnectionLimitService userConnectionLimitService

    @Autowired
    UserRepository userRepository

    @Autowired
    UserConnectionRepository userConnectionRepository

    def "연결 요청 수락은 연결 제한 수를 넘을 수 없다."() {
        given:
        userConnectionLimitService.setLimitConnection(10)
        (0..19).collect { userService.addUser("testUser${it}", "testpass${it}")}
        def userIdA = userService.getUserId("testuser0").get()
        def inviteCodeA = userService.getInviteCode(userIdA).get()
        (1..9).collect {
            userConnectionService.invite(userService.getUserId("testuser${it}").get(), inviteCodeA)
            userConnectionService.acceptInvite(userIdA, "testuser${it}")
        }
        def inviteCodes = (10..19).collect {
            userService.getInviteCode(userService.getUserId("testuser${it}").get()).get()
        }
        inviteCodes.each { userConnectionService.invite(userIdA, it)}

        def results = Collections.synchronizedList(new ArrayList<Optional<UserId>>())

        when:
        def threads = (10..19).collect { idx ->
            Thread.start {
                def userId = userService.getUserId("testuser${idx}")
                results << userConnectionService.acceptInvite(userId.get(), "testuser0").getFirst()
            }
        }
        threads*.join()

        then:
        results.count { it.isPresent() } == 1
    }

    def "연결 종료는 연결 카운트 0보다 작을 수 없다."() {
        given:
        (0..10).collect { userService.addUser("testUser${it}", "testpass${it}")}
        def userIdA = userService.getUserId("testuser0").get()
        def inviteCodeA = userService.getInviteCode(userIdA).get()
        (1..10).collect {
            userConnectionService.invite(userService.getUserId("testuser${it}").get(), inviteCodeA)
        }
        (1..5).each {
            userConnectionService.acceptInvite(userIdA, "testuser${it}")
        }

        def results = Collections.synchronizedList(new ArrayList<Boolean>())

        when:
        def threads = (1..10).collect { idx ->
            Thread.start {
                def userId = userService.getUserId("testuser${idx}")
                results << userConnectionService.disconnect(userId.get(), "testuser0").getFirst()
            }
        }
        threads*.join()

        then:
        results.count { it == true } == 5
        userService.getConnectionCount(userService.getUserId("testuser0").get()).get() == 0
    }

    def cleanup() {
        (0..19).each {
            userService.getUserId("testuser${it}").ifPresent { userId ->
                userRepository.deleteById(userId.id())
                userConnectionRepository.findConnectionsByPartnerAUserIdAndStatus(userId.id(), UserConnectionStatus.PENDING).each {
                    userConnectionRepository.deleteById(new UserConnectionId(
                            Long.min(userId.id(), it.getUserId()),
                            Long.max(userId.id(), it.getUserId())
                    ))
                }
                userConnectionRepository.findConnectionsByPartnerBUserIdAndStatus(userId.id(), UserConnectionStatus.PENDING).each {
                    userConnectionRepository.deleteById(new UserConnectionId(
                            Long.min(userId.id(), it.getUserId()),
                            Long.max(userId.id(), it.getUserId())
                    ))
                }
                userConnectionRepository.findConnectionsByPartnerAUserIdAndStatus(userId.id(), UserConnectionStatus.ACCEPTED).each {
                    userConnectionRepository.deleteById(new UserConnectionId(
                            Long.min(userId.id(), it.getUserId()),
                            Long.max(userId.id(), it.getUserId())
                    ))
                }
                userConnectionRepository.findConnectionsByPartnerBUserIdAndStatus(userId.id(), UserConnectionStatus.ACCEPTED).each {
                    userConnectionRepository.deleteById(new UserConnectionId(
                            Long.min(userId.id(), it.getUserId()),
                            Long.max(userId.id(), it.getUserId())
                    ))
                }
                userConnectionRepository.findConnectionsByPartnerAUserIdAndStatus(userId.id(), UserConnectionStatus.DISCONNECTED).each {
                    userConnectionRepository.deleteById(new UserConnectionId(
                            Long.min(userId.id(), it.getUserId()),
                            Long.max(userId.id(), it.getUserId())
                    ))
                }
                userConnectionRepository.findConnectionsByPartnerBUserIdAndStatus(userId.id(), UserConnectionStatus.DISCONNECTED).each {
                    userConnectionRepository.deleteById(new UserConnectionId(
                            Long.min(userId.id(), it.getUserId()),
                            Long.max(userId.id(), it.getUserId())
                    ))
                }
            }
        }
    }
}
