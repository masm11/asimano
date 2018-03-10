# ASimano

メール通知。

## サーバ側

- rb-inotify をインストール

  ```sh
  gem install rb-inotify
  ```

- asimanod.rb を適当な場所にインストール

  asimanod.rb の先頭に TOKEN_LIST_FILE がある。アプリから受け取ったトークンがこのファイルに格納される。
  
  SERVER_KEY には Firebase のギアアイコン → クラウドメッセージング → サーバーキー に表示されているトークンを
  設定しておく。

- asimanod.service を `~/.config/systemd/user/` に配置

  asimanod.service の中の `WorkingDirectory =` と `ExecStart =` を修正しておく。

  ```sh
  systemctl --user start asimanod
  systemctl --user enable asimanod
  ```

- nginx を設定

  例えば、 `/asimano/register` を `localhost:18080` に転送するように設定する。

## アプリ側

  Android 8.0 Oreo 以降が必要。

  以下のようにするとスマホにインストールできる。

  ```sh
  ./gradlew --daemon assembleDebug installDebug
  ```

  スマホでアプリを起動し、三点リーダ → 設定 で URL を設定する。
  ここで設定する URL は、サーバ側で nginx に設定した、asimanod.rb に転送される URL。

  メールアカウントは Gmail アプリのメールアカウント。入力欄はあるが未対応。

## ライセンス

GPLv3.

## 作者

masm11.
