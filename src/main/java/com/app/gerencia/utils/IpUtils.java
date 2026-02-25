package com.app.gerencia.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    /**
     * Extrai o IP real do cliente, considerando headers de proxy reverso
     * (Nginx, Apache, Cloudflare, load balancers, etc.).
     */
    public static String getClientIp(HttpServletRequest request) {

        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP",        // Cloudflare
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",      // WebLogic
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For pode ter vários IPs: "clientIp, proxy1, proxy2"
                // O primeiro é o IP real do cliente
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}