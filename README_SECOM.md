# 세콤 연동 기능 사용 방법

## 🚀 빠른 시작

### 1. 프로그램 실행
```bash
cd /home/user/yjkm_erp_fresh
mvn javafx:run
```

### 2. 세콤 연동 설정
1. 프로그램 실행 후 **"세콤 연동 설정"** 탭 클릭
2. MySQL 연결 정보 입력:
   ```
   MySQL 서버 주소: localhost (또는 세콤 서버 IP)
   MySQL 포트: 3306
   데이터베이스 이름: secomdb
   MySQL 사용자명: your_username
   MySQL 비밀번호: your_password
   출퇴근 테이블 이름: TB_INOUT
   ✓ 이 설정 활성화
   ```
3. **"연결 테스트"** 버튼 클릭
4. 연결 성공 확인 후 **"저장"** 클릭

### 3. 출퇴근 데이터 가져오기
1. **"출퇴근 관리"** 탭 클릭
2. 조회 기간 선택 (예: 2026-01-01 ~ 2026-01-31)
3. **"세콤 데이터 가져오기"** 버튼 클릭
4. 결과 확인

---

## ⚠️ 주의사항

### 세콤 DB 컬럼명 확인 필수!

현재 코드는 다음 영문 컬럼명을 가정합니다:
- `EMP_NO` (사번)
- `EMP_NAME` (성명)
- `INOUT_TIME` (출입일시)
- `INOUT_TYPE` (출입구분)

**실제 세콤 DB 컬럼명이 다르다면** 반드시 수정해야 합니다!

#### 컬럼명 확인 방법:
```sql
-- MySQL에 접속 후
USE secomdb;
DESC TB_INOUT;
SELECT * FROM TB_INOUT LIMIT 5;
```

#### 컬럼명 수정 위치:
`src/main/java/com/yjkm/payroll/service/SecomSyncService.java` 파일의 **79-86번째 줄**

```java
// 실제 컬럼명으로 수정 예시:
sql.append("사번, ");          // 한글 컬럼명인 경우
sql.append("성명, ");
sql.append("출입일시, ");
sql.append("출입구분 ");
sql.append("FROM ").append(tableName).append(" ");
sql.append("WHERE DATE(출입일시) >= '").append(startDate).append("' ");
sql.append("AND DATE(출입일시) <= '").append(endDate).append("' ");
sql.append("ORDER BY 사번, 출입일시");
```

---

## 🔧 문제 해결

### "실행이 안 돼요!"

#### 1단계: 컴파일 확인
```bash
mvn clean compile
```

오류가 발생하면 오류 메시지를 확인하세요.

#### 2단계: 실행
```bash
mvn javafx:run
```

#### 3단계: 로그 확인
프로그램 실행 시 콘솔에 출력되는 로그를 확인하세요.

### "연결 테스트가 실패해요!"

#### MySQL 서버 확인:
```bash
# MySQL 서버 연결 테스트
mysql -h localhost -P 3306 -u your_username -p
```

#### 방화벽 확인:
```bash
# 포트 확인
telnet localhost 3306
```

#### MySQL 권한 확인:
```sql
SHOW GRANTS FOR 'your_username'@'%';
-- SELECT 권한이 있어야 함
```

### "데이터가 가져와지지 않아요!"

#### 1. 직원 사번 확인
- **직원 관리** 탭에서 직원들이 등록되어 있는지 확인
- 사번이 세콤 DB의 사번과 일치하는지 확인

#### 2. 세콤 DB 데이터 확인:
```sql
SELECT * FROM TB_INOUT
WHERE DATE(INOUT_TIME) >= '2026-01-01'
AND DATE(INOUT_TIME) <= '2026-01-31'
LIMIT 10;
```

#### 3. 사번 형식 확인:
```sql
-- 세콤 DB의 사번 형식
SELECT DISTINCT EMP_NO FROM TB_INOUT ORDER BY EMP_NO LIMIT 20;
```

---

## 📚 상세 가이드

더 자세한 설정 방법은 **`SECOM_SETUP_GUIDE.md`** 파일을 참고하세요!

---

## 🐛 버그 리포트

문제가 발생하면 다음 정보와 함께 리포트해주세요:

1. **오류 메시지** (전체 텍스트)
2. **실행 환경**:
   - OS: Linux / Windows / Mac
   - Java 버전: `java -version`
   - Maven 버전: `mvn -version`
3. **세콤 DB 정보**:
   - MySQL 버전
   - 테이블 구조 (DESC TB_INOUT)
   - 샘플 데이터 (개인정보 제외)
4. **로그 파일** (콘솔 출력)

---

**버전**: v3.0.0
**최종 수정일**: 2026-01-12
