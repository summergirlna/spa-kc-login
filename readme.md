# spa-kc-login

React SPA + Spring Boot backend + Keycloak を使ったログイン検証プロジェクトです。

ユーザー登録、メール確認、パスワードリセット、メールアドレス変更、多要素認証を扱います。

## 構成

- Frontend
  - React SPA
- Backend
  - Spring Boot
  - Keycloak Admin API を使用して Keycloak ユーザーを作成・更新する
- Identity Provider
  - Keycloak
- MFA
  - EMAIL
  - TOTP
  - WEBAUTHN

## ローカルURL

| 用途 | URL |
|---|---|
| Frontend | `http://localhost/` |
| Keycloak Admin Console | `http://localhost/auth/admin` |

---

# データベース

## registration_attempts

仮登録用の確認コードを管理するテーブルです。

| カラム | 説明 |
|---|---|
| `registration_id` | 仮登録ID |
| `email` | 登録対象メールアドレス |
| `confirmation_code` | 確認コード |
| `status` | 仮登録ステータス |
| `verified_at` | 確認コード検証日時 |
| `expires_at` | 確認コード有効期限 |
| `created_at` | 作成日時 |
| `updated_at` | 更新日時 |

## app_users

アプリケーション側のユーザーを管理するテーブルです。

| カラム | 説明 |
|---|---|
| `id` | 内部ID |
| `user_id` | アプリケーションユーザーID |
| `email` | メールアドレス |
| `keycloak_user_id` | Keycloak ユーザーID |
| `mfa_type` | MFAタイプ |
| `status` | ユーザーステータス |
| `created_at` | 作成日時 |
| `updated_at` | 更新日時 |

### mfa_type

| 値 | 説明 |
|---|---|
| `EMAIL` | メール確認コードを使用する |
| `TOTP` | 認証アプリのワンタイムパスワードを使用する |
| `WEBAUTHN` | WebAuthn をパスワード + MFA として使用する |

## password_reset_attempts

パスワードリセット用の確認コードを管理するテーブルです。

| カラム | 説明 |
|---|---|
| `reset_id` | パスワードリセットID |
| `email` | 対象メールアドレス |
| `confirmation_code` | 確認コード |
| `status` | リセット処理ステータス |
| `verified_at` | 確認コード検証日時 |
| `expires_at` | 確認コード有効期限 |
| `created_at` | 作成日時 |
| `updated_at` | 更新日時 |

## email_change_attempts

メールアドレス変更用の確認コードを管理するテーブルです。

| カラム | 説明 |
|---|---|
| `change_id` | メールアドレス変更ID |
| `user_id` | アプリケーションユーザーID |
| `keycloak_user_id` | Keycloak ユーザーID |
| `current_email` | 現在のメールアドレス |
| `new_email` | 新しいメールアドレス |
| `confirmation_code` | 確認コード |
| `status` | メールアドレス変更ステータス |
| `verified_at` | 確認コード検証日時 |
| `expires_at` | 確認コード有効期限 |
| `created_at` | 作成日時 |
| `updated_at` | 更新日時 |

---

# MFA設計

## 基本方針

MFA 方式はアプリケーション DB と Keycloak user attribute の両方に保存します。

| 保存先 | 項目 |
|---|---|
| アプリDB | `app_users.mfa_type` |
| Keycloak user attribute | `mfa_type` |

Keycloak の Browser Flow では、`mfa_type` user attribute を見て MFA 方式を分岐します。

## MFAタイプ

| mfa_type | 初回設定 | ログイン時MFA |
|---|---|---|
| `EMAIL` | なし | Email OTP Authenticator |
| `TOTP` | `CONFIGURE_TOTP` | OTP Form |
| `WEBAUTHN` | `webauthn-register` | WebAuthn Authenticator |

## ユーザー登録時の Keycloak requiredActions

| mfa_type | requiredActions |
|---|---|
| `EMAIL` | なし |
| `TOTP` | `CONFIGURE_TOTP` |
| `WEBAUTHN` | `webauthn-register` |

