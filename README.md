# MinecraftServerController Plugin v1.4.3

Minecraft Server Control API と連携してゲーム内からサーバー管理を行うための Paper/Spigot プラグインです。

## 🆕 v1.4.3 新機能・修正

### 🔧 重要な不具合修正
- **stats コマンドの修正**: APIサーバー依存を解消し、ローカルデータベースから統計を取得
- **パフォーマンスモニターの最適化**: DB書き込み間隔を5秒→1分に変更（ディスクI/O 92%削減）
- **OnlinePlayersGUI 実装**: クリックイベント処理を追加（インベントリ表示、テレポート、管理オプション）
- **エラーハンドリング強化**: PerformanceMonitorGUI で詳細な診断情報を表示

### ⚡ パフォーマンス改善
- **ディスクI/O負荷**: 92%削減（720回/時 → 60回/時）
- **キャッシュ機構**: 5秒ごとにメモリキャッシュを更新してレスポンスを維持
- **メモリ使用量**: 約100KB増加（キャッシュ用）のみで大幅な性能向上

### 🎨 GUI機能強化
- **パフォーマンスGUI**: サーバー起動直後の検出、詳細なトラブルシューティング情報
- **オンラインプレイヤーGUI**: 
  - 左クリック: インベントリ表示
  - 右クリック: プレイヤーへテレポート
  - Shift+クリック: 管理オプション（OP、キック、BAN等）
- **バックアップスケジュールGUI**: スロットマッピングで正確なスケジュール操作

---

## 📊 v1.3.9 の主要機能

### プレイヤーアクティビティ統計
- **ログイン/ログアウト履歴**: すべてのプレイヤーの接続履歴を自動記録
- **総プレイ時間**: プレイヤーごとの累計プレイ時間を表示
- **セッション追跡**: 各セッションの詳細情報（時間、期間）
- **統計ダッシュボード**: 初回ログイン、最終ログイン、セッション数

### サーバーパフォーマンスモニタリング
- **リアルタイムTPS表示**: 1分、5分、15分平均
- **メモリ使用率グラフ**: 視覚的なメモリ使用状況
- **エンティティ/チャンク追跡**: パフォーマンスに影響する要素を監視
- **自動警告**: TPS低下やメモリ不足を自動検知
- **履歴データ**: 過去1時間のパフォーマンス推移
- **🆕 最適化**: ディスクI/O 92%削減で負荷軽減

### ワールド管理
- **マルチワールド対応**: 複数ワールドの管理
- **ワールド読み込み/アンロード**: リソース管理の最適化
- **ワールド別バックアップ**: 個別ワールドのバックアップ作成
- **サイズ表示**: 各ワールドのディスク使用量

### コマンドテンプレート
- **お気に入りコマンド**: よく使うコマンドを保存
- **プレースホルダー対応**: `{player}`, `{item}`, `{amount}` などの変数使用
- **コマンド履歴**: 最大50件の履歴を自動保存

### チャットログビューア
- **全チャット記録**: すべてのチャットメッセージを記録
- **プレイヤー別フィルタ**: 特定プレイヤーの発言を抽出
- **キーワード検索**: 過去のメッセージを検索
- **30日間保存**: 自動的に古いログを削除

---

## ✨ 既存機能

### 🎮 GUI メニュー
- `/msc` でインタラクティブなGUIメニュー
- ダッシュボード - サーバー全体を一目で把握
- コンソールGUI - よく使うコマンドのショートカット

### 💾 バックアップ管理
- バックアップの作成・一覧・リストア・削除
- 自動バックアップスケジュール管理GUI
- スケジュールの有効/無効切り替え

### 👥 プレイヤー管理
- ホワイトリスト管理
- OP権限管理
- **🆕 オンラインプレイヤー詳細**: インベントリ閲覧、テレポート、管理操作

### 📊 モニタリング
- メモリ使用状況
- リアルタイム通知システム
- **🆕 最適化されたパフォーマンスメトリクス**

---

## 📋 必要要件

- **Minecraft**: 1.20.4 (Paper推奨 / Spigot対応)
- **Java**: 17以上
- **API サーバー**: MinecraftServerController API が稼働していること

---

## 🚀 インストール

### ビルド

```bash
./gradlew clean build
```

出力: `build/libs/MinecraftServerController-1.4.3.jar`

### 配置

