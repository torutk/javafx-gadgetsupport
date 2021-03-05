/*
 * © 2017 TAKAHASHI,Toru
 */
package com.torutk.gadget.support;

import java.util.prefs.Preferences;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * ガジェットプログラムに共通する振る舞いをユーティリティとして提供するクラス。
 * <p>
 * 主な機能は次のとおり。
 * <ul>
 * <li>マウスドラッグによるウィンドウの移動
 * <li>Ctrlキー+マウスホイールによるウィンドウの大きさ変更
 * <li>ピンチ操作によるウィンドウの大きさ変更
 * <li>ポップアップメニューからウィンドウ終了
 * <li>終了時に位置・大きさを保存し、起動時に復元
 * <li>OSのタスクバーに実行中のガジェットプログラムを表示させない
 * </ul>
 * <p>
 * 使用方法
 * <ol>
 * <li>保存と復元機能を使わない場合
 * <pre>{@code
 * public class GadgetSample extends Application {
 *     public void start(Stage primaryStage) {
 *         var support = TinyGadgetSupport.of(primaryStage);
 *         var root = ...
 *         var scene = new Scene(root);
 *         var stage = support.getTransparentStage();
 *         stage.setScene(scene);
 *         stage.show();
 *     }
 * }
 * }</pre>
 * <li>保存と復元機能を使う場合
 * <pre>{@code
 * public class GadgetSample extends Application {
 *     public void start(Stage primaryStage) {
 *         var support = TinyGadgetSupport.of(stage, Preferences.userNodeForPackage(this.getClass()));
 *         :
 *     }
 * }
 * }</pre>
 * <li>OSのタスクバーに実行中のガジェットプログラムを表示させない場合
 * <pre>{@code
 * public class GadgetSample extends Application {
 *     public void start(Stage primaryStage) {
 *         var support = TinyGadgetSupport.ofTaskbarless(primaryStage);
 *         var stage = support.getTransparentStage();
 *         :
 * }</pre>
 * </ol>
 */
public class TinyGadgetSupport {
    private static final int MIN_WIDTH = 128;
    private static final int MIN_HEIGHT = 128;

    // taskbarに非表示なガジェットでは primaryStage を不可視な Utility スタイルとする
    private Stage utilityStage;
    private Stage transparentStage;
    private Scene scene;
    
    // ドラッグでウィンドウの移動開始時のウィンドウ内マウス座標を保持
    private double dragStartX;
    private double dragStartY;
    // 設定の保存で使用するプリファレンスとキー
    private Preferences prefs;
    private static final String KEY_STAGE_X = "stageX";
    private static final String KEY_STAGE_Y = "stageY";
    private static final String KEY_STAGE_WIDTH = "stageWidth";
    private static final String KEY_STAGE_HEIGHT = "stageHeight";

    /**
     * OSのタスクバーに非表示なガジェット用のインスタンスを生成するファクトリメソッド。
     *
     * 指定した primaryStage を StageStyle.UTILITY に設定し、不可視にし、第二の Stage を生成し、これに対して
     * ガジェットプログラムの振る舞いを提供する。
     * <p>
     * 本ファクトリメソッドを使用した場合、終了時の状態保存と起動時の復元は行わない。
     * @param primaryStage JavaFX Application派生クラスのstartメソッドに渡されるトップレベルstage
     * @return タスクバーに非表示なガジェット用の本クラスのインスタンス
     */
    public static TinyGadgetSupport ofTaskbarless(Stage primaryStage) {
        TinyGadgetSupport support = new TinyGadgetSupport();
        support.setAsUtilityStage(primaryStage);
        return support;
    }

    /**
     * OSのタスクバーに非表示なガジェット用のインスタンスを生成するファクトリメソッド。
     *
     * 指定した primaryStage を StageStyle.UTILITY に設定し、不可視にし、第二の Stage を生成し、これに対して
     * ガジェットプログラムの振る舞いを提供する。
     * @param primaryStage JavaFX Application派生クラスのstartメソッドに渡されるトップレベルstage
     * @param prefs ガジェットの状態を保存する preferences
     * @return タスクバーに非表示なガジェット用の本クラスのインスタンス
     */
    public static TinyGadgetSupport ofTaskbarless(Stage primaryStage, Preferences prefs) {
        TinyGadgetSupport support = new TinyGadgetSupport();
        support.setAsUtilityStage(primaryStage);
        support.setPreferences(prefs);
        return support;
    }

    /**
     * OSのタスクバーに表示されるガジェット用のインスタンスを生成するファクトリメソッド。
     * <p>
     * 本呼び出し時に、まだstageにsceneをセットしていない場合、あるいは後でsceneを差し替えた場合は
     * sceneがセット（差し替え）された時点でsceneに対すて振る舞いを設定する。
     * <p>
     * 本ファクトリメソッドを使用した場合、終了時の状態保存と起動時の復元は行わない。
     * @param stage ガジェットの表示に使うstage
     * @return タスクバーに表示されるガジェット用の本クラスのインスタンス
     */
    public static TinyGadgetSupport of(Stage stage) {
        TinyGadgetSupport support = new TinyGadgetSupport();
        support.setAsTransparentStage(stage);
        return support;
    }

    /**
     * OSのタスクバーに表示されるガジェット用のインスタンスを生成するファクトリメソッド。
     * <p>
     * 本呼び出し時に、まだstageにsceneをセットしていない場合、あるいは後でsceneを差し替えた場合は
     * sceneがセット（差し替え）された時点でsceneに対すて振る舞いを設定する。
     * @param stage ガジェットの表示に使うstage
     * @param prefs ガジェットの状態を保存する preferences
     * @return タスクバーに表示されるガジェット用の本クラスのインスタンス
     */
    public static TinyGadgetSupport of(Stage stage, Preferences prefs) {
        TinyGadgetSupport support = new TinyGadgetSupport();
        support.setAsTransparentStage(stage);
        support.setPreferences(prefs);
        return support;
    }

    /**
     * デフォルトコンストラクタ
     */
    private TinyGadgetSupport() {
    }

    /**
     * 指定した stage に対してガジェットプログラムの振る舞いを提供する。
     * <p>
     * 本呼び出し時に、まだstageにsceneをセットしていない場合、あるいは後でsceneを差し替えた場合は
     * sceneがセット（差し替え）された時点でsceneに対すて振る舞いを設定する。
     * <p>
     * 本コンストラクタを使用した場合、終了時の状態保存と起動時の復元は行わない。
     * 
     * @param stage ガジェットの振る舞いを提供する対象 stage
     */
    @Deprecated(forRemoval = true)
    public TinyGadgetSupport(Stage stage) {
        setAsTransparentStage(stage);
    }
    
    /**
     * 指定した stage と prefs を対象としてインスタンス化。
     * 
     * @param stage ガジェットの振る舞いを提供する対象 stage
     * @param prefs ガジェットの状態を保存する preferences
     */
    @Deprecated(forRemoval = true)
    public TinyGadgetSupport(Stage stage, Preferences prefs) {
        this(stage);
        this.prefs = prefs;
    }

    /**
     * 引数に指定した primaryStage を、不可視の UTILITY スタイルに設定し、新たにStage を TRANSPARENT スタイルで生成しガジェットに
     * 使用する。
     * @param primaryStage タスクバーに非表示にする primaryStage
     */
    private void setAsUtilityStage(Stage primaryStage) {
        utilityStage = primaryStage;
        utilityStage.initStyle(StageStyle.UTILITY);
        utilityStage.setOpacity(0);
        utilityStage.setWidth(0);
        utilityStage.setHeight(0);
        utilityStage.setX(Double.MAX_VALUE);

        transparentStage = new Stage();
        transparentStage.initOwner(primaryStage);
        setAsTransparentStage(transparentStage);
        utilityStage.show();
    }

    /**
     * 引数のstageを透明化してガジェットに使用する。
     * @param stage 透明化したいstage
     */
    private void setAsTransparentStage(Stage stage) {
        transparentStage = stage;
        if (scene != null) {
            setup();
        }
        transparentStage.sceneProperty().addListener((obs, ov, nv) -> {
            if (ov != nv && nv != null) {
                scene = nv;
                setup();
            }
        });
        transparentStage.initStyle(StageStyle.TRANSPARENT);
    }

    /**
     * 透明化してガジェットに使用する stage を返却する。
     * @return 透明化してガジェットに使用する stage
     */
    public Stage getTransparentStage() {
        return transparentStage;
    }

    /**
     * 引数で指定した prefs に終了時のガジェット表示状態を保存し、起動時に復元する。
     * @param prefs ガジェットの状態を保存する
     */
    public void setPreferences(Preferences prefs) {
        this.prefs = prefs;
    }

    /**
     * ガジェットの振る舞いを、stage, scene に設定する。
     */
    private void setup() {
        scene.setFill(Color.TRANSPARENT);
        setupDragMove();
        setupResize();
        setupContextMenu();
        if (prefs != null) {
            setupStatusResume();
        }
    }
    
    /**
     * コンストラクタで渡された scene 上へのドラッグ操作で stage のウィンドウを移動。
     */
    protected void setupDragMove() {
        scene.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });
        scene.setOnMouseDragged(event -> {
            transparentStage.setX(event.getScreenX() - dragStartX);
            transparentStage.setY(event.getScreenY() - dragStartY);
        });
    }
    
    protected void setupResize() {
        // Ctrol + マウスホイール操作でウィンドウの大きさを変更
        scene.setOnScroll(event -> {
            if (event.isControlDown()) {
                zoom(event.getDeltaY() > 0 ? 1.1 : 0.9);
            }
        });
        // タッチパネルのズーム（ピンチ）操作でウィンドウサイズを変更
        scene.setOnZoom(event -> {
            zoom(event.getZoomFactor());
        });
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
        double nextWidth = Math.max(width* factor, MIN_WIDTH);
        double nextHeight = Math.max(height * factor, MIN_HEIGHT);
        
        transparentStage.setWidth(nextWidth);
        transparentStage.setHeight(nextHeight);
        transparentStage.setX(centerX - nextWidth / 2);
        transparentStage.setY(centerY - nextHeight / 2);
    }
    
    protected void setupContextMenu() {
        // マウス右クリックでポップアップメニューを表示
        ContextMenu popup = createContextMenu(transparentStage);
        scene.setOnContextMenuRequested(event -> {
            popup.show(transparentStage, event.getScreenX(), event.getScreenY());
        });
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

        ContextMenu popup = new ContextMenu(exitItem);
        return popup;
    }
    
    protected void setupStatusResume() {
        // ウィンドウが終了するときに状態を保存
        transparentStage.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == true && newValue == false) {
                saveStatus();
            }
        });
        
        // 保存した状態があれば復元
        loadStatus();
    }
    
    /**
     * 状態を永続領域に保存する。
     */
    private void saveStatus() {
        prefs.putInt(KEY_STAGE_X, (int) transparentStage.getX());
        prefs.putInt(KEY_STAGE_Y, (int) transparentStage.getY());
        prefs.putInt(KEY_STAGE_WIDTH, (int) transparentStage.getWidth());
        prefs.putInt(KEY_STAGE_HEIGHT, (int) transparentStage.getHeight());
    }
    
    /**
     * 永続領域に保存された状態を復元する。
     */
    private void loadStatus() {
        double x = prefs.getInt(KEY_STAGE_X, 0);
        double y = prefs.getInt(KEY_STAGE_Y, 0);
        double width = prefs.getInt(KEY_STAGE_WIDTH, 320);
        double height = prefs.getInt(KEY_STAGE_HEIGHT, 200);
        if (Screen.getScreensForRectangle(x, y, width, height).isEmpty()) {
            x = 0;
            y = 0;
            width = 320;
            height = 200;
        }
        transparentStage.setX(x);
        transparentStage.setY(y);
        transparentStage.setWidth(width);
        transparentStage.setHeight(height);
    }
}
