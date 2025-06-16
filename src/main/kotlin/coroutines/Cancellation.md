# Exercise: Correct mistakes with cancellation

## updateUser

- readUserやreadUserSettingsでblockingしてしまっている。withContext(Dispatchers.IO)を使うべき
- readUserの後にyieldを入れて途中でキャンセル可能にする
- CancellationExceptionをcatchした後に再throwしていない
    - updateUserを呼び出す関数側も、キャンセル時になにか処理をしたい可能性があるため
- キャンセル後にsuspend関数を実行したい場合はwithContext(NonCancellable)を使う必要がある

## sendSignature

- fileを扱っているのでwithContext(Dispatchers.IO)を使うべき
- sendSignatureがキャンセルされたときにCancellationExceptionを再throwしていない
- readTextの後にyieldを入れて途中でキャンセル可能にする

## trySendUntilSuccess

- CancellationExceptionを再throwしていない
    - キャンセルされてもsuccessがfalseのままなのでループから抜けない