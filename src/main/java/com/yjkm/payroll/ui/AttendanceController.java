package com.yjkm.payroll.ui;

import com.yjkm.payroll.model.Attendance;
import com.yjkm.payroll.model.SecomConfig;
import com.yjkm.payroll.service.SecomConfigService;
import com.yjkm.payroll.service.SecomSyncService;
import com.yjkm.payroll.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 출퇴근 관리 화면 컨트롤러
 */
public class AttendanceController {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    private final SecomConfigService configService = new SecomConfigService();
    private final SecomSyncService syncService = new SecomSyncService();

    private TableView<Attendance> attendanceTable;
    private ObservableList<Attendance> attendanceData;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;

    /**
     * 출퇴근 관리 화면 생성
     */
    public VBox createAttendanceView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // 제목
        Label titleLabel = new Label("출퇴근 관리");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 검색 및 동기화 영역
        HBox controlBox = createControlBox();

        // 테이블
        attendanceTable = createAttendanceTable();

        // 상태 영역
        HBox statusBox = createStatusBox();

        root.getChildren().addAll(titleLabel, new Separator(), controlBox, attendanceTable, statusBox);
        VBox.setVgrow(attendanceTable, Priority.ALWAYS);

        // 초기 데이터 로드
        loadAttendanceData();

        return root;
    }

    /**
     * 컨트롤 영역 생성
     */
    private HBox createControlBox() {
        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER_LEFT);
        controlBox.setPadding(new Insets(10));
        controlBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        // 날짜 범위 선택
        Label dateLabel = new Label("조회 기간:");

        startDatePicker = new DatePicker(LocalDate.now().minusDays(30));
        startDatePicker.setPrefWidth(150);

        Label toLabel = new Label("~");

        endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPrefWidth(150);

        Button searchButton = new Button("조회");
        searchButton.setOnAction(e -> loadAttendanceData());

        // 구분선
        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // 세콤 동기화 버튼
        Button syncButton = new Button("세콤 데이터 가져오기");
        syncButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        syncButton.setOnAction(e -> syncFromSecom());

        controlBox.getChildren().addAll(
                dateLabel, startDatePicker, toLabel, endDatePicker, searchButton,
                sep, syncButton
        );

        return controlBox;
    }

    /**
     * 출퇴근 테이블 생성
     */
    private TableView<Attendance> createAttendanceTable() {
        TableView<Attendance> table = new TableView<>();
        attendanceData = FXCollections.observableArrayList();
        table.setItems(attendanceData);

        // 사번 컬럼
        TableColumn<Attendance, String> empNoCol = new TableColumn<>("사번");
        empNoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEmployee().getEmployeeNumber()
                )
        );
        empNoCol.setPrefWidth(100);

        // 이름 컬럼
        TableColumn<Attendance, String> nameCol = new TableColumn<>("이름");
        nameCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEmployee().getName()
                )
        );
        nameCol.setPrefWidth(100);

        // 근무일 컬럼
        TableColumn<Attendance, String> dateCol = new TableColumn<>("근무일");
        dateCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getWorkDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
        );
        dateCol.setPrefWidth(120);

        // 출근시간 컬럼
        TableColumn<Attendance, String> checkInCol = new TableColumn<>("출근시간");
        checkInCol.setCellValueFactory(data -> {
            if (data.getValue().getCheckIn() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCheckIn().format(DateTimeFormatter.ofPattern("HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        checkInCol.setPrefWidth(100);

        // 퇴근시간 컬럼
        TableColumn<Attendance, String> checkOutCol = new TableColumn<>("퇴근시간");
        checkOutCol.setCellValueFactory(data -> {
            if (data.getValue().getCheckOut() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCheckOut().format(DateTimeFormatter.ofPattern("HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        checkOutCol.setPrefWidth(100);

        // 근무시간 컬럼
        TableColumn<Attendance, String> workTimeCol = new TableColumn<>("근무시간");
        workTimeCol.setCellValueFactory(data -> {
            Integer minutes = data.getValue().getWorkMinutes();
            if (minutes != null && minutes > 0) {
                int hours = minutes / 60;
                int mins = minutes % 60;
                return new javafx.beans.property.SimpleStringProperty(
                        String.format("%d시간 %d분", hours, mins)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        workTimeCol.setPrefWidth(120);

        // 부서 컬럼
        TableColumn<Attendance, String> deptCol = new TableColumn<>("부서");
        deptCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEmployee().getDepartment()
                )
        );
        deptCol.setPrefWidth(100);

        table.getColumns().addAll(empNoCol, nameCol, dateCol, checkInCol, checkOutCol, workTimeCol, deptCol);

        return table;
    }

    /**
     * 상태 영역 생성
     */
    private HBox createStatusBox() {
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label();
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(20, 20);
        progressIndicator.setVisible(false);

        statusBox.getChildren().addAll(progressIndicator, statusLabel);

        return statusBox;
    }

    /**
     * 출퇴근 데이터 로드
     */
    private void loadAttendanceData() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showStatus("조회 기간을 선택해주세요.", true);
                return;
            }

            if (startDate.isAfter(endDate)) {
                showStatus("시작일이 종료일보다 늦을 수 없습니다.", true);
                return;
            }

            List<Attendance> attendances = em.createQuery(
                            "SELECT a FROM Attendance a " +
                                    "WHERE a.workDate >= :startDate AND a.workDate <= :endDate " +
                                    "ORDER BY a.workDate DESC, a.employee.employeeNumber",
                            Attendance.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();

            attendanceData.clear();
            attendanceData.addAll(attendances);

            showStatus(String.format("총 %d건의 출퇴근 기록을 조회했습니다.", attendances.size()), false);

        } catch (Exception e) {
            logger.error("출퇴근 데이터 로드 실패", e);
            showStatus("데이터 조회 실패: " + e.getMessage(), true);
        } finally {
            em.close();
        }
    }

    /**
     * 세콤 데이터 동기화
     */
    private void syncFromSecom() {
        // 세콤 설정 확인
        SecomConfig config = configService.getActiveConfig();
        if (config == null) {
            showStatus("활성화된 세콤 설정이 없습니다. 먼저 세콤 설정을 완료해주세요.", true);
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showStatus("조회 기간을 선택해주세요.", true);
            return;
        }

        // 비동기 처리
        progressIndicator.setVisible(true);
        showStatus("세콤 데이터를 가져오는 중...", false);

        new Thread(() -> {
            try {
                // 세콤 데이터 가져오기
                List<Map<String, Object>> secomData = syncService.fetchSecomData(config, startDate, endDate);

                if (secomData.isEmpty()) {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        showStatus("조회된 세콤 데이터가 없습니다.", false);
                    });
                    return;
                }

                // 로컬 DB에 동기화
                int savedCount = syncService.syncToAttendance(secomData);

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showStatus(String.format("✓ 세콤 데이터 %d건을 성공적으로 가져왔습니다.", savedCount), false);
                    loadAttendanceData(); // 테이블 새로고침
                });

            } catch (Exception e) {
                logger.error("세콤 데이터 동기화 실패", e);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showStatus("✗ 세콤 데이터 가져오기 실패: " + e.getMessage(), true);

                    // 에러 상세 정보 표시
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("세콤 동기화 오류");
                    alert.setHeaderText("세콤 데이터를 가져오는 중 오류가 발생했습니다.");
                    alert.setContentText(e.getMessage() + "\n\n세콤 설정을 확인하거나, 로그를 확인해주세요.");
                    alert.showAndWait();
                });
            }
        }).start();
    }

    /**
     * 상태 메시지 표시
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
    }
}
