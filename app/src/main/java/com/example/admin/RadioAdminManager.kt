package com.example.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class AdminPromotion(
    val id: String = UUID.randomUUID().toString(),
    val tag: String,
    val title: String,
    val schedule: String,
    val description: String,
    val bannerUrl: String? = null,
    val tagColorHex: String = "#C78400",
    val active: Boolean = true
)

data class PromotionParticipant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lastName: String = "",
    val email: String = "",
    val phone: String,
    val birthDate: String = "",
    val gender: String = "",
    val campaignId: String,
    val campaignTitle: String,
    val onAirProgram: String = "",
    val notes: String? = null,
    val registeredAt: Long = System.currentTimeMillis()
)

data class SponsorAdBanner(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sponsorName: String,
    val imageUrl: String,
    val targetPurchaseUrl: String,
    val callToAction: String = "Comprar Agora",
    val priceOrOffer: String? = null,
    val durationSeconds: Int = 6,
    val publishedToCarousel: Boolean = true,
    val active: Boolean = true
)

data class StationConfig(
    val whatsappNumber: String = "+55 85 99278-8437",
    val contactEmail: String = "roberios9.tialpaz@gmail.com",
    val websiteUrl: String = "https://radiostudioarmix.com.br",
    val adminPin: String = "8437"
)

object RadioAdminManager {
    private const val TAG = "RadioAdminManager"
    private const val PREFS_NAME = "studio_armix_admin_prefs"

    // Strictly authorized contact details for receiving admin PINs
    const val AUTHORIZED_ADMIN_PHONE = "+5585992788437"
    const val AUTHORIZED_ADMIN_PHONE_CLEAN = "5585992788437"
    const val AUTHORIZED_ADMIN_EMAIL = "roberios9.tialpaz@gmail.com"

    private var appContext: Context? = null
    var isAuthenticatedAsAdmin: Boolean = false

    // One-time dynamic security code storage
    private var activeSecurityCode: String? = null
    private var codeGeneratedTimestamp: Long = 0L
    private const val CODE_VALIDITY_MS = 15 * 60 * 1000L // 15 minutes

    // Initial default promotions matching radio station campaigns and Print 3 reference
    private val defaultPromotions = listOf(
        AdminPromotion(
            id = "promo_basike_speaker",
            tag = "PROMO ESPECIAL",
            title = "Caixa de Som Bluetooth Basike Pro RGB",
            schedule = "Sorteio ao vivo na programação de hoje",
            description = "Concorra à caixa de som bluetooth Basike com anel de LED dinâmico RGB e graves potentes. Prêmio oficial dos ouvintes Studio Armix!",
            bannerUrl = "res://drawable/promo_basike_speaker_1794049765618",
            tagColorHex = "#D32F2F",
            active = true
        ),
        AdminPromotion(
            id = "promo_smartwatch_ultra",
            tag = "PRÊMIO DA HORA",
            title = "Smartwatch Ultra Series Digital",
            schedule = "Sorteio na próxima hora com o locutor no ar",
            description = "Relógio inteligente multifunções com monitoramento esportivo, notificações e tela AMOLED HD de alta resolução.",
            bannerUrl = "res://drawable/promo_smartwatch_prize_1794049779277",
            tagColorHex = "#F5A603",
            active = true
        ),
        AdminPromotion(
            id = "kit_studio_armix",
            tag = "EXCLUSIVO",
            title = "Kit Oficial Rádio Studio Armix",
            schedule = "Sorteio todo sábado ao vivo",
            description = "Camiseta exclusiva personalizada, caneca térmica, boné e adesivos oficiais da melhor rádio do Brasil.",
            bannerUrl = "res://drawable/radio_station_logo",
            tagColorHex = "#E65100",
            active = true
        )
    )

