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

    private TextField dsnNameField;
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
        Label descLabel = new Label("세콤 매니저(에스원) 데이터베이스 연동 설정을 관리합니다.");
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

        // DSN 이름
        grid.add(new Label("ODBC DSN 이름:"), 0, row);
        dsnNameField = new TextField();
        dsnNameField.setPromptText("예: secomdb");
        dsnNameField.setPrefWidth(300);
        grid.add(dsnNameField, 1, row++);

        // 구분선
        Separator sep1 = new Separator();
        GridPane.setColumnSpan(sep1, 2);
        grid.add(sep1, 0, row++);

        Label orLabel = new Label("또는 직접 서버 정보 입력:");
        orLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        GridPane.setColumnSpan(orLabel, 2);
        grid.add(orLabel, 0, row++);

        // 서버 주소
        grid.add(new Label("서버 주소:"), 0, row);
        serverAddressField = new TextField();
        serverAddressField.setPromptText("예: localhost 또는 192.168.0.100");
        serverAddressField.setPrefWidth(300);
        grid.add(serverAddressField, 1, row++);

        // 서버 포트
        grid.add(new Label("서버 포트:"), 0, row);
        serverPortField = new TextField();
        serverPortField.setPromptText("예: 1433 (MS SQL Server)");
        grid.add(serverPortField, 1, row++);

        // 데이터베이스 이름
        grid.add(new Label("데이터베이스 이름:"), 0, row);
        databaseNameField = new TextField("secomdb");
        grid.add(databaseNameField, 1, row++);

        // 사용자명
        grid.add(new Label("사용자명:"), 0, row);
        usernameField = new TextField();
        usernameField.setPromptText("(선택사항)");
        grid.add(usernameField, 1, row++);

        // 비밀번호
        grid.add(new Label("비밀번호:"), 0, row);
        passwordField = new PasswordField();
        passwordField.setPromptText("(선택사항)");
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
                dsnNameField.setText(currentConfig.getDsnName());
                serverAddressField.setText(currentConfig.getServerAddress() != null ? currentConfig.getServerAddress() : "");
                serverPortField.setText(currentConfig.getServerPort() != null ? currentConfig.getServerPort().toString() : "");
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
            if (dsnNameField.getText().trim().isEmpty() &&
                    (serverAddressField.getText().trim().isEmpty() || databaseNameField.getText().trim().isEmpty())) {
                showStatus("DSN 이름 또는 서버 정보를 입력해주세요.", true);
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

        config.setDsnName(dsnNameField.getText().trim());
        config.setServerAddress(serverAddressField.getText().trim().isEmpty() ? null : serverAddressField.getText().trim());

        if (!serverPortField.getText().trim().isEmpty()) {
            try {
                config.setServerPort(Integer.parseInt(serverPortField.getText().trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("포트 번호는 숫자여야 합니다.");
            }
        }

        config.setDatabaseName(databaseNameField.getText().trim());
        config.setUsername(usernameField.getText().trim().isEmpty() ? null : usernameField.getText().trim());
        config.setPassword(passwordField.getText().trim().isEmpty() ? null : passwordField.getText().trim());
        config.setTableName(tableNameField.getText().trim());
        config.setIsActive(activeCheckBox.isSelected());

        return config;
    }

    /**
     * 폼 초기화
     */
    private void resetForm() {
        dsnNameField.clear();
        serverAddressField.clear();
        serverPortField.clear();
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
