package com.torutk.gadget.support;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GadgetApplication extends Application {
    private Configuration conf = new Configuration();

    private Stage transparentStage;
    private Scene scene;
    // ドラッグでウィンドウの移動開始時のウィンドウ内マウス座標を保持
    private double dragStartX;
    private double dragStartY;

    @Override
    public void start(Stage primaryStage) throws Exception {
        transparentStage = createTransparentStage(primaryStage);
    }

    /**
     * Make primaryStage as UTILITY and invisible, create secondaryStage as TRANSPARENT.
     *
     * @param primaryStage primaryStage to be UTILITY and invisible.
     * @return secondaryStage as TRANSPARENT
     */
    private Stage createTransparentStage(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.setWidth(0);
        primaryStage.setHeight(0);
        primaryStage.setX(Double.MAX_VALUE);

        Stage secondaryStage = new Stage(StageStyle.TRANSPARENT);
        secondaryStage.initOwner(primaryStage);
        primaryStage.show();
        return secondaryStage;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        scene.setFill(Color.TRANSPARENT);
        setupDragMove();
        setupResize();
        setupContextMenu();
    }

    /**
     * コンストラクタで渡された scene 上へのドラッグ操作で stage のウィンドウを移動。
     */
    private void setupDragMove() {
        scene.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });
        scene.setOnMouseDragged(event -> {
            transparentStage.setX(event.getScreenX() - dragStartX);
            transparentStage.setY(event.getScreenY() - dragStartY);
        });
    }

    private void setupResize() {
        // Ctrl + マウスホイール操作でウィンドウの大きさを変更
        scene.setOnScroll(event -> {
            if (event.isControlDown()) {
                zoom(event.getDeltaY() > 0 ? 1.1 : 0.9);
            }
        });
        // タッチパネルのズーム（ピンチ）操作でウィンドウサイズを変更
        scene.setOnZoom(event -> zoom(event.getZoomFactor()));
    }

    /**
     * 指定した拡大率で指定した stage の大きさを変更する。
     *
     * @param factor 拡大率（1.0が等倍で、 1.0 より大で大きく、 1.0　より小で小さくする）
     */
    private void zoom(double factor) {
        double width = transparentStage.getWidth();
        double height = transparentStage.getHeight();
        double x = transparentStage.getX();
        double y = transparentStage.getY();
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double nextWidth = Math.max(width* factor, Configuration.MIN_WIDTH);
        double nextHeight = Math.max(height * factor, Configuration.MIN_HEIGHT);

        transparentStage.setWidth(nextWidth);
        transparentStage.setHeight(nextHeight);
        transparentStage.setX(centerX - nextWidth / 2);
        transparentStage.setY(centerY - nextHeight / 2);
    }

    private void setupContextMenu() {
        // マウス右クリックでポップアップメニューを表示
        ContextMenu popup = createContextMenu(transparentStage);
        scene.setOnContextMenuRequested(event -> popup.show(transparentStage, event.getScreenX(), event.getScreenY()));
    }

    /**
     * ポップアップメニューを生成する。
     *
     * @return ポップアップメニュー
     */
    private ContextMenu createContextMenu(Stage stage) {
        MenuItem exitItem = new MenuItem("終了");
        exitItem.setStyle("-fx-font-size: 2em");
        exitItem.setOnAction(event -> stage.close());
        return new ContextMenu(exitItem);
    }
}
