package jp.co.studio.kaka.data.remote.converter

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Minimal Retrofit <-> kotlinx.serialization bridge, written in-house instead of depending on
 * com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0 (last released 2023).
 * That artifact's .kotlin_module fails to resolve under this project's real compileDebugKotlin
 * frontend (Kotlin 2.2.10 + AGP 9.2 built-in Kotlin) with "Unresolved reference:
 * KotlinSerializationConverterFactory", even though KSP's separate analysis frontend resolves it
 * fine during kspDebugKotlin - a genuine split between the two frontends on this toolchain, not
 * a usage mistake. See [[build-config-gotchas]] memory for the full bisection trail.
 */
class SerializationConverterFactory private constructor(
    private val json: Json,
    private val contentType: MediaType,
) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val bodySerializer = serializer(type)
        return Converter<ResponseBody, Any> { body ->
            body.use { json.decodeFromString(bodySerializer, it.string()) }
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody> {
        val bodySerializer = serializer(type)
        return Converter<Any, RequestBody> { value ->
            json.encodeToString(bodySerializer, value).toRequestBody(contentType)
        }
    }

    companion object {
        fun create(json: Json, contentType: MediaType): SerializationConverterFactory =
            SerializationConverterFactory(json, contentType)
    }
}
