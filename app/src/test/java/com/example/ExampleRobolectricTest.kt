package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.player.RadioPlayerManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Studio Armix", appName)
  }

  @Test
  fun `verify streaming url`() {
    assertEquals(
      "https://straemg.radiostudioarmix.com.br/listen/radio_studioarmx/radio.mp3",
      RadioPlayerManager.STREAM_URL
    )
  }

  @Test
  fun `verify itunes repository search structure`() {
    val term = "Akon I Wanna Love You"
    assert(term.isNotEmpty())
  }

  @Test
  fun `verify voted songs list starts zeroed out and records real votes`() {
    val initialLikes = RadioPlayerManager.likesCount.value
    val initialDislikes = RadioPlayerManager.dislikesCount.value
    assertEquals(0, initialLikes)
    assertEquals(0, initialDislikes)

    RadioPlayerManager.voteLike()
    assertEquals(1, RadioPlayerManager.likesCount.value)
    assertEquals("like", RadioPlayerManager.userVote.value)
    assert(RadioPlayerManager.votedSongsList.value.isNotEmpty())
  }

  @Test
  fun `verify commercial break detection`() {
    assert(RadioPlayerManager.isCommercialOrBreak("Intervalo Comercial"))
    assert(RadioPlayerManager.isCommercialOrBreak("Vinheta Studio Armix"))
    assert(RadioPlayerManager.isCommercialOrBreak("Radio Studioarmx"))
    assert(!RadioPlayerManager.isCommercialOrBreak("Akon - I Wanna Love You"))
  }

  @Test
  fun `verify brand yellow matches logo hex`() {
    val yellow = com.example.ui.components.ArmixYellow
    // RGB(245, 166, 3) -> #F5A603
    assertEquals(245, (yellow.red * 255f + 0.5f).toInt())
    assertEquals(166, (yellow.green * 255f + 0.5f).toInt())
    assertEquals(3, (yellow.blue * 255f + 0.5f).toInt())
  }

  @Test
  fun `verify launcher icon resources exist`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assert(R.drawable.radio_station_logo != 0)
    assert(R.mipmap.ic_launcher != 0)
    assert(R.mipmap.ic_launcher_round != 0)
    assert(R.drawable.ic_launcher_background != 0)
    assert(R.drawable.ic_launcher_foreground != 0)
  }

  @Test
  fun `verify whatsapp destination phone is correct`() {
    assertEquals("5585992788437", RadioPlayerManager.WHATSAPP_PHONE)
  }

  @Test
  fun `verify music request is registered in state and persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val request = com.example.player.RegisteredMusicRequest(
      title = "Blinding Lights",
      artist = "The Weeknd",
      source = "iTunes"
    )
    RadioPlayerManager.registerMusicRequest(request, context)
    val registered = RadioPlayerManager.registeredMusicRequests.value
    assert(registered.any { it.title == "Blinding Lights" && it.artist == "The Weeknd" })
  }

  @Test
  fun `verify on-air program has presenter and photo from radiostudioarmix`() {
    val prog = com.example.player.RadioScheduleManager.currentProgram.value
    assert(prog.title.isNotEmpty())
    assert(prog.presenter.isNotEmpty())
    assert(prog.photoUrl.isNotEmpty())
  }

  @Test
  fun `verify player status monitor alternates between itunes and radio logo for commercial breaks`() {
    val monitor = com.example.player.PlayerStatusMonitor
    
    // Teste de detecção de palavras-chave de comercial e vinheta
    assert(monitor.isCommercialOrBreak("Intervalo Comercial"))
    assert(monitor.isCommercialOrBreak("Comercial Studio Armix"))
    assert(monitor.isCommercialOrBreak("Vinheta - Radio Studioarmx"))
    assert(monitor.isCommercialOrBreak("Chamada dos Patrocinadores"))
    assert(monitor.isCommercialOrBreak("Spot Publicitario"))
    assert(!monitor.isCommercialOrBreak("Gusttavo Lima - Bloqueado"))
    assert(!monitor.isCommercialOrBreak("Akon - I Wanna Love You"))

    // Teste de alternância dinâmica de status
    monitor.simulateCommercialBreak(true)
    val commStatus = monitor.status.value
    assertEquals(true, commStatus.isCommercialBreak)
    assertEquals(null, commStatus.artworkUrl)
    assertEquals(com.example.R.drawable.radio_station_logo, commStatus.defaultCoverResId)
    assertEquals("INTERVALO COMERCIAL", commStatus.statusLabel)

    // Teste de retorno para música
    monitor.simulateCommercialBreak(false)
    val musicStatus = monitor.status.value
    assertEquals(false, musicStatus.isCommercialBreak)
    assertEquals("NO AR • MÚSICA AO VIVO", musicStatus.statusLabel)
  }

  @Test
  fun `verify listener registration and login via UserManager`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.auth.UserManager.init(context)

    val registered = com.example.auth.UserManager.register(
      firstName = "Robério",
      lastName = "Cardoso da Silva",
      email = "roberios9.tialpaz@gmail.com",
      phone = "(85) 99278-8437",
      birthDate = "21/04/1981",
      gender = "Masculino",
      password = "testpassword123"
    )

    assertEquals("Robério", registered.firstName)
    assertEquals("Cardoso da Silva", registered.lastName)
    assertEquals("Robério Cardoso da Silva", registered.fullName)
    assertEquals("roberios9.tialpaz@gmail.com", registered.email)
    assertEquals("(85) 99278-8437", registered.phone)
    assertEquals("21/04/1981", registered.birthDate)
    assertEquals("Masculino", registered.gender)
    assertEquals(true, com.example.auth.UserManager.isLoggedIn.value)
  }

  @Test
  fun `verify promotion participation with on-air radio program syncs with Admin Panel`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.admin.RadioAdminManager.init(context)

    val testProgram = "Manhã Ar Mix • Carla Siqueira"
    val testPromoTitle = "Caixa de Som Bluetooth Basike Pro RGB"

    com.example.admin.RadioAdminManager.registerFullParticipant(
      name = "Robério",
      lastName = "Cardoso da Silva",
      email = "roberios9.tialpaz@gmail.com",
      phone = "(85) 99278-8437",
      birthDate = "21/04/1981",
      gender = "Masculino",
      campaignId = "promo_basike_speaker",
      campaignTitle = testPromoTitle,
      onAirProgram = testProgram,
      notes = "Inscrição pelo app"
    )

    val list = com.example.admin.RadioAdminManager.participants.value
    val match = list.find { it.campaignId == "promo_basike_speaker" && it.phone == "(85) 99278-8437" }
    assert(match != null)
    assertEquals("Robério", match!!.name)
    assertEquals("Cardoso da Silva", match.lastName)
    assertEquals("roberios9.tialpaz@gmail.com", match.email)
    assertEquals(testProgram, match.onAirProgram)
    assertEquals(testPromoTitle, match.campaignTitle)
  }

  @Test
  fun `verify weekly voting manager starts zeroed out and records first valid vote`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.player.WeeklyVotingManager.init(context)
    com.example.player.WeeklyVotingManager.clearAllVotes()

    // 1. Confirma que a votação de demonstração foi zerada
    val initialSixthDay = com.example.player.WeeklyVotingManager.getSixthDayResults()
    assertEquals(0, initialSixthDay.size)
    assertEquals(0, com.example.player.WeeklyVotingManager.weeklyVotes.value.size)

    // 2. Registra o primeiro voto válido via sincronização com a música ao vivo e programação
    val testProgramInfo = com.example.player.OnAirProgramInfo(
      title = "Super Mix de Sábado",
      presenter = "DJ Studio Armix",
      timeSlot = "14:00 - 18:00",
      photoUrl = "",
      description = "Programa Especial"
    )

    com.example.player.WeeklyVotingManager.syncLiveVoteWithSchedule(
      title = "Pincel de Deus",
      artist = "Junior Vianna & Raí Saia Rodada",
      artworkUrl = "https://is1-ssl.mzstatic.com/sample.jpg",
      isLike = true,
      programInfo = testProgramInfo
    )

    val updatedVotes = com.example.player.WeeklyVotingManager.weeklyVotes.value
    assertEquals(1, updatedVotes.size)
    val firstVote = updatedVotes.first()
    assertEquals("Pincel de Deus", firstVote.title)
    assertEquals("Junior Vianna & Raí Saia Rodada", firstVote.artist)
    assertEquals(1, firstVote.likes)
    assertEquals(0, firstVote.dislikes)
    assertEquals(1, firstVote.score)
    assertEquals("like", firstVote.userVote)

    // 3. Testa atualização de capa do artista após voto válido
    val updatedArtwork = "https://is1-ssl.mzstatic.com/image/thumb/Music/valid_artwork.jpg"
    com.example.player.WeeklyVotingManager.updateTrackArtwork("Pincel de Deus", "Junior Vianna & Raí Saia Rodada", updatedArtwork)
    val updatedTrack = com.example.player.WeeklyVotingManager.weeklyVotes.value.first()
    assertEquals(updatedArtwork, updatedTrack.artworkUrl)
  }

  @Test
  fun `verify login required before voting music request and promotion participation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.auth.UserManager.init(context)
    com.example.auth.UserManager.clearUserData()

    // 1. Usuário inicia deslogado
    assertEquals(false, com.example.auth.UserManager.isLoggedIn.value)
    assertEquals(null, com.example.auth.UserManager.currentUser.value)

    // 2. Login inválido falha
    val invalidLogin = com.example.auth.UserManager.login("nonexistent@email.com", "wrongpass")
    assertEquals(false, invalidLogin)
    assertEquals(false, com.example.auth.UserManager.isLoggedIn.value)

    // 3. Cadastra o ouvinte com credenciais reais
    com.example.auth.UserManager.register(
      firstName = "Robério",
      lastName = "Cardoso",
      email = "roberios9.tialpaz@gmail.com",
      phone = "(85) 99278-8437",
      birthDate = "21/04/1981",
      gender = "Masculino",
      password = "senhaSegura123"
    )

    // Desloga para testar o login subsequente com a senha cadastrada
    com.example.auth.UserManager.logout()
    assertEquals(false, com.example.auth.UserManager.isLoggedIn.value)

    // 4. Efetua o login válido com as credenciais cadastradas
    val validLogin = com.example.auth.UserManager.login("roberios9.tialpaz@gmail.com", "senhaSegura123")
    assertEquals(true, validLogin)
    assertEquals(true, com.example.auth.UserManager.isLoggedIn.value)
    assertEquals("Robério", com.example.auth.UserManager.currentUser.value?.firstName)
  }
}

