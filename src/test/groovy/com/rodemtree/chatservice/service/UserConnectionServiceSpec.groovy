package com.rodemtree.chatservice.service


import com.rodemtree.chatservice.constant.UserConnectionStatus
import com.rodemtree.chatservice.dto.domain.InviteCode
import com.rodemtree.chatservice.dto.domain.User
import com.rodemtree.chatservice.dto.domain.UserId
import com.rodemtree.chatservice.dto.projection.InviterUserIdProjection
import com.rodemtree.chatservice.dto.projection.UserConnectionStatusProjection
import com.rodemtree.chatservice.entity.UserConnectionEntity
import com.rodemtree.chatservice.entity.UserEntity
import com.rodemtree.chatservice.repository.UserConnectionRepository
import com.rodemtree.chatservice.repository.UserRepository
import com.rodemtree.chatservice.util.JsonUtil
import org.springframework.data.util.Pair
import spock.lang.Specification

class UserConnectionServiceSpec extends Specification {

    UserConnectionService userConnectionService
    UserConnectionLimitService userConnectionLimitService
    UserService userService = Stub()
    CacheService cacheService = Stub()
    UserRepository userRepository = Stub()
    UserConnectionRepository userConnectionRepository = Stub()

    def setup() {
        userConnectionLimitService = new UserConnectionLimitService(cacheService, userRepository, userConnectionRepository)
        userConnectionService = new UserConnectionService(userService, userConnectionLimitService, cacheService, userConnectionRepository, new JsonUtil())
    }

    def "사용자 연결 신청에 대한 테스트."() {
        given:
        userService.getUser(inviteCodeOfTargetUser) >> Optional.of(new User(targetUserId, targetUserName))
        userConnectionRepository.findStatusByPartnerAUserIdAndPartnerBUserId(_ as Long, _ as Long) >> {
            Optional.of(Stub(UserConnectionStatusProjection) {
                getStatus() >> beforeConnectionStatus.name()
            })
        }
        userService.getConnectionCount(senderUserId) >> { senderUserId.id() != 8 ? Optional.of(0) : Optional.of(1000) }
        userService.getUsername(senderUserId) >> Optional.of(senderUserName)

        when:
        def result = userConnectionService.invite(senderUserId, usedInviteCode)

        then:
        result == expectedResult

        where:
        scenario              | senderUserId  | senderUserName | targetUserId  | targetUserName | inviteCodeOfTargetUser      | usedInviteCode                | beforeConnectionStatus            | expectedResult
        'Valid invite code'   | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code')   | UserConnectionStatus.NONE         | Pair.of(Optional.of(new UserId(2)), 'userA')
        'Already connected'   | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code')   | UserConnectionStatus.ACCEPTED     | Pair.of(Optional.empty(), 'Already connected with ' + targetUserName)
        'Already invited'     | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code')   | UserConnectionStatus.PENDING      | Pair.of(Optional.empty(), 'Already invited to ' + targetUserName)
        'Already reject'      | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code')   | UserConnectionStatus.REJECTED     | Pair.of(Optional.empty(), 'Already invited to ' + targetUserName)
        'After disconnected'  | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code')   | UserConnectionStatus.DISCONNECTED | Pair.of(Optional.of(new UserId(2)), 'userA')
        'Invalid invite code' | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('nobody code') | UserConnectionStatus.NONE         | Pair.of(Optional.empty(), 'Invalid invite code.')
        'Self invite'         | new UserId(1) | 'userA'        | new UserId(1) | 'userA'        | new InviteCode('user1Code') | new InviteCode('user1Code')   | UserConnectionStatus.NONE         | Pair.of(Optional.empty(), 'Cannot self invite.')
        'Limit reached'       | new UserId(8) | 'userH'        | new UserId(9) | 'userI'        | new InviteCode('user9Code') | new InviteCode('user9Code')   | UserConnectionStatus.NONE         | Pair.of(Optional.empty(), 'Connection limit reached.')
    }

