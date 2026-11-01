window.stockflowSwaggerSession = (function () {
    const publicPaths = new Set([
        "/api/health",
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/auth/trocar-senha",
        "/api/auth/esqueci-senha",
        "/api/auth/redefinir-senha"
    ]);
    let sessionRequest;

    function restoreSwaggerSession() {
        if (!sessionRequest) {
            sessionRequest = (async function () {
                const session = await fetch("/api/auth/me", {
                    credentials: "same-origin",
                    cache: "no-store"
                });
                if (session.status === 401) {
                    const refresh = await fetch("/api/auth/refresh", {
                        method: "POST",
                        credentials: "same-origin",
                        cache: "no-store"
                    });
                    return refresh.ok;
                }
                return session.ok;
            })().catch(function () {
                return false;
            }).finally(function () {
                sessionRequest = undefined;
            });
        }
        return sessionRequest;
    }

    async function requestInterceptor(request) {
        const url = new URL(request.url, window.location.origin);
        if (url.origin === window.location.origin && url.pathname.startsWith("/api/")) {
            request.credentials = "same-origin";
            if (!publicPaths.has(url.pathname)
                    && !url.pathname.startsWith("/api/v3/api-docs")) {
                await restoreSwaggerSession();
            }
        }
        return request;
    }

    return {
        requestInterceptor: requestInterceptor,
        onComplete: restoreSwaggerSession
    };
})();