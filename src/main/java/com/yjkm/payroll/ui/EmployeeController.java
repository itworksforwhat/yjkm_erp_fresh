package com.yjkm.payroll.ui;

import com.yjkm.payroll.model.Employee;
import com.yjkm.payroll.service.EmployeeService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService = new EmployeeService();
    private final ObservableList<Employee> employees = FXCollections.observableArrayList();

    // UI 캐러모 저장
    private TableView<Employee> employeeTable;
    private TextField searchField;
    private TextField numField;
    private TextField nameField;
    private TextField deptField;
    private TextField posField;
    private TextField wageField;
    private DatePicker hirePicker;
    private DatePicker resPicker;
    private CheckBox activeCheck;

    private Employee selectedEmployee = new Employee();

    public BorderPane createEmployeeView() {
        BorderPane root = new BorderPane();
        root.setTop(createSearchPanel());
        root.setCenter(createTablePanel());
        root.setRight(createDetailForm());
        
        // UI 먼저 보여주고 나중에 DB 로드 (백그라운드 스레드)
        Platform.runLater(this::loadEmployeesAsync);
        
        return root;
    }

    private VBox createSearchPanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        HBox searchBox = new HBox(10);
        Label label = new Label("검색 (이름/부서):");
        searchField = new TextField();
        searchField.setPromptText("이름 또는 부서 입력...");
        searchField.setPrefWidth(200);

        Button searchBtn = new Button("검색");
        searchBtn.setOnAction(e -> handleSearch());
        Button resetBtn = new Button("초기화");
        resetBtn.setOnAction(e -> handleReset());

        searchBox.getChildren().addAll(label, searchField, searchBtn, resetBtn);
        vbox.getChildren().add(searchBox);
        return vbox;
    }

    private VBox createTablePanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        employeeTable = new TableView<>();
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 컬럼 정의
        TableColumn<Employee, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        idCol.setPrefWidth(50);

        TableColumn<Employee, String> numCol = new TableColumn<>("직원번호");
        numCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmployeeNumber() != null ? c.getValue().getEmployeeNumber() : ""));
        numCol.setPrefWidth(100);

        TableColumn<Employee, String> nameCol = new TableColumn<>("이름");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName() != null ? c.getValue().getName() : ""));
        nameCol.setPrefWidth(100);

        TableColumn<Employee, String> deptCol = new TableColumn<>("부서");
        deptCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDepartment() != null ? c.getValue().getDepartment() : ""));
        deptCol.setPrefWidth(100);

        TableColumn<Employee, String> posCol = new TableColumn<>("직급");
        posCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPosition() != null ? c.getValue().getPosition() : ""));
        posCol.setPrefWidth(100);

        TableColumn<Employee, Integer> wageCol = new TableColumn<>("시급");
        wageCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getHourlyWage() != null ? c.getValue().getHourlyWage() : 0));
        wageCol.setPrefWidth(80);

        TableColumn<Employee, Boolean> activeCol = new TableColumn<>("재직");
        activeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().getIsActive() != null ? c.getValue().getIsActive() : false));
        activeCol.setPrefWidth(50);

        employeeTable.getColumns().addAll(idCol, numCol, nameCol, deptCol, posCol, wageCol, activeCol);
        employeeTable.setItems(employees);
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedEmployee = newVal;
                updateDetailForm();
            }
        });

        VBox.setVgrow(employeeTable, Priority.ALWAYS);
        vbox.getChildren().add(employeeTable);

        // 버튼
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        Button addBtn = new Button("신규");
        addBtn.setOnAction(e -> handleNewEmployee());
        Button delBtn = new Button("삭제");
        delBtn.setOnAction(e -> handleDeleteEmployee());
        Button exportBtn = new Button("엑셀로 내보내기");
        exportBtn.setOnAction(e -> logger.info("엑셀 내보내기 기능은 나중에 구현..."));

        buttonBox.getChildren().addAll(addBtn, delBtn, exportBtn);
        vbox.getChildren().add(buttonBox);

        return vbox;
    }

    private VBox createDetailForm() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
        vbox.setPrefWidth(300);

        Label titleLabel = new Label("직원 상세정보");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // 각 필드
        Label numLabel = new Label("직원번호:");
        numField = new TextField();
        numField.setEditable(false);

        Label nameLabel = new Label("이름:");
        nameField = new TextField();

        Label deptLabel = new Label("부서:");
        deptField = new TextField();

        Label posLabel = new Label("직급:");
        posField = new TextField();

        Label wageLabel = new Label("시급:");
        wageField = new TextField();

        Label hireLabel = new Label("입사일:");
        hirePicker = new DatePicker();

        Label resLabel = new Label("퇴사일:");
        resPicker = new DatePicker();

        Label activeLabel = new Label("재직 상태:");
        activeCheck = new CheckBox("재직중");

        // 저장/취소 버튼
        HBox btnBox = new HBox(10);
        Button saveBtn = new Button("저장");
        saveBtn.setPrefWidth(100);
        saveBtn.setOnAction(e -> handleSaveEmployee());
        Button cancelBtn = new Button("취소");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(e -> handleCancel());

        btnBox.getChildren().addAll(saveBtn, cancelBtn);

        vbox.getChildren().addAll(
                titleLabel,
                new Separator(),
                numLabel, numField,
                nameLabel, nameField,
                deptLabel, deptField,
                posLabel, posField,
                wageLabel, wageField,
                hireLabel, hirePicker,
                resLabel, resPicker,
                activeLabel, activeCheck,
                new Separator(),
                btnBox
        );

        return vbox;
    }

    // 백그라운드에서 직원 로드 (UI 블로킹 안 함)
    private void loadEmployeesAsync() {
        Thread thread = new Thread(() -> {
            try {
                var empList = employeeService.getAllEmployees();
                Platform.runLater(() -> {
                    employees.clear();
                    employees.addAll(empList);
                    logger.info("✅ 직원 목록 로드 완료: {} 명", employees.size());
                });
            } catch (Exception e) {
                logger.warn("⚠️ 직원 목록 로드 실패 (무시 가능): {}", e.getMessage());
                // 데이터베이스가 없어도 UI는 계속 작동
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void loadEmployees() {
        try {
            employees.clear();
            employees.addAll(employeeService.getAllEmployees());
            logger.info("✅ 직원 목록 로드 완료: {} 명", employees.size());
        } catch (Exception e) {
            logger.error("❌ 직원 목록 로드 실패", e);
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadEmployees();
            return;
        }
        try {
            employees.clear();
            employees.addAll(employeeService.searchEmployees(keyword));
            logger.info("✅ 검색 완료: {} 건", employees.size());
        } catch (Exception e) {
            logger.error("❌ 검색 실패", e);
        }
    }

    private void handleReset() {
        searchField.clear();
        loadEmployees();
    }

    private void handleNewEmployee() {
        selectedEmployee = new Employee();
        clearDetailForm();
    }

    private void handleDeleteEmployee() {
        if (selectedEmployee == null || selectedEmployee.getId() == null) {
            showWarning("삭제할 직원을 선택하세요");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("삭제 확인");
        confirm.setHeaderText("정말 삭제하시겠습니까?");
        confirm.setContentText(selectedEmployee.getName());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                employeeService.deleteEmployee(selectedEmployee.getId());
                loadEmployees();
                clearDetailForm();
                logger.info("✅ 직원 삭제 완료");
            } catch (Exception e) {
                logger.error("❌ 삭제 실패", e);
            }
        }
    }

    private void updateDetailForm() {
        if (selectedEmployee == null) return;
        numField.setText(selectedEmployee.getEmployeeNumber() != null ? selectedEmployee.getEmployeeNumber() : "");
        nameField.setText(selectedEmployee.getName() != null ? selectedEmployee.getName() : "");
        deptField.setText(selectedEmployee.getDepartment() != null ? selectedEmployee.getDepartment() : "");
        posField.setText(selectedEmployee.getPosition() != null ? selectedEmployee.getPosition() : "");
        wageField.setText(selectedEmployee.getHourlyWage() != null ? selectedEmployee.getHourlyWage().toString() : "");
        hirePicker.setValue(selectedEmployee.getHireDate());
        resPicker.setValue(selectedEmployee.getResignationDate());
        activeCheck.setSelected(selectedEmployee.getIsActive() != null && selectedEmployee.getIsActive());
    }

    private void handleSaveEmployee() {
        if (nameField.getText().trim().isEmpty() || numField.getText().trim().isEmpty()) {
            showWarning("이름과 직원번호를 입력하세요");
            return;
        }
        try {
            if (selectedEmployee.getId() == null) {
                selectedEmployee = new Employee();
            }
            selectedEmployee.setEmployeeNumber(numField.getText().trim());
            selectedEmployee.setName(nameField.getText().trim());
            selectedEmployee.setDepartment(deptField.getText().trim());
            selectedEmployee.setPosition(posField.getText().trim());
            selectedEmployee.setHourlyWage(Integer.parseInt(wageField.getText().trim()));
            selectedEmployee.setHireDate(hirePicker.getValue());
            selectedEmployee.setResignationDate(resPicker.getValue());
            selectedEmployee.setIsActive(activeCheck.isSelected());

            employeeService.saveEmployee(selectedEmployee);
            loadEmployees();
            clearDetailForm();
            logger.info("✅ 직원 저장 완료: {}", selectedEmployee.getName());
        } catch (NumberFormatException e) {
            showError("오류", "시급은 숫자로 입력하세요");
        } catch (Exception e) {
            logger.error("❌ 저장 실패", e);
            showError("정장", e.getMessage());
        }
    }

    private void handleCancel() {
        selectedEmployee = new Employee();
        clearDetailForm();
    }

    private void clearDetailForm() {
        numField.clear();
        nameField.clear();
        deptField.clear();
        posField.clear();
        wageField.clear();
        hirePicker.setValue(null);
        resPicker.setValue(null);
        activeCheck.setSelected(false);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("경고");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