    // Initial sponsor product ad banners with real products from radiostudioarmix.com.br
    private val defaultSponsorBanners = listOf(
        SponsorAdBanner(
            id = "ad_lift_detox",
            title = "Lift Detox Black - Fórmula Original",
            sponsorName = "Lift Detox Oficial",
            imageUrl = "https://studioarmix.unaux.com/audio/liftdetox123456.png",
            targetPurchaseUrl = "https://radiostudioarmix.com.br",
            callToAction = "Comprar com Desconto",
            priceOrOffer = "Oferta Exclusiva Ouvintes Studio Armix",
            active = true
        ),
        SponsorAdBanner(
            id = "ad_durasil",
            title = "Durasil Serum Íntimo Masculino",
            sponsorName = "Durasil Brasil",
            imageUrl = "https://studioarmix.unaux.com/audio/durasilserumintimo1254.png",
            targetPurchaseUrl = "https://radiostudioarmix.com.br",
            callToAction = "Acessar Loja Oficial",
            priceOrOffer = "Frete Grátis para todo Brasil",
            active = true
        ),
        SponsorAdBanner(
            id = "ad_huge_hair",
            title = "Huge Hair - Vitaminas para Cabelo e Barba",
            sponsorName = "Huge Hair Care",
            imageUrl = "https://studioarmix.unaux.com/audio/hugehair1245.png",
            targetPurchaseUrl = "https://radiostudioarmix.com.br",
            callToAction = "Garantir Meu Kit",
            priceOrOffer = "Até 50% OFF Hoje",
            active = true
        ),
        SponsorAdBanner(
            id = "ad_ozenfit",
            title = "Ozenfit Caps - Suplemento Premium",
            sponsorName = "Ozenfit Nutrição",
            imageUrl = "https://studioarmix.unaux.com/audio/ozenfitcaps147.png",
            targetPurchaseUrl = "https://radiostudioarmix.com.br",
            callToAction = "Ver Promoção",
            priceOrOffer = "Compre 2 Leve 3",
            active = true
        )
    )

    private val _promotions = MutableStateFlow<List<AdminPromotion>>(defaultPromotions)
    val promotions: StateFlow<List<AdminPromotion>> = _promotions.asStateFlow()

    private val _participants = MutableStateFlow<List<PromotionParticipant>>(emptyList())
    val participants: StateFlow<List<PromotionParticipant>> = _participants.asStateFlow()

    private val _sponsorBanners = MutableStateFlow<List<SponsorAdBanner>>(defaultSponsorBanners)
    val sponsorBanners: StateFlow<List<SponsorAdBanner>> = _sponsorBanners.asStateFlow()

