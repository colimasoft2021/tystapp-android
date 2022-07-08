package com.app.tyst.data.remote


import com.app.tyst.data.model.request.GetPlaidTransactionsRequest
import com.app.tyst.data.model.response.*
import com.app.tyst.data.model.response.plaid.PlaidTransactionsResponse
import com.app.tyst.data.model.hb.WSGenericResponse
import com.dc.mvvmskeleton.data.model.hb.WSListResponse
import com.dc.mvvmskeleton.data.model.hb.WSObjectResponse
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Url
import okhttp3.ResponseBody
import retrofit2.http.GET


interface ApiService {

    @FormUrlEncoded
    @POST("user_login_email")
    //fun loginWithEmail(@FieldMap map: HashMap<String, String>): Call<WSListResponse<LoginResponse>>
    fun loginWithEmail(@FieldMap map: HashMap<String, String>): Call<WSObjectResponse<UserDetailResponse>>

    @FormUrlEncoded
    @POST("user_login_phone")
    fun loginWithPhone(@FieldMap map: HashMap<String, String>): Call<WSListResponse<LoginResponse>>

    @FormUrlEncoded
    @POST("social_login")
    fun loginWithSocial(@FieldMap map: HashMap<String, String>): Call<WSObjectResponse<UserDetailResponse>>

