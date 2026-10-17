# HystrixDashboard

이 프로젝트는 Spring Boot 기반의 Hystrix Dashboard 애플리케이션입니다. 마이크로서비스 환경에서 Hystrix를 사용하는 서비스들의 상태를 실시간으로 모니터링할 수 있는 대시보드를 제공합니다.

## 주요 기능

-   Hystrix 스트림을 시각적으로 모니터링
-   서비스의 Circuit Breaker 상태 확인
-   실시간 메트릭 시각화
-   Turbine 클러스터 스트림(`turbine.stream`) 모니터링

## 기술 스택

| 구분        | 버전                                       |
| ----------- | ------------------------------------------ |
| Java        | 25                                         |
| Spring Boot | 4.1.1 (Spring Framework 7, Jakarta EE 11)  |
| Web 서버    | Embedded Tomcat 11                         |
| 템플릿      | FreeMarker                                 |
| 빌드        | Maven 3.9.16 (Maven Wrapper 3.3.4)         |
| 프론트엔드  | jQuery 3.7.1, d3.js 3.4.11 (WebJars)       |

> **Spring Cloud 의존성 제거 안내**
>
> `spring-cloud-starter-netflix-hystrix-dashboard`는 Spring Cloud Hoxton(2.2.x)을 마지막으로 제거되어
> Spring Boot 3 이상(Jakarta EE)에서는 사용할 수 없습니다.
> 그래서 Spring Cloud Netflix 2.2.10의 대시보드 리소스(정적 파일, 템플릿)와 프록시 서블릿을
> 이 프로젝트 안으로 가져와 Jakarta Servlet과 JDK `HttpClient` 기반으로 포팅했습니다.
> 따라서 `@EnableHystrixDashboard` 애너테이션은 더 이상 필요 없습니다.

## 실행 방법

1. **JDK 25**가 필요합니다. `JAVA_HOME`이 JDK 25를 가리키도록 설정합니다.
    ```powershell
    # PowerShell
    $env:JAVA_HOME = "C:\java\jdk-25.0.4.1"
    ```
    ```bash
    # Git Bash
    export JAVA_HOME=/c/java/jdk-25.0.4.1
    ```
2. Maven Wrapper로 프로젝트를 빌드합니다. Maven을 따로 설치할 필요는 없습니다. 처음 실행할 때 Maven 3.9.16을 자동으로 내려받습니다.
    ```bash
    ./mvnw clean package          # Git Bash / macOS / Linux
    .\mvnw.cmd clean package      # Windows PowerShell / CMD
    ```
3. 애플리케이션을 실행합니다.
    ```bash
    ./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
    # 또는
    java -jar target/HystrixDashboard-0.0.1-SNAPSHOT.jar
    ```
4. 브라우저에서 [http://localhost:9010/hystrix](http://localhost:9010/hystrix) 에 접속하여 대시보드에 접근할 수 있습니다.
5. 모니터링할 스트림 URL을 입력합니다.
    - 단일 애플리케이션: `http://hystrix-app:port/actuator/hystrix.stream`
    - Turbine 클러스터: `http://turbine-hostname:port/turbine.stream?cluster=[clusterName]`

## 설정 정보

-   기본 포트: `9010`
-   주요 설정 파일: `src/main/resources/application.yml`

| 속성                                                  | 기본값  | 설명                                                                                                            |
| ----------------------------------------------------- | ------- | --------------------------------------------------------------------------------------------------------------- |
| `hystrix.dashboard.proxy-stream-allow-list`           | (비어 있음) | `/proxy.stream`으로 중계를 허용할 호스트 패턴 목록입니다. `.`으로 구분하는 Ant 패턴을 씁니다(예: `*.example.com`). **비어 있으면 모든 호스트를 허용합니다.** |
| `hystrix.dashboard.enable-ignore-connection-close-header` | `false` | 스트림 응답의 `Connection: close` 헤더를 무시할지 정합니다.                                                       |
| `hystrix.dashboard.init-parameters`                   | -       | `ProxyStreamServlet`에 전달할 서블릿 init-parameter입니다.                                                       |

> ⚠️ `/proxy.stream`은 서버가 대신 원격 URL에 접속하는 프록시입니다. 외부에 노출되는 환경이라면
> SSRF 방지를 위해 `proxy-stream-allow-list`에 허용할 호스트를 반드시 지정하세요.
>
> ```yaml
> hystrix:
>   dashboard:
>     proxy-stream-allow-list:
>       - localhost
>       - "*.example.com"
> ```

## 프로젝트 구조

```
src/main/java/kr/co/lunasoft
├── HystrixDashboardApplication.java          # 애플리케이션 진입점
└── hystrix/dashboard
    ├── HystrixDashboardConfiguration.java    # /proxy.stream 서블릿 등록
    ├── HystrixDashboardController.java       # /hystrix, /hystrix/monitor 화면
    ├── HystrixDashboardProperties.java       # hystrix.dashboard.* 설정
    └── ProxyStreamServlet.java               # 원격 Hystrix 스트림 중계(SSE)
src/main/resources
├── static/hystrix/                           # 대시보드 JS/CSS/이미지
└── templates/hystrix/                        # index.ftlh, monitor.ftlh
```

## 테스트

```bash
./mvnw test
```

## 참고

-   Hystrix Dashboard에 대한 자세한 내용은 [공식 문서](https://github.com/Netflix/Hystrix/wiki/Dashboard)를 참고하세요.
-   Netflix Hystrix는 유지보수 모드(maintenance mode)입니다. 새로 개발하는 서비스라면 [Resilience4j](https://resilience4j.readme.io/) 사용을 고려하세요.
-   대시보드 정적 리소스와 템플릿은 [Spring Cloud Netflix](https://github.com/spring-cloud/spring-cloud-netflix) 2.2.10.RELEASE에서 가져왔습니다(Apache License 2.0).
