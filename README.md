# [항해플러스 백엔드 5기] 서버구축 시나리오 : 콘서트 예약 

## Description

- `콘서트 예약 서비스`를 구현해 봅니다.
- 대기열 시스템을 구축하고, 예약 서비스는 작업가능한 유저만 수행할 수 있도록 해야합니다.
- 사용자는 좌석예약 시에 미리 충전한 잔액을 이용합니다.
- 좌석 예약 요청시에, 결제가 이루어지지 않더라도 일정 시간동안 다른 유저가 해당 좌석에 접근할 수 없도록 합니다.

## Requirements

- 아래 5가지 API 를 구현합니다.
    - 유저 토큰 발급 API
    - 예약 가능 날짜 / 좌석 API
    - 좌석 예약 요청 API
    - 잔액 충전 / 조회 API
    - 결제 API
- 각 기능 및 제약사항에 대해 단위 테스트를 반드시 하나 이상 작성하도록 합니다.
- 다수의 인스턴스로 어플리케이션이 동작하더라도 기능에 문제가 없도록 작성하도록 합니다.
- 동시성 이슈를 고려하여 구현합니다.
- 대기열 개념을 고려해 구현합니다.

## API Specs

1️⃣ **`주요` 유저 대기열 토큰 기능**

- 서비스를 이용할 토큰을 발급받는 API를 작성합니다.
- 토큰은 유저의 UUID 와 해당 유저의 대기열을 관리할 수 있는 정보 ( 대기 순서 or 잔여 시간 등 ) 를 포함합니다.
- 이후 모든 API 는 위 토큰을 이용해 대기열 검증을 통과해야 이용 가능합니다.

> 기본적으로 폴링으로 본인의 대기열을 확인한다고 가정하며, 다른 방안 또한 고려해보고 구현해 볼 수 있습니다.
>

**2️⃣ `기본` 예약 가능 날짜 / 좌석 API**

- 예약가능한 날짜와 해당 날짜의 좌석을 조회하는 API 를 각각 작성합니다.
- 예약 가능한 날짜 목록을 조회할 수 있습니다.
- 날짜 정보를 입력받아 예약가능한 좌석정보를 조회할 수 있습니다.

> 좌석 정보는 1 ~ 50 까지의 좌석번호로 관리됩니다.
>

3️⃣ **`주요` 좌석 예약 요청 API**

- 날짜와 좌석 정보를 입력받아 좌석을 예약 처리하는 API 를 작성합니다.
- 좌석 예약과 동시에 해당 좌석은 그 유저에게 약 5분간 임시 배정됩니다. ( 시간은 정책에 따라 자율적으로 정의합니다. )
- 만약 배정 시간 내에 결제가 완료되지 않는다면 좌석에 대한 임시 배정은 해제되어야 하며 다른 사용자는 예약할 수 없어야 한다.

4️⃣ **`기본`**  **잔액 충전 / 조회 API**

- 결제에 사용될 금액을 API 를 통해 충전하는 API 를 작성합니다.
- 사용자 식별자 및 충전할 금액을 받아 잔액을 충전합니다.
- 사용자 식별자를 통해 해당 사용자의 잔액을 조회합니다.

5️⃣ **`주요` 결제 API**

- 결제 처리하고 결제 내역을 생성하는 API 를 작성합니다.
- 결제가 완료되면 해당 좌석의 소유권을 유저에게 배정하고 대기열 토큰을 만료시킵니다.

> 💡 KEY POINT
- 유저간 대기열을 요청 순서대로 정확하게 제공할 방법을 고민해 봅니다.
- 동시에 여러 사용자가 예약 요청을 했을 때, 좌석이 중복으로 배정 가능하지 않도록 합니다.


## Sequence Diagram

