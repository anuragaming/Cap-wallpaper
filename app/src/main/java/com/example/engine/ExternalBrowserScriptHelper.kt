package com.example.engine

object ExternalBrowserScriptHelper {

    /**
     * Generates a 1-tap Bookmarklet for Google Chrome, Edge, and Samsung Internet.
     * Users save this as a bookmark and tap it when on any webpage.
     */
    fun getBookmarkletCode(): String {
        return """
javascript:(function(){
if(window.__ANTI_RETURN_ACTIVE__){alert('Nhật Anh browser Shield đã được kích hoạt trước đó trên trang này!');return;}
window.__ANTI_RETURN_ACTIVE__=true;
try{
var p=function(){return'visible';};
var f=function(){return false;};
var t=function(){return true;};
Object.defineProperty(Document.prototype,'visibilityState',{get:p,configurable:true});
Object.defineProperty(Document.prototype,'hidden',{get:f,configurable:true});
Object.defineProperty(document,'visibilityState',{get:p,configurable:true});
Object.defineProperty(document,'hidden',{get:f,configurable:true});
Document.prototype.hasFocus=t;
document.hasFocus=t;
['visibilitychange','webkitvisibilitychange','blur','focusout','pagehide'].forEach(function(e){
window.addEventListener(e,function(ev){ev.stopImmediatePropagation();ev.stopPropagation();},true);
document.addEventListener(e,function(ev){ev.stopImmediatePropagation();ev.stopPropagation();},true);
});
setInterval(function(){
if(document.visibilityState!=='visible'){Object.defineProperty(document,'visibilityState',{get:p,configurable:true});}
if(document.hidden!==false){Object.defineProperty(document,'hidden',{get:f,configurable:true});}
if(!document.hasFocus()){document.hasFocus=t;}
},250);
var b=document.createElement('div');
b.style='position:fixed;bottom:16px;right:16px;z-index:999999;background:#0f172a;color:#00f0ff;border:2px solid #00f0ff;padding:8px 14px;border-radius:24px;font-family:sans-serif;font-size:12px;font-weight:bold;box-shadow:0 4px 12px rgba(0,0,0,0.5);';
b.innerText='🛡️ Nhật Anh browser: ĐANG BẬT';
document.body.appendChild(b);
setTimeout(function(){b.style.opacity='0.4';},3000);
}catch(err){alert('Lỗi kích hoạt: '+err.message);}
})();
        """.trimIndent().replace("\n", "")
    }

    /**
     * Generates standard Tampermonkey / Violentmonkey Userscript for Kiwi Browser, Firefox Android, Lemur, etc.
     * Runs automatically at document-start on ALL websites.
     */
    fun getUserscriptCode(): String {
        return """
// ==UserScript==
// @name         Nhật Anh browser
// @namespace    https://anhdev.io.vn/
// @version      1.0
// @description  Chặn tính năng Return Detector, khóa visibilityState=visible và hasFocus=true trên mọi website
// @match        *://*/*
// @run-at       document-start
// @grant        none
// ==/UserScript==

(function() {
    'use strict';
    if (window.__ANTI_RETURN_ACTIVE__) return;
    window.__ANTI_RETURN_ACTIVE__ = true;

    var p = function() { return 'visible'; };
    var f = function() { return false; };
    var t = function() { return true; };

    function lock() {
        try {
            Object.defineProperty(Document.prototype, 'visibilityState', { get: p, configurable: true });
            Object.defineProperty(Document.prototype, 'hidden', { get: f, configurable: true });
            Object.defineProperty(document, 'visibilityState', { get: p, configurable: true });
            Object.defineProperty(document, 'hidden', { get: f, configurable: true });
            Document.prototype.hasFocus = t;
            document.hasFocus = t;
        } catch(e) {}
    }
    lock();

    ['visibilitychange', 'webkitvisibilitychange', 'mozvisibilitychange'].forEach(function(e) {
        window.addEventListener(e, function(ev) {
            ev.stopImmediatePropagation();
            ev.stopPropagation();
        }, true);
        document.addEventListener(e, function(ev) {
            ev.stopImmediatePropagation();
            ev.stopPropagation();
        }, true);
    });

    window.addEventListener('blur', function(ev) {
        if (ev.target === window || ev.target === document) {
            ev.stopImmediatePropagation();
            ev.stopPropagation();
        }
    }, true);

    setInterval(lock, 250);
})();
        """.trimIndent()
    }
}
