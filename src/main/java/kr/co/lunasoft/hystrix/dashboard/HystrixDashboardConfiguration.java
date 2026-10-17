package kr.co.lunasoft.hystrix.dashboard;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hystrix Dashboard 구성.
 * <p>
 * Spring Cloud Netflix 2.2.x 의 {@code HystrixDashboardConfiguration} 을 Spring Boot 4 / Jakarta Servlet 으로 포팅했다.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HystrixDashboardProperties.class)
public class HystrixDashboardConfiguration {

	@Bean
	public ServletRegistrationBean<ProxyStreamServlet> proxyStreamServlet(HystrixDashboardProperties properties) {
		ServletRegistrationBean<ProxyStreamServlet> registration =
				new ServletRegistrationBean<>(new ProxyStreamServlet(properties), "/proxy.stream");
		registration.setInitParameters(properties.initParameters());
		return registration;
	}

}
