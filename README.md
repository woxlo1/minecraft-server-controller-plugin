# MinecraftServerController Plugin

Minecraft Server Control API と連携してゲーム内からサーバー管理を行うための Paper/Spigot プラグインです。

## ✨ 特徴

### 🎮 **GUI メニュー**
- `/msc` でインタラクティブなGUIメニューを開く
- **★ NEW** ダッシュボード - サーバー全体を一目で把握
- **★ NEW** コンソールGUI - よく使うコマンドのショートカット

### 💾 **バックアップ管理**
- バックアップの作成・一覧・リストア・削除
- **★ NEW** 自動バックアップスケジュール管理GUI
- スケジュールの有効/無効切り替え

### 🔧 **サーバーコントロール**
- サーバーの起動・停止・ステータス確認
- **★ NEW** 詳細なサーバーステータス表示
- **★ NEW** メモリ使用率の視覚化（プログレスバー）

### 👥 **プレイヤー管理**
- ホワイトリスト管理（追加・削除・有効化/無効化）
- OP権限管理（付与・削除）
- オンラインプレイヤー一覧

### 🔌 **プラグイン管理**
- プラグイン一覧表示
- プラグインリロード

### 📊 **モニタリング**
- メモリ使用状況
- サーバー情報
- **★ NEW** リアルタイム通知システム

### 📝 **ログ・監査**
- サーバーログ閲覧
- **★ NEW** 監査ログGUI（管理者専用）
- 操作履歴の視覚的な確認

### ⚙️ **設定管理**
- **★ NEW** ゲーム内設定変更GUI
- デバッグモード切り替え
- 設定のリロード

### 🎯 **その他**
- コンソールコマンド実行
- **★ NEW** コマンドショートカット

## 📋 必要要件

- **Minecraft**: 1.20.4 (Paper推奨 / Spigot対応)
- **Java**: 17以上
- **API サーバー**: MinecraftServerController API が稼働していること

## 🚀 インストール

### ビルド方法

#### Gradle を使用（推奨 - Paper向け）:
```bash
# Linux/macOS
./build-gradle.sh

# Windows
build-gradle.bat

# または直接
./gradlew clean build
```

#### Maven を使用:
```bash
# Linux/macOS
./build.sh

# Windows
build.bat

# または直接
mvn clean package
```

### プラグインの配置

1. ビルド後のJARをサーバーの`plugins/`フォルダにコピー:
```bash
# Gradle
cp build/libs/MinecraftServerController-1.0.0.jar /path/to/server/plugins/

# Maven
cp target/MinecraftServerController-1.0.0.jar /path/to/server/plugins/
```

2. サーバーを起動または再起動

3. `plugins/MinecraftServerController/config.yml` を編集:
```yaml
api:
  url: "http://localhost:8000"      # APIサーバーのURL
  key: "your-api-key-here"          # APIキー

plugin:
  debug: false
  timeout: 30
```

4. プラグインをリロード:
```
/msc reload
```

## 🎮 コマンド一覧

### GUI コマンド
| コマンド | 説明 | 権限 |
|---------|------|------|
| `/msc` | GUIメニューを開く | `msc.use` |
| `/msc gui` | GUIメニューを開く | `msc.use` |

### サーバー管理
| コマンド | 説明 | 権限 |
|---------|------|------|
| `/msc status` | サーバーステータス表示 | `msc.server` |
| `/msc server start` | サーバー起動 | `msc.server.control` |
| `/msc server stop` | サーバー停止 | `msc.server.control` |
| `/msc metrics` | メトリクス表示 | `msc.server` |

### バックアップ管理
| コマンド | 説明 | 権限 |
|---------|------|------|
| `/msc backup` | バックアップ作成 | `msc.backup` |
| `/msc backup list` | バックアップ一覧 | `msc.backup.list` |
| `/msc backup restore <file>` | バックアップをリストア | `msc.backup.restore` |
| `/msc backup delete <file>` | バックアップ削除 | `msc.backup.restore` |
| `/msc schedules` | バックアップスケジュール表示 | `msc.admin` |

### プレイヤー管理
| コマンド | 説明 | 権限 |
|---------|------|------|
| `/msc players` | オンラインプレイヤー一覧 | `msc.players` |
| `/msc whitelist add <player>` | ホワイトリスト追加 | `msc.whitelist` |
| `/msc whitelist remove <player>` | ホワイトリスト削除 | `msc.whitelist` |
| `/msc whitelist list` | ホワイトリスト一覧 | `msc.whitelist` |
| `/msc whitelist on` | ホワイトリスト有効化 | `msc.whitelist` |
| `/msc whitelist off` | ホワイトリスト無効化 | `msc.whitelist` |
| `/msc op add <player>` | OP権限付与 | `msc.admin` |
| `/msc op remove <player>` | OP権限削除 | `msc.admin` |

