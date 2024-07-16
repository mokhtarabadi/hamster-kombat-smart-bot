import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
open class BaseResponse(
    @SerializedName("success") open val success: Boolean?,
    @SerializedName("code") open val code: String?,
    @SerializedName("message") open val message: String?
)

@Keep
data class ValidateSuccessResponse(
    @SerializedName("success") override val success: Boolean,
    @SerializedName("data") val data: ValidateSuccessData
) : BaseResponse(success, null, null)

@Keep
data class ValidateSuccessData(
    @SerializedName("id") val id: Int,
    @SerializedName("token") val token: String,
    @SerializedName("license_id") val licenseId: Int,
    @SerializedName("label") val label: String,
    @SerializedName("source") val source: Int,
    @SerializedName("ip_address") val ipAddress: String,
    @SerializedName("user_agent") val userAgent: String,
    @SerializedName("meta_data") val metaData: List<Any>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("deactivated_at") val deactivatedAt: String?,
    @SerializedName("license") val license: License? = null
)

@Keep
data class License(
    @SerializedName("id") val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("license_key") val licenseKey: String,
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("valid_for") val validFor: Int,
    @SerializedName("source") val source: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("times_activated") val timesActivated: Int,
    @SerializedName("activations_limit") val activationsLimit: Int,
    @SerializedName("is_expired") val isExpired: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("created_by") val createdBy: Int,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("updated_by") val updatedBy: Int
)

@Keep
data class ValidateFailedResponse(
    @SerializedName("code") override val code: String,
    @SerializedName("message") override val message: String,
    @SerializedName("data") val data: ValidateFailedData
) : BaseResponse(false, code, message)

@Keep data class ValidateFailedData(@SerializedName("status") val status: Int)
