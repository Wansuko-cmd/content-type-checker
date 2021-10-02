# Content-Type-Checker

## Content-Type-Checkerとは

Content-Type-Checkerは、KtorのPluginであり、
Content-Typeを確認して、処理を加えることのできるライブラリです。

## 使い方

### 1. build.gradleやMavenに記述（例はbuild.gradle.kts）

```kotlin

repositories {
    maven { url = uri("https://wansuko-cmd.github.io/maven/") }
}

//Content-Type-Checker
implementation("com.wsr:content-type-checker:0.0.3")

```

### 2. installする

```kotlin

//デフォルト値を設定
install(ContentTypeChecker){

    //正しいContent-Typeの時の処理
    onSuccess = {
        println("Content-Type: ${call.request.contentType()}")
        call.respond("Success")
    }

    ////間違っているContent-Typeの時の処理
    onError = {
        println("Allowed: [${it.joinToString(", ")}], Request: ${call.request.contentType()}")
        call.respond(
            HttpStatusCode.UnsupportedMediaType,
            "Error"
        )
    }

    //Content-Typeが間違っているときに、処理を続けるかどうか
    continueOnError = false
}

```

### 3. Routeでラップする

```kotlin

//許可するContent-Typeを記述
allowContentType(
    ContentType.Application.Json, ContentType.Application.JavaScript
){
    get{
        //Something
    }

    post{
        //Something
    }
}

//デフォルト値をオーバーライドすることも可能
allowContentType(
    ContentType.Audio.Any,
    onSuccess = {
        call.respond(HttpStatusCode.NotFound)
    }
){
    get("world"){
        //Something
    }
    post("world") {
        //Something
    }
}

```

## install時に設定できる値

全て省略可能です

### onSuccess

#### 型

suspend PipelineContext<Unit, ApplicationCall>.(List<ContentType>) -> Unit

#### 内容

指定したContent-Typeのリクエストが来た時に走る処理を記述します。
この処理が走った後で、もともとのルート内の処理が走ります。

#### 書き方の例
```kotlin

onSuccess = {
    println("Content-Type: ${call.request.contentType()}")
    
    call.respond("${it.joinToString(", ")}に含まれます")
}

```

thisはPipelineで、普通のRouteと同じように記述可能です。
また、itはList<ContentType>で、指定したContentTypeのリストを取得できます。

### onError

#### 型

suspend PipelineContext<Unit, ApplicationCall>.(List<ContentType>) -> Unit

#### 内容
指定したContent-Typeとは違うリクエストが来た時に走る処理を記述します。
この処理が走った後で、`continueOnError`の値によって、もともとの処理が走るかどうかが
決まります。

#### 書き方の例

```kotlin

onSuccess = {
    println("Content-Type: ${call.request.contentType()}")

    call.respond(
        HttpStatusCode.UnsupportedMediaType,
        "許可されているContent-Typeは${it.joinToString(", ")}のみです"
    )
}

```

基本的な書き方は`onSuccess`と同じです。

### continueOnError

#### 型

Boolean

#### 内容

指定したContent-Typeとは違うリクエストが来た時に、`onError`の処理を
実行した後、もともとの処理を走らせるかどうかを指定します。
初期値は`true`です。

#### 書き方

```kotlin

//この時、処理は中断されます
continueOnError = false

```

## Route内での書き方

実際にRouteで利用する際には`allowContentType`を使います。

```kotlin

allowContentType(
    ContentType.Audio.Any,
    onSuccess = {
        call.respond(HttpStatusCode.NotFound)
    }
){
    get("world"){
        //Something
    }
    post("world") {
        //Something
    }
}

```

どのContent-Typeの時に成功判定にするかを第一引数に渡し、
残りは他のRouteと同じように書くことができます。

また、ここでデフォルト値をオーバーライドすることが可能です。
オーバーライドをした場合、そちらの処理が優先されます。

## サンプルアプリ

サンプルは、このプロジェクトの`sample`に記述しています。

## 注意点

このライブラリはあくまでContent-Typeによって処理を挟みこみ、
中断させることを可能にするものです。
そのため、`accept`のように、`allowContentType`で囲っておけば
同じルートが使えるわけではありません。

例えば下の例だと正常に動かない場合があります。

```kotlin

allowContentType(
    ContentType.Application.Json
){
    get("hello"){
        //Something
    }
}

allowContentType(
    ContentType.Audio.Any,
){
    get("hello"){
        //Something
    }
}

```

この場合は、`accept`と組み合わせて利用するようにするか、
ルートを分けるようにしてください。


また、こちらのライブラリは出来立てほやほやで、破壊的な変更が加えられる場合があります。

バグなどありましたらissueの方に投げていただいたり、pull requestを作成していただけたら
幸いです。

よろしくお願いいたします。
