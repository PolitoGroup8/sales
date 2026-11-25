package it.polito.wa2.group8.sales

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
class SalesApplicationTests {

	@Test
	fun contextLoads() {
	}

}
