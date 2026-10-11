# DeviceCheck

`/device` で、プレイヤーが統合版(Floodgate)か Java 版かと、端末・クライアントの情報を表示する Paper 用プラグイン(Paper 1.21.11 以降・Java 21 必須)。
リポジトリ: https://github.com/gorogoro-space/DeviceCheck (GPL-3.0)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。新しく書くコードのコメントは日本語でよい。既存の英語のコメントは、頼まれない限り書き換えない。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。
- リポジトリへの初回の `git push` の前には、「GitHub で Watch 設定(Custom → Issues と Pull requests)はしましたか?」と日本語で確認する。自動 Watch は廃止されており、設定しないと issue や PR の通知が届かない。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外済み)。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、他人の情報を見られる範囲など)
  - 安全面(再読み込み時の後始末、他プラグインの妨げ、Floodgate・ViaVersion が無いときの動作など)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## 環境

- Paper 1.21.11(`io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`、`compileOnly`)/ `plugin.yml` の `api-version: '1.21.11'`(1.21.11 未満のサーバーでは読み込まれない)
- Floodgate(`org.geysermc.floodgate:api:2.2.5-SNAPSHOT`、`compileOnly`、サーバーは b141)。`plugin.yml` の `depend: [floodgate]`
  - コンパイルに使う `DeviceOs`(依存の geyser common 2.2.1-SNAPSHOT)には `LINUX` が無い。サーバーの Floodgate には新しい定数がありうるので、無い定数は `name()` で判定する
- ViaVersion(`com.viaversion:viaversion-api:5.12.1`、`compileOnly`)。`plugin.yml` の `softdepend: [ViaVersion]`。無くても動き、Java 版のクライアントバージョンが「不明」になる
- **Java 21 必須。** `build.gradle.kts` の toolchain で JDK 21 を使い、Java 21 の class ファイルを出力する。勝手に変えない
- ビルド: Gradle(Kotlin DSL、wrapper 9.7.1)。`gradlew.bat clean build`(Windows)。**JDK 21 で実行する**(`JAVA_HOME` を `C:\Program Files\Java\jdk-21` にする。既定の `java` は JDK 27)。「ビルドして」と言われたら常にクリーンビルドする。成果物は `build/libs/DeviceCheck-<version>.jar`
  - IntelliJ の「アーティファクトのビルド」はクラスファイルが入らないことがある。必ず Gradle でビルドする
- バージョンは `gradle.properties` の `version` だけで管理する。`plugin.yml` の `version: '${version}'` は `processResources` でビルド時に置き換わる
- パッケージ: `space.gorogoro.devicecheck`(**すべて小文字**。大文字が混ざると plugin.yml の main と一致せず起動しない)
- 動作確認はサーバーを再起動して行う。PlugManX での読み込みは権限やコマンドの登録が不完全になることがある(コードは途中ロードでも動くよう、起動時に状態を持たない作りにしてある)

## 最重要の設計方針

### TPS に影響させない
- 高頻度イベント(PlayerInteractEvent、PlayerMoveEvent、AsyncPlayerChatEvent など)を使うときは、安い判定を先に行い、対象外なら即座に抜ける
- BlockPhysicsEvent、VehicleMoveEvent など発生頻度が極端に高いイベントは使わない
- メインスレッドでファイルや DB の同期 I/O をしない(起動時・リロード時に一度だけ行う小さなファイルの読み込みは除く)
- 設定ファイルを追加する場合は、起動時・リロード時に一度だけ解析して保持する
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く

### 他プラグインへの依存
- ViaVersion のクラスを参照するのは `ViaHook` だけにする。ViaVersion が無いときは `ViaHook` を読み込まない(`viaEnabled()` で確かめてから呼ぶ)
- Floodgate・ViaVersion の状態は起動時に持たず、コマンドの実行のたびに API を参照する

### データの保存
- 今はファイルを何も書き込まない。追加する場合もプラグインフォルダ以外には何も書き込まない

### 他プラグインとの関係
- 既存の機能(例: GSit、看板の click_event など)を妨げないこと。イベントをキャンセルする範囲は必要最小限にする

## 機能仕様

(現在の実装の動作。仕様を変えたらここを更新する)

- **起動時(`onEnable`)**: Floodgate が有効でなければエラーログを出して自分を停止する。ViaVersion が無ければ警告ログを出す。タスクは登録しない
- **`/device [プレイヤー名]`**: **OP のみ**(`sender.isOp()` で判定。コンソールも可)。権限は使わない(`plugin.yml` に `permission` も `permissions` も書かない)。引数なしなら自分、ありならそのオンラインのプレイヤー(`getPlayerExact`)。見つからなければ赤字で知らせる。コンソールからはプレイヤー名が必要
  - 統合版: 種類(スマホ・タブレット / PC / ゲーム機)、OS、操作方法、UI、バージョン、言語、Ping、Java 版とのリンクの有無
  - Java 版: クライアント(ブランド名。Fabric・Forge などはわかりやすい名前にする)、バージョン(ViaVersion から)、言語、Ping、描画距離

## 過去にハマった点(他プロジェクトでの経験)

- `config.getString(path, "")` のように既定値を渡すと、jar 内 config.yml の既定値が参照されない。既定値なしで取得して null を判定すること
- plugin.yml で `default: true` にした権限でも、登録されないと Bukkit は「OP のみ」として扱う。全員向けの機能を権限で縛らない

## ファイル構成

- `src/main/java/space/gorogoro/devicecheck/DeviceCheck.java` — メインクラス。起動・終了、`/device` の処理と表示
- `ViaHook.java` — ViaVersion からクライアントのバージョンを取る(ViaVersion を参照するのはこのクラスだけ)
- `src/main/resources/plugin.yml` — プラグイン定義
- `build.gradle.kts` / `settings.gradle.kts` / `gradle.properties` — Gradle の設定(バージョンは `gradle.properties`)