    def "사용자 연결 신청 수락에 대한 테스트."() {
        given:
        userService.getUserId(targetUserName) >> Optional.of(targetUserId)
        userConnectionRepository.findInviterUserIdByPartnerAUserIdAndPartnerBUserId(_ as Long, _ as Long) >> {
            inviterUserId.flatMap { UserId inviter ->
                Optional.of(Stub(InviterUserIdProjection) {
                    getInviterUserId() >> inviter.id()
                })
            }
        }
        userConnectionRepository.findStatusByPartnerAUserIdAndPartnerBUserId(_ as Long, _ as Long) >> {
            Optional.of(Stub(UserConnectionStatusProjection) {
                getStatus() >> beforeConnectionStatus.name()
            })
        }
        userService.getUsername(senderUserId) >> Optional.of(senderUserName)
        userRepository.findForUpdateByUserId(_ as Long) >> { Long userId ->
            def entity = new UserEntity()
            if (userId == 5 || userId == 7) {
                entity.setConnectionCount(1000)
            }
            return Optional.of(entity)
        }
        userConnectionRepository.findByPartnerAUserIdAndPartnerBUserIdAndStatus(_ as Long, _ as Long, _ as UserConnectionStatus) >> {
            inviterUserId.flatMap { UserId inviter ->
                Optional.of(new UserConnectionEntity(senderUserId.id(), targetUserId.id(), UserConnectionStatus.PENDING, inviter.id()))
            }
        }

        when:
        def result = userConnectionService.acceptInvite(senderUserId, targetUserName)

        then:
        result == expectedResult

        where:
        scenario                          | senderUserId  | senderUserName | targetUserId  | targetUserName | inviterUserId              | beforeConnectionStatus            | expectedResult
        'Accept invite'                   | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.PENDING      | Pair.of(Optional.of(new UserId(2)), 'userA')
        'Already connected'               | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.ACCEPTED     | Pair.of(Optional.empty(), 'Already connected.')
        'Self accept'                     | new UserId(1) | 'userA'        | new UserId(1) | 'userA'        | Optional.of(new UserId(1)) | UserConnectionStatus.PENDING      | Pair.of(Optional.empty(), 'Cannot self accept.')
        'Accept wrong invite'             | new UserId(1) | 'userA'        | new UserId(4) | 'userD'        | Optional.of(new UserId(2)) | UserConnectionStatus.PENDING      | Pair.of(Optional.empty(), 'Invalid username.')
        'Accept invalid invite'           | new UserId(1) | 'userA'        | new UserId(4) | 'userD'        | Optional.empty()           | UserConnectionStatus.NONE         | Pair.of(Optional.empty(), 'Invalid username.')
        'After reject'                    | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.REJECTED     | Pair.of(Optional.empty(), 'Accept failed.')
        'After disconnected'              | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.DISCONNECTED | Pair.of(Optional.empty(), 'Accept failed.')
        'Limit reached'                   | new UserId(5) | 'userE'        | new UserId(6) | 'userF'        | Optional.of(new UserId(6)) | UserConnectionStatus.PENDING      | Pair.of(Optional.empty(), 'Connection limit reached.')
        'Limit reached by the other user' | new UserId(8) | 'userH'        | new UserId(7) | 'userG'        | Optional.of(new UserId(7)) | UserConnectionStatus.PENDING      | Pair.of(Optional.empty(), 'Connection limit reached by the other user.')
    }

