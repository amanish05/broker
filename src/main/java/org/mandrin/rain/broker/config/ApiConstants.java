package org.mandrin.rain.broker.config;

public class ApiConstants {
    public static final String HOLDINGS_URL = "https://api.kite.trade/portfolio/holdings";
    public static final String KITE_VERSION_HEADER = "X-Kite-Version";
    public static final String KITE_VERSION = "3";
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_TOKEN_FORMAT = "token %s:%s";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final String NOT_AUTHENTICATED_MSG = "Not authenticated";
}
