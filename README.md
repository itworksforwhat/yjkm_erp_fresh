# 💼 YJKM 급여관리 ERP v3.0

**실제 사용 가능한 급여관리 시스템** - Fresh Start Edition

---

## 📋 현재 구현 상태

### ✅ 1단계 완성
- [x] JavaFX GUI 메인 프레임
- [x] 직원 관리 (등록/수정/삭제/검색)
- [x] SQLite + Hibernate ORM 연결
- [x] 데이터베이스 자동 생성 및 관리

### ⏳ 2단계 (예정)
- [ ] 근무시간/스케줄 관리
- [ ] 근태 관리 (출퇴근 기록)
- [ ] SECOM 파일 임포트

### ⏳ 3단계 (예정)
- [ ] 공제항목 설정 (4대 보험, 사우회비 등)
- [ ] 소득세/지방세 설정

### ⏳ 4단계 (예정)
- [ ] 급여 계산 엔진
- [ ] 급여 결과 조회
- [ ] 엑셀/PDF 내보내기

---

## 🚀 빠른 시작

### 요구사항
- **Java 17+**
- **Maven 3.6+**

### 설치 및 실행

```bash
# 1. 저장소 클론
git clone https://github.com/itworksforwhat/yjkm_erp_fresh.git
cd yjkm_erp_fresh

# 2. 빌드
mvn clean compile

# 3. 실행
mvn javafx:run
```

---

## 📁 프로젝트 구조

```
yjkm_erp_fresh/
├── pom.xml                           # Maven 설정
├── src/
│   ├── main/
│   │   ├── java/com/yjkm/payroll/
│   │   │   ├── Main.java             # JavaFX 메인 애플리케이션
│   │   │   ├── model/                # Entity 클래스
│   │   │   │   ├── Employee.java
│   │   │   │   ├── Attendance.java
│   │   │   │   ├── Deduction.java
│   │   │   │   └── Payroll.java
│   │   │   ├── service/              # 비즈니스 로직
│   │   │   │   └── EmployeeService.java
│   │   │   ├── ui/                   # GUI 컨트롤러
│   │   │   │   ├── EmployeeController.java
│   │   │   │   ├── AttendanceController.java (예정)
│   │   │   │   ├── ScheduleController.java (예정)
│   │   │   │   ├── PayrollController.java (예정)
│   │   │   │   └── SettingsController.java (예정)
│   │   │   └── util/                 # 유틸리티
│   │   │       └── HibernateUtil.java
│   │   └── resources/
│   │       ├── persistence.xml       # Hibernate 설정
│   │       └── logback.xml           # 로깅 설정
│   └── test/
└── payroll.db                        # SQLite 데이터베이스 (자동 생성)
```

---

## 🎯 주요 기능

### 1. 직원 관리
- **등록**: 직원번호, 이름, 부서, 직급, 시급, 입사일, 퇴사일
- **검색**: 이름/부서로 검색
- **수정**: 선택한 직원의 정보 수정
- **삭제**: 직원 삭제 (확인 팝업)
- **조회**: 전체 직원 목록 (테이블)

### 2. 데이터베이스
- **SQLite**: 로컬 파일 기반 DB (payroll.db)
- **Hibernate ORM**: 자동 스키마 생성/관리
- **자동 마이그레이션**: 엔티티 변경 시 DB 자동 업데이트

---

## 📋 다음 단계

### 2단계 구현 예정 항목
1. 근무시간(워크시간) 관리
   - 주간/야간/2교대 등 근무형태 설정
   - 시작시간, 종료시간, 휴게시간 설정

2. 근태 관리
   - 월별 근무기록 입력/수정
   - 출근시간, 퇴근시간 자동 계산
   - SECOM 파일로부터 자동 임포트

### 기술 선택사항
- **JavaFX Material Design** (UI 개선용)
- **Apache POI** (엑셀 내보내기)
- **iText** (PDF 생성)

---

## 🔧 개발 가이드

### 새 기능 추가하기

1. **Entity 추가**: `model/` 폴더에 클래스 작성
   - `persistence.xml`에 등록
   - Hibernate가 자동으로 테이블 생성

2. **Service 추가**: `service/` 폴더에 클래스 작성
   - CRUD 메서드 구현
   - `HibernateUtil.getEntityManager()` 사용

3. **Controller 추가**: `ui/` 폴더에 클래스 작성
   - JavaFX Scene 생성
   - Service 호출
   - 이벤트 처리

4. **Main.java에 탭 추가**:
   ```java
   NewController controller = new NewController();
   Tab newTab = new Tab("새 탭 이름", controller.createView());
   tabPane.getTabs().add(newTab);
   ```

---

## 🐛 문제 해결

### "Module not found" 오류
```bash
mvn clean install
```

### 한글이 깨져 나올 때
- UTF-8 인코딩이 이미 설정되어 있습니다.
- IDE: Windows → 환경 → 일반 → 콘솔 → 기본 콘솔 인코딩을 UTF-8로 설정

### 데이터베이스 초기화
```bash
# payroll.db 파일 삭제
rm payroll.db

# 다시 실행하면 자동 생성됨
mvn javafx:run
```

---

## 📞 지원

문제가 발생하면 로그를 확인하세요:
- 콘솔 출력
- `payroll.log` 파일 (있으면)

---

**버전**: 3.0.0  
**라이선스**: MIT  
**마지막 업데이트**: 2026-01-11  
