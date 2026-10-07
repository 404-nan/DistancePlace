# DistancePlace

遠くのブロックに設置できるようにする Paper プラグイン（1.20.4 / Java 17）。

## コマンド

| コマンド | 説明 |
| --- | --- |
| `/reach <ブロック数>` | 自分の設置距離を変更（上限は `reachLimit`） |
| `/reach reset` | 設置距離をデフォルトに戻す |
| `/reach status` | 現在の設定を表示 |
| `/reach speed <ms>` / `off` / `status` | 右クリック後の自動設置の間隔（左クリックで停止） |
| `/reach mode on` / `off` / `status` | 遠距離設置の有効・無効 |

権限 `distanceplace.use`（デフォルト: 全員）

## config.yml

| キー | 既定値 | 説明 |
| --- | --- | --- |
| `maxDistance` | 30 | デフォルトの設置距離 |
| `reachLimit` | 256 | `/reach` で設定できる上限 |
| `minSpeedMs` | 50 | `/reach speed` の最小間隔（50ms = 1tick） |
| `airPlace` | true | 視線の先にブロックが無いとき、設置距離の空中に置く |

## ビルド

```sh
./gradlew build
```

`build/libs/DistancePlace-<version>.jar` ができます。
