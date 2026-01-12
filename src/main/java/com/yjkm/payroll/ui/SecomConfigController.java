package com.yjkm.payroll.ui;

import com.yjkm.payroll.model.SecomConfig;
import com.yjkm.payroll.service.SecomConfigService;
import com.yjkm.payroll.util.SecomConnectionUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 세콤 연동 설정 화면 컨트롤러
 */
public class SecomConfigController {
    private static final Logger logger = LoggerFactory.getLogger(SecomConfigController.class);
    private final SecomConfigService configService = new SecomConfigService();

    private TextField serverAddressField;
    private TextField serverPortField;
    private TextField databaseNameField;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField tableNameField;
    private CheckBox activeCheckBox;
    private Label statusLabel;

    private SecomConfig currentConfig;

    /**
    * 세콤 설정 화면 생성
     */
    public VBox createSecomConfigView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // 제목
        Label titleLabel = new Label("세콤 연동 설정");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 설명
        Label descLabel = new Label("세콤 매니저(에스원) MySQL 데이터베이스 연동 설정을 관리합니다.");
        descLabel.setStyle("-fx-text-fill: gray;");

        // 설정 폼
        GridPane formGrid = createConfigForm();

        // 버튼 영역
        HBox buttonBox = createButtonBox();

        // 상태 레이블
        statusLabel = new Label();
        statusLabel.setWrapText(true);

        root.getChildren().addAll(titleLabel, descLabel, new Separator(), formGrid, buttonBox, statusLabel);

        // 기존 설정 로드
        loadActiveConfig();

