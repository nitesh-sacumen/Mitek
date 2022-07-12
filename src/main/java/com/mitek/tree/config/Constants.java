package com.mitek.tree.config;

/**
 * @author Sacumen(www.sacumen.com)
 * <p>
 * Constants class which defines constants field which will be used
 * throughout the application
 */
public final class Constants {

    private Constants() {
    }

    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String SCOPE = "scope";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CONSENT_DATA = "consent_data";

    public static final String VERIFICATION_CHOICE = "verification_choice";

    public static final String API_TOKEN_URL = "/connect/token";
    public static final String VERIFY_DOCUMENT_API_URL = "/api/verify/v2/dossier";
    public static final String IS_VERIFICATION_REFRESH = "is_verification_refresh";
    public static final String VERIFICATION_RESULT = "verification_result";

    public static final String VERIFICATION_SUCCESS = "verification_success";
    public static final String VERIFICATION_FAILURE = "verification_failure";
    public static final String VERIFICATION_RETRY = "verification_retry";
    public static final String BASE64_STARTS_WITH = "data";
    public static final String VERIFICATION_REFERENCE_ID = "verification_reference_id";
    public static final String RETAKE_COUNT = "retake_count";
    public static final String RETRY_COUNT = "retry_count";
    public static final String PDF_417_CODE = "pdf_417_code";

    public static final String BACK_VERIFICATION_OPTION = "PDF417_BARCODE";
    public static final String DOCUMENT_VERIFICATION_OPTION = "DOCUMENT";
    public static final String PASSPORT_VERIFICATION_OPTION = "PASSPORT";
    public static final String SELFIE_VERIFICATION_OPTION = "SELFIE";
    public static final String JS_URL = "/mitek/p1.js";
    public static final int REQUEST_TIMEOUT = 30;
    public static final String MAX_RETAKE_COUNT = "max_retake_count";
    public static final String MAX_RETRY_COUNT = "max_retry_count";
    public static final String TIMEOUT_VALUE = "timeout_value";
    public static final String API_URL = "api_url";
}