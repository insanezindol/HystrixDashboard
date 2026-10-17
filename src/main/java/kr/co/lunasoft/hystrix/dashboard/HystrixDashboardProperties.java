package kr.co.lunasoft.hystrix.dashboard;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Hystrix Dashboard 설정.
 *
 * @param enableIgnoreConnectionCloseHeader Hystrix 스트림 응답의 'Connection: close' 헤더를 무시할지 여부
 * @param initParameters {@link ProxyStreamServlet} 의 서블릿 init-parameter (컨테이너별 설정용)
 * @param proxyStreamAllowList /proxy.stream 으로 프록시를 허용할 호스트 패턴 목록 ('.' 구분 Ant 패턴, 예: "*.example.com").
 * 비어 있으면 모든 호스트를 허용한다.
 */
@ConfigurationProperties("hystrix.dashboard")
public record HystrixDashboardProperties(
		@DefaultValue("false") boolean enableIgnoreConnectionCloseHeader,
		@DefaultValue Map<String, String> initParameters,
		@DefaultValue List<String> proxyStreamAllowList) {
}
