Above JavaScript 開發標準化

# changelog
* <2018-09-18> zx - init
* <2018-09-19> jack - [ok] 開發環境中的 VS code 、 standardjs 的安裝說明
* <2018-09-26> zx - 修正部份內容，補 Vue Plugin 說明，webpack devserver
* <2018-09-27> zx - 補 js-common、vue-common 說明
* <2018-09-28> zx - fix typo
* <2018-09-28> posen - 改錯字, 修開發環境3.4敘述
* <2018-09-28> jack - 補上 1. vue-cli 2、3 差異 。2.  how to create a vue project 的說明
* <2018-10-05> zx - 補 JSDoc 使用說明和開發說明與文件小修正

# 開發環境
javascript 的開發環境主要使用 microsoft 的 visual studio code (以下簡稱 vscode)，
javascript 的 coding style 主要依循 [standardjs](https://standardjs.com/)，
在 vscode 中有 plugin 可以幫忙自動排版和檢查是否符合 coding style，
以下說明將介紹如果安裝 vscode 及所需的 plugins。

## vscode 的安裝

1. windows: 前往[官網](https://code.visualstudio.com/download)下載安裝檔後安裝(**安裝過nks.都點選下一步**)
2. osx: 下載後解壓縮將主程式放到 /applications/
3. linux: 下載 (.deb 或 .rpm 包) 並安裝

## editorconfig for vs code 的安裝

1. 點選 extensions (ctrl + shift + x) 並於搜尋欄輸入 'editorconfig'
2. 找到 'editorconfig for vs code'
3. 點選 install 進行安裝 ，完成後再點選 reload
4. 測試 (若確定無誤，可跳過) 請於任意資料夾下創建 .editorconfig，內容請放入:
  ```
  
  root = true  
  [*]
  indent_style = tab
  indent_size =  1
  ```
5. 之後於同目錄隨意開啟檔案，按下tab鍵若為1格空白，代表 plugin 正常運行

## standardjs 的安裝

1. 點選 extensions (ctrl + shift + x) 並於搜尋欄輸入 'standardjs'
2. 找到 'standardjs - javascript standard style'
3. 點選 install 進行安裝 ，完成後再點選 reload
4. 點選 喜好設定裡的設定 Mac快速鍵 (command + ,) 修改 settings.json 加入以下設定:
  ```json
  
  "javascript.validate.enable": false
  ```
5. 測試 (若確定無誤，可跳過) reload 請於任意資料夾下創建 test.js ，內容請放入:
  ```js
  
  var t = "1"
  console.log(t)
  ```

6. 若 "1" 底下有紅波浪底線，且游標移過去有顯示以下訊息(**只要前面有顯示 [standard]**)，代表 plugin 正常運行
>[standard] strings must use singlequote. (quotes)

### 修正 js 格式
```sh
# js
$ standard --fix file.js

# vue
# 需先安裝 eslint-plugin-html
$ standard --plugin html --fix file.vue
```

## vue plugins

1. 點選 extensions (ctrl + shift + x) 並於搜尋欄輸入 'vetur'
2. 找到 'vetur'
3. 點選 install 進行安裝 ，完成後再點選 reload
4. 到任意目錄下將測試專案 clone 下來:

```sh

git clone https://github.com/octref/veturpack.git
```

5. 用 vscode 開啟 veturpack 這個專案，測試在 .vue 檔中否能支援 auto-complete 和 syntax lint


# Front-End Project

前端專案主要用 Vue.js 開發，開新 Project 和目前現有的舊 Project 都用 Vue 官方的 boilerplate 工具 -- vue-cli。

p.s. 目前只有一個 aboveview 不是用 vue-cli 的 boilerplate。

## vue-cli 2 與 3 差異
目前 Vue.js 官方最新版的 cli 工具為 vue cli 3，3 多了很多新功能，雖然方便，
但這次改版把 webpack 的設定多包了一層 Vue 自家的設定，造成要客制化的困難，
本來有些需求就會需要改 webpack 的設定，用了 vue cli 3 還需要再找解。
另外因為目前(2018/09) vue cli 3 剛發佈，可能還有許多不成熟的地方。
因為以上兩個原因，決定不貿然用 vue cil 3，專案的 template 還是以 2 為主。

建議作法是，能裝 vue-cli 3，但是一定要用 @vue/cli-init (產生 vue-cli 2 的 template)。

### vue-cli 2
### installation

```sh 

  # Node.js 需要 6.x+ (建議 8.x+)
  npm install -g vue-cli
```

### creat project

```sh

  vue init webpack <project-name>
```

### vue-cli 3

#### installation

```sh

  #Node.js 需要 8.9.0+ (建議 8.11.0+)
  npm install -g @vue/cli
``` 
  
#### create project

  新版多了 **create** 指令，但是與 **init** 指令生成的專案結構不一樣。
  
  新版的cli會覆蓋舊版，若還需要使用舊版的功能(vue init)，請安裝 :
  
  ```sh
  
  npm install -g @vue/cli-init
  ```
  
  安裝後 **vue init** 就可以用來產生 vue-cli 2 的 project。

# how to create a vue project

以下檔案設定的名稱和設定方式都是基立在用 vue-cli 2 上，如果用 vue-cli 3 或其它 webpack template，檔案名稱和設定方法不一定會相同。
如果目前 vue-cli 版本是 3 以上，務必確認 **vue init** 指令執行結果與舊版一樣(請參考 vue cli 2與3的差異)

## 安裝說明:
進入任意資料夾下執行:
  
  ```sh
  
  vue init webpack <project-name>
  ```

執行後會要求 輸入、設定一些初始資料
以下為範例:

  ```
  
    Project name vue-demo
    Project description vue.js demo
    Author <你的英文名字> <你常用的信箱>
    Vue build stanalone (選擇 Runtime + Compiler 那一項)
    Install vue-router? (Y/n) Yes
    Install ESLint to lint your code? Yes
    Pick an ESLint preset Standard jest
    Setup e2e tests with Nightwatch? Yes
    Should we run `npm install` for you after the project has been created? npm
  ```

完成後，進入專案資料夾 (vue-demo) 內，並在此處執行:
  
  ```sh
  
    npm run dev
  ```
如果有顯示以下訊息:

  ```
  
     [DONE]  Compiled successfully in xxxxms

     [I]  Your application is running here: http://localhost:80xx
  ```
請用瀏覽器查看 http://localhost:80xx ，如有顯示 Vue 官方的 mark 代表此專案的開發環境可正常運行

## webpack 設定說明

以下為 專案資料夾 (vue-demo) 的目錄結構：
```

├── readme.md
├── build
├── config
├── index.html
├── login.html
├── package.json
├── src
├── static
└── test
```

主要是 **build** 和 **config** 這兩個是有關 webpack 設定。

## webpack-dev-server proxy server

在開發過程中，因為前後端分別開發的原因，前端在一開始要自建一個可以傳假資料的 server，
但是該 server 和開發中的前端是不同 port，以至於在 dev 和 production 時可能要利用 env variable 定義不同 server，透過 proxy server 可以減少這部份的設定。

**config/dev.env.js** 中，加入新的一組設定:
```json

  proxytable: {
    '/login': {
      target: {
        host: 'localhost',
        port: 7788,
        protocol: 'http'
      },
      secure: false,
      changeorigin: true
    }
  }
```
在 proxytable 中，可以定多個 route path，route path 下能設定該 path 要轉到哪個 target 中，
也可以解掉 server 端沒有設定 cros 的問題。
其它參數可以參考：[devserver-proxy](https://webpack.js.org/configuration/dev-server/#devserver-proxy)

# npm

## rules
- package.json 中，scripts 的部份務必使用 npm，不要用 yarn 的指令。

# 共用的 js library

目前會共用的 js library 有：
- js-common
   主要是一些 javascript 會常用的小工具，e.g. date、string 等
- vue-common
   各 project 可以共用的 vue component

目前還在慢慢把各 project 中共用的東西拉出來。
使用時為：
- 用 npm link 指令將 js-common、vue-common 在 node_modules 中作一個 symbolic link。
- 只要 node_modules 下有了以後，就可以用 commonjs 的 require 或 es6 的 import 引入到目標 project 中。

假設 project 在 ap repo 中，名為 app-abc ：
```

ap -
   |- app-abc
   |- js-common
   |- vue-common
   |...
```

link 方式如下：
```sh

# 在 node project 中下 npm link 會將該 project 記在 global 的 node_modules 中
cd ap/js-common && npm link
cd ap/vue-common && npm link

cd ap/app-abc

# 到目標 project 中再下 npm link ${要用的 module 名} 就會從 global node_modules 中作一個 symbolic link 到目標 project 的 node_modules 
npm install && npm link js-common && npm link vue-common
```

基本上 npm link common library 只要做一次，除非 node 環境改變造成 global node_modules 變更，
否則不管新增多少 project ，只要下 npm link vue-common 和 npm link js-common 就可以在該 project 中用這兩個 common library 了。


# js-common 使用方法

js-common 為了增加相容性，所以有使用一些作法讓 js-common 的程式 transpile 到 commonjs 的標準（寫 common lib 時可以用 es6 語法），當 js-common 拉下來時，或 code repo 有更新到 js-common 時，需要先執行以下指令：

```sh
cd js-common
npm install && npm run build:es && npm run build:cjs
```
p.s. 目前 build:umd 還沒完成，所以先 build 為 commonjs 讓其它 project 用，再用其它 projcet 來 minify
p.s.2 build:umd:min 完成後，js-common 這個 project 就可以用來在單一 page html 中使用

transpile CommonJS 完成後，在其它 project 也做完 npm link ，就可以在該 project 下用 js-common 的功能。
e.g. ：

```js

import {dateUtil} from 'js-common'

function adjustVal (col, val) {
    if (col === 'date') {
      return dateUtil.updateDateField(val)
    } else if (col === 'cbiv') {
      return val || '--'
    } else {
      return val
    }
  }
```

# js-common 的寫法

如果要新增新的 function 到 js-common 中，請依以下規則：

- 原始碼請加在 js-common/source/ 下
- js-common/source/index.js export 的 module 在外部才可以被使用，

沒有註冊在 index.js 中的都是內部用 function
e.g.:

```js

// index.js
// 只有寫在這 export 出去的 module 才能被其它 project 找到
export { default as date } from './date'
```
3. Coding Style 為 StandardJS
4. 盡可能 Unit Test
5. public 的 Function 在 JSDoc 一定要寫 example

## 使用方法
在使用前要先到 js-common build 出 cjs:

```js
cd js-common && npm run build
```
未來可能會考慮把 cjs 直接進 repo 或是開一個 private 的 npm server，還不確定，但目前可以先這樣作。

在自己的 prject 中，用 npm link js-common 取得 js-common 的 link 至 node_modules 下之後，
該 project 的程式中可以用以下方法載入想要的功能：

```js

// 只載入 number 這個 function
import {number} from 'js-common'

// 載入所有 function 且把 js-common 命名為 above
import * as above from 'js-common'
```

有一種作法是利用 Project 的 babel 引入 js-common 要的模組：

```js

import number from '../js-common/source/number.js'

let v = number.toFinanceNumber(123456)
```
盡量不要用這個作法，這樣會讓 module 失去義意。

## package.json (Deprecated)
在 package.json 用 link 的方式定義:

```json

"dependencies": {
    "js-common": "link:../js-common",
    // ...
}
```

p.s. yarn 才支援 package.json 的 link，因為統一用 npm，這個方法不要用

# vue-common

因為 vue 的 component 使用情況都是在前端 Project 中，所以規劃是使用 es6 的寫法，
有 import 的 project 會自己用 babel 把他轉到 CommonJS，而不用像 js-common 為了單一頁面可用，
所以要能先 build 成 CommonJS。

只要確定目標 Project 有 npm link 過 vue-common ，就可以在想要的地方用 modules path 取得到要使用的 component，
e.g.:

```js

import VueScrollingTable from 'vue-common/src/components/VueScrollingTable'

export default {
  name: 'stock-quote-chart',
  components: {
    VueScrollingTable, StockInfo
  }
  // ...
}
```

規則：
- 符合 StandardJS
- Vue Component 可以有 Single File 或是一整個資料夾的寫法，視 Component 複雜度而決定要用哪一個，
  當發現 Single File Component 的 templae 或 css 行數太多，影響 js 的閱讀時，可以考慮用資料夾的方式。
- 如果是不想給外部用的 private component，用 underline (_)做為案名開頭。
