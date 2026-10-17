package kr.co.lunasoft.hystrix.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * 브라우저의 동일 출처 정책(EventSource)을 우회하기 위해 원격 Hystrix 스트림을 그대로 중계하는 서블릿.
 * <p>
 * Spring Cloud Netflix 2.2.x 의 {@code ProxyStreamServlet} 을 Jakarta Servlet + JDK {@link HttpClient} 로 포팅했다.
 * 스트림은 장시간 유지되는 연결이므로 MVC 비동기 처리(타임아웃 존재) 대신 서블릿에서 블로킹으로 중계한다.
 */
public class ProxyStreamServlet extends HttpServlet {

	private static final Logger log = LoggerFactory.getLogger(ProxyStreamServlet.class);

	private static final String CONNECTION_CLOSE_VALUE = "close";

	/** 서블릿 컨테이너가 직접 관리하는 hop-by-hop 헤더 */
	private static final Set<String> SKIPPED_HEADERS = Set.of("transfer-encoding", "content-length");

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(5))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private final transient HystrixDashboardProperties properties;

	private final transient PathMatcher hostMatcher = new AntPathMatcher(".");

	public ProxyStreamServlet(HystrixDashboardProperties properties) {
		this.properties = properties;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String origin = request.getParameter("origin");
		if (origin == null || origin.isBlank()) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Required parameter 'origin' missing. Example: 107.20.175.135:7001");
			return;
		}

		String proxyUrl = buildProxyUrl(origin.trim(), request);
		URI proxyUri;
		try {
			proxyUri = URI.create(proxyUrl);
		}
		catch (IllegalArgumentException ex) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid origin: " + origin);
			return;
		}

		if (!isAllowedToProxy(proxyUri)) {
			log.warn("Origin parameter: {} is not in the allowed list of proxy host names. "
					+ "If it should be allowed add it to hystrix.dashboard.proxy-stream-allow-list.", origin);
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		log.info("Proxy opening connection to: {}", proxyUrl);
		HttpRequest proxyRequest = HttpRequest.newBuilder(proxyUri).GET().build();
		try {
			HttpResponse<InputStream> proxyResponse = HTTP_CLIENT.send(proxyRequest,
					HttpResponse.BodyHandlers.ofInputStream());
			try (InputStream in = proxyResponse.body()) {
				int statusCode = proxyResponse.statusCode();
				if (statusCode != HttpServletResponse.SC_OK) {
					log.warn("Failed opening connection to {} : {}", proxyUrl, statusCode);
					response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
					return;
				}
				copyHeadersToServletResponse(proxyResponse.headers(), response);
				relay(in, response.getOutputStream());
			}
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		catch (IOException ex) {
			if (isClientAbort(ex)) {
				log.debug("Connection closed by client. Will stop proxying ...");
			}
			else {
				log.error("Error proxying request: {}", proxyUrl, ex);
				if (!response.isCommitted()) {
					response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
				}
			}
		}
	}

	private String buildProxyUrl(String origin, HttpServletRequest request) {
		StringBuilder url = new StringBuilder();
		if (!origin.startsWith("http")) {
			url.append("http://");
		}
		url.append(origin);

		boolean hasQuery = origin.contains("?");
		for (var entry : request.getParameterMap().entrySet()) {
			String name = entry.getKey();
			if ("origin".equals(name)) {
				continue;
			}
			for (String value : entry.getValue()) {
				url.append(hasQuery ? '&' : '?')
					.append(URLEncoder.encode(name, StandardCharsets.UTF_8))
					.append('=')
					.append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8));
				hasQuery = true;
			}
		}
		return url.toString();
	}

	private boolean isAllowedToProxy(URI uri) {
		var allowList = properties.proxyStreamAllowList();
		if (allowList.isEmpty()) {
			return true;
		}
		String host = uri.getHost();
		return host != null && allowList.stream().anyMatch(pattern -> hostMatcher.match(pattern, host));
	}

	private void copyHeadersToServletResponse(HttpHeaders headers, HttpServletResponse response) {
		headers.map().forEach((name, values) -> {
			if (SKIPPED_HEADERS.contains(name.toLowerCase())) {
				return;
			}
			for (String value : values) {
				if (properties.enableIgnoreConnectionCloseHeader() && "Connection".equalsIgnoreCase(name)
						&& CONNECTION_CLOSE_VALUE.equalsIgnoreCase(value)) {
					log.warn("Ignoring 'Connection: close' header from stream response");
					continue;
				}
				response.addHeader(name, value);
			}
		});
	}

	/** 이벤트가 지연 없이 브라우저로 전달되도록 읽은 즉시 flush 한다. */
	private void relay(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[8192];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
			out.flush();
		}
	}

	private boolean isClientAbort(Throwable ex) {
		for (Throwable t = ex; t != null; t = t.getCause()) {
			if ("ClientAbortException".equals(t.getClass().getSimpleName())) {
				return true;
			}
		}
		return false;
	}

}
