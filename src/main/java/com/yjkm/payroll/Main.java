package com.yjkm.payroll;

import com.yjkm.payroll.ui.EmployeeController;
import com.yjkm.payroll.util.HibernateUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    static {
        // Windows 콘솔 UTF-8 인코딩 설정
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // 시스템 프로퍼티 설정
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        logger.info("=================================================");
        logger.info("   💼 YJKM 급여관리 ERP v3.0");
        logger.info("   Real-time Payroll Management System");
        logger.info("=================================================");
        logger.info("");

        try {
            // 메인 윈도우
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // 직원 관리 탭
            EmployeeController empController = new EmployeeController();
            Tab employeeTab = new Tab("직원 관리", empController.createEmployeeView());
            employeeTab.setClosable(false);

            tabPane.getTabs().addAll(
                    employeeTab
                    // 나중에 추가할 탭들:
                    // - 근무시간 관리
                    // - 근태 관리
                    // - 공제/세금 설정
                    // - 급여 계산
                    // - 리포트
            );

            Scene scene = new Scene(tabPane, 1200, 700);
            primaryStage.setTitle("YJKM 급여관리 ERP v3.0");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> {
                logger.info("프로그램 종료");
                HibernateUtil.close();
            });
            primaryStage.show();

            logger.info("✅ 메인 윈도우 열림 (1200x700)");
            logger.info("");

        } catch (Exception e) {
            logger.error("애플리케이션 시작 실패", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
