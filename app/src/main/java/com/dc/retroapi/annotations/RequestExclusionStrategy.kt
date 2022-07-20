package com.dc.retroapi.annotations

/**
 * This class is used to exclude encrption, checksum
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestExclusionStrategy(val value: String) // encryption,checksum - pass either or both with comma separated
{
    companion object {

        const val ENCRYPTION = "encryption"
        const val CHECKSUM = "checksum"
    }
}

