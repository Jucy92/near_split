# NearSplit Backend (Spring Boot)

## 📦 프로젝트 생성 가이드

### 방법 1: Spring Initializr 웹사이트 사용 (추천)

1. https://start.spring.io/ 접속
2. 다음 설정 선택:
   - **Project**: Gradle - Groovy
   - **Language**: Java
   - **Spring Boot**: 3.2.0 (또는 최신 안정 버전)
   - **Packaging**: Jar
   - **Java**: 17

3. Project Metadata:
   - **Group**: `com.nearsplit`
   - **Artifact**: `backend`
   - **Name**: `NearSplit`
   - **Package name**: `com.nearsplit`

4. Dependencies 추가:
   - Spring Web
   - Spring Data JPA
   - Spring Security
   - Validation
   - PostgreSQL Driver
   - H2 Database
   - Lombok
   - Spring for Apache Kafka
   - Spring Data Redis
   - WebSocket

5. **GENERATE** 버튼 클릭하여 다운로드
6. 압축 해제 후 `D:\NearSplit\backend\` 폴더에 모든 파일 복사

---

### 방법 2: Gradle 수동 설정

아래 내용으로 파일들을 생성하세요.

---

## 📁 생성해야 할 기본 구조

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── nearsplit/
│   │   │           ├── NearSplitApplication.java
│   │   │           ├── config/
│   │   │           ├── domain/
│   │   │           │   ├── user/
│   │   │           │   ├── splitgroup/
│   │   │           │   ├── payment/
│   │   │           │   ├── chat/
│   │   │           │   └── notification/
│   │   │           └── common/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
│       └── java/
│           └── com/
│               └── nearsplit/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── gradle/
```

---

## 🎯 Step 1: 프로젝트 생성 (Spring Initializr 사용)

위 방법 1대로 Spring Initializr에서 프로젝트를 생성하여 `D:\NearSplit\backend\` 에 압축 해제하세요.

---

## 🎯 Step 2: build.gradle 의존성 확인

생성된 `build.gradle` 파일을 열고 다음 의존성이 포함되어 있는지 확인하세요:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.kafka:spring-kafka'

    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'org.postgresql:postgresql'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

**추가로 필요한 의존성 (직접 추가)**:
```gradle
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'

// PostGIS
implementation 'org.hibernate:hibernate-spatial:6.4.0'

// Swagger
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

---

## 🎯 Step 3: application.yml 설정

`src/main/resources/application.yml` 파일을 다음 내용으로 작성하세요:

```yaml
spring:
  application:
    name: nearsplit

  profiles:
    active: dev

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true

jwt:
  secret: nearsplit-secret-key-change-this-in-production-environment
  expiration: 86400000  # 24 hours

logging:
  level:
    com.nearsplit: DEBUG
```

---

## 🎯 Step 4: 메인 애플리케이션 클래스 확인

`src/main/java/com/nearsplit/NearSplitApplication.java` 파일이 다음과 같은지 확인:

```java
package com.nearsplit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NearSplitApplication {
    public static void main(String[] args) {
        SpringApplication.run(NearSplitApplication.class, args);
    }
}
```

---

## 🎯 Step 5: 프로젝트 실행 테스트

터미널에서 다음 명령어 실행:

### Windows (PowerShell/CMD):
```bash
cd D:\NearSplit\backend
.\gradlew.bat bootRun
```

### 또는 IDE 사용:
- IntelliJ IDEA: `NearSplitApplication.java` 우클릭 → Run
- Eclipse: Run As → Spring Boot App

---

## ✅ 실행 성공 확인

브라우저에서 다음 URL 접속:
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (비워두기)

콘솔에 다음과 같은 로그가 나오면 성공:
```
Started NearSplitApplication in X.XXX seconds
```

---

## 🎯 다음 단계

프로젝트 실행이 성공하면 다음 작업을 진행하세요:

1. **User 엔티티 생성**
2. **UserRepository 생성**
3. **SecurityConfig 설정** (Spring Security)
4. **JWT 유틸 클래스 작성**

각 단계별 가이드는 별도로 제공하겠습니다!

---

## 📝 체크리스트

- [ ] Spring Initializr에서 프로젝트 생성 및 다운로드
- [ ] `D:\NearSplit\backend\` 폴더에 압축 해제
- [ ] `build.gradle` 의존성 확인 및 추가
- [ ] `application.yml` 작성
- [ ] `./gradlew bootRun` 실행 성공
- [ ] http://localhost:8080/h2-console 접속 확인
