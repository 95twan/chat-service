package com.rodemtree.chatservice.integration

import com.rodemtree.chatservice.ChatApplication
import com.rodemtree.chatservice.dto.domain.UserId
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
            userConnectionService.accept(userIdA, "testuser${it}")
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
                results << userConnectionService.accept(userId.get(), "testuser0").getFirst()
            }
        }
        threads*.join()

        then:
        results.count { it.isPresent() } == 1

        cleanup:
        (0..19).each {
            def userId = userService.getUserId("testuser${it}").get()
            userRepository.deleteById(userId.id())
        }
    }
}
