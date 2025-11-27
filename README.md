# 購買部在庫管理システム (Koubaibu)

研究室の購買部在庫を管理するWebアプリケーションです。

## 機能

- **在庫管理**: 商品の在庫数を表示し、「+」「-」ボタンで増減
- **商品管理**: 商品の追加・編集・削除
- **アラート機能**: 在庫が閾値以下になった場合にSlack通知
- **監査ログ**: すべての操作を`inventory_events`テーブルに記録

## 技術スタック

- **Frontend**: Vaadin 24
- **Backend**: Spring Boot 3.2
- **Database**: H2 (開発), PostgreSQL (本番)
- **通知**: Slack Webhook
- **CI/CD**: GitHub Actions

## 開発環境のセットアップ

### 必要条件

- Java 17+
- Maven 3.9+

### 実行方法

```bash
# 開発モードで起動
mvn spring-boot:run

# ブラウザでアクセス
# http://localhost:8080
```

### テスト実行

```bash
mvn test
```

### ビルド

```bash
# 開発ビルド
mvn clean package

# 本番ビルド（Vaadinの最適化を含む）
mvn clean package -Pproduction
```

## プロジェクト構成

```
src/
├── main/
│   ├── java/com/koubaibu/
│   │   ├── KoubaibuApplication.java    # メインクラス
│   │   ├── config/                      # 設定クラス
│   │   ├── controller/                  # REST API
│   │   ├── dto/                         # Data Transfer Objects
│   │   ├── entity/                      # JPAエンティティ
│   │   ├── filter/                      # サーブレットフィルター
│   │   ├── repository/                  # データアクセス
│   │   ├── service/                     # ビジネスロジック
│   │   └── view/                        # Vaadinビュー
│   └── resources/
│       ├── application.yml              # 共通設定
│       ├── application-dev.yml          # 開発環境設定
│       ├── application-stg.yml          # ステージング設定
│       ├── application-prod.yml         # 本番環境設定
│       ├── schema.sql                   # DBスキーマ
│       └── data.sql                     # 初期データ
└── test/                                # テストコード
```

## API エンドポイント

### 商品API

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/products | 商品一覧取得 |
| GET | /api/products/{id} | 商品詳細取得 |
| POST | /api/products | 商品追加 |
| PUT | /api/products/{id} | 商品更新 |
| DELETE | /api/products/{id} | 商品削除 |
| POST | /api/products/{id}/increase | 在庫増加 |
| POST | /api/products/{id}/decrease | 在庫減少 |

### アラートAPI

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/alert-settings | アラート設定取得 |
| PUT | /api/alert-settings | アラート設定更新 |
| GET | /api/alert-history | アラート履歴取得 |

## 環境変数

| 変数名 | 説明 | デフォルト |
|--------|------|----------|
| SPRING_PROFILES_ACTIVE | アクティブプロファイル | dev |
| DATABASE_URL | データベースURL | (H2メモリDB) |
| DATABASE_USERNAME | DBユーザー名 | sa |
| DATABASE_PASSWORD | DBパスワード | (空) |
| SLACK_WEBHOOK_URL | Slack WebhookURL | (無効) |

## ライセンス

Private - 研究室内部使用