### プラグイン管理
| コマンド | 説明 | 権限 |
|---------|------|------|
| `/msc plugins list` | プラグイン一覧 | `msc.admin` |
| `/msc plugins reload` | プラグインリロード | `msc.admin` |

### ログ・監査
| コマンド | 説明 | 権限 |
|---------|------|------|
| `/msc logs` | サーバーログ表示 | `msc.admin` |
| `/msc logs tail` | ログ末尾20行表示 | `msc.admin` |
| `/msc audit` | 監査ログ表示 | `msc.admin` |

### その他
| コマンド | 説明 | 権限 |
|---------|------|------|
| `/msc exec <command>` | コンソールコマンド実行 | `msc.exec` |
| `/msc reload` | プラグイン設定リロード | `msc.reload` |
| `/msc help` | ヘルプ表示 | `msc.use` |

## 🔐 権限一覧

| 権限 | 説明 | デフォルト |
|------|------|-----------|
| `msc.use` | 基本コマンド使用 | 全員 |
| `msc.backup` | バックアップ作成 | OP |
| `msc.backup.list` | バックアップ一覧表示 | OP |
| `msc.backup.restore` | バックアップリストア・削除 | OP |
| `msc.server` | サーバー情報閲覧 | 全員 |
| `msc.server.control` | サーバー起動・停止 | OP |
| `msc.players` | プレイヤー一覧閲覧 | 全員 |
| `msc.whitelist` | ホワイトリスト管理 | OP |
| `msc.exec` | コンソールコマンド実行 | OP |
| `msc.admin` | 管理者権限（OP・プラグイン・監査ログ） | OP |
| `msc.reload` | 設定リロード | OP |

## 🎨 GUI機能

### メインメニュー (`/msc`)
- **★ ダッシュボード** - サーバー全体の状況を一目で把握
- サーバーコントロール
- バックアップ管理
- プレイヤー管理
- プラグイン管理
- **★ コンソールコマンド** - よく使うコマンドのショートカット
- サーバーステータス
- メトリクス
- オンラインプレイヤー
- ログ表示
- **★ 監査ログ**（管理者のみ）- 操作履歴の視覚的確認
- **★ バックアップスケジュール**（管理者のみ）- 自動バックアップ管理
- **★ 設定**（管理者のみ）- ゲーム内設定変更

### ★ NEW: ダッシュボードGUI
- サーバーステータス（起動/停止）の視覚表示
- オンラインプレイヤー数と一覧
- メモリ使用率（視覚的なプログレスバー）
- 最新バックアップ情報
- アクティブなスケジュール数
- 各管理画面へのクイックアクセス
- リフレッシュボタン

### バックアップGUI
- 新規バックアップ作成
- バックアップ一覧表示
- バックアップリストア（左クリック）
- バックアップ削除（右クリック）
- **★ NEW** スケジュール管理へのアクセス

### ★ NEW: バックアップスケジュールGUI
- スケジュール一覧表示（有効/無効を色分け）
- 左クリック：有効/無効の切り替え
- 右クリック：スケジュール削除
- cron形式のヘルプ表示
- 最終実行時刻の確認

### ★ NEW: サーバーステータスGUI
- 詳細なサーバー状態表示
- コンテナ情報
- メモリ使用率（視覚的なバー + パーセンテージ）
- プレイヤー情報
- サーバーバージョン情報
- リフレッシュボタン

### サーバーコントロールGUI
- サーバー起動
- サーバー停止
- ステータス確認

### プラグインGUI
- インストール済みプラグイン一覧
- プラグインリロード

### ★ NEW: コンソールGUI
- よく使うコマンドのショートカット
   - say Hello
   - time set day
   - weather clear
   - difficulty 切り替え
   - give コマンド
   - テレポートコマンド
- カスタムコマンド実行の案内

### ★ NEW: 監査ログGUI（管理者専用）
- 操作履歴の一覧表示（最新30件）
- 日時、ユーザー、役割、アクション、詳細、IP
- アクションタイプに応じたアイコン表示
- 役割に応じた色分け（Root=赤、Admin=金、Player=緑）
- 統計情報（合計ログ数、役割別カウント）
- リフレッシュボタン

### ★ NEW: 設定GUI（管理者専用）
- API URL 表示と変更方法の案内
- API Key 表示（マスク処理）と変更方法の案内
- デバッグモード切り替え（クリックで即時切り替え）
- タイムアウト設定の表示と変更方法
- 設定リロード
- 設定ファイルの場所表示
- デフォルト設定へのリセット案内
- 設定のバックアップ/リストア案内

