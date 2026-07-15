package com.rodemtree.chatservice


import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest(classes = MessageApplication)
class MessageApplicationSpec extends Specification {

	void contextLoads() {
		expect:
		true
	}


}
