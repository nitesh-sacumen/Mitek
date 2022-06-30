package com.mitek.tree.config;

/**
 * @author Sacumen(www.sacumen.com)
 * <p>
 * Constants class which defines constants field which will be used
 * through out the application
 */
public final class Constants {

    private Constants() {
    }

    public static final String API_URL = "api_url";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String SCOPE = "scope";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CONSENT_DATA = "consent_data";

    public static final String VERIFICATION_CHOICE = "verification_choice";

    public static final String IDENTITY_CHOICE = "identity_choice";
    public static final String COUNTRY_CHOICE = "country_choice";

    public static final String TEMP = "temp";
    public static final String CAPTURED_IMAGE = "captured_image";
    public static final String API_TOKEN_URL = "https://api.sandbox.west-1.us.mitekcloud.com/connect/token";
    public static final String VERIFY_DOCUMENT_API_URL = "https://api.sandbox.west-1.us.mitekcloud.com/api/verify/v2/dossier";
    public static final String BASE_64_STRING = "base_64_string";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String IS_VERIFICATION_REFRESH = "is_verification_refresh";
    public static final String IS_CAPTURE_REFRESH = "is_capture_refresh";
    public static final String VERIFICATION_RESULT = "verification_result";

    public static final String VERIFICATION_SUCCESS = "verification_success";
    public static final String VERIFICATION_FAILURE = "verification_failure";
    public static final String VERIFICATION_RETRY = "verification_retry";
    public static final String CAPTURE_RESULT = "capture_result";
    public static final String CAPTURE_FRONT = "capture_front";
    public static final String CAPTURE_BACK = "capture_back";
    public static final String BASE64_STARTS_WITH = "data";
    public static final String VERIFICATION_REFERENCE_ID = "verification_reference_id";
    public static final String RETAKE_COUNT = "retake_count";
    public static final String RETRY_COUNT = "retry_count";
    public static final String PDF_417_CODE = "pdf_417_code";
}