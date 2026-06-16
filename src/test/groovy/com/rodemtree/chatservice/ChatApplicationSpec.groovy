package com.rodemtree.chatservice


import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest(classes = ChatApplication)
class ChatApplicationSpec extends Specification {

	void contextLoads() {
		expect:
		true
	}


}