    def "사용자 연결 신청 거부에 대한 테스트."() {
        given:
        userService.getUserId(targetUserName) >> Optional.of(targetUserId)
        userConnectionRepository.findInviterUserIdByPartnerAUserIdAndPartnerBUserId(_ as Long, _ as Long) >> {
            inviterUserId.flatMap { UserId inviter ->
                Optional.of(Stub(InviterUserIdProjection) {
                    getInviterUserId() >> inviter.id()
                })
            }
        }
        userConnectionRepository.findStatusByPartnerAUserIdAndPartnerBUserId(_ as Long, _ as Long) >> {
            Optional.of(Stub(UserConnectionStatusProjection) {
                getStatus() >> beforeConnectionStatus.name()
            })
        }

        when:
        def result = userConnectionService.rejectInvite(senderUserId, targetUserName)

        then:
        result == expectedResult

        where:
        scenario                | senderUserId  | senderUserName | targetUserId  | targetUserName | inviterUserId              | beforeConnectionStatus            | expectedResult
        'Reject invite'         | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.PENDING      | Pair.of(true, 'userB')
        'Already Reject'        | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.REJECTED     | Pair.of(false, 'Reject failed.')
        'Self Reject'           | new UserId(1) | 'userA'        | new UserId(1) | 'userA'        | Optional.of(new UserId(1)) | UserConnectionStatus.PENDING      | Pair.of(false, 'Reject failed.')
        'Reject wrong invite'   | new UserId(1) | 'userA'        | new UserId(3) | 'userC'        | Optional.of(new UserId(2)) | UserConnectionStatus.PENDING      | Pair.of(false, 'Reject failed.')
        'Reject invalid invite' | new UserId(1) | 'userA'        | new UserId(4) | 'userD'        | Optional.empty()           | UserConnectionStatus.NONE         | Pair.of(false, 'Reject failed.')
        'After disconnected'    | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.DISCONNECTED | Pair.of(false, 'Reject failed.')
    }

    def "사용자 연결 삭제에 대한 테스트."() {
        given:
        userService.getUserId(targetUserName) >> Optional.of(targetUserId)
        userConnectionRepository.findStatusByPartnerAUserIdAndPartnerBUserId(_ as Long, _ as Long) >> {
            Optional.of(Stub(UserConnectionStatusProjection) {
                getStatus() >> beforeConnectionStatus.name()
            })
        }
        userConnectionRepository.findInviterUserIdByPartnerAUserIdAndPartnerBUserId(_ as Long, _ as Long) >> {
            inviterUserId.flatMap { UserId inviter ->
                Optional.of(Stub(InviterUserIdProjection) {
                    getInviterUserId() >> inviter.id()
                })
            }
        }
        userRepository.findForUpdateByUserId(_ as Long) >> { Long userId ->
            def entity = new UserEntity()
            if (userId != 8) {
                entity.setConnectionCount(100)
            }
            return Optional.of(entity)
        }
        userConnectionRepository.findByPartnerAUserIdAndPartnerBUserIdAndStatus(_ as Long, _ as Long, _ as UserConnectionStatus) >> {
            inviterUserId.flatMap { UserId inviter ->
                Optional.of(new UserConnectionEntity(senderUserId.id(), targetUserId.id(), UserConnectionStatus.ACCEPTED, inviter.id()))
            }
        }

        when:
        def result = userConnectionService.disconnect(senderUserId, targetUserName)

        then:
        result == expectedResult

        where:
        scenario                | senderUserId  | senderUserName | targetUserId  | targetUserName | inviterUserId              | beforeConnectionStatus            | expectedResult
        'Disconnect connection' | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.ACCEPTED     | Pair.of(true, 'userB')
        'Reject status'         | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.REJECTED     | Pair.of(true, 'userB')
        'Pending status'        | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.PENDING      | Pair.of(false, 'Disconnect failed.')
        'Already disconnected'  | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | Optional.of(new UserId(2)) | UserConnectionStatus.DISCONNECTED | Pair.of(false, 'Disconnect failed.')
        'Self disconnect'       | new UserId(1) | 'userA'        | new UserId(1) | 'userA'        | Optional.of(new UserId(1)) | UserConnectionStatus.ACCEPTED     | Pair.of(false, 'Disconnect failed.')
        'Disconnect wrong user' | new UserId(1) | 'userA'        | new UserId(3) | 'userC'        | Optional.empty()           | UserConnectionStatus.NONE         | Pair.of(false, 'Disconnect failed.')
        'Wrong condition'       | new UserId(8) | 'userH'        | new UserId(9) | 'userI'        | Optional.of(new UserId(9)) | UserConnectionStatus.ACCEPTED     | Pair.of(false, 'Disconnect failed.')
    }
}
