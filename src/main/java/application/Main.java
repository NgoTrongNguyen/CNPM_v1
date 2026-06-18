package application;

import database.DB_manager;
import database.DB_manager.DbMode;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import navigation.AppRouter;

public class Main extends Application {

    // ================================================================
    //  ▼▼▼  SWITCH DEPLOYMENT TARGET HERE  ▼▼▼
    //
    //   DbMode.LOCAL  →  MS SQL Server (local machine)
    //                    .env keys: LOCAL_DB_URL, LOCAL_DB_USER, LOCAL_DB_PASSWORD
    //
    //   DbMode.CLOUD  →  Supabase / PostgreSQL
    //                    .env keys: CLOUD_DB_URL,  CLOUD_DB_USER,  CLOUD_DB_PASSWORD
    // ================================================================
    private static final DbMode DB_MODE = DbMode.LOCAL;
    // ================================================================

    @Override
    public void init() throws Exception {
        DB_manager.setMode(DB_MODE);
        DB_manager.init();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // ← THÊM DÒNG NÀY trước khi load bất kỳ FXML nào
        AppRouter.getInstance().init(stage);

        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("/view/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setScene(scene);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        DB_manager.shutdown();
    }
}
