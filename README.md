# DeviceCheck
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/DeviceCheck.svg)](https://github.com/gorogoro-space/DeviceCheck/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/DeviceCheck/issues)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/gorogoro-space/DeviceCheck/blob/main/LICENSE)

This plugin shows whether a player is on Bedrock (Floodgate) or Java Edition, and their device and client information.

プレイヤーが統合版(Floodgate)か Java 版かと、端末やクライアントの情報を表示するプラグインです。

# Requirements
- Paper 1.21.11 or later (1.21.11 以降。それより古いサーバーでは読み込まれません)
- Java 21
- Floodgate(必須)
- ViaVersion(任意。入っていると Java 版のクライアントバージョンを表示します)

# Installation method
Please place the .jar file in the Paper plugins folder.

.jar ファイルを Paper の plugins フォルダに置き、サーバーを再起動してください。

# Useage
| コマンド | 説明 | 使える人 |
|---|---|---|
| `/device` | 自分の情報を表示する | OP・コンソール |
| `/device <プレイヤー名>` | オンラインのプレイヤーの情報を表示する(コンソールからはこちら) | OP・コンソール |

権限(パーミッション)は使わず、OP かどうかで判定します。

表示する内容は次のとおりです。

- 統合版: 種類(スマホ・タブレット / PC / ゲーム機)、OS、操作方法、UI、バージョン、言語、Ping、Java 版とのリンクの有無
- Java 版: クライアント(バニラ・Fabric・Forge など)、バージョン(ViaVersion が必要)、言語、Ping、描画距離

# 情報の利用目的

取得した情報は、次の目的で利用します。

- サービスの運営・改善
- お問い合わせへの回答(本人確認を含む)
- 荒らし・不正アクセス・詐欺・サイバー攻撃などの迷惑行為や不正行為の防止と対応
- 利用規約に違反したユーザーや、不正な目的で利用しようとするユーザーの特定と、利用のお断り
- サーバーの不具合の調査と性能の改善
- 上記に付随する目的

# Disclaimer
Do not assume any responsibility by use. Please use it at your own risk.

## ビルド

JDK 21 が必要です。Gradle はラッパー(`gradlew`)が自動でダウンロードするので、別途インストールする必要はありません。

```
gradlew.bat clean build
```

(macOS / Linux では `./gradlew clean build`)

## IntelliJ IDEA でのビルド手順

本プロジェクトはビルドツールに Gradle を使用しています。
IntelliJ IDEA 上で正しくプラグイン（JARファイル）を生成するには、以下の手順を実行してください。
「ビルド → アーティファクトのビルド」はクラスファイルが入らないことがあるので使わないでください。

### 🛠️ ビルド手順

1. IntelliJ IDEA の画面右端にある **「Gradle」タブ** をクリックして開きます。
2. プロジェクト名（DeviceCheck）を展開し、 **`Tasks`** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します（古いビルドキャッシュを削除します）。
4. 続けてリスト内にある **`build`** をダブルクリックして実行します。

### 📦 生成されたファイルの場所
ビルドが成功すると、プロジェクトのルート直下に `build/libs` フォルダが作成（または更新）され、その中に中身の詰まった正しい JAR ファイルが生成されます。

* **生成先:** `build/libs/DeviceCheck-<version>.jar`(例: `DeviceCheck-1.1.0.jar`。バージョンは `gradle.properties` の `version`)

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。

## 開発(IntelliJ IDEA で Claude Code を使う)

このリポジトリには、AI コーディングツール [Claude Code](https://docs.claude.com/ja/docs/claude-code/overview) 向けの作業ルールを書いた `CLAUDE.md` があります。IntelliJ IDEA で使う手順は次のとおりです。

1. Claude の有料プラン(Pro / Max)か、Anthropic API のアカウントを用意します。
2. Windows では、先に [Git for Windows](https://git-scm.com/downloads/win) をインストールします。
3. Claude Code 本体をインストールします。Windows では PowerShell で次を実行します。
   ```
   irm https://claude.ai/install.ps1 | iex
   ```
4. IntelliJ IDEA の「設定 → プラグイン → Marketplace」で「Claude Code」を検索してインストールし、IDE を再起動します。検索結果には Anthropic 以外が作った似た名前のプラグインも表示されます。プラグイン名の下に書かれた提供元が「Anthropic」になっているものを選んでください。
5. プロジェクトを開いて、ターミナルで `claude` を実行します(`Ctrl+Esc` でも起動できます)。初回はブラウザでログインします。

起動すると `CLAUDE.md` が自動で読み込まれ、このプロジェクトの設計方針に沿って作業します。

## ライセンス

[LICENSE](LICENSE) を参照してください。

## kubotan へのメモ

リポジトリを新規作成したら、**必ず Watch を設定すること**(忘れない!)。
GitHub の自動 Watch 機能は 2025 年 5 月に廃止されたため、設定しないと他の人が立てた issue や PR の通知が届かない。

1. リポジトリのページ右上の **「Watch」** を押す
2. **「Custom」** を選び、**Issues** と **Pull requests** にチェックを入れる
