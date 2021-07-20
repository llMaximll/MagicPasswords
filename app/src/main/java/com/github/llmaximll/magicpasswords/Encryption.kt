package com.github.llmaximll.magicpasswords

import android.content.Context
import android.util.Base64
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val salt = "QWlGnHNhMTJfQWZ342bGhpV"
private const val iv = "W1397bfVQzFRQNjc4UFaXz"

class Encryption {
    private val cf = CommonFunctions.get()

    suspend fun encrypt(strToEncrypt: String, context: Context) :  String? {
        return suspendCoroutine { cont ->
            try {
                val sp = cf.getSharedPreferences(context)
                val mySecretKey: String? = sp.getString(cf.spSecretKey, null)
                val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val spec =  PBEKeySpec(
                    mySecretKey?.toCharArray(),
                    Base64.decode(salt, Base64.NO_PADDING),
                    10000,
                    256
                )
                val tmp = factory.generateSecret(spec)
                val secretKey =  SecretKeySpec(tmp.encoded, "AES")

                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
                val result = Base64.encodeToString(cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)), Base64.NO_PADDING)
                cont.resume(result)
            } catch (e: Exception) {
                println("Error while encrypting: $e")
                cont.resumeWithException(e)
            }
        }
    }

    fun decrypt(strToDecrypt : String, context: Context) : String? {
        try {
            val sp = cf.getSharedPreferences(context)
            val mySecretKey: String? = sp.getString(cf.spSecretKey, null)
            val ivParameterSpec =  IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =  PBEKeySpec(
                mySecretKey?.toCharArray(),
                Base64.decode(salt, Base64.NO_PADDING),
                10000,
                256
            )
            val tmp = factory.generateSecret(spec);
            val secretKey =  SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            return  String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.NO_PADDING)))
        } catch (e : Exception) {
            println("Error while decrypting: $e");
        }
        return null
    }
}