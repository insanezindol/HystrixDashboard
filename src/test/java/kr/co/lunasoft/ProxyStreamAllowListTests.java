package kr.co.lunasoft;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
		properties = "hystrix.dashboard.proxy-stream-allow-list=*.example.com")
class ProxyStreamAllowListTests {

	@LocalServerPort
	private int port;

	@Test
	void proxyStreamRejectsHostNotInAllowList() throws Exception {
		HttpRequest request = HttpRequest
			.newBuilder(URI.create("http://localhost:" + port + "/proxy.stream?origin=localhost:" + port + "/hystrix"))
			.build();
		HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode()).isEqualTo(403);
	}

}
