package com.lettytrain.notesapp.util

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl


//下面代码只是在运行时缓存了Cookie，当App退出的时候Cookie就不存在了
class PersistenceCookieJar : CookieJar {
    var cache: MutableList<Cookie> = mutableListOf()

    //Http请求结束，Response中有Cookie时候回调
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        //内存中缓存
        cache.addAll(cookies)
    }

    //Http发送请求前回调，Request中设置Cookie

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        //过期的Cookie
        var invalidCookies: MutableList<Cookie> = mutableListOf()
        //有效的Cookie
        var validCookies: MutableList<Cookie> = mutableListOf()

        for (cookie in cache) {

            if (cookie.expiresAt < System.currentTimeMillis()) {
                //判断是否过期
                invalidCookies.add(cookie)
            } else if (cookie.matches(url)) {
                //匹配Cookie对应url
                validCookies.add(cookie)
            }
        }

        //缓存中移除过期的Cookie
        cache.removeAll(invalidCookies)

        //返回List<Cookie>让Request进行设置
        return validCookies
    }


}