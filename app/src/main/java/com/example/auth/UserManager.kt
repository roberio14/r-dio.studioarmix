package com.example.auth

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

/**
 * Perfil completo do ouvinte cadastrado na Rádio Studio Armix.
 * Corresponde aos campos do formulário oficial (Print 2).
 */
data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String, // DDD + Celular: ex: (85) 99278-8437
    val birthDate: String, // Data de nascimento: ex: 21/04/1981
    val gender: String = "Masculino", // Masculino, Feminino, Prefiro não informar
    val termsAccepted: Boolean = true,
    val registeredAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = if (lastName.isNotBlank()) "$firstName $lastName" else firstName
}

object UserManager {
    private const val TAG = "UserManager"
    private const val PREFS_NAME = "studio_armix_user_prefs"
    private const val KEY_USER_PROFILE = "current_user_profile"
    private const val KEY_USER_PASSWORD = "current_user_password"

    private var appContext: Context? = null

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean>(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        loadUserFromDisk()
    }

    /**
     * Cadastra um novo ouvinte com todos os dados do Print 2
     */
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        birthDate: String,
        gender: String,
        password: String
    ): UserProfile {
        val profile = UserProfile(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            birthDate = birthDate.trim(),
            gender = gender.trim(),
            termsAccepted = true
        )
        _currentUser.value = profile
        _isLoggedIn.value = true
        saveUserToDisk(profile, password)
        Log.d(TAG, "Novo ouvinte cadastrado: ${profile.fullName}")
        return profile
    }

    /**
     * Efetua login com e-mail e senha conferindo com o ouvinte cadastrado
     */
    fun login(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) return false
        val ctx = appContext ?: return false
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPassword = prefs.getString(KEY_USER_PASSWORD, null)
        val jsonStr = prefs.getString(KEY_USER_PROFILE, null)

        if (!jsonStr.isNullOrEmpty()) {
            val obj = JSONObject(jsonStr)
            val savedEmail = obj.optString("email", "")
            if (savedEmail.equals(email.trim(), ignoreCase = true)) {
                if (savedPassword != null && savedPassword != password) {
                    Log.w(TAG, "Senha incorreta para ouvinte: $email")
                    return false
                }
                val profile = UserProfile(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    firstName = obj.optString("firstName", ""),
                    lastName = obj.optString("lastName", ""),
                    email = obj.optString("email", ""),
                    phone = obj.optString("phone", ""),
                    birthDate = obj.optString("birthDate", ""),
                    gender = obj.optString("gender", "Masculino"),
                    termsAccepted = obj.optBoolean("termsAccepted", true),
                    registeredAt = obj.optLong("registeredAt", System.currentTimeMillis())
                )
                _currentUser.value = profile
                _isLoggedIn.value = true
                prefs.edit().putBoolean("is_logged_in", true).apply()
                Log.d(TAG, "Login bem-sucedido para ouvinte: ${profile.fullName}")
                return true
            }
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun clearUserData() {
        _isLoggedIn.value = false
        _currentUser.value = null
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun saveUserToDisk(profile: UserProfile, password: String) {
        val ctx = appContext ?: return
        try {
            val obj = JSONObject().apply {
                put("id", profile.id)
                put("firstName", profile.firstName)
                put("lastName", profile.lastName)
                put("email", profile.email)
                put("phone", profile.phone)
                put("birthDate", profile.birthDate)
                put("gender", profile.gender)
                put("termsAccepted", profile.termsAccepted)
                put("registeredAt", profile.registeredAt)
            }
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_USER_PROFILE, obj.toString())
                .putString(KEY_USER_PASSWORD, password)
                .putBoolean("is_logged_in", true)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar perfil do usuário", e)
        }
    }

    private fun loadUserFromDisk() {
        val ctx = appContext ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_USER_PROFILE, null)
            val loggedIn = prefs.getBoolean("is_logged_in", false)
            if (!jsonStr.isNullOrEmpty()) {
                val obj = JSONObject(jsonStr)
                val profile = UserProfile(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    firstName = obj.optString("firstName", ""),
                    lastName = obj.optString("lastName", ""),
                    email = obj.optString("email", ""),
                    phone = obj.optString("phone", ""),
                    birthDate = obj.optString("birthDate", ""),
                    gender = obj.optString("gender", "Masculino"),
                    termsAccepted = obj.optBoolean("termsAccepted", true),
                    registeredAt = obj.optLong("registeredAt", System.currentTimeMillis())
                )
                _currentUser.value = profile
                _isLoggedIn.value = loggedIn
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar perfil do usuário", e)
        }
    }
}