        return root;
    }

    /**
     * 설정 폼 생성
     */
    private GridPane createConfigForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        int row = 0;

        // MySQL 서버 주소
        grid.add(new Label("MySQL 서버 주소: *"), 0, row);
        serverAddressField = new TextField();
        serverAddressField.setPromptText("예: localhost 또는 192.168.0.100");
        serverAddressField.setPrefWidth(300);
        grid.add(serverAddressField, 1, row++);

        // 서버 포트
        grid.add(new Label("MySQL 포트:"), 0, row);
        serverPortField = new TextField("3306");
        serverPortField.setPromptText("기본값: 3306");
        grid.add(serverPortField, 1, row++);

        // 데이터베이스 이름
        grid.add(new Label("데이터베이스 이름: *"), 0, row);
        databaseNameField = new TextField("secomdb");
        databaseNameField.setPromptText("예: secomdb");
        grid.add(databaseNameField, 1, row++);

        // 사용자명
        grid.add(new Label("MySQL 사용자명: *"), 0, row);
        usernameField = new TextField();
        usernameField.setPromptText("예: secom_user");
        grid.add(usernameField, 1, row++);

        // 비밀번호
        grid.add(new Label("MySQL 비밀번호: *"), 0, row);
        passwordField = new PasswordField();
        passwordField.setPromptText("데이터베이스 비밀번호");
        grid.add(passwordField, 1, row++);

        // 구분선
        Separator sep2 = new Separator();
        GridPane.setColumnSpan(sep2, 2);
        grid.add(sep2, 0, row++);

        // 테이블 이름
        grid.add(new Label("출퇴근 테이블 이름:"), 0, row);
        tableNameField = new TextField("TB_INOUT");
        tableNameField.setPromptText("예: TB_INOUT");
        grid.add(tableNameField, 1, row++);

        // 활성화 여부
        activeCheckBox = new CheckBox("이 설정 활성화");
        GridPane.setColumnSpan(activeCheckBox, 2);
        grid.add(activeCheckBox, 0, row++);

        return grid;
    }

    /**
     * 버튼 영역 생성
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button testButton = new Button("연결 테스트");
        testButton.setOnAction(e -> testConnection());

        Button saveButton = new Button("저장");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> saveConfig());

        Button resetButton = new Button("초기화");
        resetButton.setOnAction(e -> resetForm());

        buttonBox.getChildren().addAll(testButton, saveButton, resetButton);

        return buttonBox;
    }

    /**
     * 활성화된 설정 로드
     */
    private void loadActiveConfig() {
        try {
            currentConfig = configService.getActiveConfig();
            if (currentConfig != null) {
                serverAddressField.setText(currentConfig.getServerAddress() != null ? currentConfig.getServerAddress() : "");
                serverPortField.setText(currentConfig.getServerPort() != null ? currentConfig.getServerPort().toString() : "3306");
                databaseNameField.setText(currentConfig.getDatabaseName());
                usernameField.setText(currentConfig.getUsername() != null ? currentConfig.getUsername() : "");
                passwordField.setText(currentConfig.getPassword() != null ? currentConfig.getPassword() : "");
                tableNameField.setText(currentConfig.getTableName());
                activeCheckBox.setSelected(currentConfig.getIsActive());

                showStatus("기존 설정을 불러왔습니다.", false);
            }
        } catch (Exception e) {
            logger.error("설정 로드 실패", e);
            showStatus("설정 로드 실패: " + e.getMessage(), true);
        }
    }

    /**
     * 연결 테스트
     */
    private void testConnection() {
        try {
            SecomConfig testConfig = createConfigFromForm();
            testConfig.setIsActive(true); // 테스트를 위해 임시로 활성화

            boolean success = SecomConnectionUtil.testConnection(testConfig);

            if (success) {
                showStatus("✓ 연결 성공! 세콤 데이터베이스에 정상적으로 연결되었습니다.", false);
            } else {
                showStatus("✗ 연결 실패. 설정을 확인해주세요.", true);
            }

        } catch (Exception e) {
            logger.error("연결 테스트 실패", e);
            showStatus("✗ 연결 실패: " + e.getMessage(), true);
        }
    }

    /**
     * 설정 저장
     */
    private void saveConfig() {
        try {
            // 입력 검증
            if (serverAddressField.getText().trim().isEmpty()) {
                showStatus("MySQL 서버 주소를 입력해주세요.", true);
                return;
            }

            if (databaseNameField.getText().trim().isEmpty()) {
                showStatus("데이터베이스 이름을 입력해주세요.", true);
                return;
            }

            if (usernameField.getText().trim().isEmpty()) {
                showStatus("MySQL 사용자명을 입력해주세요.", true);
                return;
            }

            if (passwordField.getText().trim().isEmpty()) {
                showStatus("MySQL 비밀번호를 입력해주세요.", true);
                return;
            }

            if (tableNameField.getText().trim().isEmpty()) {
                showStatus("테이블 이름을 입력해주세요.", true);
                return;
            }

            SecomConfig config = createConfigFromForm();

            if (currentConfig != null) {
                config.setId(currentConfig.getId());
            }

            currentConfig = configService.saveConfig(config);
            showStatus("✓ 설정이 저장되었습니다.", false);

        } catch (Exception e) {
            logger.error("설정 저장 실패", e);
            showStatus("✗ 설정 저장 실패: " + e.getMessage(), true);
        }
    }

    /**
     * 폼에서 SecomConfig 객체 생성
     */
    private SecomConfig createConfigFromForm() {
        SecomConfig config = new SecomConfig();

        config.setServerAddress(serverAddressField.getText().trim());

        // 포트 번호 처리 (기본값: 3306)
        if (!serverPortField.getText().trim().isEmpty()) {
            try {
                config.setServerPort(Integer.parseInt(serverPortField.getText().trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("포트 번호는 숫자여야 합니다.");
            }
        } else {
            config.setServerPort(3306);
        }

        config.setDatabaseName(databaseNameField.getText().trim());
        config.setUsername(usernameField.getText().trim());
        config.setPassword(passwordField.getText().trim());
        config.setTableName(tableNameField.getText().trim());
        config.setIsActive(activeCheckBox.isSelected());

        return config;
    }

    /**
     * 폼 초기화
     */
    private void resetForm() {
        serverAddressField.clear();
        serverPortField.setText("3306");
        databaseNameField.setText("secomdb");
        usernameField.clear();
        passwordField.clear();
        tableNameField.setText("TB_INOUT");
        activeCheckBox.setSelected(false);
        currentConfig = null;
        statusLabel.setText("");
    }

    /**
     * 상태 메시지 표시
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
    }
}