``` mermaid
sequenceDiagram
    participant 사용자
    participant 토큰
    participant 콘서트
    participant 포인트
    participant 결제
    participant 대기열
    participant DB

    Note over 사용자: 유저 토큰 발급 API
    사용자->>토큰: 유저 토큰 발급 요청
    토큰-->>DB: 토큰 발급 정보 저장
    DB-->>토큰: 토큰 발급 완료
    토큰-->>사용자: 토큰 발급 완료

    Note over 사용자: 예약 가능 날짜 조회 API
    사용자->>토큰: 토큰 검증
    토큰-->>DB: 토큰 유효성 검사
    DB-->>토큰: 토큰 유효
    토큰->>콘서트: 예약 가능 날짜 조회 요청
    콘서트-->>DB: 예약 가능 날짜 정보 조회
    DB-->>콘서트: 예약 가능 날짜 정보 제공
    콘서트-->>사용자: 예약 가능 날짜 정보 제공

    Note over 사용자: 예약 가능 좌석 조회 API
    사용자->>토큰: 토큰 검증
    토큰-->>DB: 토큰 유효성 검사
    DB-->>토큰: 토큰 유효
    토큰->>콘서트: 예약 가능 좌석 조회 요청
    콘서트-->>DB: 예약 가능 좌석 정보 조회
    DB-->>콘서트: 예약 가능 좌석 정보 제공
    콘서트-->>사용자: 예약 가능 좌석 정보 제공

    Note over 사용자: 좌석 예약 요청 API
    사용자->>토큰: 토큰 검증
    토큰-->>DB: 토큰 유효성 검사
    DB-->>토큰: 토큰 유효
    토큰->>콘서트: 좌석 예약 요청
    콘서트-->>토큰: 토큰 확인
    토큰-->>DB: 토큰 유효성 검사
    DB-->>토큰: 토큰 유효
    토큰->>콘서트: 토큰 유효
    콘서트-->>포인트: 좌석 임시 예약 및 포인트 차감 요청
    포인트-->>DB: 포인트 차감 처리
    DB-->>포인트: 포인트 차감 완료
    포인트-->>콘서트: 임시 예약 및 차감 완료
    콘서트-->>사용자: 좌석 예약 완료

    Note over 사용자: 잔액 충전 API
    사용자->>토큰: 토큰 검증
    토큰-->>DB: 토큰 유효성 검사
    DB-->>토큰: 토큰 유효
    토큰->>포인트: 잔액 충전 요청
    포인트-->>DB: 잔액 충전 처리
    DB-->>포인트: 잔액 충전 완료
    포인트-->>사용자: 잔액 충전 완료

    Note over 사용자: 잔액 조회 API
    사용자->>토큰: 토큰 검증
    토큰-->>DB: 토큰 유효성 검사
    DB-->>토큰: 토큰 유효
    토큰->>포인트: 잔액 조회 요청
    포인트-->>DB: 잔액 조회 처리
    DB-->>포인트: 잔액 조회 결과 제공
    포인트-->>사용자: 잔액 조회 결과 제공

    Note over 사용자: 결제 API
    사용자->>토큰: 토큰 검증
    토큰-->>DB: 토큰 유효성 검사
    DB-->>토큰: 토큰 유효
    토큰->>결제: 결제 요청
    결제-->>콘서트: 결제 처리 및 좌석 확정 요청
    콘서트-->>포인트: 포인트 환불 및 최종 예약 처리
    포인트-->>DB: 포인트 환불 처리
    DB-->>포인트: 포인트 환불 완료
    포인트-->>콘서트: 환불 및 예약 처리 완료
    콘서트-->>결제: 좌석 확정 완료
    결제-->>사용자: 결제 완료


```



## ERD 설계

``` mermaid
erDiagram
    USER {
        UUID id PK "NOT NULL"
        String userName
    }

    USER_POINT{
        UUID id PK "NOT NULL"
        UUID userId FK "NOT NULL"
        int point
    }

    CHARGE_HISTORY{
        UUID id PK "NOT NULL"
        UUID pointID FK "NOT NULL"
        String type
        int amount
        Timestamp created_at
    }

    CONCERT {
        Long id PK "NOT NULL"
        String concertName
        String place
    }

    CONCERT_SCHEDULE {
        UUID id PK "NOT NULL"
        UUID concertId FK "NOT NULL"
        int capacity
        Timestamp concertDate
        Timestamp opening_at
        Timestamp closing_at
    }

    CONCERT_SEAT {
        UUID id PK "NOT NUL"
        UUID concertId FK "NOT NULL"
        UUID zoneId FK "NOT NULL"
        String seatNo
        String seatStatus
    }

    RESERVATION {
        UUID id PK "NOT NULL"
        UUID userId FK "NOT NULL"
        UUID scheduleId FK "NOT NULL"
        UUID seatId
        Timestamp created_at
        Timestamp reserved_at
        Timestamp paid_at 
    }

    PAYMENT {
        UUID id PK "NOT NULL"
        UUID userId FK "NOT NULL"
        UUID reservationId FK "NOT NULL"
        int amount
        Timestamp created_at
    }

    TOKEN {
        UUID id PK "NOT NULL"
        UUID userId FK "NOT NULL"
        String token
        Timestamp created_at
        TimeStamp expires_at
    }

    USER ||--o{ USER_POINT : ""
    USER_POINT ||--|{ CHARGE_HISTORY : ""
    USER ||--o{ RESERVATION : ""
    USER ||--o{ PAYMENT : ""
    USER ||--o{ TOKEN : ""
    CONCERT ||--o{ CONCERT_SCHEDULE : ""
    CONCERT_SCHEDULE ||--o{ CONCERT_SEAT : ""
    CONCERT_SCHEDULE ||--o{ RESERVATION : ""
    CONCERT_SEAT ||--o{ RESERVATION : ""
    RESERVATION ||--|{ PAYMENT : ""
    
```