    private val _stationConfig = MutableStateFlow(StationConfig())
    val stationConfig: StateFlow<StationConfig> = _stationConfig.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        loadFromDisk()
    }

    /**
     * Generates a confidential 6-digit PIN valid for 15 minutes.
     * The PIN is sent directly to the authorized admin phone (+5585992788437)
     * via WhatsApp, SMS, or Email (roberios9.tialpaz@gmail.com).
     */
    fun getOrGenerateSecurityCode(): String {
        val now = System.currentTimeMillis()
        if (activeSecurityCode != null && (now - codeGeneratedTimestamp) < CODE_VALIDITY_MS) {
            return activeSecurityCode!!
        }
        val newCode = (100000..999999).random().toString()
        activeSecurityCode = newCode
        codeGeneratedTimestamp = now
        return newCode
    }

    /**
     * Sends the authorization PIN to the admin's WhatsApp (+5585992788437).
     * Strictly targets this registered administrator number only.
     */
    fun sendCodeViaWhatsApp(context: Context): Boolean {
        val code = getOrGenerateSecurityCode()
        val message = "🔒 *RÁDIO STUDIO ARMIX - CÓDIGO DE AUTORIZAÇÃO ADMIN*\n\n" +
                "Seu código PIN de segurança para acesso ao Painel Administrativo é:\n" +
                "👉 *${code}*\n\n" +
                "⚠️ Este código é restrito ao administrador e expira em 15 minutos.\n" +
                "Não compartilhe com nenhuma outra pessoa."
        val url = "https://api.whatsapp.com/send?phone=$AUTHORIZED_ADMIN_PHONE_CLEAN&text=${Uri.encode(message)}"
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending WhatsApp PIN", e)
            false
        }
    }

    /**
     * Sends the authorization PIN via SMS directly to +5585992788437.
     */
    fun sendCodeViaSms(context: Context): Boolean {
        val code = getOrGenerateSecurityCode()
        val message = "Radio Studio Armix: Seu PIN de autorizacao Admin e: $code (Valido por 15 min)."
        return try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$AUTHORIZED_ADMIN_PHONE")).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS PIN", e)
            false
        }
    }

    /**
     * Sends the authorization PIN via Email directly to roberios9.tialpaz@gmail.com.
     */
    fun sendCodeViaEmail(context: Context): Boolean {
        val code = getOrGenerateSecurityCode()
        val subject = "🔒 Código de Autorização Admin - Rádio Studio Armix"
        val body = "Olá, Administrador!\n\n" +
                "Seu código PIN de segurança para acesso ao Painel Admin da Rádio Studio Armix é:\n\n" +
                "PIN: $code\n\n" +
                "Este código expira em 15 minutos.\n\n" +
                "Rádio Studio Armix - Guaiúba-CE\n" +
                "radiostudioarmix.com.br"
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$AUTHORIZED_ADMIN_EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending Email PIN", e)
            false
        }
    }

    /**
     * Verifies the PIN entered by the user without ever exposing the code.
     * Accepts either the dynamic 6-digit code or the master PIN.
     */
    fun verifyPin(inputPin: String): Boolean {
        val cleanInput = inputPin.trim()
        if (cleanInput.isEmpty()) return false
        val now = System.currentTimeMillis()
        val isDynamicValid = activeSecurityCode != null &&
                cleanInput == activeSecurityCode &&
                (now - codeGeneratedTimestamp) < CODE_VALIDITY_MS
        val isMasterValid = cleanInput == _stationConfig.value.adminPin.trim()

        if (isDynamicValid || isMasterValid) {
            isAuthenticatedAsAdmin = true
            // Invalidate dynamic code after successful login
            activeSecurityCode = null
            return true
        }
        return false
    }

    fun logoutAdmin() {
        isAuthenticatedAsAdmin = false
        activeSecurityCode = null
    }

    // --- PROMOTIONS CRUD ---
    fun addPromotion(promo: AdminPromotion) {
        val updated = _promotions.value.toMutableList()
        updated.add(0, promo)
        _promotions.value = updated
        savePromotionsToDisk()
    }

    fun updatePromotion(promo: AdminPromotion) {
        val updated = _promotions.value.map {
            if (it.id == promo.id) promo else it
        }
        _promotions.value = updated
        savePromotionsToDisk()
    }

    fun deletePromotion(id: String) {
        val updated = _promotions.value.filter { it.id != id }
        _promotions.value = updated
        savePromotionsToDisk()
    }

    fun togglePromotion(id: String, active: Boolean) {
        val updated = _promotions.value.map {
            if (it.id == id) it.copy(active = active) else it
        }
        _promotions.value = updated
        savePromotionsToDisk()
    }

    // --- PARTICIPANTS REGISTRATION ---
    fun registerParticipant(
        name: String,
        phone: String,
        campaignId: String,
        campaignTitle: String,
        notes: String? = null
    ) {
        registerFullParticipant(
            name = name,
            lastName = "",
            email = "",
            phone = phone,
            birthDate = "",
            gender = "",
            campaignId = campaignId,
            campaignTitle = campaignTitle,
            onAirProgram = "",
            notes = notes
        )
    }

    fun registerFullParticipant(
        name: String,
        lastName: String = "",
        email: String = "",
        phone: String,
        birthDate: String = "",
        gender: String = "",
        campaignId: String,
        campaignTitle: String,
        onAirProgram: String = "",
        notes: String? = null
    ) {
        val newEntry = PromotionParticipant(
            name = name.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            birthDate = birthDate.trim(),
            gender = gender.trim(),
            campaignId = campaignId,
            campaignTitle = campaignTitle,
            onAirProgram = onAirProgram.trim(),
            notes = notes
        )
        val updated = _participants.value.toMutableList()
        // Prevent duplicate registration in the same campaign with the same phone
        updated.removeAll { it.campaignId == campaignId && it.phone == phone.trim() }
        updated.add(0, newEntry)
        _participants.value = updated
        saveParticipantsToDisk()
        Log.d(TAG, "Registered participant: ${newEntry.name} ${newEntry.lastName} for $campaignTitle during $onAirProgram")
    }

    fun deleteParticipant(id: String) {
        val updated = _participants.value.filter { it.id != id }
        _participants.value = updated
        saveParticipantsToDisk()
    }

    fun clearParticipantsForCampaign(campaignId: String) {
        val updated = _participants.value.filter { it.campaignId != campaignId }
        _participants.value = updated
        saveParticipantsToDisk()
    }

    // --- SPONSOR AD BANNERS CRUD ---
    fun addSponsorBanner(banner: SponsorAdBanner) {
        val updated = _sponsorBanners.value.toMutableList()
        updated.add(0, banner)
        _sponsorBanners.value = updated
        saveSponsorBannersToDisk()
    }

    fun updateSponsorBanner(banner: SponsorAdBanner) {
        val updated = _sponsorBanners.value.map {
            if (it.id == banner.id) banner else it
        }
        _sponsorBanners.value = updated
        saveSponsorBannersToDisk()
    }

    fun deleteSponsorBanner(id: String) {
        val updated = _sponsorBanners.value.filter { it.id != id }
        _sponsorBanners.value = updated
        saveSponsorBannersToDisk()
    }

    fun toggleSponsorBanner(id: String, active: Boolean) {
        val updated = _sponsorBanners.value.map {
            if (it.id == id) it.copy(active = active) else it
        }
        _sponsorBanners.value = updated
        saveSponsorBannersToDisk()
    }

    fun publishToCarousel(id: String, published: Boolean = true) {
        val updated = _sponsorBanners.value.map {
            if (it.id == id) it.copy(publishedToCarousel = published, active = true) else it
        }
        _sponsorBanners.value = updated
        saveSponsorBannersToDisk()
    }

    // --- STATION CONFIG ---
    fun updateConfig(config: StationConfig) {
        _stationConfig.value = config
        saveConfigToDisk()
    }

    fun resetToDefaults() {
        _promotions.value = defaultPromotions
        _sponsorBanners.value = defaultSponsorBanners
        _stationConfig.value = StationConfig()
        savePromotionsToDisk()
        saveSponsorBannersToDisk()
        saveConfigToDisk()
    }

    // --- PERSISTENCE ---
    private fun savePromotionsToDisk() {
        val ctx = appContext ?: return
        try {
            val jsonArray = JSONArray()
            for (p in _promotions.value) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("tag", p.tag)
                obj.put("title", p.title)
                obj.put("schedule", p.schedule)
                obj.put("description", p.description)
                obj.put("bannerUrl", p.bannerUrl ?: "")
                obj.put("tagColorHex", p.tagColorHex)
                obj.put("active", p.active)
                jsonArray.put(obj)
            }
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("admin_promotions", jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving promotions", e)
        }
    }

    private fun saveParticipantsToDisk() {
        val ctx = appContext ?: return
        try {
            val jsonArray = JSONArray()
            for (p in _participants.value.take(200)) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("name", p.name)
                obj.put("lastName", p.lastName)
                obj.put("email", p.email)
                obj.put("phone", p.phone)
                obj.put("birthDate", p.birthDate)
                obj.put("gender", p.gender)
                obj.put("campaignId", p.campaignId)
                obj.put("campaignTitle", p.campaignTitle)
                obj.put("onAirProgram", p.onAirProgram)
                obj.put("notes", p.notes ?: "")
                obj.put("registeredAt", p.registeredAt)
                jsonArray.put(obj)
            }
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("admin_participants", jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving participants", e)
        }
    }

    private fun saveSponsorBannersToDisk() {
        val ctx = appContext ?: return
        try {
            val jsonArray = JSONArray()
            for (b in _sponsorBanners.value) {
                val obj = JSONObject()
                obj.put("id", b.id)
                obj.put("title", b.title)
                obj.put("sponsorName", b.sponsorName)
                obj.put("imageUrl", b.imageUrl)
                obj.put("targetPurchaseUrl", b.targetPurchaseUrl)
                obj.put("callToAction", b.callToAction)
                obj.put("priceOrOffer", b.priceOrOffer ?: "")
                obj.put("durationSeconds", b.durationSeconds)
                obj.put("publishedToCarousel", b.publishedToCarousel)
                obj.put("active", b.active)
                jsonArray.put(obj)
            }
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("admin_banners", jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving sponsor banners", e)
        }
    }

    private fun saveConfigToDisk() {
        val ctx = appContext ?: return
        try {
            val cfg = _stationConfig.value
            val obj = JSONObject()
            obj.put("whatsappNumber", cfg.whatsappNumber)
            obj.put("contactEmail", cfg.contactEmail)
            obj.put("websiteUrl", cfg.websiteUrl)
            obj.put("adminPin", cfg.adminPin)
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("admin_config", obj.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving station config", e)
        }
    }

    private fun loadFromDisk() {
        val ctx = appContext ?: return
        try {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // 1. Promotions
            val promoJson = prefs.getString("admin_promotions", null)
            if (!promoJson.isNullOrEmpty()) {
                val array = JSONArray(promoJson)
                val list = mutableListOf<AdminPromotion>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(
                        AdminPromotion(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            tag = o.optString("tag", "PROMOÇÃO"),
                            title = o.optString("title", ""),
                            schedule = o.optString("schedule", ""),
                            description = o.optString("description", ""),
                            bannerUrl = o.optString("bannerUrl", "").ifEmpty { null },
                            tagColorHex = o.optString("tagColorHex", "#C78400"),
                            active = o.optBoolean("active", true)
                        )
                    )
                }
                if (list.isNotEmpty()) _promotions.value = list
            }

            // 2. Participants
            val partJson = prefs.getString("admin_participants", null)
            if (!partJson.isNullOrEmpty()) {
                val array = JSONArray(partJson)
                val list = mutableListOf<PromotionParticipant>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(
                        PromotionParticipant(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            name = o.optString("name", ""),
                            lastName = o.optString("lastName", ""),
                            email = o.optString("email", ""),
                            phone = o.optString("phone", ""),
                            birthDate = o.optString("birthDate", ""),
                            gender = o.optString("gender", "Masculino"),
                            campaignId = o.optString("campaignId", ""),
                            campaignTitle = o.optString("campaignTitle", ""),
                            onAirProgram = o.optString("onAirProgram", ""),
                            notes = o.optString("notes", "").ifEmpty { null },
                            registeredAt = o.optLong("registeredAt", System.currentTimeMillis())
                        )
                    )
                }
                _participants.value = list
            }

            // 3. Sponsor Banners
            val bannerJson = prefs.getString("admin_banners", null)
            if (!bannerJson.isNullOrEmpty()) {
                val array = JSONArray(bannerJson)
                val list = mutableListOf<SponsorAdBanner>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(
                        SponsorAdBanner(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            title = o.optString("title", ""),
                            sponsorName = o.optString("sponsorName", ""),
                            imageUrl = o.optString("imageUrl", ""),
                            targetPurchaseUrl = o.optString("targetPurchaseUrl", ""),
                            callToAction = o.optString("callToAction", "Comprar Agora"),
                            priceOrOffer = o.optString("priceOrOffer", "").ifEmpty { null },
                            durationSeconds = o.optInt("durationSeconds", 6),
                            publishedToCarousel = o.optBoolean("publishedToCarousel", true),
                            active = o.optBoolean("active", true)
                        )
                    )
                }
                if (list.isNotEmpty()) _sponsorBanners.value = list
            }

            // 4. Config
            val cfgJson = prefs.getString("admin_config", null)
            if (!cfgJson.isNullOrEmpty()) {
                val o = JSONObject(cfgJson)
                _stationConfig.value = StationConfig(
                    whatsappNumber = o.optString("whatsappNumber", "+55 85 99278-8437"),
                    contactEmail = o.optString("contactEmail", "roberios9.tialpaz@gmail.com"),
                    websiteUrl = o.optString("websiteUrl", "https://radiostudioarmix.com.br"),
                    adminPin = o.optString("adminPin", "8437")
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading admin data from disk", e)
        }
    }
}
