# (BE) 먹구스꾸 
<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=Kotlin&logoColor=white"/> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white"/> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white"/> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white"/> <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=Elasticsearch&logoColor=white"/> <img src="https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=Kibana&logoColor=white"/> <img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=for-the-badge&logo=GitHub Actions&logoColor=white"/> <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=Swagger&logoColor=black"/>


2024 Spring Semester `Introduction to Software Engineering_SWE3002_42(차수영)`  Team 1의 벡엔드 프로젝트 입니다.

### 개발 기간 
2024.04.15(월) ~ 2024.06.16(일)

### 배포 주소
> 백엔드 서버:     <br>
> Swagger UI: 

### 개발자 소개
- 신경덕(2017315471) - 백엔드 PM 
- 김광원(2019312751) 
- 김태훈(2017313665)

----
# 기술 스택
- 언어: Kotlin
- 프레임워크: Spring Boot, Spring MVC
- 라이브러리: JPA, QueryDSL
- 테스트: KoTest, TestContainers
- 빌드툴: Gradle
- CI: Github Actions
- Docker, Docker Compose
- 서버: AWS EC2
- 데이터베이스: MYSQL, ElasticSearch
- 자동완성 Redis
- Swagger UI

---
# 아키텍처
<img src="https://github.com/meokgu-skku/be/assets/63694834/38d746c2-bbba-4928-839b-de9133ca6a3f" alt="image" width="500" height="500">

----
# 시작 가이드
## Requirements
- Kotlin
- JVM 1.7 이상
- Docker


## Installation
```
$ git clone https://github.com/meokgu-skku/be.git
$ cd be
$ ./gradlew build
```

## gradlew 명령어
```
$ ./gradlew build         // 빌드
$ ./gradlew clean         // 빌드 초기화
$ ./gradlew ktlintFormat  // 코드 스타일 포맷팅
```

---
# API 문서
## User
URL|Method|Description
-----|---|---
/v1/users/email/sign-up|POST|
/v1/users/email/sign-in|POST|
/v1/users/email/send|POST|
/v1/users/email/validate|POST|
/v1/users/password|PATCH|
/v1/users|PATCH|
/v1/users/{userId}|GET|
/v1/users/check-nickname|GET|

## Restaurant
URL|Method|Description
-----|---|---
/v1/restaurants|GET|
/v1/restaurants/my-like|POST|
/v1/restaurants/{restaurantId}/like|GET|
/v1/restaurants/{restaurantId}|GET|
/v1/restaurants/recommend|GET|
/v1/restaurants/categories|GET|

## Reviews
URL|Method|Description
-----|---|------
/v1/restaurants/reviews|GET|
/v1/restaurants/reviews/{reviewId}|GET|
/v1/restaurants/{restaurantId}/reviews|POST|
/v1/restaurants/{restaurantId}/reviews/{reviewId}|PATCH|
/v1/restaurants/reviews/{reviewId}|DELETE|
/v1/restaurants/my-reviews|GET|
/v1/restaurants/reviews/like|POST|