## WebAuthn の位置づけ

WebAuthn はパスワードレスログインではなく、以下の形で使用します。

username/password -> WebAuthn MFA -> login success


SPA 側で WebAuthn API を直接扱わず、WebAuthn credential の登録・認証は Keycloak に任せます。

---

# Keycloak設定

## Realm

| 項目 | 値 |
|---|---|
| Realm | `spa-kc-login` |

## Client

### spa-client

SPA 用の public client です。

| 項目 | 値 |
|---|---|
| Client ID | `spa-client` |
| Root URL | `http://localhost/` |
| Home URL | `http://localhost/` |
| Valid redirect URIs | `http://localhost/*` |
| Valid post logout redirect URIs | `http://localhost/*` |
| Web origins | `http://localhost` |

### registration-service

Backend から Keycloak Admin API を呼び出すための confidential client です。

| 項目 | 値 |
|---|---|
| Client ID | `registration-service` |
| Client authentication | On |
| Standard Flow | Off |
| Direct access grants | Off |
| Service accounts roles | On |

Service account に付与するロール:

- `view-users`
- `manage-users`
- `query-users`

## Realm settings

### Theme

| 項目 | 値 |
|---|---|
| Login theme | `spa-login` |

### User Profile

`mfa_type` を Keycloak user attribute として使用します。

| 項目 | 値 |
|---|---|
| Attribute name | `mfa_type` |
| Display name | `MFA Type` |
| Permission | Admin view / Admin edit |

また、検証用途では以下の標準属性の required を外しています。

- `first name`
- `last name`

---

# Keycloak Authentication設定

## Required actions

### Webauthn Register

| 項目 | 値 |
|---|---|
| Enabled | On |
| Set as default action | Off |

`WEBAUTHN` を選択したユーザーだけ、登録時に backend から `webauthn-register` required action を付与します。

全ユーザーに WebAuthn 登録を強制しないため、default action にはしません。

## Policies

### WebAuthn Policy

ローカル検証用の設定です。

| 項目 | 値 |
|---|---|
| Relying party entity name | `spa-kc-login` |
| Relying party ID | `localhost` |
| User verification requirement | `preferred` |
| Attestation conveyance preference | `none` |

ローカル環境では Keycloak に以下でアクセスしています。

http://localhost/auth/admin


そのため、WebAuthn の RP ID は `localhost` にしています。

`localhost` は WebAuthn の HTTPS 要件の例外として扱われるため、ローカル検証が可能です。

---

# Browser Flow

## 使用するFlow

Built-in の `browser` flow を複製し、以下の flow を使用します。

browser-with-email-otp-test

Browser flow binding に `browser-with-email-otp-test` を設定します。

## Flow構成

browser-with-email-otp-test ├── Cookie Alternative ├── Kerberos Disabled ├── Identity Provider Redirector Alternative ├── browser-with-email-otp-test Organization Alternative └── browser-with-email-otp-test-forms Alternative ├── Username Password Form Required ├── MFA - EMAIL Conditional │ ├── Condition - User Attribute Required │ │ ├── Attribute name: mfa_type │ │ └── Expected attribute value: EMAIL │ └── Email OTP Authenticator Required ├── MFA - TOTP Conditional │ ├── Condition - User Attribute Required │ │ ├── Attribute name: mfa_type │ │ └── Expected attribute value: TOTP │ └── OTP Form Required └── MFA - WEBAUTHN Conditional ├── Condition - User Attribute Required │ ├── Attribute name: mfa_type │ └── Expected attribute value: WEBAUTHN └── WebAuthn Authenticator Required


## 分岐仕様

| mfa_type | 実行される subflow | Authenticator |
|---|---|---|
| `EMAIL` | `MFA - EMAIL` | Email OTP Authenticator |
| `TOTP` | `MFA - TOTP` | OTP Form |
| `WEBAUTHN` | `MFA - WEBAUTHN` | WebAuthn Authenticator |

---

# 動作確認

## EMAILユーザー