## 🔔 NEW: 通知システム

重要なイベント時に自動的にプレイヤーに通知します：

### 通知の種類
- **バックアップ通知**
   - バックアップ作成完了（管理者のみ）
   - バックアップリストア（全員）
   - バックアップ削除（管理者のみ）
   - スケジュールバックアップ実行（管理者のみ）

- **サーバー制御通知**
   - サーバー起動中（全員）
   - サーバー停止中（全員）
   - サーバー再起動中（全員）

- **プラグイン通知**
   - プラグインアップロード（管理者のみ）
   - プラグイン削除（管理者のみ）
   - プラグインリロード（管理者のみ）

- **ホワイトリスト/OP通知**
   - ホワイトリスト追加/削除（管理者のみ）
   - OP権限付与/削除（管理者のみ）

- **警告通知**
   - エラー発生（管理者のみ）
   - 警告メッセージ（管理者のみ）
   - 高メモリ使用率（管理者のみ）
   - 危険なメモリ使用率（全員）

### 通知の特徴
- チャットメッセージと効果音で通知
- 役割に応じた通知の振り分け
- 色分けされた見やすいメッセージ
- 重要度に応じた効果音

## 🔧 設定

### config.yml
```yaml
# API Settings
api:
  # API Base URL
  url: "http://localhost:8000"
  
  # API Key (get from API server)
  key: ""

# Plugin Settings
plugin:
  # Enable debug logging
  debug: false
  
  # Async request timeout (seconds)
  timeout: 30
```

### API キーの取得

1. **Web管理画面から取得**:
   - ブラウザで `http://localhost:8000` にアクセス
   - プレイヤー名を登録してAPIキーを取得
   - `config.yml` に設定

2. **ROOT_API_KEY を使用（開発環境向け）**:
   - API サーバーの環境変数 `ROOT_API_KEY` の値を使用
   - デフォルト: `super-secret-root-key`

## 📦 ビルド環境

### Gradle（推奨）
- Gradle 8.5+
- Java 17+
- Paper API 1.20.4

### Maven
- Maven 3.6+
- Java 17+
- Spigot API 1.20.4

## 🐛 トラブルシューティング

### "API Key is not set" エラー
→ `config.yml` の `api.key` を設定してください

### "Failed to fetch ..." エラー
→ API サーバーが起動しているか確認
→ `api.url` が正しいか確認
→ ファイアウォール設定を確認

### 権限エラー
→ OP権限: `/op YourPlayerName`
→ または権限プラグイン（LuckPerms等）で権限を付与

### GUIが開かない
→ サーバーが Paper/Spigot であることを確認
→ プラグインが正常にロードされているか確認: `/plugins`

### 通知が表示されない
→ NotificationManager が正しく初期化されているか確認
→ サーバーログでエラーを確認

## 🚀 開発

### Gradleでビルド
```bash
./gradlew clean build
```

### Mavenでビルド
```bash
mvn clean package
```

### デバッグモード
`config.yml` で `plugin.debug: true` に設定

## 📝 更新履歴

### v1.1.0 (2025-02-06) ★ NEW
- ✨ **ダッシュボードGUI** - サーバー全体を一目で把握
- ✨ **バックアップスケジュールGUI** - 自動バックアップ管理
- ✨ **コンソールGUI** - よく使うコマンドのショートカット
- ✨ **監査ログGUI** - 操作履歴の視覚的確認
- ✨ **サーバーステータスGUI** - 詳細な状態表示
- ✨ **設定GUI** - ゲーム内設定変更
- ✨ **通知システム** - リアルタイム通知機能
- 🔧 **メインメニュー拡張** - 新機能へのアクセス
- 🔧 **GUIListener統合** - すべてのGUIを一元管理

### v1.0.0 (2025-01-XX)
- 🎉 初回リリース
- 基本的なGUI機能
- バックアップ管理
- サーバーコントロール
- プレイヤー管理
- プラグイン管理

## 📄 ライセンス

MIT License

## 👤 作者

**woxloi**

## 🔗 関連リンク

- [Paper公式](https://papermc.io/)
- [Spigot公式](https://www.spigotmc.org/)
- [Gradle公式](https://gradle.org/)

---

**注意**: このプラグインを使用するには、MinecraftServerController API サーバーが稼働している必要があります。

## 🎯 おすすめの使い方

1. **まずはダッシュボードを開く** - `/msc` → ★ Dashboard
2. **サーバー状態を確認** - メモリ使用率、プレイヤー数など
3. **バックアップスケジュールを設定** - 自動バックアップで安心
4. **監査ログで操作を確認** - セキュリティと透明性を確保
5. **通知を活用** - 重要なイベントを見逃さない

---

**🌟 新機能を試してみてください！**