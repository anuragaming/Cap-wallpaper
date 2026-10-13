package com.example.engine

import com.example.model.ShieldConfig

object AntiReturnScriptEngine {

    /**
     * Generates JavaScript code to inject into WebViews to neutralize Return Detectors.
     */
    fun buildInjectionScript(config: ShieldConfig): String {
        if (!config.isMasterShieldEnabled) {
            return "/* Nhật Anh browser is disabled */"
        }

        return """
        (function() {
            'use strict';
            if (window.__ANTI_RETURN_SHIELD_V2_ACTIVE__) return;
            window.__ANTI_RETURN_SHIELD_V2_ACTIVE__ = true;

            function reportBlock(type, detail) {
                try {
                    if (window.AntiReturnBridge && window.AntiReturnBridge.onDetectionBlocked) {
                        window.AntiReturnBridge.onDetectionBlocked(type, window.location.hostname || 'local', detail || '');
                    }
                } catch(e) {}
            }

            // =========================================================================
            // 1. CAPTURE-PHASE EVENT SUPPRESSION (Top Priority)
            // Even if an external library attaches before us, our capture-phase listener
            // stops immediate propagation so no listener down the DOM chain ever sees it!
            // =========================================================================
            try {
                var blockedEvents = ['visibilitychange', 'webkitvisibilitychange', 'mozvisibilitychange', 'msvisibilitychange'];
                for (var i = 0; i < blockedEvents.length; i++) {
                    var evtName = blockedEvents[i];
                    window.addEventListener(evtName, function(e) {
                        e.stopImmediatePropagation();
                        e.stopPropagation();
                        reportBlock('VISIBILITY_CHANGE', 'Triệt tiêu sự kiện ' + e.type + ' trong capture phase');
                    }, true);

                    if (typeof document !== 'undefined' && document) {
                        document.addEventListener(evtName, function(e) {
                            e.stopImmediatePropagation();
                            e.stopPropagation();
                        }, true);
                    }
                }

                // Suppress window blur & focusout in capture phase (excluding form input fields)
                window.addEventListener('blur', function(e) {
                    if (e.target && (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT' || e.target.isContentEditable)) {
                        return; // Let user focus and type in inputs without interruption
                    }
                    if (e.target === window || e.target === document) {
                        e.stopImmediatePropagation();
                        e.stopPropagation();
                        reportBlock('WINDOW_BLUR', 'Triệt tiêu sự kiện blur trong capture phase');
                    }
                }, true);

                window.addEventListener('focusout', function(e) {
                    if (e.target && (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT' || e.target.isContentEditable)) {
                        return; // Let user focus and type in inputs without interruption
                    }
                    if (e.target === window || e.target === document) {
                        e.stopImmediatePropagation();
                        e.stopPropagation();
                    }
                }, true);

                window.addEventListener('pagehide', function(e) {
                    e.stopImmediatePropagation();
                    e.stopPropagation();
                    reportBlock('PAGE_HIDE', 'Triệt tiêu sự kiện pagehide');
                }, true);

                window.addEventListener('pageshow', function(e) {
                    try {
                        Object.defineProperty(e, 'persisted', { get: function() { return false; } });
                    } catch(err) {}
                }, true);
            } catch(e) {}

            // =========================================================================
            // 2. VISIBILITY STATE & HIDDEN SPOOFER (Prototypes + Instances)
            // =========================================================================
            function enforceVisibility() {
                try {
                    var lockVisible = function() { return 'visible'; };
                    var lockFalse = function() { return false; };

                    var targets = [Document.prototype];
                    if (typeof HTMLDocument !== 'undefined') targets.push(HTMLDocument.prototype);
                    if (typeof document !== 'undefined') targets.push(document);

                    targets.forEach(function(target) {
                        if (!target) return;
                        Object.defineProperty(target, 'visibilityState', {
                            get: lockVisible,
                            set: function() {},
                            configurable: true,
                            enumerable: true
                        });
                        Object.defineProperty(target, 'hidden', {
                            get: lockFalse,
                            set: function() {},
                            configurable: true,
                            enumerable: true
                        });
                        Object.defineProperty(target, 'webkitVisibilityState', {
                            get: lockVisible,
                            set: function() {},
                            configurable: true,
                            enumerable: true
                        });
                        Object.defineProperty(target, 'webkitHidden', {
                            get: lockFalse,
                            set: function() {},
                            configurable: true,
                            enumerable: true
                        });
                    });
                } catch(e) {}
            }
            enforceVisibility();

            // =========================================================================
            // 3. DOCUMENT & WINDOW FOCUS SPOOFER
            // =========================================================================
            function enforceFocus() {
                try {
                    var alwaysFocused = function() { return true; };
                    Document.prototype.hasFocus = alwaysFocused;
                    if (typeof HTMLDocument !== 'undefined') HTMLDocument.prototype.hasFocus = alwaysFocused;
                    if (typeof document !== 'undefined') document.hasFocus = alwaysFocused;

                    Object.defineProperty(window, 'onblur', {
                        get: function() { return null; },
                        set: function() { reportBlock('WINDOW_BLUR', 'Chặn gán window.onblur'); },
                        configurable: true
                    });
                    Object.defineProperty(document, 'onblur', {
                        get: function() { return null; },
                        set: function() { reportBlock('WINDOW_BLUR', 'Chặn gán document.onblur'); },
                        configurable: true
                    });
                    Object.defineProperty(document, 'onvisibilitychange', {
                        get: function() { return null; },
                        set: function() { reportBlock('VISIBILITY_CHANGE', 'Chặn gán document.onvisibilitychange'); },
                        configurable: true
                    });
                    Object.defineProperty(window, 'onpagehide', {
                        get: function() { return null; },
                        set: function() { reportBlock('PAGE_HIDE', 'Chặn gán window.onpagehide'); },
                        configurable: true
                    });
                    Object.defineProperty(window, 'onbeforeunload', {
                        get: function() { return null; },
                        set: function() { reportBlock('EXIT_INTERCEPT', 'Chặn gán bẫy window.onbeforeunload'); },
                        configurable: true
                    });
                } catch(e) {}
            }
            enforceFocus();

            // =========================================================================
            // 4. EVENT TARGET PROTOTYPE ADD_EVENT_LISTENER HOOK
            // Hooks EventTarget (the base of Window, Document, and all Elements)
            // =========================================================================
            try {
                var origAddEventListener = EventTarget.prototype.addEventListener;
                var filteredEvents = [
                    'visibilitychange',
                    'webkitvisibilitychange',
                    'mozvisibilitychange',
                    'msvisibilitychange'
                ];

                EventTarget.prototype.addEventListener = function(type, listener, options) {
                    if (filteredEvents.indexOf(type) !== -1) {
                        reportBlock('VISIBILITY_CHANGE', 'Chặn đăng ký listener ' + type);
                        return; // Discard completely
                    }

                    if ((type === 'blur' || type === 'focusout') &&
                        (this === window || this === document || (typeof Window !== 'undefined' && this instanceof Window) || (typeof Document !== 'undefined' && this instanceof Document))) {
                        reportBlock('WINDOW_BLUR', 'Chặn đăng ký listener ' + type + ' để giữ active');
                        return; // Discard
                    }

                    if (type === 'pagehide' || type === 'freeze') {
                        reportBlock('PAGE_HIDE', 'Chặn đăng ký listener ' + type);
                        return;
                    }

                    if (type === 'beforeunload') {
                        reportBlock('EXIT_INTERCEPT', 'Chặn đăng ký beforeunload giữ chân');
                        return;
                    }

                    return origAddEventListener.apply(this, arguments);
                };

                // Also explicitly hook Document and Window in case of cached references
                if (typeof Document !== 'undefined') Document.prototype.addEventListener = EventTarget.prototype.addEventListener;
                if (typeof Window !== 'undefined') Window.prototype.addEventListener = EventTarget.prototype.addEventListener;
            } catch(e) {}

            // =========================================================================
            // 5. NAVIGATION / RETURN ENTRY SPOOFER
            // Websites check performance.getEntriesByType("navigation")[0].type === "back_forward"
            // =========================================================================
            try {
                if (typeof Performance !== 'undefined' && Performance.prototype.getEntriesByType) {
                    var origGetEntriesByType = Performance.prototype.getEntriesByType;
                    Performance.prototype.getEntriesByType = function(entryType) {
                        var entries = origGetEntriesByType.apply(this, arguments);
                        if (entryType === 'navigation' && entries && entries.length > 0) {
                            return entries.map(function(item) {
                                if (item && item.type === 'back_forward') {
                                    reportBlock('HISTORY_TRAP', 'Ngụy trang loại điều hướng back_forward thành navigate');
                                    return new Proxy(item, {
                                        get: function(target, prop) {
                                            if (prop === 'type') return 'navigate';
                                            return target[prop];
                                        }
                                    });
                                }
                                return item;
                            });
                        }
                        return entries;
                    };
                }
            } catch(e) {}

            // =========================================================================
            // 6. HISTORY RETURN HIJACK TRAP DEFENDER
            // =========================================================================
            try {
                var origPushState = history.pushState;
                var pushTimes = [];

                history.pushState = function(state, title, url) {
                    var now = Date.now();
                    pushTimes = pushTimes.filter(function(t) { return now - t < 3000; });
                    pushTimes.push(now);

                    if (pushTimes.length > 3) {
                        reportBlock('HISTORY_TRAP', 'Chặn spam history.pushState bẫy nút quay lại (' + (url || '') + ')');
                        return;
                    }
                    return origPushState.apply(this, arguments);
                };
            } catch(e) {}

            // =========================================================================
            // 7. CONTINUOUS REINFORCEMENT LOOP
            // Runs every 250ms to ensure no deferred website script ever modifies our overrides
            // =========================================================================
            setInterval(function() {
                enforceVisibility();
                enforceFocus();
            }, 250);

            reportBlock('SHIELD_INIT', 'Kích hoạt hệ thống Nhật Anh browser thành công');
        })();
        """.trimIndent()
    }
}