    @POST("forgot_password")
    @FormUrlEncoded
    fun callForgotPassword(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("forgot_password_phone")
    @FormUrlEncoded
    fun callForgotPasswordWithPhone(@FieldMap map: HashMap<String, String>): Call<WSListResponse<ForgotPasswordPhoneResponse>>

    @POST("reset_password_phone")
    @FormUrlEncoded
    fun callResetPassword(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @Multipart
    @POST("user_sign_up_email")
    fun callSignUpWithEmail(
            @PartMap map: HashMap<String, RequestBody>,
            @Part file: MultipartBody.Part? = null
    ): Call<WSGenericResponse<JsonElement>>

    @Multipart
    @POST("user_sign_up_phone")
    fun callSignUpWithPhone(
            @PartMap map: HashMap<String, RequestBody>,
            @Part file: MultipartBody.Part? = null
    ): Call<WSObjectResponse<UserDetailResponse>>

    @Multipart
    @POST("social_sign_up")
    fun callSignUpWithSocial(
            @PartMap map: HashMap<String, RequestBody>,
            @Part file: MultipartBody.Part? = null
    ): Call<WSGenericResponse<JsonElement>>

    /**
     * Api call for check unique number. This api will take following input parameter in hash map
     * @param type  phone/email    phone -> If phone number unique check, email -> If email unique check
     * @param email (optional) check email unique
     * @param mobile_number (optional) check phone unique
     * @param map HashMap<String, String>
     * @return Call<WSListResponse<OTPResponse>>
     */
    @POST("check_unique_user")
    @FormUrlEncoded
    fun callCheckUniqueUser(@FieldMap map: HashMap<String, String>): Call<WSListResponse<OTPResponse>>

    @GET("states_list")
    fun callGetStateList(): Call<WSListResponse<StatesResponse>>


    @POST("update_device_token")
    @FormUrlEncoded
    fun callUpdateDeviceToken(@Field("device_token") deviceToken: String): Call<WSGenericResponse<JsonElement>>

    /**
     * Api call for update push notification on/off setting
     * @param notification String
     * @return Call<WSGenericResponse<JsonElement>>
     */
    @POST("update_push_notification_settings")
    @FormUrlEncoded
    fun callNotificationSetting(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("logout")
    fun callLogOut(): Call<WSGenericResponse<JsonElement>>

    @POST("static_pages")
    @FormUrlEncoded
    fun callStaticPage(@FieldMap map: HashMap<String, String>): Call<WSListResponse<StaticPageResponse>>

    @POST("post_a_feedback")
    @Multipart
    fun callSendFeedback(@PartMap map: HashMap<String, RequestBody>, @Part files: List<MultipartBody.Part>?): Call<WSGenericResponse<JsonElement>>

    @POST("change_password")
    @FormUrlEncoded
    fun callChangePassword(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("change_mobile_number")
    @FormUrlEncoded
    fun callChangePhoneNumber(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @Multipart
    @POST("edit_profile")
    fun callEditProfile(
            @PartMap map: HashMap<String, RequestBody>,
            @Part file: MultipartBody.Part? = null
    ): Call<WSGenericResponse<JsonElement>>

    @POST("subscription_purchase")
    @FormUrlEncoded
    fun callSubscriptionPurchase(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("delete_account")
    fun callDeleteAccount(): Call<WSGenericResponse<JsonElement>>

    @POST("get_config_paramaters")
    fun callConfigParameters(): Call<WSListResponse<VersionConfigResponse>>

    @POST("add_bank_account")
    @FormUrlEncoded
    fun callAddBankAccount(@FieldMap map: HashMap<String, String>): Call<WSListResponse<PlaidInstitutionResponse>>

    @POST("/transactions/get")
    fun getPlaidTransactions(@Body requestBody: GetPlaidTransactionsRequest): Call<PlaidTransactionsResponse>

    @POST("get_all_institutions_transaction_data")
    @FormUrlEncoded
    fun callAllInstitutionsTransactions(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("get_user_institutions")
    fun getUserInstitutions(): Call<WSListResponse<PlaidInstitutionResponse>>

    @POST("fetch_transaction_details")
    @FormUrlEncoded
    fun callFetchTransactions(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("get_transaction_data")
    @FormUrlEncoded
    fun callTransactionData(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("get_transaction_details")
    @FormUrlEncoded
    fun callTransactionDetail(@FieldMap map: HashMap<String, String>): Call<WSListResponse<TransactionDetailResponse>>

    @Multipart
    @POST("pay_by_cash")
    fun callPayByCash(
            @PartMap map: HashMap<String, RequestBody>,
            @Part files: List<MultipartBody.Part>?
    ): Call<WSGenericResponse<JsonElement>>


    @GET("category_list")
    fun callCategoryList(): Call<WSListResponse<CategoryResponse>>

    @POST("delete_bank_account")
    @FormUrlEncoded
    fun callDeleteBankAccount(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @GET("get_log_list")
    fun callLogList(): Call<WSListResponse<LogListResponse>>

    @GET
    fun downloadTransactionLogFile(@Url fileUrl: String): Call<ResponseBody>

    @POST("generate_log")
    @FormUrlEncoded
    fun callGenerateLog(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @GET("reason_list")
    fun callReasonList(): Call<WSListResponse<ReasonListResponse>>

    @POST("change_tax")
    @FormUrlEncoded
    fun callChangeTax(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("add_tip")
    @FormUrlEncoded
    fun callAddTip(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("delete_transaction")
    @FormUrlEncoded
    fun callDeleteTransaction(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("get_plaid_settings")
    fun callPlaidSettings(): Call<WSGenericResponse<JsonElement>>

    @POST("get_subscription_status")
    fun callSubscriptionStatus(): Call<WSGenericResponse<JsonElement>>

    @POST("get_user_accounts")
    @FormUrlEncoded
    fun callUserAccounts(@FieldMap map: HashMap<String, String>): Call<WSListResponse<AccountResponse>>

    @POST("update_transaction_category")
    @FormUrlEncoded
    fun callUpdateCategory(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @FormUrlEncoded
    @POST("generate_public_token")
    fun callGeneratePublicToken(@FieldMap map: HashMap<String, String>): Call<WSListResponse<PublicTokenResponse>>

    @POST("insert_update_institution_error_log")
    @FormUrlEncoded
    fun callUpdateInstitutionErrorLog(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("update_page_version")
    @FormUrlEncoded
    fun callUpdateTNCPrivacyPolicy(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>

    @POST("send_verification_link")
    @FormUrlEncoded
    fun callSendVerificationLink(@FieldMap map: HashMap<String, String>): Call<WSGenericResponse<JsonElement>>
}