```bash
cp build/libs/MinecraftServerController-1.4.3.jar /path/to/server/plugins/
```

### 設定

`plugins/MinecraftServerController/config.yml`:
```yaml
api:
  url: "http://localhost:8000"
  key: "your-api-key-here"

plugin:
  debug: false
  timeout: 30
```

---

## 🎮 主要コマンド

### 基本
- `/msc` - GUIメニュー
- `/msc help` - ヘルプ表示

### プレイヤー管理
- `/msc players` - **🆕** オンラインプレイヤーGUI（インベントリ表示、テレポート対応）
- `/msc stats` - **🆕** 自分の統計（正常動作）
- `/msc stats <player>` - 他プレイヤーの統計（管理者のみ）

### パフォーマンス
- `/msc performance` - **🆕** 最適化されたパフォーマンスGUI
- `/msc perf` - パフォーマンス情報

### バックアップ
- `/msc backup` - バックアップ作成
- `/msc schedule list` - スケジュール一覧

### ワールド
- `/msc world` - ワールド管理GUI
- `/msc world list` - ワールド一覧

詳細なコマンド一覧は[コマンドリファレンス](#コマンド一覧完全版)を参照

---

## 🔐 権限

| 権限 | 説明 | デフォルト |
|------|------|-----------|
| `msc.use` | 基本コマンド | 全員 |
| `msc.players` | プレイヤー一覧 | 全員 |
| `msc.performance` | パフォーマンス監視 | 全員 |
| `msc.admin` | 管理者権限 | OP |
| `msc.backup` | バックアップ作成 | OP |
| `msc.world` | ワールド管理 | OP |

---

## 🎨 GUI機能

### メインメニュー (`/msc`)
- ★ ダッシュボード
- サーバーコントロール
- バックアップ管理
- プレイヤー管理
- **🆕 ★ パフォーマンスモニター**（最適化済み）
- **🆕 ★ ワールド管理**
- **🆕 ★ チャットログビューア**
- ★ 監査ログ（管理者のみ）
- ★ 設定（管理者のみ）

### パフォーマンスモニターGUI
- **リアルタイムTPS**: 1分、5分、15分平均を色分け表示
- **メモリ使用率**: 視覚的なプログレスバー
- **エンティティ/チャンク数**: パフォーマンス影響要素
- **履歴グラフ**: 過去1時間のTPS推移
- **🆕 エラー診断**: 起動直後の検出、詳細なトラブルシューティング
- **🆕 デバッグ情報**: 管理者向け詳細診断（デバッグモード時）

### オンラインプレイヤーGUI
- **プレイヤー一覧**: オンラインプレイヤーの頭アイコン表示
- **🆕 左クリック**: インベントリを表示
- **🆕 右クリック**: プレイヤーの位置へテレポート
- **🆕 Shift+クリック**: 管理オプションGUI（OP、キック、BAN等）
- サーバー統計情報

### バックアップスケジュールGUI
- スケジュール一覧（有効/無効を色分け）
- **🆕 左クリック**: 有効/無効切り替え（正確な動作）
- **🆕 右クリック**: 削除（スロットマッピングで正確）
- Cron形式のヘルプ

---

## 📊 データベース管理

### 自動作成されるファイル

```
plugins/MinecraftServerController/data/
├── activity.db      # プレイヤーアクティビティ
├── performance.db   # 🆕 最適化されたパフォーマンスメトリクス
└── chatlogs.db      # チャットログ
```

### v1.4.3 の最適化

**performance.db**:
- 書き込み間隔: 5秒 → 1分（92%削減）
- キャッシュ: メモリに5秒間隔で更新
- データ保持: 7日間

### データクリーンアップ

- **アクティビティログ**: 無制限（全履歴保存）
- **パフォーマンスデータ**: 7日以上前を自動削除
- **チャットログ**: 30日以上前を自動削除

### バックアップ推奨

```bash
cp -r plugins/MinecraftServerController/data/ backups/msc-data-$(date +%Y%m%d)/
```

---

## 🐛 トラブルシューティング

### v1.4.3 専用

#### パフォーマンスGUIで "No Performance Data" と表示される

**症状**: パフォーマンスGUIを開くとエラーが表示される

**原因**:
1. サーバー起動直後（5秒以内）
2. PerformanceMonitorが初期化されていない
3. データベース接続問題

**解決策**:
```bash
# 1. サーバー起動後5秒以上待つ
#    GUIに「⚠ Server just started!」と表示される場合は待機

# 2. リフレッシュボタンをクリック
/msc performance
# → 🔄 Refresh をクリック

# 3. それでも解決しない場合
/msc reload
```

**デバッグ方法**:
```yaml
# config.yml
plugin:
  debug: true
```
再起動後、管理者には「🐛 Debug Info」アイテムが表示される

---

#### stats コマンドで "No statistics found" と表示される

**症状**: `/msc stats` を実行しても統計が表示されない

**原因**: プラグイン導入後、まだログイン/ログアウトしていない

**解決策**:
```bash
# 一度ログアウトして再ログイン
# → activity.db にデータが記録される

# 再度実行
/msc stats
```

**期待される出力**:
```
========== YourName's Stats ==========
Total Playtime: 0時間 5分
Total Sessions: 1
First Join: 2026/02/14 10:00:00
Last Join: 2026/02/14 10:00:00

Recent Activity:
  Login: 2026/02/14 10:00:00 → Still online
```

---

#### OnlinePlayersGUIでクリックしても何も起きない

**症状**: プレイヤーアイコンをクリックしても反応なし

**原因**: v1.4.2以前のバージョン、またはGUIListenerが最新でない

**解決策**:
```bash
# v1.4.3を使用していることを確認
/plugins
# → MinecraftServerController v1.4.3

# 最新版でない場合は再ビルド
./gradlew clean build
cp build/libs/MinecraftServerController-1.4.3.jar /path/to/server/plugins/
```

**動作確認**:
- 左クリック → インベントリが表示される
- 右クリック → プレイヤーの位置へテレポート
- Shift+クリック → 管理オプションGUI（管理者のみ）

---

### 一般的なトラブルシューティング

#### "API Key is not set" エラー
→ `config.yml` の `api.key` を設定

#### "Failed to fetch ..." エラー
→ APIサーバーの起動を確認
→ `api.url` が正しいか確認

#### GUIが開かない
→ Paper/Spigotサーバーであることを確認
→ `/plugins` でプラグインがロードされているか確認

#### データベースエラー
```bash
# 権限確認
chmod 755 plugins/MinecraftServerController/data/
chmod 644 plugins/MinecraftServerController/data/*.db

# SQLite JDBC 確認（build.gradle）
dependencies {
    implementation 'org.xerial:sqlite-jdbc:3.44.1.0'
}
```

---

## 📈 パフォーマンス比較（v1.4.2 vs v1.4.3）

| 項目 | v1.4.2 | v1.4.3 | 改善率 |
|-----|--------|--------|-------|
| DB書き込み間隔 | 5秒 | 60秒 | - |
| 1時間のDB書き込み回数 | 720回 | 60回 | **92%削減** |
| ディスクI/O負荷 | 高 | 低 | **大幅改善** |
| データ取得速度 | 即座 | 即座 | 変化なし |
| メモリ使用量 | 標準 | +100KB | 微増 |
| キャッシュ更新間隔 | なし | 5秒 | 新規 |

**結論**: ディスクI/Oを大幅に削減しつつ、データの即時性を完全に維持

---

## 🆙 v1.4.2 から v1.4.3 へのアップグレード

### 手順

1. **バックアップ作成**:
```bash
cp build/libs/MinecraftServerController-1.4.2.jar backups/
cp -r plugins/MinecraftServerController/data/ backups/data-backup/
```

2. **新バージョンビルド**:
```bash
./gradlew clean build
```

3. **プラグイン置き換え**:
```bash
rm /path/to/server/plugins/MinecraftServerController-*.jar
cp build/libs/MinecraftServerController-1.4.3.jar /path/to/server/plugins/
```

4. **サーバー再起動**:
```bash
# 推奨: 完全再起動
# または /reload confirm
```

5. **動作確認**:
```bash
/msc performance  # パフォーマンスGUI
/msc stats        # 統計表示
/msc players      # オンラインプレイヤーGUI
```

### 互換性

- **データベース**: 完全互換（マイグレーション不要）
- **設定ファイル**: 変更不要
- **API**: 下位互換性維持

### 確認事項

起動ログで以下を確認:
```
[INFO] MinecraftServerController v1.4.3 has been enabled!
[INFO] Performance monitoring database initialized (v1.4.3 - optimized)
```

**"(v1.4.3 - optimized)"** の表示が重要！

---

## 🎯 使い方ガイド

### パフォーマンス監視

```bash
# GUIで視覚的に確認
/msc performance

# コマンドラインで確認
/msc perf
```

**サーバー起動直後の注意**:
- 起動後5秒以内はデータがない可能性
- 「⚠ Server just started!」表示時は待機
- リフレッシュボタンで再読み込み

### プレイヤー統計

```bash
# 自分の統計
/msc stats

# 他プレイヤーの統計（管理者のみ）
/msc stats PlayerName
```

**初回使用時**:
- データがない場合は一度ログアウト→再ログイン
- セッションデータが記録される

### オンラインプレイヤー管理

```bash
# GUIを開く
/msc players
```

**操作方法**:
- **左クリック**: インベントリ表示
- **右クリック**: テレポート
- **Shift+クリック**: 管理オプション（OP、キック、BAN等）

### バックアップスケジュール

```bash
# GUIを開く
/msc
→ ★ Backup Schedules
```

**操作方法**:
- **左クリック**: 有効/無効切り替え
- **右クリック**: 削除
- 効果音で操作確認

---

## 🔧 設定

### config.yml

```yaml
api:
  url: "http://localhost:8000"
  key: "your-api-key-here"

plugin:
  # デバッグモード（v1.4.3で詳細診断に使用）
  debug: false
  
  # タイムアウト（秒）
  timeout: 30
```

### デバッグモード（v1.4.3）

`debug: true` に設定すると:
- パフォーマンスGUIで詳細診断情報を表示
- エラー時のスタックトレースを出力
- PerformanceMonitorの内部状態を表示

**有効化**:
```yaml
plugin:
  debug: true
```

再起動後、管理者には「🐛 Debug Info」アイテムが表示される

---

## 📦 ビルド環境

- **Gradle**: 8.5+
- **Java**: 17+
- **Paper API**: 1.20.4
- **SQLite JDBC**: 3.44.1.0

---

## 📄 ライセンス

MIT License

---

## 👤 作者

**woxloi**

---

## 🔗 関連リンク

- [Paper公式](https://papermc.io/)
- [Spigot公式](https://www.spigotmc.org/)

---

## コマンド一覧完全版

<details>
<summary>クリックして展開</summary>

### GUI
- `/msc` - GUIメニュー
- `/msc gui` - GUIメニュー

### サーバー管理
- `/msc status` - ステータス表示
- `/msc server start` - 起動
- `/msc server stop` - 停止
- `/msc metrics` - メトリクス

### バックアップ
- `/msc backup` - 作成
- `/msc backup list` - 一覧
- `/msc backup restore <file>` - リストア
- `/msc backup delete <file>` - 削除
- `/msc schedule list` - スケジュール一覧
- `/msc schedule create` - 作成
- `/msc schedule toggle <id>` - 有効/無効
- `/msc schedule delete <id>` - 削除

### プレイヤー
- `/msc players` - オンラインプレイヤーGUI
- `/msc stats` - 自分の統計
- `/msc stats <player>` - 他プレイヤーの統計
- `/msc whitelist add <player>` - 追加
- `/msc whitelist remove <player>` - 削除
- `/msc whitelist list` - 一覧
- `/msc whitelist on/off` - 有効/無効
- `/msc op add <player>` - OP付与
- `/msc op remove <player>` - OP削除

### パフォーマンス
- `/msc performance` - パフォーマンスGUI
- `/msc perf` - パフォーマンス情報

### ワールド
- `/msc world` - ワールド管理GUI
- `/msc world list` - 一覧
- `/msc world load <name>` - 読み込み
- `/msc world unload <name>` - アンロード
- `/msc world backup <name>` - バックアップ

### チャットログ
- `/msc chat` - チャットログGUI
- `/msc chat search <keyword>` - 検索
- `/msc chat player <name>` - プレイヤー別

### プラグイン
- `/msc plugins list` - 一覧
- `/msc plugins reload` - リロード

### ログ
- `/msc logs` - サーバーログ
- `/msc logs tail` - 末尾20行
- `/msc audit` - 監査ログ

### その他
- `/msc exec <command>` - コンソールコマンド実行
- `/msc reload` - 設定リロード
- `/msc help` - ヘルプ

</details>

---

**🌟 v1.4.3 は安定性とパフォーマンスが大幅に向上しています！**