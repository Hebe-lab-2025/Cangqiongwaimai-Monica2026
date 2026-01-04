```
yonggan@Yonggans-MacBook-Pro project-sky-admin-vue-ts % npm run serve

> vue-typescript-admin-template@0.1.0 serve
> vue-cli-service serve

 INFO  Starting development server...
Starting type checking service...
Using 1 worker with 2048MB memory limit
10% building 2/2 modules 0 activeError: error:0308010C:digital envelope routines::unsupported
    at new Hash (node:internal/crypto/hash:79:19)
    at Object.createHash (node:crypto:139:10)
    at module.exports (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/util/createHash.js:135:53)
    at NormalModule._initBuildHash (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/NormalModule.js:417:16)
    at handleParseError (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/NormalModule.js:471:10)
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/NormalModule.js:503:5
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/NormalModule.js:358:12
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:373:3
    at iterateNormalLoaders (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:214:10)
    at iterateNormalLoaders (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:221:10)
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:236:3
    at runSyncOrAsync (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:130:11)
    at iterateNormalLoaders (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:232:2)
    at Array.<anonymous> (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:205:4)
    at Storage.finished (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/enhanced-resolve/lib/CachedInputFileSystem.js:55:16)
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/enhanced-resolve/lib/CachedInputFileSystem.js:91:9
10% building 2/5 modules 3 active ...码/project-sky-admin-vue-ts/node_modules/webpack/hot/dev-server.jsnode:internal/crypto/hash:79
  this[kHandle] = new _Hash(algorithm, xofLen, algorithmId, getHashCache());
                  ^

Error: error:0308010C:digital envelope routines::unsupported
    at new Hash (node:internal/crypto/hash:79:19)
    at Object.createHash (node:crypto:139:10)
    at module.exports (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/util/createHash.js:135:53)
    at NormalModule._initBuildHash (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/NormalModule.js:417:16)
    at handleParseError (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/NormalModule.js:471:10)
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/NormalModule.js:503:5
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/webpack/lib/NormalModule.js:358:12
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:373:3
    at iterateNormalLoaders (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:214:10)
    at Array.<anonymous> (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/loader-runner/lib/LoaderRunner.js:205:4)
    at Storage.finished (/Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/enhanced-resolve/lib/CachedInputFileSystem.js:55:16)
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/enhanced-resolve/lib/CachedInputFileSystem.js:91:9
    at /Users/yonggan/Downloads/苍穹外卖-黑马/苍穹外卖前端源码/苍穹外卖前端源码/project-sky-admin-vue-ts/node_modules/graceful-fs/graceful-fs.js:115:16
    at FSReqCallback.readFileAfterClose [as oncomplete] (node:internal/fs/read/context:68:3) {
  opensslErrorStack: [
    'error:03000086:digital envelope routines::initialization error',
    'error:0308010C:digital envelope routines::unsupported'
  ],
  library: 'digital envelope routines',
  reason: 'unsupported',
  code: 'ERR_OSSL_EVP_UNSUPPORTED'
}

Node.js v20.19.4
yonggan@Yonggans-MacBook-Pro project-sky-admin-vue-ts %
```

```
- nvm use  v16.18.1
- npm run serve 
```
