# HystrixDashboard

이 프로젝트는 Spring Boot 기반의 Hystrix Dashboard 애플리케이션입니다. 마이크로서비스 환경에서 Hystrix를 사용하는 서비스들의 상태를 실시간으로 모니터링할 수 있는 대시보드를 제공합니다.

## 주요 기능

-   Hystrix 스트림을 시각적으로 모니터링
-   서비스의 Circuit Breaker 상태 확인
-   실시간 메트릭 시각화

## 실행 방법

1. Java 8 이상이 설치되어 있어야 합니다.
2. Maven을 사용하여 프로젝트를 빌드합니다.
    ```bash
    ./mvnw clean package
    ```
3. 애플리케이션을 실행합니다.
    ```bash
    ./mvnw spring-boot:run
    ```
4. 브라우저에서 [http://localhost:9010/hystrix](http://localhost:9010/hystrix) 에 접속하여 대시보드에 접근할 수 있습니다.

## 설정 정보

-   기본 포트: `9010`
-   주요 설정 파일: `src/main/resources/application.yml`

## 참고

-   Hystrix Dashboard에 대한 자세한 내용은 [공식 문서](https://github.com/Netflix/Hystrix/wiki/Dashboard)를 참고하세요.
