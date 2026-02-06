package com.rodemtree.chatservice


import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = ChatApplication)
class ChatApplicationSpec extends Specification {

	void contextLoads() {
		expect:
		true
	}


}