1. ユーザー登録時に MFA タイプとして EMAIL を選択する
2. Keycloak user attribute に mfa_type=EMAIL が保存される
3. ログインする
4. パスワード入力後、MFA - EMAIL subflow が実行される
5. Email OTP Authenticator によりメール確認コード認証が要求される
6. 確認コード認証に成功するとログインできる

## TOTPユーザー

1. ユーザー登録時に MFA タイプとして TOTP を選択する
1. Keycloak user attribute に mfa_type=TOTP が保存される
1. Keycloak requiredActions に CONFIGURE_TOTP が設定される
1. 初回ログイン時、認証アプリの登録が要求される
1. 2回目以降のログインでは、パスワード入力後に MFA - TOTP subflow が実行される
1. OTP Form により認証アプリのコード入力が要求される
1. OTP 認証に成功するとログインできる


## WEBAUTHNユーザー

### 初回ログイン

1. ユーザー登録時に MFA タイプとして WEBAUTHN を選択する
1. Keycloak user attribute に mfa_type=WEBAUTHN が保存される
1. Keycloak requiredActions に Webauthn Register が設定される
1. 初回ログイン時、パスワード入力後に WebAuthn 登録画面が表示される
1. セキュリティキー、Touch ID、iPhone / iPad 連携などで credential を登録する
1. 登録完了後、Required user actions から Webauthn Register が消える
1. Keycloak の Credentials に WebAuthn credential が追加される


### 2回目以降のログイン

1. ログインする
1. パスワード入力後、mfa_type=WEBAUTHN の条件に一致する
1. MFA - WEBAUTHN subflow が実行される
1. WebAuthn Authenticator により追加認証が要求される
1. WebAuthn 認証に成功するとログインできる


---

# 商用化に向けた検討事項

## 本番URL / HTTPS / WebAuthn RP ID

本番では HTTPS を使用します。

例:
https://auth.example.com


この場合、WebAuthn Policy の Relying party ID は通常以下にします。

auth.example.com


WebAuthn credential は RP ID に紐づくため、本番運用開始後に RP ID を変更すると、既存 credential が利用できなくなる可能性があります。

商用前に以下を確定します。

- Keycloak の公開URL
- WebAuthn RP ID
- HTTPS証明書
- リバースプロキシ構成
- Keycloak hostname / proxy 設定

## Client設定の本番化

ローカルでは `http://localhost/*` を許可していますが、本番では redirect URI と Web origins を本番ドメインに限定します。

例:

Valid redirect URIs: [https://app.example.com/](https://app.example.com/)*

Web origins: [https://app.example.com](https://app.example.com)


## CORS設定の本番化

Backend の CORS 設定も本番フロントエンドの origin に限定します。

例:

https://app.example.com



## MFAリカバリ

商用では MFA 手段を失った場合の復旧手順が必要です。

例:

- WebAuthn credential を削除して再登録させる
- TOTP credential を削除して `CONFIGURE_TOTP` を再付与する
- 一時的に `mfa_type=EMAIL` に切り替える
- 管理者操作の監査ログを残す
- 本人確認ルールを定める

## レート制限

以下の操作にはレート制限を検討します。

- 仮登録開始
- 確認コード検証
- 確認コード再送
- パスワードリセット開始
- メールアドレス変更
- Email OTP 送信
- Email OTP 検証

制限軸の例:

- IP単位
- メールアドレス単位
- ユーザーID単位
- registrationId / resetId / changeId 単位

## Keycloak本番運用

商用運用では以下を検討します。

- 外部DBの利用
- バックアップ / リストア
- Keycloak 管理者アカウントのMFA
- 管理コンソールのアクセス制限
- Brute Force Detection の有効化
- realm export / realm import
- バージョンアップ手順
- 監視 / アラート
- ログ保全

## 設定のコード化

管理画面で作成した設定は、将来的に realm import や初期化スクリプトで再現可能にします。

対象:

- Realm
- Clients
- User Profile
- Required Actions
- WebAuthn Policy
- Browser Flow
- MFA 分岐設定
- Email OTP provider 設定