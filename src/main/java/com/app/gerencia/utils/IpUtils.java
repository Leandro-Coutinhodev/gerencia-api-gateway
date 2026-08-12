package com.app.gerencia.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    // Extrai ip
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

                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}