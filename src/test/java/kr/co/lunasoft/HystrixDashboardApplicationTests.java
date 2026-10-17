package kr.co.lunasoft;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HystrixDashboardApplicationTests {

	private final HttpClient client = HttpClient.newHttpClient();

	@LocalServerPort
	private int port;

	@Test
	void dashboardHomeRenders() throws Exception {
		HttpResponse<String> response = get("/hystrix");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("<h2>Hystrix Dashboard</h2>", "/webjars/jquery/jquery.min.js");
	}

	@Test
	void monitorPageRenders() throws Exception {
		HttpResponse<String> response = get("/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A8080%2Factuator%2Fhystrix.stream");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("<base href=\"/hystrix/\">", "/proxy.stream?origin=");
	}

	@Test
	void staticResourcesAndWebjarsAreServed() throws Exception {
		assertThat(get("/hystrix/js/tmpl.js").statusCode()).isEqualTo(200);
		assertThat(get("/hystrix/components/hystrixCommand/hystrixCommand.js").statusCode()).isEqualTo(200);
		assertThat(get("/webjars/jquery/jquery.min.js").statusCode()).isEqualTo(200);
		assertThat(get("/webjars/d3js/d3.min.js").statusCode()).isEqualTo(200);
	}

	@Test
	void proxyStreamRequiresOrigin() throws Exception {
		HttpResponse<String> response = get("/proxy.stream");
		assertThat(response.statusCode()).isEqualTo(500);
		assertThat(response.body()).contains("Required parameter 'origin' missing");
	}

	@Test
	void proxyStreamRelaysOrigin() throws Exception {
		String origin = URLEncoder.encode("localhost:" + port + "/hystrix/js/tmpl.js", StandardCharsets.UTF_8);
		HttpResponse<String> response = get("/proxy.stream?origin=" + origin);
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).isEqualTo(get("/hystrix/js/tmpl.js").body());
	}

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

}
