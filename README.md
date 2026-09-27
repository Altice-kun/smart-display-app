# スマートディスプレイアプリ

これは単なるファイル置き場ではなく、`./gradlew assembleDebug`（またはAndroid Studioの
Run）でそのままビルドできる、完結したAndroid Studioプロジェクトです。パッケージ名は
`com.smartdisplay.app` で仮置きしているので、必要なら実際のパッケージ名にリネームして
ください。

## 含まれるもの

| ファイル | 内容 | 外部APIキー |
|---|---|---|
| `MainActivity.kt` | 6タブ（ホーム/音楽/TV/カレンダー/写真/ツール）を統括する実際のエントリーポイント | - |
| `ui/HomeScreen.kt` | ドット時計・天気・ニュース・予定・クイックランチャー | 不要 |
| `ui/MusicScreen.kt` | Spotify風プレイヤーUI（見た目のみ。実接続は次フェーズ） | 不要 |
| `ui/TvScreen.kt` | Google TVリモコン画面（GoogleTvRemoteClientに接続済み） | 不要 |
| `ui/CalendarScreen.kt` / `ui/PhotosScreen.kt` / `ui/ToolsScreen.kt` | カレンダー・写真スライドショー・ツール一覧 | 不要 |
| `clock/DotMatrixClock.kt` | ドットマトリクス（LED風）時計。HH:MM:SSを3x5ドットフォントで描画 | 不要 |
| `voice/VoiceCommandManager.kt` | Android標準SpeechRecognizerによる音声操作 | 不要 |
| `screensaver/ScreensaverOverlay.kt` | ホーム放置検知＋全画面アニメーション＋環境音ループ再生 | 不要 |
| `tv/GoogleTvRemoteClient.kt` | Google TV（Android TV Remote protocol v2）へのキー送信・アプリ起動 | 不要（下記の注意点あり） |
| `proto/remotemessage.proto` | 上記TVクライアントが使うメッセージ定義（検証済み） | - |
| `res/raw/ambient_loop.wav` | スクリーンセーバー用の仮の環境音（自動生成した簡易的な音） | - |
| `.github/workflows/build-apk.yml` | GitHub Actionsでdebug APKを自動ビルドするワークフロー | - |

## GitHub Actionsでビルドする

プッシュする（または「Actions」タブから手動実行する）と、`assembleDebug`が走り、
`smartdisplay-debug-apk` というアーティファクト名でAPKがダウンロードできるように
なります。ビルド環境（Android SDK・Gradle）はGitHub側が用意するので、こちらで
何かインストールする必要はありません。

## セットアップ

### 1. 権限（AndroidManifest.xml）

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 2. 環境音の音源

`res/raw/ambient_loop.wav` に、コンパイルを通すための仮の環境音（自動生成した簡易的な
低い持続音）を同梱済みです。そのままビルドは通りますが、実際に使うなら著作権フリーの
環境音に差し替えてください（同じファイル名 `ambient_loop` のままでOK）。

### 3. Google TV連携

`GoogleTvRemoteClient` が使うprotobufクラスの生成設定は `app/build.gradle.kts` に
すでに組み込み済みです。追加設定は不要です。

## 重要: 動画配信サービス連携の実装方針について

Netflix・Prime Video・Disney+・U-NEXT・ABEMA・Amazon Musicには第三者アプリ向けの
公開APIが存在しないため、「Google TV自体をリモコンのように操作するAPK」という方針で
代用します。具体的には **Android TV Remote protocol v2**（Google TVアプリ自身が
使っているプロトコル）を使い、以下を実現します。

- ADBも開発者モードも不要（TVに標準搭載の「Android TV Remote Service」だけで動く）
- mDNS（`_androidtvremote2._tcp`）でTVを発見し、ポート6467に接続してキー入力・アプリ起動を送信
- 無料。API利用料やアカウント登録は不要
- 「ディスプレイ側は操作、実際の再生・表示はTV側」という、ご要望どおりの役割分担になる

**ただし正直に言うと、1点だけ実装できていない部分があります。**
初回の「ペアリング」（自己署名証明書を作ってTVに送り、TV画面に出るPINコードを
確認する手順、ポート6466側の通信）は、非公式の解析情報しか存在せず、正確なメッセージ
形式を裏取りできていません。ここを憶測で実装すると「一見動きそうだが実機では
繋がらないコード」になりかねないため、あえて含めていません。

対応方法は2つあります。

1. **既存の実装からペアリング部分だけ移植する**（推奨）
   - `github.com/tronikos/androidtvremote2`（Python, Apache-2.0）
   - `github.com/dgmltn/Dpad`（Kotlin Multiplatform、`:protocol` が単体ライブラリ相当）
   - `github.com/kud/androidtv-remote`（TypeScript、ペアリングフロー実装あり）
   
   いずれも同じプロトコルの実装なので、ペアリング部分のロジックだけ読んで
   Kotlinに移植すれば、`GoogleTvRemoteClient` にそのまま繋げられます。

2. **一度だけ手動でペアリングを済ませる**
   TV側にPINが表示される画面まではどの実装でも同じなので、開発中は上記リポジトリの
   CLIツール等で一度ペアリングし、生成された証明書ファイルをアプリに埋め込む、
   という割り切り方も可能です（ただし証明書の有効期限や複数端末対応は別途考慮が必要）。

アプリ起動用のディープリンク（`launchApp()` に渡すURL）は、Netflix/YouTube/Prime Video
などサービスごとに異なります。正確なURIは実機でのテストで確認するのが確実です。

## 既知の制約

- 音声操作は`SpeechRecognizer`の「認識→結果待ち→再開」を繰り返す簡易実装のため、
  ネットワーク接続が必要で、バッテリー消費も大きめです。将来的にはPicovoice Porcupine
  のようなオンデバイスのウェイクワードエンジンへの置き換えを検討してください。
- `MusicScreen`はまだ見た目だけのUIシェルです。実際のSpotify連携（Web API / App Remote
  SDK）が次のフェーズになります。
- `TvScreen`は`GoogleTvRemoteClient`に接続済みですが、ペアリング未実装のため実機では
  まだ動きません（上記「Google TV連携の実装方針」の対応が必要です）。
- スイッチボット・歌詞モード・リアルタイムニュースの実データ取得は未着手です。
