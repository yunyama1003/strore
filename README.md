# くらしのストック（strore）Ver.1

既存のSpring Boot在庫アプリを、自宅の日用品管理向けに拡張しました。
自宅のPCで起動し、PCまたは同じWi-Fiのスマホのブラウザから操作できます。
外出先では購入リストをメモにコピーして使います。

## できること

- 商品の登録・編集・削除（削除前に確認）
- ストック数の＋1・−1（0〜9999個）
- 最低ストック数、7種類のカテゴリ、メモの設定
- 在庫状態の自動判定、購入リスト、カテゴリ絞り込み
- 購入リストのテキストコピー（コピーが使えない端末では選択して手動コピー）
- PC・スマホ向け画面
- H2ファイルDBへの保存（アプリ再起動後も保持）

使用中の商品を含めるかは、自分の数え方で統一してください。
例えば「未開封の予備だけを数え、購入時に＋、開封時に−」とすると簡単です。
初期の商品データは空です。自分の日用品から登録してください。

## 起動（Windows）

Java 25が必要です。Gradleは同梱のWrapperを使うので、別途インストール不要です。
初回だけ依存ライブラリを取得するインターネット接続が必要です。

1. このフォルダにある **start.batをダブルクリック**します。
2. 起動ログに「Started StroreApplication」と表示されたら開きます。
3. PCのブラウザ: **http://localhost:8081/**
4. 終了する場合は起動したウィンドウでCtrl+Cを押します。

PowerShellから起動する場合:

```powershell
cd path\to\strore
.\gradlew.bat bootRun
```

このパス以外へ移動した場合は、移動先のstroreフォルダで実行してください。
データ保存場所を一定にするため、必ずプロジェクト直下から起動します。
8081番ポートが使用中の場合は、二重起動していないか確認してください。

GitHubから取得する場合（この改修をpushした後に利用可能）:

```powershell
git clone https://github.com/yunyama1003/strore.git
cd strore
.\gradlew.bat bootRun
```

## 自宅のスマホから使う

1. PCとスマホを同じ自宅Wi-Fiにつなぎます。
2. PCでアプリを起動したままにします（スリープ中はアクセスできません）。
3. PCで `ipconfig` を実行し、Wi-Fiの「IPv4 アドレス」を確認します。
4. スマホで `http://＜PCのIPv4アドレス＞:8081/` を開きます。
   例: PCのIPが192.168.1.10なら `http://192.168.1.10:8081/` です。
5. Windowsのファイアウォールで通信許可を求められた場合は、自宅のプライベートネットワークに限定して許可してください。

接続できない場合は、同じWi-Fiか、PCが起動中か、Windowsの許可設定を確認してください。
ゲストWi-Fiでは端末同士の通信が禁止されている場合があります。
スマホのlocalhostはスマホ自身を指すので、PCのIPを使ってください。
ルーターのポート開放は不要です。ログインなしの自宅専用版のため、インターネットへ公開しないでください。

## 買い物中に見る

購入リストで「リストをコピー」を押し、スマホのメモアプリなどに貼り付けます。
HTTP接続のスマホでは自動コピーが使えないことがあります。
その場合は展開されたテキストを端末のコピー操作でコピーできます。
コピーしたリストは出力時点の内容です。メモの変更や購入チェックはアプリに反映されません。
帰宅後に購入した分を＋してください。

## 在庫ルール

| 現在数 | 表示 | 購入リスト |
|---|---|---|
| 0 | 在庫切れ | 対象 |
| 1以上、最低ストック数以下 | 残りわずか | 対象 |
| 最低ストック数より多い | 在庫あり | 対象外 |

最低ストック数が0でも、現在数0の商品は購入対象です。
一覧は在庫切れ → 残りわずか → 在庫あり、同じ状態では登録順に表示します。
カテゴリ絞り込み中の＋−・削除は、そのカテゴリ表示を維持します。
購入リストで数量が最低数を超えると、その商品はリストから外れます。

## 保存とバックアップ

- DB: H2（ファイル方式）。PostgreSQLのインストールは不要です。
- 実データ: プロジェクト直下の `data/strore.mv.db`
- DB初期化: `src/main/resources/schema.sql` でテーブルがない場合に作成
- 起動時は既存データを消しません。JPAはスキーマの一致を検証します。
- バックアップは **アプリを停止してからdataフォルダ全体をコピー**します。
- 復元はアプリ停止中にdataフォルダをバックアップで戻します。現在のデータは置き換わります。
- dataフォルダは.gitignoreに含まれ、GitHubに個人の在庫データを登録しません。
- 自動テストは専用のメモリDBを使い、普段のデータを変更しません。

## テーブル設計

`items` の1テーブルです。在庫状態・購入リストは現在数と最低数から計算し、重複保存しません。

| 列 | 型 | 内容 |
|---|---|---|
| id | BIGINT / IDENTITY | 自動採番の主キー |
| name | VARCHAR(100) | 必須の商品名 |
| category | VARCHAR(30) | 固定7カテゴリのコード |
| quantity | INTEGER | 現在数、0〜9999 |
| minimum_quantity | INTEGER | 最低数、0〜9999 |
| note | VARCHAR(1000) | メモ、空文字可 |
| created_at | TIMESTAMP WITH TIME ZONE | 作成日時 |
| updated_at | TIMESTAMP WITH TIME ZONE | 更新日時 |

DBにも非負数・上限・カテゴリ等の制約を設定しています。
更新はトランザクションと行ロックを使い、PCとスマホからの同時＋−による取りこぼしを防ぎます。
商品編集画面を複数同時に開いた場合の変更は、後から保存した内容が優先されます。

## 技術と構成

Java 25 / Spring Boot 4.0.8 / Gradle 9.2.1 / Thymeleaf / Spring Data JPA / H2。
既存のController・Service・Repository・Entity・DTO構成を引き継いでいます。
Spring Securityはログインを要求せず、書き込みリクエストのCSRF保護に使用します。

```text
src/main/java/com/example/
  config/SecurityConfig.java
  controller/ItemViewController.java  # 画面の受付
  controller/ItemController.java      # 既存JSON API
  service/ItemService.java            # 更新・抽出・コピー用テキスト
  repository/ItemRepository.java      # DBへの読み書き
  entity/Item.java                    # 商品・日時・在庫判定
  entity/Category.java
  entity/StockStatus.java
  dto/ItemForm.java                   # 画面入力・バリデーション
  dto/ItemRequest.java
  dto/UpdateQuantityRequest.java
src/main/resources/
  templates/                         # 一覧・登録編集・共通部品・エラー
  static/css/app.css
  static/js/app.js                    # 削除確認・コピーのみ
  application.properties
  messages.properties
  schema.sql
src/test/java/com/example/InventoryTests.java
start.bat
```

画面: `/`（旧URL `/items/view` も維持）、`/items/new`、`/items/{id}/edit`、`/shopping`。
既存JSON API: GET/POST `/items`、PATCH `/items/{id}`。
JSON APIの更新もCSRFトークンとセッションが必要です。入力検証を適用しています。

## テスト・配布用ビルド

```powershell
.\gradlew.bat test bootJar
java -jar build/libs/strore-0.0.1-SNAPSHOT.jar
```

テスト結果: `build/reports/tests/test/index.html`。
登録・編集・削除、各画面の描画、判定の境界値、絞り込み、不正入力、
存在しないID、0未満防止、CSRF、同時＋操作を確認します。

## 今後の候補

外出先からの閲覧・更新とアクセス制限、必要になった時点でのDB変更。
家族共有・履歴・バーコード等はVer.1には含めていません。
