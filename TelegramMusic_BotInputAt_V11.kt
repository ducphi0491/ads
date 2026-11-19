@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
package duc_phi.music

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContentValues
import android.widget.Toast
import android.content.pm.PackageManager
import android.Manifest
import android.provider.MediaStore
import java.io.FileInputStream
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.C
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import org.json.JSONArray
import org.json.JSONObject
import android.telephony.TelephonyManager
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.ui.text.input.KeyboardType
import com.google.i18n.phonenumbers.PhoneNumberUtil
import androidx.compose.material.icons.rounded.Lock

// ===== TelegramMusic (merged into TelegramMusic.kt) =====
internal enum class TelegramConnectionStatus { DISCONNECTED, ACTIVE }
internal data class TelegramConnectionState(
    val status: TelegramConnectionStatus = TelegramConnectionStatus.DISCONNECTED,
    val botChatId: Long = 0L,
    val botUsername: String = "",
    val botDisplayName: String = ""
) {
    val linked get() = status == TelegramConnectionStatus.ACTIVE
}

internal data class TelegramRecentBot(
    val username: String,
    val displayName: String = "",
    val lastConnectedAt: Long = 0L
)

// Short fallback labels for the 15 languages supported by MeloWave. If the
// language asset already provides these keys, mwString() takes precedence.
private fun telegramBotClipboardLabels(language: String): Pair<String, String> = when (language) {
    "en" -> "Copy bot username" to "Bot username copied"
    "zh" -> "复制机器人用户名" to "已复制机器人用户名"
    "ja" -> "ボット名をコピー" to "ボット名をコピーしました"
    "ko" -> "봇 사용자 이름 복사" to "봇 사용자 이름을 복사했습니다"
    "es" -> "Copiar usuario del bot" to "Usuario del bot copiado"
    "fr" -> "Copier le nom du bot" to "Nom du bot copié"
    "de" -> "Bot-Benutzernamen kopieren" to "Bot-Benutzername kopiert"
    "ru" -> "Скопировать имя бота" to "Имя бота скопировано"
    "pt" -> "Copiar usuário do bot" to "Usuário do bot copiado"
    "id" -> "Salin nama pengguna bot" to "Nama pengguna bot disalin"
    "hi" -> "बॉट उपयोगकर्ता नाम कॉपी करें" to "बॉट उपयोगकर्ता नाम कॉपी हो गया"
    "ar" -> "نسخ اسم مستخدم البوت" to "تم نسخ اسم مستخدم البوت"
    "th" -> "คัดลอกชื่อผู้ใช้บอต" to "คัดลอกชื่อผู้ใช้บอตแล้ว"
    "it" -> "Copia nome utente bot" to "Nome utente bot copiato"
    else -> "Sao chép tên bot" to "Đã sao chép tên bot"
}

internal enum class TelegramAuthStep {
    NOT_CONFIGURED, STARTING, PHONE, CODE, PASSWORD, EMAIL, EMAIL_CODE,
    QR_CONFIRM, REGISTRATION, READY, LOGGING_OUT, ERROR
}

internal enum class TelegramActionKind { TEXT, CALLBACK, URL, UNSUPPORTED }
internal data class TelegramBotAction(
    val label: String,
    val kind: TelegramActionKind,
    val callbackData: ByteArray = byteArrayOf(),
    val url: String = ""
)
internal data class TelegramAudioResult(
    val chatId: Long,
    val messageId: Long,
    val tdFileId: Int,
    val title: String,
    val artist: String,
    val fileName: String,
    val mimeType: String,
    val durationMs: Long,
    val size: Long,
    val uniqueFileId: String = "",
    val remoteFileId: String = "",
    // Telegram's in-memory JPEG preview; no MP3 download needed for album art.
    val albumCoverBytes: ByteArray? = null,
    // Small standalone JPEG from TDLib. Does not require downloading the music file.
    val albumCoverThumbnailFileId: Int = 0
)
internal data class TelegramBotResult(
    val messageId: Long,
    val text: String,
    val outgoing: Boolean,
    val buttons: List<TelegramBotAction>,
    val audio: TelegramAudioResult? = null
)

/** Detail read AFTER tapping one bot result. Never classify an entire search page as an album. */
internal data class TelegramSelectedDetail(
    val count: Int? = null,
    val tracks: List<TelegramUnifiedTrack> = emptyList(),
    val audio: TelegramAudioResult? = null,
    val technicalText: String = ""
)

/** Results are only CONFIRMED after a bot reply to the selected result.
 * A timed-out callback is UNKNOWN, not a single-track result.
 */
internal enum class TelegramMetadataCheckStatus { CHECKING, VERIFIED, UNKNOWN }
internal enum class TelegramSearchSaveDestination { DEVICE, DRIVE }
private fun telegramHasConfirmedIdentity(detail: TelegramSelectedDetail?): Boolean =
    detail?.audio != null || (detail?.count ?: 0) >= 1 || (detail?.tracks?.size ?: 0) >= 2

/** Album track buttons must represent distinct playable file selections, not pagination. */
private val TELEGRAM_NUMBERED_TRACK = Regex("""^\s*\d{1,3}\s*[.)\-]\s*\S.{2,}""")
private val TELEGRAM_NUMBERED_TRACK_PREFIX = Regex("""^\s*\d{1,3}\s*[.)\-]\s*""")
private val TELEGRAM_GENERIC_MEDIA_ACTION = Regex(
    """(?iu)^\s*(?:▶|🎵|🎶|♫)?\s*(?:nghe|phát|play|listen|preview|xem|view|download|tải)(?:\s+(?:nhạc|bài|file|audio))?\s*$""")
/** Menu album thật: nhiều nút chọn BÀI độc lập sau khi chọn một kết quả.
 * Bot có thể chỉ hiển thị tên bài, hoàn toàn không có số thứ tự/thời lượng.
 * KHÔNG bao giờ gọi hàm này để phân loại nguyên trang kết quả tìm kiếm.
 */
private val TELEGRAM_NON_SONG_MENU = Regex(
    """(?iu)^\s*(?:↩|↪|⬅|➡|⏪|⏩|◀|▶)?\s*(?:trở\s*lại|quay\s*lại|back|home|menu|search|tìm\s*kiếm|chọn\s*chất\s*lượng|quality|bitrate|mp3|flac|wav|m4a|320\s*kbps|128\s*kbps|192\s*kbps|subscribe|subscription|subscribed|join|channel|album|подпис\w*|lyrics|lời\s*bài\s*hát|share|chia\s*sẻ|lưu|save|yêu\s*thích|favorite|phát\s*tất\s*cả|nghe\s*tất\s*cả|play\s*all|tải\s*tất\s*cả|download\s*all|chọn\s*tất\s*cả|trang|page|chi\s*tiết|xem\s*thông\s*tin|mở\s*telegram|tùy\s*chọn|settings)(?:\b|$)""")
private val TELEGRAM_SONG_WORD = Regex("""[\p{L}]{2,}""")
private fun telegramAlbumFileButtons(result: TelegramBotResult): List<TelegramBotAction> {
    val candidates = result.buttons.filter { button ->
        val label = button.label.trim()
        val labelWithoutIcon = label.replace(Regex("""^[^\p{L}\p{N}]+"""), "").trim()
        (button.kind == TelegramActionKind.CALLBACK || button.kind == TelegramActionKind.TEXT) &&
                label.length in 3..110 && TELEGRAM_SONG_WORD.containsMatchIn(label) &&
                !isTelegramDownloadButton(label) &&
                !TELEGRAM_GENERIC_MEDIA_ACTION.matches(label) &&
                !TELEGRAM_NON_SONG_MENU.containsMatchIn(labelWithoutIcon) &&
                !isTelegramAlbumPageButton(button, +1) &&
                !isTelegramAlbumPageButton(button, -1) &&
                telegramPagerKind(button) == TelegramPagerKind.OTHER
    }.distinctBy { button ->
        if (button.kind == TelegramActionKind.CALLBACK)
            "callback:${button.callbackData.contentHashCode()}"
        else "text:${button.label.trim().lowercase(Locale.ROOT)}"
    }
    // Không đếm hai nút trùng tên bài thành hai file chắc chắn khác nhau.
    val distinctTitles = candidates.map {
        telegramSongMatchKey(it.label.replace(TELEGRAM_NUMBERED_TRACK_PREFIX, ""))
    }.filter { it.isNotBlank() }.toSet()
    return if (candidates.size >= 2 && distinctTitles.size >= 2) candidates else emptyList()
}

/** Only explicit technical fields returned by the bot. No guessed file size or quality. */
private fun telegramBotTechnicalFields(text: String): String {
    // Some bots use a caption ("MP3 | 320 kbps | 8.2 MB") instead of
    // labelled lines. Extract only actual technical tokens from a RESPONSE;
    // never infer size, format, or quality from the search-result title.
    val format = Regex("""(?iu)\b(MP3|FLAC|WAV|M4A|AAC|OGG|OPUS|ALAC|AIFF)\b""")
        .find(text)?.value?.uppercase(Locale.ROOT)
    val bitrate = Regex("""(?iu)\b(\d{2,4}\s*(?:kbps|kbit/s)|lossless)\b""")
        .find(text)?.value?.replace(Regex("""\s+"""), " ")
    val size = Regex("""(?iu)\b(\d+(?:[.,]\d+)?\s*(?:GB|MB|KB|GiB|MiB|KiB))\b""")
        .find(text)?.value?.replace(Regex("""\s+"""), " ")
    return listOfNotNull(format, bitrate, size).joinToString(" \u00b7 ")
}

/** A metadata probe may follow one clearly identified single-file button.
 * Do NOT click buttons that choose a different quality, paginate, send an
 * entire album, delete data, or open an unrelated bot action.
 */
private fun telegramSingleFileMetadataAction(result: TelegramBotResult): TelegramBotAction? {
    val allowed = result.buttons.filter { button ->
        if (button.kind != TelegramActionKind.CALLBACK &&
            button.kind != TelegramActionKind.TEXT) return@filter false
        val key = telegramSongMatchKey(button.label)
        val blockedCommands = setOf("tatca", "toanbo", "all", "playall",
            "downloadall", "nghetatca", "phattatca", "taitatca", "taialbum")
        val blockedPrefixes = listOf("trang", "page", "next", "prev", "back",
            "trove", "quaylai", "menu", "cancel", "huy", "delete", "xoa",
            "premium", "subscribe", "pay", "share", "chiase", "report",
            "search", "timkiem", "chonchatluong", "settings")
        if (key.isBlank() || key.length > 100 || key in blockedCommands ||
            blockedPrefixes.any { key.startsWith(it) } ||
            listOf("downloadalbum", "playalbum", "folderzip", "bulkdownload",
                "playlist", "selectall").any { key.contains(it) })
            return@filter false
        !isTelegramAlbumPageButton(button, +1) &&
                !isTelegramAlbumPageButton(button, -1) &&
                telegramPagerKind(button) == TelegramPagerKind.OTHER
    }
    val download = allowed.filter { isTelegramDownloadButton(it.label) }
    if (download.size == 1) return download.single()
    if (download.size > 1) return null
    val fileActions = allowed.filter { button ->
        val key = telegramSongMatchKey(button.label)
        key in setOf("play", "listen", "nghe", "phat", "playaudio", "nghenhac",
            "phatnhac", "nghebai", "playtrack", "sendfile", "sendaudio", "getaudio",
            "getfile", "guifile", "guinhac", "layfile", "laynhac", "taifile") ||
                key.startsWith("guifile") || key.startsWith("getaudio")
    }
    if (fileActions.size == 1) return fileActions.single()
    if (fileActions.size > 1) return null
    // A single selectable song is common in a one-track bot detail menu.
    // When there are multiple buttons, the menu is ambiguous: never guess.
    return allowed.singleOrNull()?.takeIf { isTelegramTrackButton(it) }
}

// A result button is not an audio file. We first select it, then wait for
// the bot's real audio/document response before invoking Media3 playback.
private val TELEGRAM_TRACK_DURATION = Regex("""\[\s*\d{1,2}:\d{2}(?::\d{2})?\s*]""")
private val TELEGRAM_ALBUM_TRACK_COUNT = Regex("""(?i)\btracks\s*:\s*([2-9]|[1-9]\d+)\b""")
// An explicit file/track count is provided by some bots AFTER the selected callback.
// Never read a count from a general multi-result search page as if it belonged
// to every button. A pending click is needed to associate this count with a row.
private val TELEGRAM_DETAIL_FILE_COUNT = Regex(
    """(?i)\b(?:tracks?|files?|songs?|bài(?:\s+hát)?|tệp)\s*[:=]\s*(\d{1,4})\b""")
private val TELEGRAM_DETAIL_FILE_COUNT_SUFFIX = Regex(
    """(?i)\b(\d{1,4})\s*(?:tracks?|audio\s+files?|files?|songs?|bài(?:\s+hát)?|tệp)\b""")
private fun telegramBotSelectionKey(result: TelegramBotResult, button: TelegramBotAction): String =
    "${result.messageId}|${button.label.trim()}|${button.callbackData.contentHashCode()}"
private fun isTelegramDownloadButton(label: String): Boolean {
    val value = label.trim().lowercase(Locale.ROOT)
    // A bot's "Download page / Download all" is a BULK command, not the
    // requested song. Never automatically press it while waiting for one audio.
    if (value.contains("download page") || value.contains("download all") ||
        value.contains("tải tất cả") || value.contains("tải cả trang") ||
        value.contains("скачать все") || value.contains("скачать страницу")) return false
    return value.contains("download") || value.contains("tải về") ||
            value.contains("tải nhạc") || value.startsWith("⬇") || value.startsWith("📥")
}
private fun isTelegramTrackButton(button: TelegramBotAction): Boolean {
    if (button.kind != TelegramActionKind.CALLBACK && button.kind != TelegramActionKind.TEXT) return false
    val label = button.label.trim()
    if (label.length < 4 || telegramPagerKind(button) != TelegramPagerKind.OTHER) return false
    val lowered = label.lowercase(Locale.ROOT)
    if (lowered.startsWith("search") || lowered.startsWith("view ") ||
        lowered.startsWith("open") || lowered.startsWith("next") ||
        lowered.startsWith("back") || lowered.startsWith("trang ") ||
        lowered.startsWith("subscribe") || lowered.startsWith("/")) return false
    // Telegram bot keyboards may use an en dash (–) or em dash (—), not ASCII '-'.
    // A standalone play/page icon is NEVER a song.
    return TELEGRAM_TRACK_DURATION.containsMatchIn(label) ||
            Regex("""\s+[-–—]\s+""").containsMatchIn(label) ||
            (label.startsWith("🎵") || label.startsWith("🎶")) && label.length > 5
}

/** Only explicit pagination controls are used to cross an album page.
 * Never auto-press Download album or Back (they are not page navigation).
 */
private fun isTelegramAlbumPageButton(button: TelegramBotAction, direction: Int): Boolean {
    if (button.kind !in setOf(TelegramActionKind.CALLBACK, TelegramActionKind.TEXT)) return false
    val label = button.label.trim().lowercase(Locale.ROOT)
        .replace("\uFE0F", "").replace(Regex("\\s+"), "")
    return if (direction > 0) {
        label.contains("next") || label.contains("trangtiếp") ||
                label.contains("trangsau") || label.contains("tiếptheo") ||
                label in setOf("→", "➡", "⏭", "»", ">>", "›", "trangtới", "sau")
    } else {
        label.contains("previous") || label.contains("prevpage") ||
                label.contains("trangtrước") || label.contains("trướcđó") ||
                label in setOf("←", "⬅", "⏮", "«", "<<", "‹")
    }
}

/** Telegram album page navigation. Every action still uses the real callback from the bot. */
private enum class TelegramPagerKind { FIRST, PREVIOUS, CURRENT, NUMBER, NEXT, LAST, OTHER }
private fun telegramPagerKind(button: TelegramBotAction): TelegramPagerKind {
    if (button.kind != TelegramActionKind.TEXT && button.kind != TelegramActionKind.CALLBACK)
        return TelegramPagerKind.OTHER
    val value = button.label.trim().lowercase(Locale.ROOT)
        .replace("\uFE0F", "").replace(Regex("\\s+"), "")
    val compact = value.trim('-')
    return when {
        Regex("^-\\d+-$").matches(value) -> TelegramPagerKind.CURRENT
        Regex("^\\d+$").matches(value) -> TelegramPagerKind.NUMBER
        Regex("^\\d+(»|≫|⏭)$").matches(value) || value in setOf("»", "≫", "⏭", ">>") -> TelegramPagerKind.LAST
        Regex("^(«|≪|⏮)\\d+$").matches(value) || value in setOf("«", "≪", "⏮", "<<") -> TelegramPagerKind.FIRST
        value in setOf("<", "‹", "←", "⬅", "◀") || value.contains("prev") || value.contains("trangtrước") -> TelegramPagerKind.PREVIOUS
        value in setOf(">", "›", "→", "➡", "▶") || value.contains("next") || value.contains("trangtiếp") -> TelegramPagerKind.NEXT
        compact == "-" -> TelegramPagerKind.CURRENT
        else -> TelegramPagerKind.OTHER
    }
}
private fun telegramPagerNumber(label: String): Int? =
    Regex("\\d+").find(label)?.value?.toIntOrNull()


private fun mooMusicSubscriptionRequired(text: String): Boolean {
    val value = text.lowercase(Locale.ROOT)
    return (value.contains("subscribe") &&
                (value.contains("partners") || value.contains("subscribed"))) ||
            (value.contains("подпис") && (value.contains("канал") || value.contains("партн")))
}

/** Explicit refusal from the bot itself, not a Media3 or network error.
 * Never try to bypass paid Proxy/Stars/channel requirements.
 */
private fun telegramBotPlaybackRefusal(raw: String): String? {
    val text = java.text.Normalizer.normalize(raw.lowercase(Locale.ROOT).replace('đ', 'd'),
        java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
    return when {
        mooMusicSubscriptionRequired(raw) ||
            (text.contains("subscribe") && text.contains("channel")) ||
            (text.contains("join") && text.contains("channel") && text.contains("required")) ->
            "Bot yêu cầu tham gia kênh trước khi cung cấp âm thanh. Hãy kiểm tra trong Telegram."
        text.contains("/proxy") && (text.contains("chua ai tai") ||
            text.contains("ban moi") || text.contains("not downloaded") ||
            text.contains("need") || text.contains("require")) ->
            "Bot yêu cầu Proxy hoặc điều kiện riêng cho bài này; hiện chưa có file nhạc để phát."
        text.contains("payment required") || text.contains("buy stars") ||
            text.contains("pay telegram stars") ->
            "Bot yêu cầu thanh toán để cung cấp file nhạc. Hãy chọn bài hoặc bot khác."
        text.contains("file not found") || text.contains("audio unavailable") ||
            text.contains("failed to download") || text.contains("khong tim thay file") ->
            "Bot thông báo chưa có file âm thanh cho bài này. Hãy thử bài hoặc bot khác."
        else -> null
    }
}

private fun telegramBotPageFingerprint(result: TelegramBotResult): String = buildString {
    append(result.text)
    result.buttons.forEach { button ->
        append('|').append(button.label).append(':').append(button.kind)
            .append(':').append(button.callbackData.contentHashCode())
    }
    append(':').append(result.audio?.messageId ?: 0L)
}

/** Read-only lookup for a bot result, never a Media3 playback or TDLib audio download.
 * The bot may return an edited callback message, new audio, or an album submenu.
 * Only one probe runs at a time because bot replies do not contain a universal
 * request identifier. The manager cancels it as soon as the user chooses a track.
 */
private fun telegramProbeActionSignature(action: TelegramBotAction): String =
    "${action.kind}|${action.label.trim()}|${action.callbackData.contentHashCode()}"

private class TelegramMetadataProbe(
    val key: String,
    val chatId: Long,
    val sourceMessageId: Long,
    val sourceFingerprint: String,
    val sourceActionSignatures: List<String>,
    val sourceActionLabels: Set<String>,
    val finished: CompletableDeferred<TelegramSelectedDetail>,
    var followUpCount: Int = 0,
    val clickedActions: MutableSet<String> = mutableSetOf()
)

private class TelegramAlbumPageChange(
    val token: Long,
    val botChatId: Long,
    val sourceMessageId: Long,
    val oldFingerprint: String,
    val direction: Int,
    val controller: MusicController
)

private class TelegramPendingPlayback(
    val token: Long,
    val chatId: Long,
    val title: String,
    val controller: MusicController,
    val playAfterSave: Boolean,
    val saveToDrive: Boolean = false,
    var downloadRequested: Boolean = false,
    val choiceKey: String? = null,
    val sourceMessageId: Long = 0L,
    val sourceFingerprint: String = ""
)
private data class TelegramSavedSong(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val mime: String,
    val path: String,
    val duration: Long,
    val date: Long,
    val uniqueFileId: String = "",
    val botUsername: String = "",
    val chatId: Long = 0L,
    val messageId: Long = 0L,
    val actionMessageId: Long = 0L,
    val actionKind: String = "",
    val actionPayload: String = "",
    val actionLabel: String = "",
    val fileName: String = "",
    val remoteFileId: String = "",
    val driveSaved: Boolean = false,
    val sizeBytes: Long = 0L
) {
    fun json(): JSONObject = JSONObject().apply {
        put("id", id); put("title", title); put("artist", artist)
        put("album", album); put("mime", mime); put("path", path)
        put("duration", duration); put("date", date)
        put("uniqueFileId", uniqueFileId)
        put("botUsername", botUsername); put("chatId", chatId)
        put("messageId", messageId); put("actionMessageId", actionMessageId)
        put("actionKind", actionKind); put("actionPayload", actionPayload)
        put("actionLabel", actionLabel); put("fileName", fileName)
        put("remoteFileId", remoteFileId); put("driveSaved", driveSaved)
        put("sizeBytes", sizeBytes)
    }
    companion object {
        fun from(json: JSONObject) = TelegramSavedSong(
            id = json.optString("id"), title = json.optString("title"),
            artist = json.optString("artist"), album = json.optString("album"),
            mime = json.optString("mime"), path = json.optString("path"),
            duration = json.optLong("duration"), date = json.optLong("date"),
            uniqueFileId = json.optString("uniqueFileId"),
            botUsername = json.optString("botUsername"),
            chatId = json.optLong("chatId"), messageId = json.optLong("messageId"),
            actionMessageId = json.optLong("actionMessageId"),
            actionKind = json.optString("actionKind"),
            actionPayload = json.optString("actionPayload"),
            actionLabel = json.optString("actionLabel"),
            fileName = json.optString("fileName"),
            remoteFileId = json.optString("remoteFileId"),
            driveSaved = json.optBoolean("driveSaved", false),
            sizeBytes = json.optLong("sizeBytes", 0L)
        )
    }
}

/** Metadata from Telegram survives streaming and does not depend on a local MP3 path. */
private fun telegramExt(fileName: String, mime: String): String {
    val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
    if (ext in setOf("flac", "wav", "mp3", "m4a", "aac", "ogg", "oga", "opus", "wma", "alac", "aiff")) {
        return ext.uppercase(Locale.ROOT)
    }
    return when (mime.substringBefore(';').trim().lowercase(Locale.ROOT)) {
        "audio/flac", "audio/x-flac" -> "FLAC"
        "audio/wav", "audio/wave", "audio/x-wav" -> "WAV"
        "audio/mpeg", "audio/mp3" -> "MP3"
        "audio/mp4", "audio/x-m4a" -> "M4A"
        "audio/aac" -> "AAC"
        "audio/ogg" -> "OGG"
        "audio/opus" -> "OPUS"
        else -> "AUDIO"
    }
}

private fun telegramBytesLabel(bytes: Long): String = when {
    bytes <= 0L -> "Không rõ dung lượng"
    bytes >= 1024L * 1024L * 1024L -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    bytes >= 1024L * 1024L -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
    else -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
}

/** Overall file size / duration is an *average estimate*, not encoded bitrate metadata. */
private fun telegramTechnicalInfo(fileName: String, mime: String, bytes: Long, durationMs: Long): AudioTechnicalInfo {
    val ext = telegramExt(fileName, mime)
    val approxKbps = if (bytes > 0L && durationMs > 0L)
        ((bytes.toDouble() * 8.0) / durationMs).toInt().takeIf { it > 0 }
    else null
    val lossless = ext in setOf("FLAC", "WAV", "ALAC", "AIFF")
    val quality = when {
        lossless -> "Lossless"
        approxKbps != null -> "≈$approxKbps kbps"
        ext in setOf("AAC", "M4A", "MP3", "OPUS", "OGG") -> ext
        else -> "Chất lượng chưa rõ"
    }
    return AudioTechnicalInfo(qualityLabel = quality, extensionLabel = ext,
        sizeLabel = telegramBytesLabel(bytes), bitrateKbps = approxKbps, sizeBytes = bytes.takeIf { it > 0L })
}

private class TelegramDeferredAudio(
    val chatId: Long,
    val deferred: CompletableDeferred<TelegramAudioResult>,
    var requestedDownload: Boolean = false
)

private fun songLabelMetadata(label: String): Triple<String, String, Long> {
    val durationText = TELEGRAM_TRACK_DURATION.find(label)?.value?.trim('[', ']', ' ').orEmpty()
    val pieces = durationText.split(':').mapNotNull(String::toLongOrNull)
    val durationMs = when (pieces.size) {
        2 -> (pieces[0] * 60L + pieces[1]) * 1000L
        3 -> (pieces[0] * 3600L + pieces[1] * 60L + pieces[2]) * 1000L
        else -> 0L
    }
    val text = label.replace(TELEGRAM_TRACK_DURATION, "").trim()
        .removePrefix("▶").removePrefix("🎵").removePrefix("🎶").trim()
    val split = text.split(" - ", limit = 2)
    return if (split.size == 2) Triple(split[0].trim(), split[1].trim(), durationMs)
    else Triple("Nghệ sĩ chưa biết", text, durationMs)
}

/** Cache eviction is scoped to files that MeloWave streamed or uploaded. Never delete
 * a file held by a Media3 reader, a Google Drive upload, or a permanent offline path.
 * TDLib's general OptimizeStorage is deliberately NOT used: it can also remove unrelated
 * documents/audio from the Telegram account instead of just MeloWave's temporary streams.
 */
internal object TelegramStreamCachePolicy {
    fun canDelete(
        fileId: Int,
        activeReaders: Int,
        driveUploading: Boolean,
        driveSourceFileId: Int,
        downloadingInTdlib: Boolean,
        ownedOfflineCopy: Boolean,
        canBeDeleted: Boolean
    ): Boolean = fileId > 0 && activeReaders <= 0 &&
            !(driveUploading && driveSourceFileId == fileId) &&
            !downloadingInTdlib && !ownedOfflineCopy && canBeDeleted
}

/**
 * Telegram USER client: login locally with TDLib, choose any public bot username,
 * send searches, present real bot replies (including inline/reply keyboards),
 * download actual audio and play it through the existing MusicController.
 * No BotFather, Bot Token, proxy, PHP, webhook, or MeloWave server.
 */
internal class TelegramMusicManager(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs = appContext.getSharedPreferences("melowave_telegram_client_v1", Context.MODE_PRIVATE)
    private val savedSongs = mutableListOf<TelegramSavedSong>()
    // Only bots explicitly chosen by the user. Never ship or preselect a hard-coded bot.
    val recentBots = mutableStateListOf<TelegramRecentBot>()

    private fun readRecentBots(): List<TelegramRecentBot> = runCatching {
        val array = JSONArray(prefs.getString("recent_bots", "[]"))
        (0 until array.length()).mapNotNull { index ->
            val record = array.optJSONObject(index) ?: return@mapNotNull null
            val name = record.optString("username").removePrefix("@")
            if (!Regex("^[A-Za-z0-9_]{5,32}$").matches(name)) return@mapNotNull null
            TelegramRecentBot(name, record.optString("displayName"), record.optLong("lastConnectedAt"))
        }.distinctBy { it.username.lowercase(Locale.ROOT) }.take(20)
    }.getOrDefault(emptyList())

    private fun persistRecentBots() {
        val array = JSONArray()
        recentBots.forEach { entry ->
            array.put(JSONObject().apply {
                put("username", entry.username)
                put("displayName", entry.displayName)
                put("lastConnectedAt", entry.lastConnectedAt)
            })
        }
        prefs.edit().putString("recent_bots", array.toString()).apply()
    }

    private fun savedBotUsername(): String = prefs.getString("last_bot", "").orEmpty()
        .trim().removePrefix("@")
        .takeIf { Regex("^[A-Za-z0-9_]{5,32}$").matches(it) }.orEmpty()

    private fun noteConnectedBot(username: String, displayName: String) {
        recentBots.removeAll { it.username.equals(username, ignoreCase = true) }
        recentBots.add(0, TelegramRecentBot(username, displayName, System.currentTimeMillis()))
        while (recentBots.size > 20) recentBots.removeAt(recentBots.lastIndex)
        persistRecentBots()
    }

    fun removeRecentBot(username: String) {
        recentBots.removeAll { it.username.equals(username, ignoreCase = true) }
        persistRecentBots()
    }

    fun clearRecentBots() { recentBots.clear(); persistRecentBots() }
    // Temporary streams are not added to the playlist, but still need file format/size in Now Playing.
    private val activeStreamInfo = ConcurrentHashMap<String, TelegramAudioResult>()
    // TDLib stream reads run on Media3 loader threads, never the UI thread.
    private val openStreamCounts = ConcurrentHashMap<Int, AtomicInteger>()
    private val streamDownloadLocks = ConcurrentHashMap<Int, Mutex>()
    // Protect explicit user downloads until MediaStore finishes writing them.
    // Unlike TDLib partial streams, these copies must NEVER be removed by cache cleanup.
    private val offlineSavingFileIds = ConcurrentHashMap.newKeySet<Int>()
    // All tracked IDs are for temporary Telegram audio only (not saved playlists or cover art).
    // Persist IDs so files left by an Android process kill are cleaned on the next login.
    private val downloadedStreamIds = LinkedHashSet<Int>()
    private var cleanupJob: Job? = null
    // Requests made during an existing cleanup must not cancel the cleanup that is running.
    // The worker will do another pass for newly closed files instead.
    private var cacheCleanupRequested = false
    private val telegramAudioCacheLimitBytes = 100L * 1024L * 1024L
    private val telegramCacheTrimDelayMs = 2_500L // permits an immediate Previous/seek
    private val telegramCacheIdsPref = "temporary_stream_file_ids_v2"

    private fun restoreTrackedTelegramAudio() {
        val array = runCatching { JSONArray(prefs.getString(telegramCacheIdsPref, "[]")) }
            .getOrNull() ?: return
        for (index in 0 until array.length()) {
            val id = array.optInt(index, 0)
            if (id > 0) downloadedStreamIds.add(id)
        }
    }

    /** Called only on the manager/Main coroutine; does not store audio or modify the playlist. */
    private fun trackTemporaryTelegramAudio(fileId: Int) {
        if (fileId <= 0) return
        downloadedStreamIds.remove(fileId)
        downloadedStreamIds.add(fileId) // last-use order
        persistTrackedTelegramAudio()
    }

    private fun persistTrackedTelegramAudio() {
        val array = JSONArray()
        downloadedStreamIds.forEach { array.put(it) }
        prefs.edit().putString(telegramCacheIdsPref, array.toString()).apply()
    }
    private val client = TelegramTdClient(
        context = appContext,
        onAuthorization = { state -> scope.launch { onAuthorizationChanged(state) } },
        onMessage = { message -> scope.launch { receive(message) } },
        onMessageChanged = { chatId, messageId -> scope.launch {
            if ((chatId == connection.botChatId && connection.linked) ||
                chatId == pendingDeferredAudio?.chatId) {
                runCatching { clientRequestMessage(chatId, messageId) }.getOrNull()?.let(::receive)
            }
        } },
        onFileUpdated = { file -> scope.launch {
            if (searchSaveRunning && searchSaveDownloadingFromTelegram &&
                file.id == searchSaveActiveFileId) {
                val total = listOf(file.size.toLong(), file.expectedSize.toLong())
                    .filter { it > 0L }.maxOrNull() ?: 0L
                val received = file.local?.downloadedSize?.toLong()?.coerceAtLeast(0L) ?: 0L
                if (total > 0L) {
                    val downloadWeight = if (searchSaveDestination == TelegramSearchSaveDestination.DRIVE)
                        0.45f else 0.92f
                    updateSearchSaveProgress((received.toFloat() / total).coerceIn(0f, 1f) * downloadWeight)
                }
            }
            if (driveUploading && driveUploadStage == "telegram" && file.id == driveSourceFileId) {
                driveTelegramBytesReceived = file.local?.downloadedSize?.toLong()?.coerceAtLeast(0L) ?: 0L
                driveTelegramTotalBytes = file.size.toLong().takeIf { it > 0L }
                    ?: file.expectedSize.toLong().coerceAtLeast(0L)
            }
        } },
        onError = { text -> scope.launch { error = text; if (authStep == TelegramAuthStep.STARTING) authStep = TelegramAuthStep.ERROR } }
    )
    private var operation: Job? = null
    private var download: Job? = null
    private var pendingPlayback: TelegramPendingPlayback? = null
    // Search-result playback is asynchronous: Next first asks the bot for audio,
    // then Media3 starts its own range streaming. Expose one loading state for
    // BOTH phases so the circular Play control reacts on the user's tap.
    var playbackPreparing by mutableStateOf(false)
        private set
    var playbackPreparingTitle by mutableStateOf("")
        private set
    private var playbackPreparingMediaId: String? = null
    private var playbackPreparingController: MusicController? = null
    private var playbackPreparingToken = 0L
    private var playbackPreparingTimer: Job? = null

    private fun beginPlaybackPreparation(title: String, controller: MusicController) {
        // Start immediately, before any refresh/callback/coroutine delay.
        playbackPreparingToken++
        val token = playbackPreparingToken
        playbackPreparing = true
        playbackPreparingTitle = title
        playbackPreparingController = controller
        playbackPreparingMediaId = null
        error = null
        playbackPreparingTimer?.cancel()
        playbackPreparingTimer = scope.launch {
            delay(45_000L)
            if (token == playbackPreparingToken && playbackPreparing) {
                failPlaybackPreparation("Bot chưa gửi đủ dữ liệu để phát '$title' sau 45 giây.")
            }
        }
    }

    private fun stopPlaybackPreparation() {
        playbackPreparingToken++
        playbackPreparingTimer?.cancel()
        playbackPreparingTimer = null
        playbackPreparing = false
        playbackPreparingTitle = ""
        playbackPreparingController = null
        playbackPreparingMediaId = null
    }

    private fun failPlaybackPreparation(reason: String) {
        val controller = playbackPreparingController
        if (!playbackPreparing && pendingPlayback == null) return
        stopPlaybackPreparation()
        cancelPendingPlayback()
        queuedAlbumResolveGeneration++
        queuedAlbumResolveJob?.cancel()
        queuedAlbumResolveJob = null
        autoPlaySearchResults = false
        message = reason
        error = reason
        // The app-level Toast is displayed even if the Telegram panel is closed.
        controller?.errorMessage = "Telegram Music: $reason"
    }

    private fun expectTelegramMediaReady(songId: String) {
        if (playbackPreparing) playbackPreparingMediaId = songId
    }

    /** Invoked from the active Media3 listener, never from an old bot callback. */
    fun onMedia3Ready(mediaId: String?) {
        if (playbackPreparing && playbackPreparingMediaId != null &&
            playbackPreparingMediaId == mediaId) stopPlaybackPreparation()
    }

    fun onMedia3Failure(mediaId: String?) {
        if (playbackPreparing && playbackPreparingMediaId != null &&
            playbackPreparingMediaId == mediaId) {
            // MusicController already displays the precise Media3 error.
            stopPlaybackPreparation()
            autoPlaySearchResults = false
        }
    }

    // Confirmed counts are scoped to the exact bot result button (message + action).
    // This state also refreshes the displayed album label after opening a result.
    val verifiedEntryFileCounts = mutableStateMapOf<String, Int>()
    /** Only the original search page, never bot detail menus. Kept in memory
     * when the Telegram window closes and reopens; no callback is persisted. */
    var dialogSearchPageSnapshot by mutableStateOf<List<TelegramUnifiedTrack>>(emptyList())
    var dialogSearchKeyword by mutableStateOf("")
    /** Live results from the bot, keyed to the selected search-result button. */
    val selectedDetails = mutableStateMapOf<String, TelegramSelectedDetail>()
    // Telegram bots often REPLACE the search menu after any callback. Original
    // callbacks must never be reused blindly for the next search result.
    private var botReplyRevision = 0L
    private val recentBotReplies = mutableListOf<Pair<Long, TelegramBotResult>>()
    private var lastUnifiedSearchKeyword = ""
    private var metadataPreviewJob: Job? = null
    private var metadataPreviewGeneration = 0L
    private var activeMetadataProbe: TelegramMetadataProbe? = null
    var metadataPreviewRunning by mutableStateOf(false)
        private set
    var metadataPreviewCompleted by mutableStateOf(0)
        private set
    var metadataPreviewVerified by mutableStateOf(0)
        private set
    var metadataPreviewUnknown by mutableStateOf(0)
        private set
    val metadataPreviewStatuses = mutableStateMapOf<String, TelegramMetadataCheckStatus>()
    // Exactly one result (or one child within an album) is inspected at a time.
    // Used by Home and Library to put a small spinner on that result card.
    var metadataPreviewActiveKey by mutableStateOf<String?>(null)
        private set
    var metadataPreviewParentKey by mutableStateOf<String?>(null)
        private set
    var metadataPreviewTotal by mutableStateOf(0)
        private set

    // An explicit, sequential download queue. Metadata prefetch never saves audio.
    var searchSaveRunning by mutableStateOf(false)
        private set
    var searchSaveCurrent by mutableStateOf(0)
        private set
    var searchSaveTotal by mutableStateOf(0)
        private set
    var searchSaveMessage by mutableStateOf("")
        private set
    // The active destination/track are observable by search result icons.
    // Only one Telegram download/upload queue may run at a time.
    var searchSaveDestination by mutableStateOf<TelegramSearchSaveDestination?>(null)
        private set
    var searchSaveActiveTrackKey by mutableStateOf<String?>(null)
        private set
    var searchSaveActiveAlbumKey by mutableStateOf<String?>(null)
        private set
    var searchSaveItemProgress by mutableStateOf(0f)
        private set
    var searchSaveAlbumProgress by mutableStateOf(0f)
        private set
    val searchSaveCompletedTracks = mutableStateMapOf<String, TelegramSearchSaveDestination>()
    val searchSaveCompletedAlbums = mutableStateMapOf<String, TelegramSearchSaveDestination>()
    private var searchSaveActiveFileId = 0
    private var searchSaveDownloadingFromTelegram = false
    private var searchSaveAlbumDone = 0
    private var searchSaveAlbumTotal = 0
    private fun updateSearchSaveProgress(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        searchSaveItemProgress = clamped
        searchSaveAlbumProgress = if (searchSaveAlbumTotal > 0)
            ((searchSaveAlbumDone + clamped) / searchSaveAlbumTotal).coerceIn(0f, 1f)
        else 0f
    }
    private var searchSaveJob: Job? = null

    /** Stop any automatic bot clicks before navigation, new search or user playback. */
    fun cancelSearchMetadataPrefetch() {
        metadataPreviewGeneration++
        metadataPreviewJob?.cancel()
        metadataPreviewJob = null
        // Dismissing Search cancels previews, but must NOT cancel the active
        // album probe used to start the user's selected listening queue.
        if (queuedAlbumResolveJob?.isActive != true && !searchSaveRunning) {
            activeMetadataProbe?.finished?.cancel()
            activeMetadataProbe = null
        }
        metadataPreviewRunning = false
        metadataPreviewActiveKey = null
        metadataPreviewParentKey = null
        metadataPreviewTotal = 0
        metadataPreviewCompleted = 0
        metadataPreviewVerified = 0
        metadataPreviewUnknown = 0
        // Preserve already confirmed details when the user opens an album.
        metadataPreviewStatuses.keys.toList().forEach { key ->
            if (metadataPreviewStatuses[key] == TelegramMetadataCheckStatus.CHECKING)
                metadataPreviewStatuses[key] = TelegramMetadataCheckStatus.UNKNOWN
        }
    }

    /**
     * Tự mở từng kết quả như thao tác click trong tab Telegram, NHƯNG không phát
     * và không tải file vào máy. Bot có trạng thái: trước kết quả thứ hai, phải
     * yêu cầu lại trang tìm kiếm để lấy đúng callback/messageId đang có hiệu lực.
     */
    fun prefetchSearchMetadata(entries: List<TelegramUnifiedTrack>, searchKeyword: String) {
        cancelSearchMetadataPrefetch()
        cancelAlbumMetadata()
        // Numbered search keyboards (@vkmusic_bot / @musgetbot) are actionable.
        // Auto-probing every result would spam 8-10 requests and delay playback.
        if (isNumberedMusicBot || isMooMusicBot || isLyBot ||
            entries.any { it.isNumericReplyChoice || it.isDirectMusicBotChoice }) return
        if (!connection.linked || authStep != TelegramAuthStep.READY) return
        val candidates = entries.filter { it.button != null && it.audio == null }
            .distinctBy { selectionKey(it) }
        if (candidates.isEmpty() || searchKeyword.isBlank()) return
        lastUnifiedSearchKeyword = searchKeyword.trim().take(120)
        val chatId = connection.botChatId
        val generation = metadataPreviewGeneration
        metadataPreviewTotal = candidates.size
        metadataPreviewCompleted = 0
        metadataPreviewVerified = 0
        metadataPreviewUnknown = 0
        candidates.forEach { metadataPreviewStatuses[it.stableKey] = TelegramMetadataCheckStatus.UNKNOWN }
        metadataPreviewJob = scope.launch {
            metadataPreviewRunning = true
            try {
                // Search result callbacks can be invalidated by the previous click.
                // Re-query the bot before each selection, and RETRY an unconfirmed
                // result once instead of silently counting it as inspected.
                for ((index, original) in candidates.withIndex()) {
                    currentCoroutineContext().ensureActive()
                    if (!connection.linked || connection.botChatId != chatId ||
                        pendingPlayback != null || waitingForAudio != null ||
                        downloading || driveUploading) break
                    val originalKey = selectionKey(original) ?: continue
                    metadataPreviewActiveKey = original.stableKey
                    metadataPreviewParentKey = original.stableKey
                    metadataPreviewStatuses[original.stableKey] = TelegramMetadataCheckStatus.CHECKING
                    try {
                        var detail: TelegramSelectedDetail? = null
                        for (attempt in 0..1) {
                            currentCoroutineContext().ensureActive()
                            if (pendingPlayback != null || waitingForAudio != null ||
                                connection.botChatId != chatId || !connection.linked ||
                                downloading || driveUploading) break
                            var waitCycles = 0
                            while (busy && waitCycles++ < 30) delay(120L)
                            if (busy) break
                            // The first button comes from the just-returned page;
                            // EVERY later button MUST be refreshed from the bot.
                            // On the second attempt re-request the page even for #1.
                            val current = if (index == 0 && attempt == 0) original else
                                refreshSearchChoice(lastUnifiedSearchKeyword, original)
                                    ?: original.takeIf(::isOriginalBotButtonStillAvailable)
                            if (current == null) {
                                if (BuildConfig.DEBUG) android.util.Log.w("MWTelegramProbe",
                                    "Cannot recover callback for ${original.title}, attempt ${attempt + 1}")
                                delay(500L)
                                continue
                            }
                            detail = probeMetadata(current, originalKey, force = true)
                            if (telegramHasConfirmedIdentity(detail)) break
                            if (BuildConfig.DEBUG) android.util.Log.w("MWTelegramProbe",
                                "No confirmed bot reply: ${original.title}, attempt ${attempt + 1}")
                            delay(700L)
                        }
                        if (telegramHasConfirmedIdentity(detail)) {
                            metadataPreviewVerified++
                            metadataPreviewStatuses[original.stableKey] = TelegramMetadataCheckStatus.VERIFIED
                        } else {
                            metadataPreviewUnknown++
                            metadataPreviewStatuses[original.stableKey] = TelegramMetadataCheckStatus.UNKNOWN
                        }
                        if (BuildConfig.DEBUG) android.util.Log.d("MWTelegramProbe",
                            "Result ${index + 1}/${candidates.size}: ${original.title}; " +
                                    "confirmed=${telegramHasConfirmedIdentity(detail)}; " +
                                    "count=${detail?.count}; children=${detail?.tracks?.size}; " +
                                    "audio=${detail?.audio != null}")
                    } catch (cancel: CancellationException) {
                        throw cancel
                    } catch (failure: Exception) {
                        metadataPreviewUnknown++
                        metadataPreviewStatuses[original.stableKey] = TelegramMetadataCheckStatus.UNKNOWN
                        if (BuildConfig.DEBUG) android.util.Log.w("MWTelegramProbe",
                            "Bot inspection failed for ${original.title}", failure)
                    } finally {
                        metadataPreviewCompleted++ // attempted, not necessarily verified
                        metadataPreviewActiveKey = null
                        metadataPreviewParentKey = null
                    }
                    delay(350L)
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } finally {
                if (metadataPreviewGeneration == generation) {
                    metadataPreviewStatuses.keys.toList().forEach { key ->
                        if (metadataPreviewStatuses[key] == TelegramMetadataCheckStatus.CHECKING)
                            metadataPreviewStatuses[key] = TelegramMetadataCheckStatus.UNKNOWN
                    }
                    activeMetadataProbe = null
                    metadataPreviewActiveKey = null
                    metadataPreviewParentKey = null
                    metadataPreviewRunning = false
                }
            }
        }
    }


    // A second, strictly on-demand pass: run only after the user OPENS an album.
    // Never call this from the global Telegram search-results verification.
    private var albumMetadataJob: Job? = null
    private var albumMetadataGeneration = 0L
    var albumMetadataRunning by mutableStateOf(false)
        private set
    var albumMetadataActiveTrack by mutableStateOf<String?>(null)
        private set

    fun cancelAlbumMetadata() {
        albumMetadataGeneration++
        albumMetadataJob?.cancel()
        albumMetadataJob = null
        albumMetadataRunning = false
        albumMetadataActiveTrack = null
    }

    fun loadAlbumMetadataOnOpen(album: TelegramUnifiedTrack, keyword: String) {
        // Opening an album is an explicit user action and supersedes background
        // verification, otherwise bot callbacks from the two jobs can collide.
        cancelSearchMetadataPrefetch()
        cancelAlbumMetadata()
        val parentKey = selectionKey(album) ?: return
        val initial = selectedDetails[parentKey] ?: return
        if ((initial.count ?: 0) <= 1 && initial.tracks.size <= 1) return
        val generation = albumMetadataGeneration
        val chatId = connection.botChatId
        albumMetadataJob = scope.launch {
            albumMetadataRunning = true
            try {
                // A bot's inline keyboard is mutable: reopen the parent album
                // before checking each child, just like album playback does.
                val knownChildren = initial.tracks.filter { it.button != null || it.audio != null }
                for (child in knownChildren) {
                    currentCoroutineContext().ensureActive()
                    if (generation != albumMetadataGeneration || !connection.linked ||
                        connection.botChatId != chatId || pendingPlayback != null ||
                        waitingForAudio != null || downloading || driveUploading ||
                        searchSaveRunning) break
                    val oldKey = selectionKey(child) ?: continue
                    // A mere "1 file" reply is not sufficient metadata:
                    // still request the actual file fields on album open.
                    val previouslyKnown = selectedDetails[oldKey]
                    if (previouslyKnown?.audio != null ||
                        previouslyKnown?.technicalText?.isNotBlank() == true) continue
                    albumMetadataActiveTrack = child.stableKey
                    try {
                        val freshParent = refreshSearchChoice(keyword, album) ?: continue
                        val freshAlbum = probeMetadata(freshParent, parentKey, force = true)
                        // Keep the already-opened album's row identities stable.
                        // The fresh menu is used only to obtain a valid child callback.
                        if (freshAlbum != null && initial.tracks.isNotEmpty())
                            selectedDetails[parentKey] = initial
                        val newChild = freshAlbum?.tracks?.firstOrNull {
                            telegramSongMatchKey(it.title) == telegramSongMatchKey(child.title)
                        } ?: freshAlbum?.tracks?.getOrNull(knownChildren.indexOf(child))
                        if (newChild != null) {
                            val result = probeMetadata(newChild, oldKey, force = true)
                            if (result != null) {
                                selectedDetails[oldKey] = result
                                selectionKey(newChild)?.let { selectedDetails[it] = result }
                            }
                        }
                    } catch (cancel: CancellationException) {
                        throw cancel
                    } catch (failure: Exception) {
                        if (BuildConfig.DEBUG) android.util.Log.w("MWTelegramProbe",
                            "Album child metadata unavailable: ${child.title}", failure)
                    } finally {
                        albumMetadataActiveTrack = null
                    }
                    delay(300L)
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } finally {
                if (generation == albumMetadataGeneration) {
                    albumMetadataRunning = false
                    albumMetadataActiveTrack = null
                }
            }
        }
    }


    /** Resolve a result as a real audio file without opening the player.
     * Telegram bot callbacks are stateful: reopen the parent album each time.
     */
    private suspend fun resolveSearchAudioForExport(
        entry: TelegramUnifiedTrack, keyword: String
    ): TelegramAudioResult? {
        val known = entry.audio ?: selectionKey(entry)?.let { selectedDetails[it]?.audio }
        if (known != null && known.tdFileId > 0) return known
        if (entry.button == null) return null
        if (entry.isNumericReplyChoice) {
            // Plain reply-keyboard numbers do not have callback IDs to refresh.
            // Only an explicit user Save action reaches this branch.
            return probeMetadata(entry, selectionKey(entry), force = true)?.audio
                ?.takeIf { it.tdFileId > 0 }
        }
        val parent = entry.albumParent
        if (parent != null) {
            val freshParent = refreshSearchChoice(keyword, parent) ?: return null
            val parentDetail = probeMetadata(freshParent, selectionKey(parent), force = true)
            val menu = parentDetail?.tracks.orEmpty()
            val matching = menu.firstOrNull { isMatchingTelegramTrack(it, entry) }
                ?: menu.getOrNull(entry.albumTrackPosition)?.takeIf {
                    telegramSongMatchKey(it.title) == telegramSongMatchKey(entry.title)
                }
            if (matching == null) return null
            return (matching.audio ?: probeMetadata(matching, selectionKey(entry), force = true)?.audio)
                ?.takeIf { it.tdFileId > 0 }
        }
        val fresh = refreshSearchChoice(keyword, entry) ?: return null
        return (fresh.audio ?: probeMetadata(fresh, selectionKey(entry), force = true)?.audio)
            ?.takeIf { it.tdFileId > 0 }
    }

    /** User-invoked save of only the selected tracks. This is NOT called by search/prefetch.
     * Files go to Download/Telegram or Drive/MeloWave/Telegram Music.
     * A bot that does not send a real audio file causes a skipped item, never a fake download.
     */
    fun saveSearchSelection(
        entries: List<TelegramUnifiedTrack>, keyword: String,
        destination: TelegramSearchSaveDestination, controller: MusicController,
        driveToken: String? = null
    ) {
        if (searchSaveRunning || searchSaveJob?.isActive == true) {
            searchSaveMessage = "Đang lưu danh sách trước, vui lòng chờ."
            return
        }
        if (!connection.linked || authStep != TelegramAuthStep.READY ||
            busy || pendingPlayback != null || waitingForAudio != null ||
            driveUploading || downloading) {
            searchSaveMessage = "Telegram đang bận hoặc chưa kết nối."
            return
        }
        if (destination == TelegramSearchSaveDestination.DRIVE && driveToken.isNullOrBlank()) {
            searchSaveMessage = "Cần cấp quyền Google Drive trước khi lưu."
            return
        }
        val ordered = entries.distinctBy {
            "${it.albumParent?.stableKey.orEmpty()}|${it.stableKey}"
        }.filter { !it.isAlbum && (it.button != null || it.audio != null) }
        if (ordered.isEmpty()) {
            searchSaveMessage = "Không có bài hát để tải."
            return
        }
        cancelSearchMetadataPrefetch()
        searchSaveRunning = true
        searchSaveDestination = destination
        searchSaveActiveTrackKey = null
        searchSaveActiveAlbumKey = null
        searchSaveCompletedTracks.clear()
        searchSaveCompletedAlbums.clear()
        searchSaveCurrent = 0
        searchSaveTotal = ordered.size
        searchSaveItemProgress = 0f
        searchSaveAlbumProgress = 0f
        val searchWord = keyword.trim().take(120).ifBlank { lastUnifiedSearchKeyword }
        searchSaveJob = scope.launch {
            var saved = 0
            var failed = 0
            try {
                for ((index, entry) in ordered.withIndex()) {
                    currentCoroutineContext().ensureActive()
                    searchSaveActiveTrackKey = entry.stableKey
                    searchSaveActiveAlbumKey = entry.albumParent?.stableKey
                    searchSaveAlbumTotal = ordered.count {
                        it.albumParent?.stableKey == searchSaveActiveAlbumKey
                    }.takeIf { searchSaveActiveAlbumKey != null } ?: 0
                    searchSaveAlbumDone = ordered.take(index).count {
                        it.albumParent?.stableKey == searchSaveActiveAlbumKey
                    }
                    updateSearchSaveProgress(0f)
                    searchSaveMessage = "Đang xử lý ${index + 1}/${ordered.size}: ${entry.title}"
                    var fileId = 0
                    var ownsOfflineLock = false
                    try {
                        val audio = resolveSearchAudioForExport(entry, searchWord)
                            ?: throw IOException("Bot chưa gửi file nhạc cho '${entry.title}'.")
                        fileId = audio.tdFileId
                        searchSaveActiveFileId = fileId
                        if (!offlineSavingFileIds.add(fileId))
                            throw IOException("File này đang được tải.")
                        ownsOfflineLock = true
                        downloading = true
                        searchSaveDownloadingFromTelegram = true
                        val file = try { downloadTelegramFile(audio) } finally {
                            searchSaveDownloadingFromTelegram = false
                        }
                        updateSearchSaveProgress(
                            if (destination == TelegramSearchSaveDestination.DRIVE) 0.45f else 0.92f
                        )
                        val suffix = audio.fileName.substringAfterLast('.', "mp3")
                            .lowercase(Locale.ROOT).takeIf { it in AUDIO_EXTENSIONS } ?: "mp3"
                        if (destination == TelegramSearchSaveDestination.DEVICE) {
                            val albumName = entry.albumParent?.title.orEmpty()
                                .replace(Regex("""[^\p{L}\p{N} _().-]"""), "_")
                                .replace(Regex("""\.{2,}"""), "_")
                                .trim().take(65)
                            val folder = if (albumName.isBlank()) "Telegram" else "Telegram/$albumName"
                            val song = SongItem(
                                id = "tg_export_${audio.chatId}_${audio.messageId}",
                                title = audio.title.ifBlank { entry.title },
                                artist = audio.artist.ifBlank { entry.artist },
                                uri = Uri.EMPTY, durationMs = audio.durationMs,
                                source = SongSource.TELEGRAM,
                                mimeType = audio.mimeType, sizeBytes = audio.size.takeIf { it > 0L }
                            )
                            withContext(Dispatchers.IO) {
                                writeAudioToDownloads(file, audio, song, folder)
                            }
                        } else {
                            driveUploading = true
                            driveSourceFileId = fileId
                            try {
                                val cleanName = "${audio.artist} - ${audio.title}.$suffix"
                                    .replace(Regex("""[\\/:*?"<>|\r\n]"""), "_").take(180)
                                val sourceId = audio.uniqueFileId
                                    .ifBlank { "${audio.chatId}_${audio.messageId}" }
                                withContext(Dispatchers.IO) {
                                    controller.uploadTelegramFileToDrive(
                                        driveToken.orEmpty(), sourceId, file, cleanName, audio.mimeType
                                    ) { uploadedBytes, totalBytes ->
                                        withContext(Dispatchers.Main.immediate) {
                                            if (totalBytes > 0L) {
                                                updateSearchSaveProgress(0.45f +
                                                        (uploadedBytes.toFloat() / totalBytes)
                                                            .coerceIn(0f, 1f) * 0.55f)
                                            }
                                        }
                                    }
                                }
                            } finally {
                                driveUploading = false
                                driveSourceFileId = 0
                            }
                        }
                        updateSearchSaveProgress(1f)
                        saved++
                        searchSaveCompletedTracks[entry.stableKey] = destination
                        entry.albumParent?.let { parent ->
                            val children = ordered.filter { it.albumParent?.stableKey == parent.stableKey }
                            val detail = selectionKey(parent)?.let { selectedDetails[it] }
                            // A partial album selection is NOT a complete album backup.
                            val expectedCount = detail?.count ?: detail?.tracks?.size ?: 0
                            if (expectedCount > 0 && children.size >= expectedCount &&
                                children.all { searchSaveCompletedTracks[it.stableKey] == destination }) {
                                searchSaveCompletedAlbums[parent.stableKey] = destination
                            }
                        }
                    } catch (cancel: CancellationException) {
                        throw cancel
                    } catch (expired: PersonalDriveAuthorizationException) {
                        controller.onTelegramSearchBatchDriveAuthorizationExpired(
                            ordered.drop(index), searchWord, driveToken.orEmpty()
                        )
                        searchSaveMessage = "Quyền Drive hết hạn. Hãy cấp lại để lưu tiếp."
                        return@launch
                    } catch (error: Exception) {
                        failed++
                        if (BuildConfig.DEBUG) android.util.Log.w(
                            "MWTelegramSave", "Cannot save ${entry.title}", error
                        )
                    } finally {
                        downloading = false
                        searchSaveDownloadingFromTelegram = false
                        searchSaveActiveFileId = 0
                        if (fileId > 0 && ownsOfflineLock) {
                            offlineSavingFileIds.remove(fileId)
                            trackTemporaryTelegramAudio(fileId)
                            scheduleTelegramCacheTrim()
                        }
                        searchSaveCurrent = index + 1
                    }
                }
                searchSaveMessage = "Đã lưu $saved/${ordered.size} bài" +
                        if (failed > 0) " · $failed bài chưa thành công." else "."
            } catch (cancel: CancellationException) {
                searchSaveMessage = "Đã dừng tải."
                throw cancel
            } finally {
                searchSaveRunning = false
                searchSaveDownloadingFromTelegram = false
                searchSaveActiveFileId = 0
                searchSaveActiveTrackKey = null
                searchSaveActiveAlbumKey = null
                searchSaveDestination = null
                searchSaveJob = null
            }
        }
    }

    /** Only reuse the original callback if Telegram still has that exact keyboard.
     * An edited/replaced menu must not be mistaken for the search result page.
     */
    private fun isOriginalBotButtonStillAvailable(original: TelegramUnifiedTrack): Boolean =
        results.any { it.messageId == original.result.messageId &&
                telegramBotPageFingerprint(it) == telegramBotPageFingerprint(original.result) }

    /** Send the original keyword and wait for a NEW search page, then pick the
     * current callback corresponding to the original result. A bot's old inline
     * keyboard can be edited/removed after clicking even a DIFFERENT result.
     */
    private suspend fun refreshSearchChoice(keyword: String,
                                            original: TelegramUnifiedTrack): TelegramUnifiedTrack? {
        val target = original.button ?: return null
        if (keyword.isBlank() || pendingPlayback != null || !connection.linked) return null
        val chatId = connection.botChatId
        val marker = botReplyRevision
        val expected = original.result.buttons
            .filter { isTelegramTrackButton(it) || TELEGRAM_NUMBERED_TRACK.containsMatchIn(it.label) ||
                (isMooMusicBot && mooMusicSongAction(it)) || (isLyBot && lyBotSongAction(it)) }
            .map { telegramSongMatchKey(it.label) }.filter { it.isNotBlank() }.toSet()
        val send = TdApi.SendMessage().apply {
            this.chatId = chatId
            inputMessageContent = TdApi.InputMessageText().apply {
                text = TdApi.FormattedText().apply {
                    text = keyword
                    entities = emptyArray()
                }
            }
        }
        try { client.request(send) } catch (cancel: CancellationException) { throw cancel }
        catch (_: Exception) { return null }
        return withTimeoutOrNull(9_000L) {
            var chosen: TelegramUnifiedTrack? = null
            while (chosen == null && connection.linked && connection.botChatId == chatId) {
                currentCoroutineContext().ensureActive()
                for ((revision, response) in recentBotReplies.asReversed()) {
                    if (revision <= marker) break
                    val buttons = response.buttons.filter {
                        it.kind == TelegramActionKind.CALLBACK || it.kind == TelegramActionKind.TEXT
                    }
                    val overlap = buttons.count { telegramSongMatchKey(it.label) in expected }
                    // With >=2 search hits, require >=2 hits in the reply. This
                    // prevents mistaking an album submenu for a search page.
                    if (overlap < minOf(2, expected.size.coerceAtLeast(1))) continue
                    val match = buttons.firstOrNull { next ->
                        telegramSongMatchKey(next.label) == telegramSongMatchKey(target.label) ||
                                telegramSongMatchKey(songLabelMetadata(next.label).second) ==
                                telegramSongMatchKey(original.title)
                    }
                    if (match != null) {
                        chosen = original.copy(result = response, button = match)
                        break
                    }
                }
                if (chosen == null) delay(180L)
            }
            chosen
        }
    }

    /** A user tap follows the SAME probe path, then opens an album list or plays
     * a confirmed single file. It does not assume that a bot selection is a file.
     */
    fun resolveSearchResultOnTap(entry: TelegramUnifiedTrack, keyword: String,
                                 controller: MusicController) {
        cancelSearchMetadataPrefetch()
        if (isMooMusicBot && botAccessRequirement != null) {
            message = botAccessRequirement
            return
        }
        val originalKey = selectionKey(entry) ?: return
        selectedDetails[originalKey]?.let { prior ->
            if ((prior.count ?: 0) > 1) return
            if (prior.audio != null) {
                playFromBot(prior.audio, controller)
                return
            }
        }
        scope.launch {
            val fresh = refreshSearchChoice(keyword, entry) ?: entry
            val detail = probeMetadata(fresh, originalKey, force = true)
            if ((detail?.count ?: 0) == 1 && detail?.audio != null) {
                playFromBot(detail.audio, controller)
            } else if (detail == null) {
                // Bot-specific callbacks may send a message later than the
                // preview timeout. Keep the UI neutral and let the user retry.
                message = "Bot chưa trả thông tin cho mục này. Có thể chọn lại hoặc mở trong Telegram."
            }
        }
    }

    private suspend fun probeMetadata(entry: TelegramUnifiedTrack, aliasKey: String? = null,
                                      force: Boolean = false): TelegramSelectedDetail? {
        val button = entry.button ?: return null
        if (button.kind != TelegramActionKind.CALLBACK &&
            button.kind != TelegramActionKind.TEXT) return null
        val key = aliasKey ?: selectionKey(entry) ?: return null
        if (!force) selectedDetails[key]?.let { detail ->
            if (telegramHasConfirmedIdentity(detail)) return detail
        }
        if (!connection.linked || pendingPlayback != null || busy || downloading) return null
        val probe = TelegramMetadataProbe(
            key = key, chatId = connection.botChatId,
            sourceMessageId = entry.result.messageId,
            sourceFingerprint = telegramBotPageFingerprint(entry.result),
            sourceActionSignatures = entry.result.buttons.map(::telegramProbeActionSignature),
            sourceActionLabels = entry.result.buttons.map {
                telegramSongMatchKey(it.label)
            }.filter { it.isNotBlank() }.toSet(),
            finished = CompletableDeferred()
        )
        activeMetadataProbe = probe
        probe.clickedActions.add("${entry.result.messageId}|${telegramProbeActionSignature(button)}")
        if (BuildConfig.DEBUG) android.util.Log.d("MWTelegramProbe",
            "Selecting bot result, action=${button.kind}, sourceButtons=${entry.result.buttons.size}")
        try {
            performBotAction(probe.chatId, entry.result.messageId, button)
            val response = withTimeoutOrNull(8_000L) { probe.finished.await() }
                ?: selectedDetails[key]
            if (BuildConfig.DEBUG) android.util.Log.d("MWTelegramProbe",
                "Selection outcome: count=${response?.count}, children=${response?.tracks?.size}, audio=${response?.audio != null}")
            return response
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            return selectedDetails[key]
        } finally {
            if (activeMetadataProbe === probe) activeMetadataProbe = null
        }
    }

    /** Correlate only an AFTER-click bot message or an edited originating message. */
    private fun captureMetadataProbe(entry: TelegramBotResult, chatId: Long) {
        val probe = activeMetadataProbe ?: return
        if (chatId != probe.chatId || entry.outgoing || probe.finished.isCompleted) return
        if (entry.messageId == probe.sourceMessageId &&
            telegramBotPageFingerprint(entry) == probe.sourceFingerprint) return
        // An edited search page with the same original buttons is NOT an album
        // detail. Some bots just update the search header after a callback.
        if (entry.audio == null &&
            entry.buttons.isNotEmpty() &&
            entry.buttons.map(::telegramProbeActionSignature) == probe.sourceActionSignatures &&
            telegramBotTechnicalFields(entry.text).isBlank()) return
        // A bot may resend the same search page with NEW callback IDs. A page
        // sharing >=2 original search-result titles is not proof of an album.
        val overlapWithSearch = entry.buttons.count { button ->
            telegramSongMatchKey(button.label) in probe.sourceActionLabels
        }
        val explicitCountInReply = TELEGRAM_DETAIL_FILE_COUNT.containsMatchIn(entry.text) ||
                TELEGRAM_DETAIL_FILE_COUNT_SUFFIX.containsMatchIn(entry.text)
        if (entry.audio == null && !explicitCountInReply &&
            probe.sourceActionLabels.size > 1 && overlapWithSearch >= 2) return
        // A bot reply is not necessarily audio: many bots return an album track
        // keyboard, or a detail card with a button to send the audio message.
        val audio = entry.audio
        if (audio != null) {
            // A follow-up preview audio must not turn a bot-confirmed album
            // into a single song while its list keyboard is still arriving.
            if ((selectedDetails[probe.key]?.count ?: 0) > 1) return
            val detail = TelegramSelectedDetail(count = 1, audio = audio,
                technicalText = telegramBotTechnicalFields(entry.text))
            selectedDetails[probe.key] = detail
            verifiedEntryFileCounts[probe.key] = 1
            probe.finished.complete(detail)
            return
        }
        val buttons = telegramAlbumFileButtons(entry)
        val text = entry.text
        val explicitCount = (TELEGRAM_DETAIL_FILE_COUNT.find(text)
            ?.groupValues?.getOrNull(1) ?: TELEGRAM_DETAIL_FILE_COUNT_SUFFIX.find(text)
            ?.groupValues?.getOrNull(1))?.toIntOrNull()
        val count = maxOf(explicitCount ?: 0, buttons.size)
        if (BuildConfig.DEBUG) android.util.Log.d("MWTelegramProbe",
            "Bot reply: buttons=${entry.buttons.size}, trackButtons=${buttons.size}, explicitCount=$explicitCount")
        val albumTracks = if (count > 1) buttons.mapIndexed { index, button ->
            val (artist, title, duration) = songLabelMetadata(button.label)
            TelegramUnifiedTrack("preview_album_${entry.messageId}_$index", entry,
                button = button,
                title = title.replace(TELEGRAM_NUMBERED_TRACK_PREFIX, "").trim(),
                artist = artist, durationMs = duration)
        } else emptyList()
        val technical = telegramBotTechnicalFields(text)
        val detail = TelegramSelectedDetail(count = count.takeIf { it > 0 },
            tracks = albumTracks, technicalText = technical)
        if (count > 0 || technical.isNotBlank()) selectedDetails[probe.key] = detail
        if (count > 1) {
            verifiedEntryFileCounts[probe.key] = count
            if (albumTracks.isNotEmpty()) {
                probe.finished.complete(detail)
            } else {
                // Some bots first send a short "7 tracks" caption, THEN edit
                // the keyboard or send the 7 selectable titles. Do not close
                // this probe before those real track buttons have arrived.
                scope.launch {
                    delay(3_000L)
                    if (activeMetadataProbe === probe && !probe.finished.isCompleted)
                        probe.finished.complete(selectedDetails[probe.key] ?: detail)
                }
            }
            return
        }
        // Inspect a SINGLE result like a normal bot click, including one or two
        // nested "send audio" actions. Metadata comes from the bot's real
        // MessageAudio / MessageDocument, not from guesses about the title.
        // Avoid selecting among multiple bitrate variants or downloading albums.
        val fileButton = telegramSingleFileMetadataAction(entry)
        val actionKey = fileButton?.let {
            "${entry.messageId}|${telegramProbeActionSignature(it)}"
        }
        if (fileButton != null && actionKey != null && probe.followUpCount < 2 &&
            probe.clickedActions.add(actionKey)) {
            probe.followUpCount++
            scope.launch {
                delay(250L)
                if (activeMetadataProbe === probe && !probe.finished.isCompleted) {
                    try { performBotAction(probe.chatId, entry.messageId, fileButton) }
                    catch (cancel: CancellationException) { throw cancel }
                    catch (_: Exception) { /* Keep any authoritative bot text */ }
                }
            }
        } else if (technical.isNotBlank() || count == 1) {
            // A bot may send a short "1 file" caption and then the actual
            // MessageAudio a little later. Keep this probe alive briefly so the
            // file size and format can replace partial caption information.
            scope.launch {
                delay(if (technical.isNotBlank()) 1_500L else 3_000L)
                if (activeMetadataProbe === probe && !probe.finished.isCompleted)
                    probe.finished.complete(selectedDetails[probe.key] ?: detail)
            }
        }
    }

    fun selectionKey(entry: TelegramUnifiedTrack): String? =
        entry.button?.let { telegramBotSelectionKey(entry.result, it) }
    private var nextPlaybackToken = 0L
    // Snapshot of the exact bot results visible when a user presses Play.
    // Buttons can request files asynchronously; unlike a single-item Media3 queue,
    // this lets Next/Previous request the adjacent result from the same bot.
    private var searchPlayOrder by mutableStateOf<List<TelegramUnifiedTrack>>(emptyList())
    private var searchPlayIndex by mutableStateOf(-1)
    private var searchPlaySongId by mutableStateOf<String?>(null)
    private var searchQueueKeyword = ""
    private var queuedAlbumResolveJob: Job? = null
    private var queuedAlbumResolveGeneration = 0L
    // Pending automatic bot page turn triggered by Next/Previous on album boundary.
    // It only progresses when the bot actually returns a different page with tracks.
    private var albumPageChange: TelegramAlbumPageChange? = null
    private var albumPageToken = 0L
    // Enabled only by Play all / Play selected, not a single tapped result.
    private var autoPlaySearchResults = false
    private var pendingDeferredAudio: TelegramDeferredAudio? = null
    var driveUploading by mutableStateOf(false)
        private set
    var driveUploadStage by mutableStateOf("")
        private set
    var driveBytesUploaded by mutableStateOf(0L)
        private set
    var driveTotalBytes by mutableStateOf(0L)
        private set
    var driveTelegramBytesReceived by mutableStateOf(0L)
        private set
    var driveTelegramTotalBytes by mutableStateOf(0L)
        private set
    private var driveSourceFileId = 0

    /** Returns metadata that is already present in Telegram messages / saved references. */
    fun technicalInfoForSong(song: SongItem): AudioTechnicalInfo? {
        if (song.source != SongSource.TELEGRAM) return null
        val active = activeStreamInfo[song.id]
        if (active != null) return telegramTechnicalInfo(active.fileName, active.mimeType,
            active.size, active.durationMs)
        val storedId = song.id.removePrefix("telegram_")
        val stored = savedSongs.firstOrNull { it.id == storedId } ?: return null
        val size = stored.sizeBytes.takeIf { it > 0L } ?: stored.path.takeIf { it.isNotBlank() }
            ?.let { File(it).takeIf(File::isFile)?.length() } ?: 0L
        return telegramTechnicalInfo(stored.fileName, stored.mime, size, stored.duration)
    }

    /** Metadata already confirmed from a prior Telegram audio response, not
     * inferred from search labels. Only use an unambiguous same-bot match.
     */
    fun knownSearchTechnicalText(entry: TelegramUnifiedTrack): String? {
        if (entry.isAlbum || entry.audio != null) return null
        val title = telegramSongMatchKey(entry.title)
        if (title.isBlank()) return null
        val artist = telegramSongMatchKey(entry.artist)
        val matches = savedSongs.filter { saved ->
            saved.sizeBytes > 0L && saved.fileName.isNotBlank() &&
                    saved.botUsername.equals(connection.botUsername, ignoreCase = true) &&
                    telegramSongMatchKey(saved.title) == title &&
                    (artist.isBlank() || artist == telegramSongMatchKey(saved.artist) ||
                            artist == "nghesichuabiet")
        }
        val saved = matches.singleOrNull() ?: return null
        val info = telegramTechnicalInfo(saved.fileName, saved.mime,
            saved.sizeBytes, saved.duration)
        return listOfNotNull(info.extensionLabel.takeUnless { it == "AUDIO" },
            info.qualityLabel.takeUnless { it == info.extensionLabel ||
                    it == "Chất lượng chưa rõ" },
            info.sizeLabel.takeUnless { it == "Không rõ dung lượng" })
            .joinToString(" \u00b7 ").takeIf { it.isNotBlank() }
    }

    var authStep by mutableStateOf(
        if (BuildConfig.TELEGRAM_API_ID > 0 && BuildConfig.TELEGRAM_API_HASH.isNotBlank())
            TelegramAuthStep.STARTING else TelegramAuthStep.NOT_CONFIGURED
    )
        private set
    var connection by mutableStateOf(TelegramConnectionState())
        private set
    // Selective compatibility adapter. All other Telegram bots use the existing parser.
    val isVkMusicBot: Boolean get() = connection.linked &&
        connection.botUsername.equals("vkmusic_bot", ignoreCase = true)
    val isMusgetBot: Boolean get() = connection.linked &&
        connection.botUsername.equals("musgetbot", ignoreCase = true)
    val isNumberedMusicBot: Boolean get() = isVkMusicBot || isMusgetBot
    val isMooMusicBot: Boolean get() = connection.linked &&
        connection.botUsername.equals("MooMusicBot", ignoreCase = true)
    val isLyBot: Boolean get() = connection.linked &&
        connection.botUsername.equals("LyBot", ignoreCase = true)
    // Set only when the selected bot explicitly demands partner subscriptions.
    var botAccessRequirement by mutableStateOf<String?>(null)
        private set
    /** Only restore the bot the user actually connected, never an arbitrary recent bot. */
    val hasSavedBot: Boolean
        get() = savedBotUsername().isNotBlank()
    var restoringSavedBot by mutableStateOf(false)
        private set
    private var restoreAttemptedThisSession = false
    private var savedBotRestoreJob: Job? = null
    var busy by mutableStateOf(false)
        private set
    var downloading by mutableStateOf(false)
        private set
    var waitingForAudio by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    val results = mutableStateListOf<TelegramBotResult>()
    val songs = mutableStateListOf<SongItem>()
    private val telegramArtworkFallbacks = mutableStateMapOf<String, String>()
    private val pendingArtworkFallbacks = mutableSetOf<String>()

    /** Used only when Apple Music / user artwork has no suitable image for the song. */
    fun fallbackArtworkForSong(songId: String): String? = telegramArtworkFallbacks[songId]

    /** Save the actual Telegram audio cover, not the MP3/FLAC. A shared album cover is
     * acceptable only as a fallback; individual Apple Music art takes priority.
     */
    fun rememberTelegramArtwork(songId: String, audio: TelegramAudioResult) {
        if (songId.isBlank() || telegramArtworkFallbacks.containsKey(songId) ||
            !pendingArtworkFallbacks.add(songId)) return
        scope.launch {
            try {
                val uri = withContext(Dispatchers.IO) {
                    val folder = File(appContext.cacheDir, "telegram_song_covers").apply { mkdirs() }
                    val key = MessageDigest.getInstance("SHA-256")
                        .digest(songId.toByteArray(Charsets.UTF_8))
                        .joinToString("") { "%02x".format(it.toInt() and 0xff) }.take(28)
                    val thumbnail = if (audio.albumCoverThumbnailFileId > 0) {
                        runCatching {
                            client.request(TdApi.DownloadFile(audio.albumCoverThumbnailFileId,
                                8, 0L, 0L, true)) as? TdApi.File
                        }.getOrNull()
                    } else null
                    val tdPath = thumbnail?.local?.path.orEmpty()
                    val bytes = if (thumbnail?.local?.isDownloadingCompleted == true &&
                        tdPath.isNotBlank() && File(tdPath).isFile) {
                        File(tdPath).takeIf { it.length() in 1L..(1024L * 1024L) }?.readBytes()
                    } else null
                    val jpg = (bytes ?: audio.albumCoverBytes)?.takeIf { it.size in 1..(1024 * 1024) }
                        ?: return@withContext null
                    if (BitmapFactory.decodeByteArray(jpg, 0, jpg.size) == null)
                        return@withContext null
                    val file = File(folder, "$key.jpg")
                    if (!file.isFile || file.length() != jpg.size.toLong()) file.writeBytes(jpg)
                    Uri.fromFile(file).toString()
                }
                if (!uri.isNullOrBlank()) telegramArtworkFallbacks[songId] = uri
            } catch (_: Exception) {
                // A missing Telegram thumbnail must not prevent playback or Apple art lookup.
            } finally { pendingArtworkFallbacks.remove(songId) }
        }
    }

    private val loadedCoverThumbnails = mutableStateMapOf<Int, String>()
    private val pendingCoverThumbnailIds = mutableSetOf<Int>()

    /** Download an image-only thumbnail from TDLib. Never loads the MP3/FLAC body. */
    fun coverThumbnailUri(fileId: Int): String? {
        if (fileId <= 0) return null
        loadedCoverThumbnails[fileId]?.let { return it }
        if (pendingCoverThumbnailIds.add(fileId)) {
            scope.launch {
                try {
                    val cover = client.request(TdApi.DownloadFile(fileId, 8, 0L, 0L, true)) as? TdApi.File
                    val path = cover?.local?.path.orEmpty()
                    if (cover?.local?.isDownloadingCompleted == true && path.isNotBlank() &&
                        File(path).isFile && File(path).length() > 0L) {
                        loadedCoverThumbnails[fileId] = Uri.fromFile(File(path)).toString()
                    }
                } catch (_: Exception) {
                    // A missing cover is not a music playback failure.
                } finally {
                    pendingCoverThumbnailIds.remove(fileId)
                }
            }
        }
        return null
    }

    init {
        restoreTrackedTelegramAudio()
        recentBots.addAll(readRecentBots())
        // Migrate the previously selected bot into history without preselecting it.
        if (recentBots.isEmpty()) {
            val previous = prefs.getString("last_bot", "").orEmpty().removePrefix("@")
            if (Regex("^[A-Za-z0-9_]{5,32}$").matches(previous)) {
                recentBots.add(TelegramRecentBot(previous))
                persistRecentBots()
            }
        }
        savedSongs.addAll(readCache())
        // Allow playback of tracks previously downloaded by the older Bot API build.
        if (savedSongs.isEmpty()) savedSongs.addAll(migrateOldLocalMusic())
        rebuildSongs()
        if (authStep != TelegramAuthStep.NOT_CONFIGURED) client.start()
    }

    fun clearFeedback() { error = null; message = null }

    private fun onAuthorizationChanged(state: TdApi.AuthorizationState) {
        authStep = when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                client.configure(BuildConfig.TELEGRAM_API_ID, BuildConfig.TELEGRAM_API_HASH)
                TelegramAuthStep.STARTING
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> TelegramAuthStep.PHONE
            is TdApi.AuthorizationStateWaitCode -> TelegramAuthStep.CODE
            is TdApi.AuthorizationStateWaitPassword -> TelegramAuthStep.PASSWORD
            is TdApi.AuthorizationStateWaitEmailAddress -> TelegramAuthStep.EMAIL
            is TdApi.AuthorizationStateWaitEmailCode -> TelegramAuthStep.EMAIL_CODE
            is TdApi.AuthorizationStateWaitOtherDeviceConfirmation -> {
                message = "Xác nhận đăng nhập từ thiết bị Telegram khác: ${state.link}"
                TelegramAuthStep.QR_CONFIRM
            }
            is TdApi.AuthorizationStateWaitRegistration -> TelegramAuthStep.REGISTRATION
            is TdApi.AuthorizationStateReady -> {
                // Restore only a user-chosen bot; no scanning of other private chats.
                authStep = TelegramAuthStep.READY
                // Reclaim tracked streams left after a crash or an Android process kill.
                // Older untracked TDLib files are not deleted indiscriminately.
                scheduleTelegramCacheTrim()
                TelegramAuthStep.READY
            }
            is TdApi.AuthorizationStateLoggingOut,
            is TdApi.AuthorizationStateClosing -> TelegramAuthStep.LOGGING_OUT
            is TdApi.AuthorizationStateClosed -> TelegramAuthStep.PHONE
            else -> TelegramAuthStep.STARTING
        }
        if (state is TdApi.AuthorizationStateReady) restoreLastConnectedBot()
    }

    /** Resolve from the cached TDLib chat first (fast/offline), then by public username.
     * Both requests are bounded; a stalled bot must not hold the Connect button forever.
     */
    private suspend fun resolveBotChat(username: String, cachedChatId: Long = 0L): TdApi.Chat {
        if (cachedChatId != 0L) {
            val cached = try {
                withTimeoutOrNull(3_000L) {
                    val chat = client.request(TdApi.GetChat(cachedChatId)) as? TdApi.Chat
                    val userId = (chat?.type as? TdApi.ChatTypePrivate)?.userId
                    val user = userId?.let { client.request(TdApi.GetUser(it)) as? TdApi.User }
                    chat?.takeIf { user?.type is TdApi.UserTypeBot }
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                // A stale chat ID is normal after changing devices/sessions.
                null
            }
            if (cached != null) return cached
        }
        val chat = withTimeoutOrNull(9_000L) {
            client.request(TdApi.SearchPublicChat(username)) as? TdApi.Chat
        } ?: throw IOException("Chưa thể kết nối @$username. Kiểm tra mạng hoặc thử lại.")
        val userId = (chat.type as? TdApi.ChatTypePrivate)?.userId
            ?: throw IOException("Đây không phải hội thoại riêng với bot.")
        val user = withTimeoutOrNull(5_000L) {
            client.request(TdApi.GetUser(userId)) as? TdApi.User
        } ?: throw IOException("Telegram chưa trả thông tin bot. Hãy thử lại.")
        if (user.type !is TdApi.UserTypeBot)
            throw IOException("@$username không phải bot Telegram.")
        return chat
    }

    private fun saveActiveBot(chat: TdApi.Chat, username: String) {
        connection = TelegramConnectionState(
            status = TelegramConnectionStatus.ACTIVE,
            botChatId = chat.id,
            botUsername = username,
            botDisplayName = chat.title
        )
        noteConnectedBot(username, chat.title)
        prefs.edit().putString("last_bot", username)
            .putLong("last_bot_chat_id", chat.id).apply()
    }

    /** Called after TDLib finishes restoring the local Telegram USER session. */
    private fun restoreLastConnectedBot() {
        if (restoreAttemptedThisSession || connection.linked || authStep != TelegramAuthStep.READY)
            return
        restoreAttemptedThisSession = true
        val username = savedBotUsername()
        if (username.isBlank()) return
        val cachedId = prefs.getLong("last_bot_chat_id", 0L)
        savedBotRestoreJob?.cancel()
        savedBotRestoreJob = scope.launch {
            restoringSavedBot = true
            try {
                val chat = resolveBotChat(username, cachedId)
                // No /start, no re-login and no automatic message is sent to the bot.
                saveActiveBot(chat, username)
                message = "Đã khôi phục bot @$username."
                error = null
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                // Keep the saved preference for manual retry; avoid a red error banner.
                message = "Chưa khôi phục được @$username. Có thể thử kết nối lại."
                error = null
            } finally {
                restoringSavedBot = false
            }
        }
    }

    fun retrySavedBotConnection() {
        if (authStep != TelegramAuthStep.READY || busy || connection.linked) return
        restoreAttemptedThisSession = false
        restoreLastConnectedBot()
    }

    fun submitLogin(value: String) {
        val trimmed = value.trim()
        if (trimmed.isBlank()) { error = "Hãy nhập thông tin xác thực Telegram."; return }
        when (authStep) {
            TelegramAuthStep.PHONE -> client.send(TdApi.SetAuthenticationPhoneNumber(trimmed, null))
            TelegramAuthStep.CODE -> client.send(TdApi.CheckAuthenticationCode(trimmed))
            TelegramAuthStep.PASSWORD -> client.send(TdApi.CheckAuthenticationPassword(value))
            TelegramAuthStep.EMAIL -> client.send(TdApi.SetAuthenticationEmailAddress(trimmed))
            TelegramAuthStep.EMAIL_CODE -> client.send(
                TdApi.CheckAuthenticationEmailCode(TdApi.EmailAddressAuthenticationCode(trimmed))
            )
            else -> error = "Telegram chưa yêu cầu nhập thông tin này."
        }
    }

    fun logOut() {
        if (authStep != TelegramAuthStep.READY) return
        cancelSearchMetadataPrefetch()
        cancelPendingPlayback()
        disconnect()
        // File IDs are scoped to this Telegram session. Never try to delete IDs
        // carried over from a different account after a logout/login.
        cleanupJob?.cancel()
        cacheCleanupRequested = false
        downloadedStreamIds.clear()
        persistTrackedTelegramAudio()
        prefs.edit().remove("last_bot").remove("last_bot_chat_id").apply()
        authStep = TelegramAuthStep.LOGGING_OUT
        client.send(TdApi.LogOut())
    }

    fun connectBot(rawUsername: String) {
        val username = rawUsername.trim().substringAfterLast('/').removePrefix("@").substringBefore('?')
        if (authStep != TelegramAuthStep.READY) {
            error = "Hãy đăng nhập Telegram trước."; return
        }
        if (!Regex("^[A-Za-z0-9_]{5,32}$").matches(username)) {
            error = "Tên bot chưa hợp lệ. Hãy nhập @username có từ 5–32 ký tự."
            return
        }
        // Selecting the same bot twice is a no-op, not a new network connection.
        if (connection.linked && connection.botUsername.equals(username, ignoreCase = true)) {
            error = null
            message = "Bot @$username đã liên kết."
            return
        }
        // A manual switch overrides an in-flight automatic recovery.
        savedBotRestoreJob?.cancel()
        savedBotRestoreJob = null
        restoringSavedBot = false
        cancelPendingPlayback()
        perform {
            val previousId = if (savedBotUsername().equals(username, ignoreCase = true))
                prefs.getLong("last_bot_chat_id", 0L) else 0L
            val chat = resolveBotChat(username, previousId)
            saveActiveBot(chat, username)
            botAccessRequirement = null
            results.clear()
            dialogSearchPageSnapshot = emptyList()
            dialogSearchKeyword = ""
            verifiedEntryFileCounts.clear()
            selectedDetails.clear()
            clearSearchPlayOrder()
            // A slow history request must not leave the UI stuck at "Đang kết nối...".
            try {
                withTimeoutOrNull(4_000L) { loadHistoryInternal() }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                // History is optional: a valid bot connection can search without it.
            }
            message = "Đã kết nối @$username. Có thể tìm nhạc ngay."
        }
    }

    fun disconnect() {
        savedBotRestoreJob?.cancel()
        savedBotRestoreJob = null
        restoringSavedBot = false
        cancelSearchMetadataPrefetch()
        cancelPendingPlayback()
        connection = TelegramConnectionState()
        botAccessRequirement = null
        results.clear()
        dialogSearchPageSnapshot = emptyList()
        dialogSearchKeyword = ""
        verifiedEntryFileCounts.clear()
        selectedDetails.clear()
        clearSearchPlayOrder()
        prefs.edit().remove("last_bot").remove("last_bot_chat_id").apply()
        message = "Đã ngắt bot. Tài khoản Telegram vẫn đăng nhập trên máy; nhạc đã tải vẫn còn."
    }

    fun refresh() {
        if (!connection.linked) { error = "Chưa kết nối bot Telegram."; return }
        perform { loadHistoryInternal() }
    }

    private suspend fun loadHistoryInternal() {
        val chatId = connection.botChatId
        if (chatId == 0L) return
        val req = TdApi.GetChatHistory().apply {
            this.chatId = chatId; fromMessageId = 0L; offset = 0; limit = 35; onlyLocal = false
        }
        val batch = client.request(req) as? TdApi.Messages ?: return
        val messages = batch.messages?.filterNotNull().orEmpty()
        if (chatId != connection.botChatId) return
        // Loading old messages must NEVER auto-download or auto-play old media.
        messages.asReversed().forEach { receive(it, fromHistory = true) }
    }

    private suspend fun clientRequestMessage(chatId: Long, messageId: Long): TdApi.Message? =
        client.request(TdApi.GetMessage(chatId, messageId)) as? TdApi.Message

    fun search(rawKeyword: String) {
        cancelSearchMetadataPrefetch()
        cancelAlbumMetadata()
        if (rawKeyword.isBlank()) { error = "Hãy nhập tên bài hát hoặc ca sĩ."; return }
        if (!connection.linked) { error = "Hãy kết nối bot trước."; return }
        if (busy) { message = "Hãy đợi thao tác hiện tại kết thúc."; return }
        cancelPendingPlayback()
        // Searching again must not erase the playlist already playing in Now Playing.
        // A new explicit Play/Play selected action will replace it instead.
        if (searchPlaySongId == null) clearSearchPlayOrder()
        // Show the current search without unrelated historical messages above it.
        results.clear()
        dialogSearchPageSnapshot = emptyList()
        dialogSearchKeyword = ""
        verifiedEntryFileCounts.clear()
        selectedDetails.clear()
        metadataPreviewStatuses.clear()
        recentBotReplies.clear()
        lastUnifiedSearchKeyword = rawKeyword.trim().take(120)
        botAccessRequirement = null  // A new attempt may succeed after user subscribes in Telegram.
        sendText(rawKeyword)
    }
    fun sendText(rawText: String) {
        val text = rawText.trim().take(300)
        if (text.isEmpty()) { error = "Hãy nhập tên bài hát hoặc ca sĩ."; return }
        if (!connection.linked) { error = "Hãy kết nối bot trước."; return }
        perform {
            val request = TdApi.SendMessage().apply {
                chatId = connection.botChatId
                inputMessageContent = TdApi.InputMessageText().apply {
                    this.text = TdApi.FormattedText().apply {
                        this.text = text
                        entities = emptyArray()
                    }
                }
            }
            val sent = client.request(request)
            if (sent is TdApi.Message) receive(sent)
        }
    }

    private fun cancelPendingPlayback() {
        pendingPlayback = null
        waitingForAudio = null
    }

    fun cancelPlayRequest() {
        cancelPendingPlayback()
        stopPlaybackPreparation()
        queuedAlbumResolveGeneration++
        queuedAlbumResolveJob?.cancel()
        queuedAlbumResolveJob = null
        message = "Đã hủy yêu cầu phát nhạc đang chờ bot."
    }

    private fun beginPendingPlayback(label: String, controller: MusicController,
                                     playAfterSave: Boolean, saveToDrive: Boolean = false, choiceKey: String? = null,
                                     sourceMessageId: Long = 0L, sourceFingerprint: String = "") {
        nextPlaybackToken++
        val token = nextPlaybackToken
        if (playAfterSave && !saveToDrive && !playbackPreparing) {
            beginPlaybackPreparation(label, controller)
        }
        pendingPlayback = TelegramPendingPlayback(token, connection.botChatId, label, controller,
            playAfterSave, saveToDrive, choiceKey = choiceKey,
            sourceMessageId = sourceMessageId, sourceFingerprint = sourceFingerprint)
        waitingForAudio = label
        error = null
        message = when {
            saveToDrive -> "Đang yêu cầu bot cung cấp file để tải lên Google Drive…"
            playAfterSave -> "Đang yêu cầu bot gửi '$label' để phát…"
            else -> "Đang yêu cầu bot gửi '$label'…"
        }
        scope.launch {
            delay(75_000L)
            if (pendingPlayback?.token == token) {
                val timeoutReason = "Bot chưa gửi file nhạc cho '$label'. Hãy kiểm tra bot hoặc chọn bài khác."
                if (playbackPreparing) failPlaybackPreparation(timeoutReason)
                else {
                    cancelPendingPlayback()
                    message = timeoutReason
                }
            }
        }
    }

    // PLAY streams from TDLib in small segments; ADD ONLY saves a bot reference.
    // Neither action makes a permanent MP3 copy or replaces the listening queue.
    fun selectResult(
        result: TelegramBotResult,
        button: TelegramBotAction,
        controller: MusicController,
        playAfterSave: Boolean = true,
        saveToDrive: Boolean = false,
        resolveSearchChoice: Boolean = false
    ) {
        if (searchSaveRunning) {
            message = "Đang lưu danh sách; hãy chờ hoàn tất."
            return
        }
        cancelSearchMetadataPrefetch()
        if (busy || downloading || !connection.linked) {
            if (playbackPreparing) failPlaybackPreparation("Bot đang bận hoặc đã ngắt kết nối.")
            return
        }
        if (isMooMusicBot && botAccessRequirement != null && mooMusicSongAction(button)) {
            if (playbackPreparing) failPlaybackPreparation(botAccessRequirement.orEmpty())
            else message = botAccessRequirement
            return
        }
        if (!playAfterSave && !saveToDrive && isTelegramTrackButton(button)) {
            addButtonReference(result, button, controller)
            return
        }
        if (pendingPlayback != null) {
            message = "Hãy đợi bot trả lời hoặc nhấn Hủy trước khi chọn bài khác."
            // Do not replace/cancel the original request; it owns the spinner.
            return
        }
        // A search-result button may represent a SINGLE audio OR an ALBUM menu.
        // Track labels alone are not enough: correlate the bot reply to the click.
        val wantsAudio = resolveSearchChoice || isTelegramTrackButton(button) ||
                isTelegramDownloadButton(button.label)
        if (wantsAudio) beginPendingPlayback(button.label, controller, playAfterSave,
            saveToDrive, choiceKey = telegramBotSelectionKey(result, button),
            sourceMessageId = result.messageId,
            sourceFingerprint = telegramBotPageFingerprint(result))
        else cancelPendingPlayback()
        action(result, button)
    }

    fun isTrackReferenceSaved(label: String): Boolean {
        val rawKey = "${connection.botChatId}|${label.lowercase(Locale.ROOT)}"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(rawKey.toByteArray(Charsets.UTF_8)).take(12)
            .joinToString("") { "%02x".format(it) }
        return savedSongs.any { it.id == "ref_$hash" }
    }

    /** A bookmark never downloads music. Button references can expire if the bot edits/deletes its message. */
    private fun addButtonReference(result: TelegramBotResult, button: TelegramBotAction, controller: MusicController) {
        val (artist, title, duration) = songLabelMetadata(button.label)
        val rawKey = "${connection.botChatId}|${button.label.lowercase(Locale.ROOT)}"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(rawKey.toByteArray(Charsets.UTF_8)).take(12)
            .joinToString("") { "%02x".format(it) }
        val key = "ref_$hash"
        if (savedSongs.any { it.id == key }) {
            message = "Bài hát đã có trong danh sách Telegram (${savedSongs.size} bài)."
            return
        }
        val payload = if (button.kind == TelegramActionKind.CALLBACK)
            Base64.encodeToString(button.callbackData, Base64.NO_WRAP) else ""
        val newSong = TelegramSavedSong(
            id = key, title = title, artist = artist, album = "", mime = "audio/mpeg",
            path = "", duration = duration, date = System.currentTimeMillis(),
            botUsername = connection.botUsername, chatId = connection.botChatId,
            actionMessageId = result.messageId, actionKind = button.kind.name,
            actionPayload = payload, actionLabel = button.label
        )
        savedSongs.add(newSong)
        if (!writeCache()) { savedSongs.remove(newSong); return }
        rebuildSongs()
        songs.firstOrNull { it.id == "telegram_$key" }?.let(controller::appendTelegramSongToActiveQueue)
        message = "Đã lưu tham chiếu '$title' (${savedSongs.size} bài). Không tải MP3."
    }

    fun action(result: TelegramBotResult, button: TelegramBotAction) {
        if (!connection.linked) return
        when (button.kind) {
            TelegramActionKind.TEXT -> sendText(button.label)
            TelegramActionKind.CALLBACK -> perform {
                val answer = client.request(TdApi.GetCallbackQueryAnswer(
                    connection.botChatId,
                    result.messageId,
                    TdApi.CallbackQueryPayloadData(button.callbackData)
                )) as? TdApi.CallbackQueryAnswer
                if (!answer?.text.isNullOrBlank()) {
                    message = answer?.text
                    val refusal = telegramBotPlaybackRefusal(answer?.text.orEmpty())
                    if (refusal != null && playbackPreparing) {
                        failPlaybackPreparation(refusal)
                        return@perform
                    }
                    if (isMooMusicBot && mooMusicSubscriptionRequired(answer?.text.orEmpty())) {
                        botAccessRequirement = "Bot yêu cầu đăng ký kênh đối tác trước khi gửi âm thanh. " +
                            "Hãy hoàn tất điều kiện trong Telegram và tìm lại bài."
                        if (playbackPreparing) failPlaybackPreparation(botAccessRequirement.orEmpty())
                        else {
                            message = botAccessRequirement
                            cancelPendingPlayback()
                        }
                    }
                }
                if (!answer?.url.isNullOrBlank()) {
                    val reason = "Bot chỉ trả liên kết ngoài, không gửi file âm thanh để phát trong MeloWave."
                    if (playbackPreparing) failPlaybackPreparation(reason)
                    else { cancelPendingPlayback(); message = reason }
                }
            }
            TelegramActionKind.URL -> {
                cancelPendingPlayback()
                val url = button.url
                if ((url.startsWith("https://") || url.startsWith("http://")) && url.length < 2048) {
                    runCatching {
                        appContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }.onFailure { error = "Không thể mở liên kết của bot." }
                } else error = "Liên kết không hỗ trợ."
            }
            TelegramActionKind.UNSUPPORTED -> {
                val reason = "Bot yêu cầu thao tác Telegram đặc biệt; hãy mở Telegram để kiểm tra."
                if (playbackPreparing) failPlaybackPreparation(reason)
                else { cancelPendingPlayback(); message = reason }
            }
        }
    }

    private fun autoRequestDownload(result: TelegramBotResult, button: TelegramBotAction, token: Long) {
        scope.launch {
            // A bot may edit the message before the previous callback finishes.
            // Wait to avoid overlapping callback requests or lost clicks.
            operation?.join()
            if (pendingPlayback?.token != token || !connection.linked) return@launch
            message = "Đang yêu cầu bot tải file nhạc…"
            action(result, button)
        }
    }

    private fun receive(msg: TdApi.Message, fromHistory: Boolean = false) {
        val deferredAudio = pendingDeferredAudio?.takeIf { it.chatId == msg.chatId }
        if (deferredAudio == null && (!connection.linked || msg.chatId != connection.botChatId)) return
        val content = msg.content
        val audio = when (content) {
            is TdApi.MessageAudio -> content.audio?.let { a ->
                TelegramAudioResult(msg.chatId, msg.id, a.audio.id,
                    a.title.ifBlank { a.fileName.substringBeforeLast('.') },
                    a.performer.ifBlank { "Nghệ sĩ chưa biết" },
                    a.fileName, a.mimeType, a.duration.toLong() * 1000L,
                    maxOf(a.audio.size.toLong(), a.audio.expectedSize.toLong()),
                    a.audio.remote?.uniqueId.orEmpty(), a.audio.remote?.id.orEmpty(),
                    a.albumCoverMinithumbnail?.data,
                    a.albumCoverThumbnail?.file?.id ?: 0)
            }
            is TdApi.MessageDocument -> content.document?.let { d ->
                if (isAudioDocument(d.fileName, d.mimeType)) {
                    val (artist, title) = splitArtistTitle(d.fileName)
                    TelegramAudioResult(msg.chatId, msg.id, d.document.id,
                        title, artist, d.fileName, d.mimeType, 0L,
                        maxOf(d.document.size.toLong(), d.document.expectedSize.toLong()),
                        d.document.remote?.uniqueId.orEmpty(), d.document.remote?.id.orEmpty())
                } else null
            }
            else -> null
        }
        val text = when (content) {
            is TdApi.MessageText -> content.text?.text.orEmpty()
            is TdApi.MessageAudio -> content.caption?.text.orEmpty()
            is TdApi.MessageDocument -> content.caption?.text.orEmpty()
            else -> ""
        }.ifBlank { if (audio != null) "${audio.title} — ${audio.artist}" else "" }

        val buttons = when (val markup = msg.replyMarkup) {
            is TdApi.ReplyMarkupInlineKeyboard -> markup.rows.orEmpty().flatMap { row ->
                row.map { button ->
                    when (val type = button.type) {
                        is TdApi.InlineKeyboardButtonTypeCallback -> TelegramBotAction(
                            button.text, TelegramActionKind.CALLBACK, type.data
                        )
                        is TdApi.InlineKeyboardButtonTypeUrl -> TelegramBotAction(
                            button.text, TelegramActionKind.URL, url = type.url
                        )
                        else -> TelegramBotAction(button.text, TelegramActionKind.UNSUPPORTED)
                    }
                }
            }
            is TdApi.ReplyMarkupShowKeyboard -> markup.rows.orEmpty().flatMap { row ->
                row.map { b -> TelegramBotAction(b.text, TelegramActionKind.TEXT) }
            }
            else -> emptyList()
        }.take(42)
        if (text.isBlank() && buttons.isEmpty() && audio == null) return
        // Some bots wrap a 10-30 song keyboard list across more than 1,200 chars.
        val entry = TelegramBotResult(msg.id, text.take(5000), msg.isOutgoing, buttons, audio)
        if (connection.linked && msg.chatId == connection.botChatId) {
            val old = results.indexOfFirst { it.messageId == msg.id }
            if (old >= 0) results[old] = entry else results.add(entry)
            while (results.size > 45) results.removeAt(0)
        }
        if (!fromHistory && !msg.isOutgoing && msg.chatId == connection.botChatId) {
            botReplyRevision++
            recentBotReplies.add(botReplyRevision to entry)
            while (recentBotReplies.size > 80) recentBotReplies.removeAt(0)
        }
        // Stop the Play loading ring as soon as a bot explicitly refuses to
        // send audio. A plain search-result menu is not interpreted as failure.
        if (!fromHistory && !msg.isOutgoing && audio == null &&
            (pendingPlayback != null || playbackPreparing)) {
            val refusal = telegramBotPlaybackRefusal(text)
            if (refusal != null) {
                if (playbackPreparing) failPlaybackPreparation(refusal)
                else { cancelPendingPlayback(); message = refusal; error = refusal }
                return
            }
        }
        if (!fromHistory && !msg.isOutgoing && isMooMusicBot &&
            mooMusicSubscriptionRequired(text)) {
            botAccessRequirement = "Bot yêu cầu đăng ký kênh đối tác trước khi gửi âm thanh. " +
                "Hãy kiểm tra điều kiện trong Telegram hoặc chọn bot khác."
            if (playbackPreparing) failPlaybackPreparation(botAccessRequirement.orEmpty())
            else { message = botAccessRequirement; cancelPendingPlayback() }
            // A subscription prompt is not an album and not an audio response.
            return
        }
        if (!fromHistory && !msg.isOutgoing) captureMetadataProbe(entry, msg.chatId)
        // A Next/Previous button at an album-page boundary may edit the existing
        // bot message instead of posting a new one. Handle both reply patterns.
        // Do not reinterpret the new page itself as a Download-album response.
        if (!fromHistory && !msg.isOutgoing && consumeAlbumPageChange(entry)) return
        if (!fromHistory && !msg.isOutgoing && deferredAudio != null) {
            if (audio != null) {
                deferredAudio.deferred.complete(audio)
                return
            }
            val downloadButton = buttons.firstOrNull { button ->
                (button.kind == TelegramActionKind.CALLBACK || button.kind == TelegramActionKind.TEXT) &&
                        isTelegramDownloadButton(button.label)
            }
            if (downloadButton != null && !deferredAudio.requestedDownload &&
                !TELEGRAM_ALBUM_TRACK_COUNT.containsMatchIn(text)) {
                deferredAudio.requestedDownload = true
                scope.launch { runCatching { performBotAction(msg.chatId, msg.id, downloadButton) } }
            }
            return
        }

        val pending = pendingPlayback
        if (fromHistory || msg.isOutgoing || pending?.chatId != msg.chatId) return
        // Ignore the unchanged original search page: it is not a detail response.
        if (entry.messageId == pending.sourceMessageId &&
            telegramBotPageFingerprint(entry) == pending.sourceFingerprint) return

        if (audio != null) {
            botAccessRequirement = null
            // TDLib gives the real filename, MIME, file size and duration. Store these
            // against the clicked row BEFORE playback clears its pending state.
            pending.choiceKey?.let { key ->
                selectedDetails[key] = TelegramSelectedDetail(count = 1, audio = audio)
                verifiedEntryFileCounts[key] = 1
            }
            cancelPendingPlayback()
            if (pending.saveToDrive) pending.controller.saveTelegramAudioToDrive(audio)
            else saveFromBot(audio, pending.controller, pending.playAfterSave)
            return
        }

        // A Telegram album is demonstrated by two or more REAL selectable track
        // buttons in the response, or an explicit per-selection file count.
        // An artist name, year, word 'album', or lack of bitrate is NEVER evidence.
        // For direct song-picker bots, another list/menu is NOT proof that the
        // selected song is an album. Only an actual audio/document starts playback.
        val albumButtons = if (isMooMusicBot || isLyBot) emptyList()
            else telegramAlbumFileButtons(entry)
        val explicitCount = (TELEGRAM_DETAIL_FILE_COUNT.find(text)
            ?.groupValues?.getOrNull(1) ?: TELEGRAM_DETAIL_FILE_COUNT_SUFFIX.find(text)
            ?.groupValues?.getOrNull(1))?.toIntOrNull()
        val albumTracks = if (albumButtons.size > 1) albumButtons.mapIndexed { index, button ->
            val (artist, title, duration) = songLabelMetadata(button.label)
            val cleaned = title.replace(TELEGRAM_NUMBERED_TRACK_PREFIX, "").trim()
            TelegramUnifiedTrack("album_${entry.messageId}_$index", entry,
                button = button, title = cleaned, artist = artist, durationMs = duration)
        } else emptyList()
        val confirmedCount = maxOf(explicitCount ?: 0, albumTracks.size)
        pending.choiceKey?.let { key ->
            if (confirmedCount > 0) verifiedEntryFileCounts[key] = confirmedCount
            selectedDetails[key] = TelegramSelectedDetail(
                count = confirmedCount.takeIf { it > 0 }, tracks = albumTracks,
                technicalText = telegramBotTechnicalFields(text))
        }
        if (confirmedCount > 1) {
            cancelPendingPlayback()
            message = "Album có $confirmedCount bài. Chọn từng bài trong kết quả tìm kiếm."
            return
        }
        // A single track-info card may offer a Download button (the actual
        // audio/size/quality are only authoritative once the bot sends a file).
        if (!pending.downloadRequested) {
            val downloadButton = buttons.firstOrNull { button ->
                (button.kind == TelegramActionKind.CALLBACK || button.kind == TelegramActionKind.TEXT) &&
                        isTelegramDownloadButton(button.label)
            }
            if (downloadButton != null) {
                pending.downloadRequested = true
                autoRequestDownload(entry, downloadButton, pending.token)
            }
        }
    }

    private fun matchesSavedAudio(saved: TelegramSavedSong, item: TelegramAudioResult): Boolean {
        if (saved.id == "${item.chatId}_${item.messageId}") return true
        if (item.uniqueFileId.isNotBlank() && item.uniqueFileId == saved.uniqueFileId) return true
        // Older saved tracks lack a remote unique ID. Only match by metadata
        // when the actual downloaded file size agrees, avoiding false merges.
        return saved.uniqueFileId.isBlank() && item.size > 0L &&
                saved.title.equals(item.title, ignoreCase = true) &&
                saved.artist.equals(item.artist, ignoreCase = true) &&
                saved.duration == item.durationMs && File(saved.path).length() == item.size
    }

    fun isAudioSaved(item: TelegramAudioResult): Boolean = savedSongs.any { matchesSavedAudio(it, item) }

    /** Lightweight add: save a real Telegram message reference, never the MP3 bytes. */
    fun addFromBot(item: TelegramAudioResult, controller: MusicController) {
        if (!connection.linked || item.chatId != connection.botChatId) {
            error = "Bài hát không thuộc bot hiện tại."
            return
        }
        val existing = savedSongs.firstOrNull { matchesSavedAudio(it, item) }
        if (existing != null) {
            message = "Đã có '${existing.title}' trong danh sách Telegram."
            return
        }
        val id = "${item.chatId}_${item.messageId}"
        val entry = TelegramSavedSong(
            id = id, title = item.title, artist = item.artist, album = "",
            mime = item.mimeType, path = "", duration = item.durationMs,
            date = System.currentTimeMillis(), uniqueFileId = item.uniqueFileId,
            botUsername = connection.botUsername, chatId = item.chatId,
            messageId = item.messageId, fileName = item.fileName,
            remoteFileId = item.remoteFileId, sizeBytes = item.size
        )
        savedSongs.add(entry)
        if (!writeCache()) { savedSongs.remove(entry); return }
        rebuildSongs()
        songs.firstOrNull { it.id == "telegram_$id" }?.let(controller::appendTelegramSongToActiveQueue)
        message = "Đã thêm '${item.title}' (${savedSongs.size} bài). Chưa tải MP3."
    }

    private fun clearSearchPlayOrder() {
        stopPlaybackPreparation()
        queuedAlbumResolveGeneration++
        queuedAlbumResolveJob?.cancel()
        queuedAlbumResolveJob = null
        searchQueueKeyword = ""
        searchPlayOrder = emptyList()
        searchPlayIndex = -1
        searchPlaySongId = null
        autoPlaySearchResults = false
        albumPageChange = null
        albumPageToken++
    }

    /** A bot may paginate its album in the same edited message or a new reply.
     * Start the next page only if it exposes a real track list or audio item.
     */
    private fun consumeAlbumPageChange(entry: TelegramBotResult): Boolean {
        val change = albumPageChange ?: return false
        if (!connection.linked || connection.botChatId != change.botChatId) {
            albumPageChange = null
            return false
        }
        if (entry.messageId == change.sourceMessageId &&
            telegramBotPageFingerprint(entry) == change.oldFingerprint) return false
        val pageTracks = telegramUnifiedTracks(listOf(entry), verifiedEntryFileCounts,
            isVkMusicBot, isMusgetBot, isMooMusicBot, isLyBot)
        if (pageTracks.isEmpty()) return false
        albumPageChange = null
        searchPlayOrder = pageTracks
        val selected = if (change.direction > 0) 0 else pageTracks.lastIndex
        searchPlayIndex = selected
        searchPlaySongId = null
        message = "Đã mở trang album ${if (change.direction > 0) "tiếp theo" else "trước đó"}; đang lấy bài hát…"
        scope.launch {
            // The callback can still be in flight when Telegram edits its message.
            // Avoid dropping selectResult() merely because manager.busy is true.
            operation?.join()
            if (albumPageToken != change.token || !connection.linked ||
                connection.botChatId != change.botChatId) return@launch
            launchSearchResult(pageTracks[selected], change.controller)
        }
        return true
    }

    private fun turnAlbumPage(controller: MusicController, direction: Int): Boolean {
        val displayedMessageIds = searchPlayOrder.map { it.result.messageId }.toSet()
        val candidate = results.asReversed().firstNotNullOfOrNull { result ->
            if (result.outgoing || result.messageId !in displayedMessageIds) null else {
                result.buttons.firstOrNull { isTelegramAlbumPageButton(it, direction) }
                    ?.let { result to it }
            }
        }
        if (candidate == null) {
            message = if (direction > 0)
                "Đã hết ${searchPlayOrder.size} bài ở trang album này; bot không cung cấp nút trang tiếp."
            else "Đang ở bài đầu trang album; bot không cung cấp nút trang trước."
            return false
        }
        val (result, button) = candidate
        albumPageToken++
        val token = albumPageToken
        albumPageChange = TelegramAlbumPageChange(token, connection.botChatId,
            result.messageId, telegramBotPageFingerprint(result), direction, controller)
        message = if (direction > 0) "Đang yêu cầu trang album tiếp theo…"
        else "Đang yêu cầu trang album trước…"
        // A page-boundary Next is a user skip too: show the spinner now.
        beginPlaybackPreparation("Trang album ${if (direction > 0) "tiếp theo" else "trước"}", controller)
        action(result, button)
        scope.launch {
            delay(20_000L)
            if (albumPageChange?.token == token) {
                albumPageChange = null
                val reason = "Bot chưa trả về trang album mới. Hãy mở album và chọn trang thủ công."
                if (playbackPreparing) failPlaybackPreparation(reason) else message = reason
            }
        }
        return true
    }

    /** Play one selected result. Manual Next/Previous can still move through visible results,
     * but natural track end will not silently start the next song.
     */
    fun playSearchResult(entry: TelegramUnifiedTrack, controller: MusicController) {
        cancelSearchMetadataPrefetch()
        if (entry.isAlbum && entry.button != null) {
            // Request the actual track list as the Telegram screen does.
            // Keep response association so the search view can expand that album.
            selectResult(entry.result, entry.button, controller,
                resolveSearchChoice = true)
            return
        }
        val visible = results.filter { !it.outgoing }.takeLast(40)
        val parsedOrder = telegramUnifiedTracks(visible, verifiedEntryFileCounts,
            isVkMusicBot, isMusgetBot, isMooMusicBot, isLyBot,
            dialogSearchKeyword.ifBlank { lastUnifiedSearchKeyword })
        val rawOrder = if (dialogSearchPageSnapshot.any {
                it.stableKey == entry.stableKey }) dialogSearchPageSnapshot else parsedOrder
        // Manual Next follows only the same tracks shown to the listener. Never
        // step into hidden, unrelated results from a broad third-party bot.
        val order = telegramFilterSearchResults(rawOrder,
            dialogSearchKeyword.ifBlank { lastUnifiedSearchKeyword },
            isNumberedMusicBot || entry.isNumericReplyChoice)
        val selected = order.indexOfFirst { it.stableKey == entry.stableKey }
        if (selected < 0) {
            message = "Danh sách vừa thay đổi. Hãy nhấn lại bài muốn phát."
            return
        }
        // The user explicitly selected a track; cancel any stale auto-page request.
        albumPageChange = null
        albumPageToken++
        searchPlayOrder = order
        searchPlayIndex = selected
        searchPlaySongId = null
        autoPlaySearchResults = false
        searchQueueKeyword = dialogSearchKeyword.ifBlank { lastUnifiedSearchKeyword }
        // A queued item does not become active until the bot returns a real audio file.
        launchSearchResult(order[selected], controller)
    }

    /** The bot may expose a list of callback buttons without sending audio yet.
     * Expose that selected search playlist to the Now Playing queue UI, while
     * Media3 resolves and streams only the currently requested audio file.
     * These are NOT permanent saved songs and are never bulk-downloaded.
     */
    fun playerSearchTracks(current: SongItem?): List<TelegramUnifiedTrack> =
        if (current?.source == SongSource.TELEGRAM &&
            current.id == searchPlaySongId && searchPlayIndex in searchPlayOrder.indices)
            searchPlayOrder else emptyList()

    fun playerSearchIndex(current: SongItem?): Int =
        if (playerSearchTracks(current).isNotEmpty()) searchPlayIndex else -1

    fun playPlayerSearchTrack(index: Int, controller: MusicController) {
        if (playerSearchTracks(controller.currentSong).isEmpty() ||
            index !in searchPlayOrder.indices) return
        if (pendingPlayback != null || downloading || busy || waitingForAudio != null ||
            driveUploading || queuedAlbumResolveJob?.isActive == true) {
            message = "Telegram đang chuẩn bị bài hát, vui lòng đợi."
            return
        }
        if (index == searchPlayIndex && controller.isPlaying) return
        searchPlayIndex = index
        controller.pauseForSystemMediaControls()
        launchSearchResult(searchPlayOrder[index], controller)
    }

    /** Play the accumulating Telegram library in saved order, without requesting
     * a bulk bot download or altering the saved references.
     */
    fun playSavedSongs(controller: MusicController) {
        val saved = songs.toList()
        if (saved.isEmpty()) {
            message = "Danh sách Telegram chưa có bài nào được lưu."
            return
        }
        clearSearchPlayOrder()
        controller.playCollection(saved, 0)
        message = "Đang phát ${saved.size} bài trong danh sách Telegram đã lưu."
    }

    /** UI uses the same gate as the playback method so a failed request cannot
     * silently close the selection dialog while another bot action is pending. */
    val canStartSearchPlaylist: Boolean
        get() = connection.linked && authStep == TelegramAuthStep.READY &&
                !busy && !downloading && !driveUploading && !searchSaveRunning &&
                waitingForAudio == null && pendingPlayback == null &&
                 !(isMooMusicBot && botAccessRequirement != null)

    /** Play every result or an explicitly selected subset in the visible order.
     * Files are requested one at a time from the bot: this does NOT download an album.
     */
    fun playSearchPlaylist(playlist: List<TelegramUnifiedTrack>, controller: MusicController,
                           keyword: String = "") {
        if (!connection.linked || authStep != TelegramAuthStep.READY) {
            message = "Hãy kết nối bot Telegram trước khi phát danh sách."
            return
        }
        if (isMooMusicBot && botAccessRequirement != null) {
            message = botAccessRequirement
            return
        }
        if (!canStartSearchPlaylist) {
            message = "Telegram đang xử lý bài hát; hãy thử lại sau khi hoàn thành."
            return
        }
        val ordered = playlist.distinctBy { entry ->
            // An album menu may reuse the same callback ID as another album.
            // The album identity is therefore part of the queue identity.
            "${entry.albumParent?.stableKey.orEmpty()}|${entry.stableKey}"
        }.filter { !it.isAlbum && (it.audio != null || it.button != null) }
        if (ordered.isEmpty()) {
            message = "Không có bài hát nào được chọn."
            return
        }
        // Only an explicit Play action replaces the current bot listening queue.
        // Never download an entire album or change the user's saved Telegram list.
        cancelSearchMetadataPrefetch()
        clearSearchPlayOrder()
        searchQueueKeyword = keyword.trim().take(120).ifBlank { lastUnifiedSearchKeyword }
        searchPlayOrder = ordered
        searchPlayIndex = 0
        autoPlaySearchResults = true
        message = "Đang phát danh sách ${ordered.size} bài theo thứ tự…"
        launchSearchResult(ordered.first(), controller)
    }

    /** Called only when ExoPlayer naturally reaches the end of the current Telegram song.
     * For manual single-track playback there is no automatic advance.
     */
    fun onSearchTrackEnded(controller: MusicController): Boolean {
        // A tapped bot result is a single-track play: don't fall back to an
        // unrelated saved Telegram queue just because that file was saved before.
        if (controller.currentSong?.id != searchPlaySongId ||
            searchPlayIndex !in searchPlayOrder.indices) return false
        if (!autoPlaySearchResults) return true
        return navigateSearchResult(controller, 1)
    }

    private fun launchSearchResult(entry: TelegramUnifiedTrack, controller: MusicController) {
        queuedAlbumResolveGeneration++
        queuedAlbumResolveJob?.cancel()
        queuedAlbumResolveJob = null
        // Synchronous state change: the Play spinner begins on Next/Previous,
        // not several seconds later when the bot finally sends the MP3.
        beginPlaybackPreparation(entry.title, controller)
        val knownAudio = entry.audio ?: selectionKey(entry)?.let { selectedDetails[it]?.audio }
        if (knownAudio != null) {
            playFromBot(knownAudio, controller)
            return
        }
        val parent = entry.albumParent
        if (parent == null) {
            if (entry.button == null) {
                failPlaybackPreparation("Bot chưa cung cấp file hoặc nút nghe bài này.")
                return
            }
            if ((isMooMusicBot || isLyBot ||
                    (entry.isDirectMusicBotChoice && entry.button.kind == TelegramActionKind.CALLBACK)) &&
                searchPlaySongId != null && searchQueueKeyword.isNotBlank()) {
                // LyBot may replace its inline keyboard after selection. Use the
                // exact choice from the newest result page, not stale callback bytes.
                val generation = queuedAlbumResolveGeneration
                queuedAlbumResolveJob = scope.launch {
                    val refreshed = refreshSearchChoice(searchQueueKeyword, entry)
                    if (generation != queuedAlbumResolveGeneration) return@launch
                    if (refreshed == null || refreshed.button == null) {
                        failPlaybackPreparation("Bot đã thay đổi danh sách. Hãy tìm lại bài muốn phát.")
                        return@launch
                    }
                    selectResult(refreshed.result, refreshed.button, controller,
                        playAfterSave = true, resolveSearchChoice = true)
                }
            } else {
                selectResult(entry.result, entry.button, controller,
                    playAfterSave = true, resolveSearchChoice = true)
            }
            return
        }
        // Different albums have different, mutable inline keyboards. Always
        // re-search, click the original album, then use the CURRENT track button.
        val generation = queuedAlbumResolveGeneration
        queuedAlbumResolveJob = scope.launch {
            message = "Đang mở album '${parent.title}' để phát '${entry.title}'…"
            try {
                val freshParent = refreshSearchChoice(searchQueueKeyword, parent)
                if (generation != queuedAlbumResolveGeneration) return@launch
                if (freshParent == null) {
                    failPlaybackPreparation("Bot chưa mở lại được album '${parent.title}'.")
                    return@launch
                }
                val detail = probeMetadata(freshParent, selectionKey(parent), force = true)
                if (generation != queuedAlbumResolveGeneration) return@launch
                val currentTracks = detail?.tracks.orEmpty()
                val matching = currentTracks.firstOrNull { isMatchingTelegramTrack(it, entry) }
                    ?: currentTracks.getOrNull(entry.albumTrackPosition)
                        ?.takeIf { telegramSongMatchKey(it.title) == telegramSongMatchKey(entry.title) }
                val button = matching?.button
                if (button == null) {
                    failPlaybackPreparation("Không tìm thấy '${entry.title}' trong menu album hiện tại.")
                    return@launch
                }
                // TDLib sends the audio file only for the track actually played.
                // No bulk album transfer or automatic save-to-device is requested.
                selectResult(matching.result, button, controller,
                    playAfterSave = true, resolveSearchChoice = true)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                if (generation == queuedAlbumResolveGeneration)
                    failPlaybackPreparation("Chưa thể mở bài '${entry.title}' từ album.")
            }
        }
    }

    /** True means the Next/Previous action belongs to a bot-search result sequence.
     * The Media3 fallback is used for normal saved Telegram library queues.
     */
    fun navigateSearchResult(controller: MusicController, direction: Int): Boolean {
        val song = controller.currentSong ?: return false
        if (song.source != SongSource.TELEGRAM || song.id != searchPlaySongId ||
            searchPlayIndex !in searchPlayOrder.indices) return false
        if (pendingPlayback != null || downloading || busy || waitingForAudio != null ||
            queuedAlbumResolveJob?.isActive == true) {
            message = "Bot đang chuẩn bị bài hát, vui lòng đợi."
            return true
        }
        if (albumPageChange != null) {
            message = "Đang chuyển trang album trên bot Telegram…"
            return true
        }
        val nextIndex = searchPlayIndex + direction
        if (nextIndex !in searchPlayOrder.indices) {
            // A user-selected list is fixed: never append unselected tracks
            // from the bot's next album page. Manual single-result navigation
            // may still follow the bot's explicit pagination controls.
            if (!autoPlaySearchResults && turnAlbumPage(controller, direction)) {
                controller.pauseForSystemMediaControls()
                return true
            }
            if (controller.repeatSetting == RepeatSetting.ALL && searchPlayOrder.size > 1) {
                val wrapped = if (direction > 0) 0 else searchPlayOrder.lastIndex
                searchPlayIndex = wrapped
                controller.pauseForSystemMediaControls()
                launchSearchResult(searchPlayOrder[wrapped], controller)
            }
            return true
        }
        searchPlayIndex = nextIndex
        controller.pauseForSystemMediaControls()
        launchSearchResult(searchPlayOrder[nextIndex], controller)
        return true
    }

    /** A direct Play streams TDLib partial ranges; no full-file download or permanent copy. */
    fun playFromBot(item: TelegramAudioResult, controller: MusicController) {
        if (!playbackPreparing) beginPlaybackPreparation(item.title, controller)
        saveFromBot(item, controller, playAfterSave = true)
    }

    private fun saveFromBot(item: TelegramAudioResult, controller: MusicController, playAfterSave: Boolean) {
        cancelPendingPlayback()
        if (download?.isActive == true) {
            if (playbackPreparing) failPlaybackPreparation("Đang chuẩn bị bài trước đó; hãy thử lại.")
            else message = "Đang chuẩn bị bài trước đó."
            return
        }
        download = scope.launch {
            downloading = true
            error = null
            try {
                val existing = savedSongs.firstOrNull { matchesSavedAudio(it, item) }
                if (!playAfterSave) {
                    addFromBot(item, controller)
                    return@launch
                }
                val song = existing?.let { original ->
                    val key = "telegram_${original.id}"
                    songs.firstOrNull { it.id == key }
                } ?: SongItem(
                    "telegram_temp_${item.chatId}_${item.messageId}", item.title, item.artist,
                    "", "Telegram / Trực tuyến", TelegramStreamDataSource.uriForFile(item.tdFileId), item.durationMs,
                    SongSource.TELEGRAM, item.mimeType, genre = "Telegram",
                    dateAddedMs = System.currentTimeMillis()
                )
                // Preserve source metadata for the player even while Media3 streams via a virtual URI.
                // Keep only a bounded set of recent temporary references; no audio bytes are kept here.
                if (activeStreamInfo.size > 40) activeStreamInfo.clear()
                activeStreamInfo[song.id] = item
                rememberTelegramArtwork(song.id, item)
                controller.ensureArtwork(song)
                controller.noteTelegramAudioMetadata(song.id,
                    telegramTechnicalInfo(item.fileName, item.mimeType, item.size, item.durationMs))
                // PLAY must not silently grow the permanent playlist.
                expectTelegramMediaReady(song.id)
                controller.playSong(song, if (existing != null) songs.toList() else listOf(song))
                if (controller.currentSong?.id != song.id) {
                    failPlaybackPreparation("Không thể mở trình phát cho '${item.title}'. Hãy kiểm tra quyền sử dụng.")
                    return@launch
                }
                if (searchPlayIndex in searchPlayOrder.indices) searchPlaySongId = song.id
                message = "Đang phát trực tuyến '${item.title}'. Cache Telegram được dọn tự động (mục tiêu 100 MB)."
            } catch (cancel: CancellationException) { throw cancel }
            catch (failure: Throwable) {
                val reason = failure.message ?: "Không lấy được file nhạc Telegram."
                if (playbackPreparing) failPlaybackPreparation(reason) else error = reason
            }
            finally { downloading = false }
        }
    }

    /**
     * Backing up to Drive must share the SAME per-file download gate used by
     * TelegramStreamDataSource. TDLib permits only one active downloadFile
     * request with a particular offset/limit per file. Otherwise a 512-KB
     * streaming seek cancels the synchronous full-file backup (error 200).
     *
     * The lock is held only while Telegram is acquiring the source file; it
     * is released before the separate Google Drive upload begins.
     */
    private suspend fun downloadTelegramFile(item: TelegramAudioResult): File = withContext(Dispatchers.IO) {
        val gate = streamDownloadLocks.computeIfAbsent(item.tdFileId) { Mutex() }
        gate.lock()
        try {
            fun completedFile(file: TdApi.File?): File? {
                if (file?.local?.isDownloadingCompleted != true) return null
                val path = file.local?.path.orEmpty()
                if (path.isBlank()) return null
                val local = File(path)
                if (!local.isFile || local.length() <= 0L) return null
                if (item.size > 0L && local.length() < item.size) return null
                return local
            }
            // When the player already downloaded this audio, no new TDLib
            // download request is needed (and the player remains uninterrupted).
            completedFile(client.request(TdApi.GetFile(item.tdFileId)) as? TdApi.File)?.let {
                return@withContext it
            }
            var lastFailure: Throwable? = null
            repeat(2) { attempt ->
                try {
                    val result = client.request(
                        TdApi.DownloadFile(item.tdFileId, 24, 0L, 0L, true)
                    ) as? TdApi.File
                    completedFile(result)?.let { return@withContext it }
                    lastFailure = IOException("Telegram chưa xác nhận tải xong file nhạc.")
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (failure: Throwable) {
                    // With synchronous=true TDLib can report a canceled request
                    // even when the bytes have already reached 100%. Check the
                    // authoritative file state before deciding backup failed.
                    val finalState = runCatching {
                        client.request(TdApi.GetFile(item.tdFileId)) as? TdApi.File
                    }.getOrNull()
                    completedFile(finalState)?.let { return@withContext it }
                    lastFailure = failure
                }
                if (attempt == 0) delay(300L)
            }
            throw IOException(
                "Không thể tải đủ file Telegram: ${lastFailure?.message.orEmpty()}",
                lastFailure
            )
        } finally {
            gate.unlock()
        }
    }

    private suspend fun performBotAction(chatId: Long, messageId: Long, action: TelegramBotAction) {
        when (action.kind) {
            TelegramActionKind.CALLBACK -> client.request(TdApi.GetCallbackQueryAnswer(
                chatId, messageId, TdApi.CallbackQueryPayloadData(action.callbackData)))
            TelegramActionKind.TEXT -> {
                val request = TdApi.SendMessage().apply {
                    this.chatId = chatId
                    inputMessageContent = TdApi.InputMessageText().apply {
                        text = TdApi.FormattedText().apply {
                            text = action.label
                            entities = emptyArray()
                        }
                    }
                }
                client.request(request)
            }
            else -> throw IOException("Bot không cho phép phát bài qua nút này.")
        }
    }

    /** Called only from Media3 loader threads. Do not invoke from Compose/UI callbacks. */
    internal fun getTelegramStreamSizeBlocking(fileId: Int): Long = runBlocking(Dispatchers.IO) {
        withTimeout(20_000L) {
            val file = client.request(TdApi.GetFile(fileId)) as? TdApi.File
                ?: throw IOException("Không tìm thấy file âm thanh Telegram")
            file.size.toLong().takeIf { it > 0L } ?: file.expectedSize.toLong()
        }
    }

    internal data class StreamRange(val path: String, val availableBytes: Long)

    /** Synchronously fetch ONLY the requested segment, respecting the TDLib partial-file prefix. */
    internal fun downloadTelegramStreamRangeBlocking(fileId: Int, position: Long, wanted: Int): StreamRange =
        runBlocking(Dispatchers.IO) {
            withTimeout(60_000L) {
                // TDLib cancels an older range when a different range is requested
                // for the same file. Serialize seek/prefetch readers per file ID.
                val gate = streamDownloadLocks.computeIfAbsent(fileId) { Mutex() }
                gate.lock()
                try {
                    suspend fun availableCachedRange(): StreamRange? {
                        val state = client.request(TdApi.GetFile(fileId)) as? TdApi.File ?: return null
                        val path = state.local?.path.orEmpty()
                        if (path.isBlank()) return null
                        val localFile = File(path)
                        if (!localFile.isFile) return null
                        val bytes = if (state.local?.isDownloadingCompleted == true) {
                            minOf(wanted.toLong(), localFile.length() - position)
                        } else {
                            val prefix = client.request(TdApi.GetFileDownloadedPrefixSize(fileId, position))
                                    as? TdApi.FileDownloadedPrefixSize
                            minOf(wanted.toLong(), prefix?.size ?: 0L, localFile.length() - position)
                        }
                        return bytes.takeIf { it > 0L }?.let { StreamRange(path, it) }
                    }
                    // Reuse file bytes TDLib already has. In particular, do not
                    // issue a partial DownloadFile after Drive downloaded 100%.
                    availableCachedRange()?.let { return@withTimeout it }
                    var lastError: Throwable? = null
                    repeat(3) {
                        try {
                            client.request(TdApi.DownloadFile(fileId, 31, position, wanted.toLong(), true))
                            availableCachedRange()?.let { return@withTimeout it }
                            lastError = IOException("Dữ liệu Telegram chưa sẵn sàng tại vị trí $position")
                        } catch (cancel: CancellationException) { throw cancel }
                        catch (e: Throwable) {
                            // A canceled request can still leave a valid chunk.
                            availableCachedRange()?.let { return@withTimeout it }
                            lastError = e
                        }
                        delay(250L)
                    }
                    throw IOException("Không tải được đoạn nhạc Telegram: ${lastError?.message.orEmpty()}", lastError)
                } finally { gate.unlock() }
            }
        }

    internal fun telegramStreamOpened(fileId: Int) {
        if (fileId <= 0) return
        // Atomic compute avoids a race where close() removes a count incremented by open().
        openStreamCounts.compute(fileId) { _, previous ->
            (previous ?: AtomicInteger()).apply { incrementAndGet() }
        }
        scope.launch { trackTemporaryTelegramAudio(fileId) }
    }

    internal fun telegramStreamClosed(fileId: Int) {
        if (fileId <= 0) return
        openStreamCounts.computeIfPresent(fileId) { _, previous ->
            if (previous.decrementAndGet() <= 0) null else previous
        }
        scope.launch {
            trackTemporaryTelegramAudio(fileId)
            // A short delay protects rapid Next/Previous, then evicts the OLD audio even
            // below 100 MB. An active reader or an in-flight Drive upload always wins.
            scheduleTelegramCacheTrim()
        }
    }

    /**
     * Evict only temporary audio owned by MeloWave after its Media3 reader closes.
     * A second Next/Previous does NOT cancel a cleanup already in progress.
     * TDLib may still report a canceled partial download as active for a short time:
     * retry these files rather than silently abandoning them until the next switch.
     * Never touch cover art, saved-list metadata, or user-owned offline copies.
     */
    private fun scheduleTelegramCacheTrim() {
        cacheCleanupRequested = true
        if (cleanupJob?.isActive == true) return
        cleanupJob = scope.launch {
            var retryRounds = 0
            try {
                while (cacheCleanupRequested) {
                    cacheCleanupRequested = false
                    delay(if (retryRounds == 0) telegramCacheTrimDelayMs else 1_500L)
                    var retryPending = false
                    // Track list and saved metadata are owned by the Main coroutine.
                    for (id in downloadedStreamIds.toList()) {
                        // A playing file or Drive upload remains tracked until its close callback.
                        if ((openStreamCounts[id]?.get() ?: 0) > 0 ||
                            (driveUploading && driveSourceFileId == id) ||
                            offlineSavingFileIds.contains(id)) continue
                        val gate = streamDownloadLocks.computeIfAbsent(id) { Mutex() }
                        gate.lock()
                        try {
                            // Re-check after taking the same gate used by range downloads.
                            if ((openStreamCounts[id]?.get() ?: 0) > 0 ||
                                (driveUploading && driveSourceFileId == id) ||
                                offlineSavingFileIds.contains(id)) continue
                            val file = try {
                                client.request(TdApi.GetFile(id)) as? TdApi.File
                            } catch (cancel: CancellationException) {
                                throw cancel
                            } catch (e: Exception) {
                                android.util.Log.w("MeloWaveTgCache", "GetFile failed for $id; will retry", e)
                                retryPending = true
                                continue
                            }
                            if (file == null) { retryPending = true; continue }
                            val path = file.local?.path.orEmpty()
                            val isOffline = path.isNotBlank() && savedSongs.any { saved ->
                                saved.path.isNotBlank() &&
                                        File(saved.path).absolutePath == File(path).absolutePath
                            }
                            if (isOffline) {
                                downloadedStreamIds.remove(id) // User-owned local copy: never delete it.
                                continue
                            }
                            if (path.isBlank() || !File(path).isFile) {
                                downloadedStreamIds.remove(id) // Nothing left to evict.
                                continue
                            }
                            var local = file.local ?: continue
                            if (local.isDownloadingActive) {
                                // Cancel any TDLib prefetch, not the active Media3 reader.
                                try {
                                    client.request(TdApi.CancelDownloadFile(id, false))
                                } catch (cancel: CancellationException) {
                                    throw cancel
                                } catch (e: Exception) {
                                    android.util.Log.w("MeloWaveTgCache", "CancelDownloadFile failed for $id", e)
                                    retryPending = true
                                    continue
                                }
                                // TDLib state changes asynchronously after CancelDownloadFile.
                                // Wait for the next pass if it still reports an active download.
                                val refreshed = try {
                                    (client.request(TdApi.GetFile(id)) as? TdApi.File)?.local
                                } catch (cancel: CancellationException) {
                                    throw cancel
                                } catch (e: Exception) {
                                    retryPending = true
                                    continue
                                }
                                if (refreshed == null) { retryPending = true; continue }
                                local = refreshed
                            }
                            if (!TelegramStreamCachePolicy.canDelete(
                                    fileId = id,
                                    activeReaders = openStreamCounts[id]?.get() ?: 0,
                                    driveUploading = driveUploading,
                                    driveSourceFileId = driveSourceFileId,
                                    downloadingInTdlib = local.isDownloadingActive,
                                    ownedOfflineCopy = isOffline,
                                    canBeDeleted = local.canBeDeleted
                                )) {
                                if (local.isDownloadingActive || !local.canBeDeleted) {
                                    retryPending = true
                                    android.util.Log.d("MeloWaveTgCache",
                                        "Waiting for TDLib fileId=$id active=${local.isDownloadingActive} deletable=${local.canBeDeleted}")
                                }
                                continue
                            }
                            // TDLib alone owns its media directory; never File.delete() it directly.
                            try {
                                client.request(TdApi.DeleteFile(id))
                                downloadedStreamIds.remove(id)
                                android.util.Log.d("MeloWaveTgCache", "Removed closed audio fileId=$id")
                            } catch (cancel: CancellationException) {
                                throw cancel
                            } catch (e: Exception) {
                                retryPending = true
                                android.util.Log.w("MeloWaveTgCache", "DeleteFile failed for $id; will retry", e)
                            }
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (e: Exception) {
                            // One corrupt/stale item must not prevent cleanup of other songs.
                            retryPending = true
                            android.util.Log.w("MeloWaveTgCache", "Cleanup failed for $id; will retry", e)
                        } finally {
                            gate.unlock()
                        }
                    }
                    // Prevent an endless loop when TDLib refuses deletion; retain the ID
                    // for the next close/login so no other data is touched to force space.
                    if (cacheCleanupRequested) retryRounds = 0
                    else if (retryPending && retryRounds < 3) {
                        retryRounds++
                        cacheCleanupRequested = true
                    }
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } finally {
                persistTrackedTelegramAudio()
                cleanupJob = null
            }
        }
    }

    /** Used by the library's playback pipeline. All tracks retain references; audio is fetched only on Play. */
    suspend fun resolveStreamUri(song: SongItem): Uri {
        if (song.id.startsWith("telegram_temp_")) {
            if (song.uri.scheme == TelegramStreamDataSource.SCHEME) return song.uri
            val f = song.uri.path?.let(::File)
            if (f?.isFile == true && f.length() > 0L) return Uri.fromFile(f)
        }
        val stored = withContext(Dispatchers.Main) {
            savedSongs.firstOrNull { "telegram_${it.id}" == song.id }
        } ?: throw IOException("Không tìm thấy bài Telegram đã lưu.")
        val old = File(stored.path)
        if (stored.path.isNotBlank() && old.isFile && old.length() > 0L) return Uri.fromFile(old)
        if (stored.chatId == 0L) throw IOException("Bài này thuộc phiên bản cũ, không có tham chiếu. Hãy tìm và thêm lại từ bot.")
        val actual: TelegramAudioResult = if (stored.messageId != 0L) {
            val msg = client.request(TdApi.GetMessage(stored.chatId, stored.messageId)) as? TdApi.Message
                ?: throw IOException("Tin nhắn gốc của bot không còn khả dụng. Hãy tìm lại bài.")
            extractTelegramAudio(msg) ?: throw IOException("Bot không còn cung cấp file tại tin nhắn này.")
        } else {
            if (stored.actionMessageId == 0L || stored.actionKind.isBlank()) {
                throw IOException("Không có tham chiếu phát nhạc, hãy tìm lại bài từ bot.")
            }
            val request = TelegramDeferredAudio(stored.chatId, CompletableDeferred())
            withContext(Dispatchers.Main) {
                if (pendingDeferredAudio != null) throw IOException("Đang yêu cầu một bài Telegram khác, vui lòng đợi.")
                pendingDeferredAudio = request
                waitingForAudio = stored.title
            }
            try {
                val button = TelegramBotAction(stored.actionLabel,
                    TelegramActionKind.valueOf(stored.actionKind),
                    callbackData = if (stored.actionPayload.isNotBlank())
                        Base64.decode(stored.actionPayload, Base64.DEFAULT) else byteArrayOf())
                performBotAction(stored.chatId, stored.actionMessageId, button)
                withTimeout(75_000L) { request.deferred.await() }
            } catch (err: Throwable) {
                throw IOException("Không phát lại được liên kết bot. Hãy tìm bài và chọn lại. ${err.message.orEmpty()}", err)
            } finally {
                withContext(Dispatchers.Main) {
                    if (pendingDeferredAudio === request) pendingDeferredAudio = null
                    waitingForAudio = null
                }
            }
        }
        // Recover real file metadata for songs added earlier using only a bot button reference.
        activeStreamInfo[song.id] = actual
        rememberTelegramArtwork(song.id, actual)
        withContext(Dispatchers.Main) {
            val idx = savedSongs.indexOfFirst { it.id == stored.id }
            if (idx >= 0 && (savedSongs[idx].sizeBytes <= 0L || savedSongs[idx].fileName.isBlank())) {
                savedSongs[idx] = savedSongs[idx].copy(
                    fileName = actual.fileName, mime = actual.mimeType,
                    duration = actual.durationMs.takeIf { it > 0L } ?: savedSongs[idx].duration,
                    sizeBytes = actual.size)
                writeCache()
            }
        }
        // A virtual URI keeps the saved playlist intact and streams only the requested bytes.
        return TelegramStreamDataSource.uriForFile(actual.tdFileId)
    }

    private fun extractTelegramAudio(msg: TdApi.Message): TelegramAudioResult? = when (val content = msg.content) {
        is TdApi.MessageAudio -> content.audio?.let { a ->
            TelegramAudioResult(msg.chatId, msg.id, a.audio.id,
                a.title.ifBlank { a.fileName.substringBeforeLast('.') },
                a.performer.ifBlank { "Nghệ sĩ chưa biết" }, a.fileName,
                a.mimeType, a.duration.toLong() * 1000L,
                a.audio.size, a.audio.remote?.uniqueId.orEmpty(), a.audio.remote?.id.orEmpty(),
                a.albumCoverMinithumbnail?.data,
                a.albumCoverThumbnail?.file?.id ?: 0)
        }
        is TdApi.MessageDocument -> content.document?.let { d ->
            if (isAudioDocument(d.fileName, d.mimeType)) {
                val (artist, title) = splitArtistTitle(d.fileName)
                TelegramAudioResult(msg.chatId, msg.id, d.document.id, title, artist,
                    d.fileName, d.mimeType, 0L, d.document.size,
                    d.document.remote?.uniqueId.orEmpty(), d.document.remote?.id.orEmpty())
            } else null
        }
        else -> null
    }


    /** Explicit user download of one Telegram audio into the public Downloads/Telegram folder.
     * The original audio stays available outside MeloWave, even after TDLib cache cleanup.
     * TDLib owns its private files: acquire them using its API, copy them using MediaStore,
     * and then release the protection so the private temporary copy can be trimmed.
     */
    suspend fun downloadSongToDownloads(song: SongItem): String {
        require(song.source == SongSource.TELEGRAM) { "Không phải bài hát Telegram." }
        // Saved references may require the bot to resend its file. Resolving them
        // is the SAME operation used by playback and does not change the playlist.
        val resolvedUri = resolveStreamUri(song)
        val fileId = activeStreamInfo[song.id]?.tdFileId
            ?: if (resolvedUri.scheme == TelegramStreamDataSource.SCHEME)
                resolvedUri.lastPathSegment?.toIntOrNull() else null
        val existingFile = if (resolvedUri.scheme == "file")
            resolvedUri.path?.let(::File)?.takeIf { it.isFile && it.length() > 0L } else null
        if (fileId == null && existingFile == null) {
            throw IOException("Không tìm được file Telegram; hãy kết nối bot và phát lại bài hát.")
        }
        if (fileId != null && !offlineSavingFileIds.add(fileId)) {
            throw IOException("Bài hát này đang được tải xuống. Vui lòng chờ hoàn tất.")
        }
        try {
            val audio = activeStreamInfo[song.id]
                ?: TelegramAudioResult(0L, 0L, fileId ?: 0,
                    song.title, song.artist, "", song.mimeType.orEmpty(), song.durationMs, 0L)
            val source = existingFile ?: downloadTelegramFile(audio)
            return withContext(Dispatchers.IO) {
                writeAudioToDownloads(source, audio, song, destinationFolder = "Telegram")
            }
        } finally {
            if (fileId != null) {
                offlineSavingFileIds.remove(fileId)
                withContext(Dispatchers.Main.immediate) {
                    trackTemporaryTelegramAudio(fileId)
                    scheduleTelegramCacheTrim()
                }
            }
        }
    }

    /** Explicit Google Drive download, including Drive/MeloWave/Telegram Music backups.
     * The source is a real fully fetched Drive cache file, NEVER the TDLib URI.
     * Put all Google Drive downloads under Download/Drive, regardless of folder name.
     */
    internal fun copyDriveTrackToDownloads(sourceUri: Uri, song: SongItem): String {
        require(song.source == SongSource.PERSONAL_DRIVE || song.source == SongSource.PUBLIC_DRIVE) {
            "Không phải bài hát Google Drive."
        }
        if (sourceUri.scheme != "file") throw IOException("Chưa có file Google Drive để lưu.")
        val sourceFile = sourceUri.path?.let(::File)
            ?: throw IOException("Không đọc được đường dẫn file Google Drive.")
        val metadata = TelegramAudioResult(
            chatId = 0L, messageId = 0L, tdFileId = 0,
            title = song.title, artist = song.artist, fileName = sourceFile.name,
            mimeType = song.mimeType.orEmpty(), durationMs = song.durationMs,
            size = sourceFile.length()
        )
        return writeAudioToDownloads(sourceFile, metadata, song, destinationFolder = "Drive")
    }

    /** MediaStore creates the public folder if missing and reuses it if already present.
     * Only trusted internal callers choose the folder: Telegram or Drive.
     */
    private fun writeAudioToDownloads(
        sourceFile: File, audio: TelegramAudioResult, song: SongItem,
        destinationFolder: String
    ): String {
        require(destinationFolder == "Telegram" || destinationFolder == "Drive" ||
                (destinationFolder.startsWith("Telegram/") &&
                        destinationFolder.removePrefix("Telegram/").matches(Regex("""[A-Za-z0-9 _().\-\p{L}]{1,70}"""))))
        if (!sourceFile.isFile || sourceFile.length() <= 0L) {
            throw IOException("File âm thanh chưa tải đủ dữ liệu.")
        }
        val sourceExtension = audio.fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val extension = sourceExtension.takeIf {
            it in setOf("flac", "mp3", "m4a", "aac", "wav", "ogg", "opus", "wma", "aiff")
        } ?: when (song.mimeType?.lowercase(Locale.ROOT)) {
            "audio/flac", "audio/x-flac" -> "flac"
            "audio/mp4", "audio/x-m4a" -> "m4a"
            "audio/wav", "audio/x-wav" -> "wav"
            "audio/ogg" -> "ogg"
            else -> "mp3"
        }
        val cleanArtist = song.artist.trim().take(70)
        val cleanTitle = song.title.trim().take(100).ifBlank { "Audio" }
        val baseName = "$cleanArtist - $cleanTitle".trimStart(' ', '-').trim()
        val fileName = baseName.replace(Regex("""[\\/:*?"<>|\r\n]"""), "_") + ".$extension"
        val mime = audio.mimeType.takeIf { it.startsWith("audio/") }
            ?: song.mimeType?.takeIf { it.startsWith("audio/") }
            ?: when (extension) {
                "flac" -> "audio/flac"
                "m4a" -> "audio/mp4"
                "wav" -> "audio/wav"
                "ogg", "opus" -> "audio/ogg"
                else -> "audio/mpeg"
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = appContext.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$destinationFolder/")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val target = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Không tạo được tệp trong Download/$destinationFolder.")
            try {
                val copied = FileInputStream(sourceFile).use { input ->
                    resolver.openOutputStream(target, "w")?.use { output ->
                        input.copyTo(output, 128 * 1024)
                    } ?: throw IOException("Không ghi được file vào Downloads.")
                }
                if (copied != sourceFile.length()) {
                    throw IOException("Dữ liệu Telegram sao chép không đầy đủ ($copied byte).")
                }
                val published = resolver.update(target, ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }, null, null)
                if (published <= 0) throw IOException("Không thể hoàn tất file trong Downloads.")
                val actualName = resolver.query(target,
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }.orEmpty().ifBlank { fileName }
                return "Download/$destinationFolder/$actualName"
            } catch (failure: Throwable) {
                runCatching { resolver.delete(target, null, null) }
                throw failure
            }
        }
        // Legacy Android 8/9: MediaStore.Downloads/RELATIVE_PATH do not exist.
        if (appContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            throw IOException("Android 8/9 cần cấp quyền bộ nhớ để lưu vào Downloads.")
        }
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            destinationFolder
        )
        if (!dir.isDirectory && !dir.mkdirs()) {
            throw IOException("Không tạo được thư mục Download/$destinationFolder.")
        }
        var destination = File(dir, fileName)
        var suffix = 1
        while (destination.exists()) {
            destination = File(dir, "${baseName.replace(Regex("""[\\/:*?"<>|\r\n]"""), "_")} ($suffix).$extension")
            suffix++
        }
        try {
            FileInputStream(sourceFile).use { input ->
                destination.outputStream().use { output -> input.copyTo(output, 128 * 1024) }
            }
            if (destination.length() != sourceFile.length()) {
                throw IOException("Sao chép file âm thanh chưa đầy đủ.")
            }
        } catch (failure: Throwable) {
            destination.delete()
            throw failure
        }
        return "Download/$destinationFolder/${destination.name}"
    }

    /** Explicit Drive backup: get audio once, upload to user's Drive, no permanent secondary copy. */
    fun uploadToDrive(item: TelegramAudioResult, token: String, controller: MusicController) {
        if (driveUploading || downloading) { message = "Đang xử lý một file khác."; return }
        download = scope.launch {
            driveUploading = true
            downloading = true
            driveUploadStage = "telegram"
            driveBytesUploaded = 0L
            driveTotalBytes = 0L
            driveTelegramBytesReceived = 0L
            driveTelegramTotalBytes = item.size.coerceAtLeast(0L)
            driveSourceFileId = item.tdFileId
            error = null
            try {
                val file = downloadTelegramFile(item)
                val suffix = item.fileName.substringAfterLast('.', "mp3").lowercase(Locale.ROOT)
                    .takeIf { it in AUDIO_EXTENSIONS } ?: "mp3"
                val fileName = "${item.artist} - ${item.title}.$suffix"
                    .replace(Regex("""[\\/:*?"<>|\r\n]"""), "_").take(180)
                val sourceKey = item.uniqueFileId.ifBlank { "${item.chatId}_${item.messageId}" }
                driveUploadStage = "checking"
                message = "Đang kiểm tra thư mục và bản sao trên Google Drive…"
                val uploaded = withContext(Dispatchers.IO) {
                    controller.uploadTelegramFileToDrive(token, sourceKey, file, fileName,
                        item.mimeType) { uploadedBytes, totalBytes ->
                        // Google-confirmed bytes only; retransmitted chunks never inflate progress.
                        withContext(Dispatchers.Main.immediate) {
                            driveUploadStage = "upload"
                            driveBytesUploaded = uploadedBytes.coerceAtLeast(0L)
                            driveTotalBytes = totalBytes.coerceAtLeast(0L)
                        }
                    }
                }
                // Preserve a reference on Drive action, but never make a second permanent audio copy.
                if (savedSongs.none { matchesSavedAudio(it, item) }) addFromBot(item, controller)
                val saved = savedSongs.indexOfFirst { matchesSavedAudio(it, item) }
                if (saved >= 0) {
                    savedSongs[saved] = savedSongs[saved].copy(driveSaved = true)
                    writeCache()
                }
                message = if (uploaded) "Đã lưu '${item.title}' vào Drive / MeloWave / Telegram Music."
                else "Bài '${item.title}' đã có trên Google Drive, không tải trùng."
            } catch (cancel: CancellationException) { throw cancel }
            catch (expired: PersonalDriveAuthorizationException) {
                controller.onTelegramDriveAuthorizationExpired(item, token)
                message = "Quyền Google Drive đã hết hạn. Vui lòng cấp lại để tiếp tục lưu bài hát."
            }
            catch (failure: Throwable) { error = failure.message ?: "Không tải lên Drive được." }
            finally {
                val finishedFileId = driveSourceFileId
                driveUploading = false
                downloading = false
                driveUploadStage = ""
                driveSourceFileId = 0
                // Full-file Drive backup can create TDLib cache without an open Media3 stream.
                // Track that file for safe deletion only AFTER the upload has released it.
                trackTemporaryTelegramAudio(finishedFileId)
                scheduleTelegramCacheTrim()
            }
        }
    }

    fun openBot(context: Context = appContext) {
        val username = connection.botUsername
        if (username.isBlank()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$username"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure { error = "Không mở được Telegram." }
    }

    fun openLibrary(controller: MusicController) {
        controller.libraryTab = LibraryTab.SONGS
        controller.libraryQuery = ""
        controller.openLibrary(LibrarySource.TELEGRAM)
    }

    fun clearDownloadedMusic() {
        perform {
            // Legacy tracks without a Telegram reference cannot be downloaded again.
            // Preserve their only audio copy instead of breaking the user's library.
            val oldFiles = savedSongs.filter { song ->
                song.path.isNotBlank() && (song.messageId != 0L || song.actionMessageId != 0L)
            }.map { File(it.path) }.filter { it.isFile }
            val count = withContext(Dispatchers.IO) { oldFiles.count { it.delete() } }
            // Clear paths, never clear the accumulated playlist.
            for (index in savedSongs.indices) {
                val entry = savedSongs[index]
                if (entry.path.isNotBlank() && (entry.messageId != 0L || entry.actionMessageId != 0L)) {
                    savedSongs[index] = entry.copy(path = "")
                }
            }
            writeCache(); rebuildSongs()
            scheduleTelegramCacheTrim()
            message = "Đã xóa $count file nhạc cũ, giữ nguyên ${savedSongs.size} bài trong danh sách. " +
                    "Cache TDLib sẽ được dọn khi không có bài đang phát."
        }
    }

    fun resolveLocalWaveformUri(song: SongItem): Uri? =
        if (song.source != SongSource.TELEGRAM) null else savedSongs
            .firstOrNull { "telegram_${it.id}" == song.id }
            ?.path?.let(::File)?.takeIf { it.isFile && it.length() > 0L }?.let(Uri::fromFile)


    fun shareSourceLabel(): String = "Telegram · lưu tham chiếu"

    fun release() { savedBotRestoreJob?.cancel(); cancelSearchMetadataPrefetch(); cancelPendingPlayback(); stopPlaybackPreparation(); cleanupJob?.cancel(); operation?.cancel(); download?.cancel(); client.close(); scope.cancel() }

    private fun perform(action: suspend () -> Unit) {
        if (busy) { message = "Thao tác Telegram trước đang chạy."; return }
        operation = scope.launch {
            busy = true; error = null
            try { action() }
            catch (cancel: CancellationException) { throw cancel }
            catch (failure: Throwable) {
                val reason = failure.message ?: "Lỗi Telegram."
                if (playbackPreparing) failPlaybackPreparation(reason)
                else { cancelPendingPlayback(); error = reason }
            }
            finally { busy = false }
        }
    }

    private fun rebuildSongs() {
        songs.clear()
        // Cumulative order: older additions first, new songs appended at the end.
        songs.addAll(savedSongs.sortedWith(compareBy<TelegramSavedSong> { it.date }.thenBy { it.id }).map { data ->
            val f = File(data.path)
            SongItem("telegram_${data.id}", data.title, data.artist, data.album,
                if (data.path.isNotBlank()) "Telegram / Ngoại tuyến" else "Telegram / Đã thêm",
                if (data.path.isNotBlank()) Uri.fromFile(f)
                else Uri.parse("telegram://saved/${Uri.encode(data.id)}"), data.duration,
                SongSource.TELEGRAM, data.mime, genre = "Telegram", dateAddedMs = data.date)
        })
    }

    private fun writeCache(): Boolean {
        val list = JSONArray().apply { savedSongs.forEach { put(it.json()) } }
        val saved = prefs.edit().putString("songs", list.toString()).commit()
        if (!saved) error = "Không ghi được danh sách Telegram vào bộ nhớ. Hãy kiểm tra dung lượng thiết bị."
        return saved
    }
    private fun readCache(): List<TelegramSavedSong> = runCatching {
        val data = JSONArray(prefs.getString("songs", "[]"))
        (0 until data.length()).mapNotNull { data.optJSONObject(it)?.let(TelegramSavedSong::from) }
            // Keep metadata even if storage is temporarily unavailable.
            // A missing file is a playback error, NOT permission to erase the list.
            .filter { it.id.isNotBlank() }
    }.getOrDefault(emptyList())

    private fun migrateOldLocalMusic(): List<TelegramSavedSong> = runCatching {
        val old = appContext.getSharedPreferences("melowave_telegram_local_v2", Context.MODE_PRIVATE)
        val data = JSONArray(old.getString("local_tracks", "[]"))
        (0 until data.length()).mapNotNull { index ->
            val json = data.optJSONObject(index) ?: return@mapNotNull null
            val path = json.optString("local_path")
            if (!File(path).isFile) return@mapNotNull null
            TelegramSavedSong("legacy_${json.optString("id")}", json.optString("title"),
                json.optString("artist"), json.optString("album"), json.optString("mime_type"),
                path, json.optLong("duration_ms"), json.optLong("date_added_ms"))
        }.also { if (it.isNotEmpty()) { savedSongs.addAll(it); writeCache(); savedSongs.clear() } }
    }.getOrDefault(emptyList())

    private companion object {
        val AUDIO_EXTENSIONS = setOf("mp3", "m4a", "aac", "flac", "wav", "wave", "ogg", "oga", "opus", "wma")
        fun isAudioDocument(fileName: String, mime: String) =
            mime.startsWith("audio/", ignoreCase = true) ||
                    fileName.substringAfterLast('.', "").lowercase(Locale.ROOT) in AUDIO_EXTENSIONS
        fun splitArtistTitle(name: String): Pair<String, String> {
            val bare = name.substringBeforeLast('.', name)
            val parts = bare.split(" - ", limit = 2)
            return if (parts.size == 2) parts[0] to parts[1] else "Nghệ sĩ chưa biết" to bare
        }
    }
}

@Composable
internal fun TelegramMusicLibraryPanel(controller: MusicController, onManage: () -> Unit) {
    val colors = LocalMusicColors.current
    val telegram = controller.telegramMusic
    Surface(color = colors.backgroundSoft, shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colors.border)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Send, null, tint = Color(0xFF35B5EA), modifier = Modifier.size(24.dp).rotate(-30f))
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (telegram.connection.linked) mwString("telegram.connected", "Đã kết nối Telegram")
                else mwString("telegram.not_linked", "Chưa liên kết bot. Mở Cài đặt để chọn bot Telegram."), color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(mwString("telegram.library_summary", "Danh sách: {count} bài · Thêm không tải MP3", "count" to telegram.songs.size),
                    color = colors.textSecondary, fontSize = 11.sp)
            }
            TextButton(onClick = onManage) { Text(mwString("telegram.search", "Tìm nhạc"), color = colors.purpleBright, fontSize = 12.sp) }
        }
    }
}

/** Public-facing status deliberately omits bot usernames; configuration still works. */
/**
 * Telegram AI bot-settings icon: the small settings cog is the robot's NECK,
 * centered below the head instead of floating at the top-right.
 * Drawn as one Compose Canvas (no bitmap, badges or extra dependencies).
 */
@Composable
private fun TelegramRobotHeadSettingsIcon(
    modifier: Modifier = Modifier,
    settingsTint: Color = Color(0xFFACB8C9)
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val unit = size.minDimension / 40f
        fun p(x: Float, y: Float) = androidx.compose.ui.geometry.Offset(x * unit, y * unit)
        fun rect(w: Float, h: Float) = androidx.compose.ui.geometry.Size(w * unit, h * unit)
        fun corner(r: Float) = androidx.compose.ui.geometry.CornerRadius(r * unit)
        val cyan = Color(0xFF37C9F5)
        val face = Color(0xFF153047)
        val eyes = Color(0xFFDDFAFF)

        // Antenna and two small ears belong to the head, not the settings cog.
        drawLine(cyan, p(20f, 8.4f), p(20f, 4.5f),
            strokeWidth = 2.6f * unit, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawCircle(cyan, radius = 2.4f * unit, center = p(20f, 3.1f))
        drawRoundRect(cyan, topLeft = p(2.6f, 16f), size = rect(5.2f, 8f), cornerRadius = corner(2.2f))
        drawRoundRect(cyan, topLeft = p(32.2f, 16f), size = rect(5.2f, 8f), cornerRadius = corner(2.2f))

        // Short connector keeps the gear visually attached as the robot's neck.
        drawRoundRect(cyan, topLeft = p(17.7f, 27f), size = rect(4.6f, 6.5f),
            cornerRadius = corner(1.5f))
        // Main robot head, leaving enough room beneath it for the cog.
        drawRoundRect(face, topLeft = p(5.7f, 9f), size = rect(28.6f, 20.3f),
            cornerRadius = corner(5.9f))
        drawRoundRect(cyan, topLeft = p(5.7f, 9f), size = rect(28.6f, 20.3f),
            cornerRadius = corner(5.9f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.4f * unit))
        drawRoundRect(eyes, topLeft = p(11.4f, 16f), size = rect(6f, 5.4f),
            cornerRadius = corner(2.1f))
        drawRoundRect(eyes, topLeft = p(22.6f, 16f), size = rect(6f, 5.4f),
            cornerRadius = corner(2.1f))
        drawLine(Color(0xFFA855F7), p(17f, 24.9f), p(23f, 24.9f),
            strokeWidth = 2.4f * unit, cap = androidx.compose.ui.graphics.StrokeCap.Round)

        // Eight-tooth gear, centered BELOW the face as the neck/collar.
        // The darker inner hub keeps it recognizable on dark backgrounds.
        val gear = androidx.compose.ui.graphics.Path()
        val toothArc = Math.PI * 2.0 / 8.0
        val fractions = doubleArrayOf(0.0, 0.15, 0.60, 0.78)
        val radii = floatArrayOf(5.4f, 6.3f, 6.3f, 5.4f)
        for (tooth in 0 until 8) {
            for (j in 0..3) {
                val angle = -Math.PI / 2.0 + tooth * toothArc + fractions[j] * toothArc
                val x = 20f + kotlin.math.cos(angle).toFloat() * radii[j]
                val y = 33.2f + kotlin.math.sin(angle).toFloat() * radii[j]
                val offset = p(x, y)
                if (tooth == 0 && j == 0) gear.moveTo(offset.x, offset.y)
                else gear.lineTo(offset.x, offset.y)
            }
        }
        gear.close()
        drawPath(gear, color = settingsTint)
        drawCircle(face, radius = 2.3f * unit, center = p(20f, 33.2f))
        drawCircle(cyan, radius = 0.9f * unit, center = p(20f, 33.2f))
    }
}

/** Keep a single leading @ in the bot input. The user only types the name;
 * pasting @username and deleting all text preserve the leading @. */
internal fun telegramNormalizeBotInput(raw: String): String =
    "@" + raw.replace("@", "").trimStart().take(59)

private fun telegramUiStatusWithoutBotName(raw: String): String =
    raw.replace(Regex("@[A-Za-z0-9_]{4,}"), "bot Telegram")

/** A bot can edit the SAME message when changing search pages. Compare button
 * contents as well as the message ID, rather than treating all bot replies as
 * new search hits. Callback data is never exposed in the user interface. */
private fun telegramSearchPageFingerprint(result: TelegramBotResult): String =
    buildString {
        append(result.messageId)
        append('|')
        append(result.text)
        result.buttons.forEach { button ->
            append('|')
            append(button.kind.name)
            append(':')
            append(button.label)
            append(':')
            append(button.callbackData.contentHashCode())
        }
    }

@Composable
internal fun TelegramMusicDialog(controller: MusicController, onDismiss: () -> Unit) {
    val colors = LocalMusicColors.current
    val tg = controller.telegramMusic
    val appLanguage = LocalAppLanguage.current
    val window = LocalConfiguration.current
    val narrowScreen = window.screenWidthDp < 375 || window.fontScale > 1.15f
    var phone by remember { mutableStateOf("") }
    var authValue by remember { mutableStateOf("") }
    var bot by rememberSaveable { mutableStateOf("@") }
    var keyword by rememberSaveable { mutableStateOf(tg.dialogSearchKeyword) }
    // Keep bot configuration out of the search view. The gear reveals it on demand.
    var showBotSettings by rememberSaveable {
        mutableStateOf(!tg.connection.linked && !tg.hasSavedBot)
    }
    // Shared 15-language guide: accessible even before Telegram authorization.
    var showTelegramGuide by rememberSaveable { mutableStateOf(false) }
    var showBotHistory by remember { mutableStateOf(false) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    val selectedResultKeys = remember { mutableStateListOf<String>() }
    var openedTelegramEntry by remember { mutableStateOf<TelegramUnifiedTrack?>(null) }
    var albumSongSelectMode by remember { mutableStateOf(false) }
    val selectedAlbumSongKeys = remember { mutableStateListOf<String>() }
    var searchRun by remember { mutableStateOf(0) }
    var searchRunQuery by remember { mutableStateOf("") }
    // Freeze the bot's ORIGINAL search suggestions. tg.results also contains
    // later detail / album-menu replies produced by automatic verification;
    // those replies must never become additional top-level search results.
    var searchPageSnapshot by remember { mutableStateOf(tg.dialogSearchPageSnapshot) }
    var searchPageLoading by remember { mutableStateOf(false) }
    var pageNavigationRun by remember { mutableStateOf(0) }
    var pageNavigationBaseline by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    fun runTelegramSearch(query: String) {
        val trimmed = query.trim().take(120)
        if (trimmed.isBlank() || !tg.connection.linked || tg.busy || tg.searchSaveRunning) return
        selectionMode = false
        selectedResultKeys.clear()
        openedTelegramEntry = null
        albumSongSelectMode = false
        selectedAlbumSongKeys.clear()
        searchPageSnapshot = emptyList()
        tg.dialogSearchPageSnapshot = emptyList()
        searchPageLoading = true
        // Abort a pending page-change observer from the previous keyword.
        pageNavigationRun = 0
        tg.search(trimmed)
        tg.dialogSearchKeyword = trimmed
        searchRunQuery = trimmed
        searchRun++
    }
    fun openBotSearchPage(result: TelegramBotResult, button: TelegramBotAction) {
        if (!tg.connection.linked || tg.busy || tg.downloading) return
        tg.cancelSearchMetadataPrefetch()
        // A callback may EDIT an old bot message; remember its prior version.
        pageNavigationBaseline = tg.results.associate { entry ->
            entry.messageId to telegramSearchPageFingerprint(entry)
        }
        // Pagination / "Download page" are BOT CONTROL commands, not a song.
        // Never start a Media3 playback spinner or an auto-download for them.
        tg.action(result, button)
        pageNavigationRun++
    }
    // Mirror Home search: inspect bot responses one-by-one to distinguish a
    // confirmed album from a single audio file before showing metadata.
    LaunchedEffect(searchRun) {
        if (searchRun <= 0) return@LaunchedEffect
        var firstUnrelatedReplyAt = -1
        for (attempt in 0 until 70) {
            delay(250L)
            if (!tg.connection.linked || tg.authStep != TelegramAuthStep.READY) break
            val botResults = tg.results.filter { !it.outgoing }
            if (botResults.isNotEmpty() && !tg.busy) {
                val tracks = telegramUnifiedTracks(botResults, tg.verifiedEntryFileCounts,
                    tg.isVkMusicBot, tg.isMusgetBot, tg.isMooMusicBot, tg.isLyBot,
                    searchRunQuery)
                if (tracks.isNotEmpty()) {
                    val relevant = telegramFilterSearchResults(
                        tracks, searchRunQuery,
                        tg.isNumberedMusicBot || tracks.any { it.isNumericReplyChoice })
                    if (tracks.any { it.isNumericReplyChoice } && relevant.isEmpty()) {
                        // A late reply from the PREVIOUS query can arrive just
                        // before the current bot search result. Wait briefly for
                        // a matching reply, but show 'no matching tracks' if the
                        // bot really returns unrelated titles.
                        if (firstUnrelatedReplyAt < 0) firstUnrelatedReplyAt = attempt
                        if (attempt - firstUnrelatedReplyAt < 10) continue
                    }
                    // Preserve the raw page for actual Telegram pagination;
                    // only the rendered playlist is relevance-filtered.
                    searchPageSnapshot = tracks
                    tg.dialogSearchPageSnapshot = tracks
                    searchPageLoading = false
                    if (relevant.any { it.button != null } && searchRunQuery.isNotBlank()) {
                        tg.prefetchSearchMetadata(relevant, searchRunQuery)
                    }
                    break
                }
            }
        }
        searchPageLoading = false
    }
    // Explicit user navigation is the ONLY time the frozen search page may be
    // replaced. Auto-probed album menus do not cause the list to grow.
    LaunchedEffect(pageNavigationRun, searchRun) {
        if (pageNavigationRun <= 0) return@LaunchedEffect
        for (attempt in 0 until 55) {
            delay(300L)
            if (!tg.connection.linked) break
            val updatedPage = tg.results.asReversed().firstOrNull { entry ->
                !entry.outgoing && entry.buttons.isNotEmpty() &&
                        telegramSearchPageFingerprint(entry) != pageNavigationBaseline[entry.messageId]
            } ?: continue
            val fresh = telegramUnifiedTracks(listOf(updatedPage), tg.verifiedEntryFileCounts,
                tg.isVkMusicBot, tg.isMusgetBot, tg.isMooMusicBot, tg.isLyBot,
                searchRunQuery)
            if (fresh.isNotEmpty()) {
                searchPageSnapshot = fresh
                tg.dialogSearchPageSnapshot = fresh
                if (searchRunQuery.isNotBlank()) tg.prefetchSearchMetadata(fresh, searchRunQuery)
                break
            }
        }
    }
    LaunchedEffect(tg.connection.botChatId) {
        selectionMode = false
        selectedResultKeys.clear()
        openedTelegramEntry = null
        albumSongSelectMode = false
        selectedAlbumSongKeys.clear()
        searchPageSnapshot = tg.dialogSearchPageSnapshot
        searchPageLoading = false
    }
    // Once a user selects a bot, return directly to the music search view.
    LaunchedEffect(tg.connection.botChatId, tg.connection.linked, tg.restoringSavedBot) {
        if (tg.connection.linked || tg.restoringSavedBot) {
            // A remembered bot should go straight to music/search, not ask to link again.
            showBotSettings = false
            bot = "@"
        }
    }
    val canSearch = tg.connection.linked && !tg.busy && !tg.searchSaveRunning && keyword.isNotBlank()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val clipboardLanguage = LocalMeloWareTranslations.current
        .resolveLanguage(LocalAppLanguage.current) ?: "vi"
    val clipboardFallback = telegramBotClipboardLabels(clipboardLanguage)
    val copyBotLabel = mwString("telegram.copy_bot", clipboardFallback.first)
    val copiedBotNotice = mwString("telegram.bot_copied", clipboardFallback.second)
    fun copyBotUsername(username: String) {
        // Copy the Telegram handle, not the display name or the t.me URL.
        clipboardManager.setText(AnnotatedString("@${username.removePrefix("@")}"))
        Toast.makeText(context, copiedBotNotice, Toast.LENGTH_SHORT).show()
    }
    val guideHelpText = mwString("telegram.guide.help", "Hướng dẫn kết nối Telegram")
    val guidePrefs = remember(context) {
        context.getSharedPreferences("melowave_telegram_guide_v1", Context.MODE_PRIVATE)
    }
    LaunchedEffect(tg.authStep, tg.connection.linked, tg.hasSavedBot) {
        // First install only. Existing sessions and remembered bots are never interrupted.
        if (tg.authStep == TelegramAuthStep.PHONE && !tg.connection.linked &&
            !tg.hasSavedBot && tg.recentBots.isEmpty() &&
            !guidePrefs.getBoolean("first_login_guide_seen", false)
        ) {
            guidePrefs.edit().putBoolean("first_login_guide_seen", true).apply()
            showTelegramGuide = true
        }
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var voiceError by remember { mutableStateOf<String?>(null) }
    val telegramSpeechLanguage = LocalMeloWareTranslations.current.resolveLanguage(LocalAppLanguage.current)
        ?.let { Locale.forLanguageTag(it).toLanguageTag() }
        ?: Locale.getDefault().toLanguageTag()
    val voiceRetryText = mwString("telegram.voice_retry", "Chưa nhận dạng được từ khóa. Hãy thử nói lại.")
    val voicePromptText = mwString("telegram.voice_prompt", "Nói tên bài hát hoặc ca sĩ")
    val voiceUnavailableText = mwString("home.voice_unavailable", "Thiết bị chưa có dịch vụ nhận dạng giọng nói.")
    val voiceDeniedText = mwString("telegram.voice_denied", "Không thể sử dụng dịch vụ nhận dạng giọng nói.")
    val botSettingsBackText = mwString("telegram.settings_back", "Quay lại tìm nhạc")
    val botSettingsOpenText = mwString("telegram.settings_open", "Cài đặt kết nối bot Telegram")
    val telegramVoiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()?.trim().orEmpty()
            if (spoken.isNotBlank()) {
                keyword = spoken.take(200)
                voiceError = null
                runTelegramSearch(spoken)
            } else voiceError = voiceRetryText
        }
    }
    fun startTelegramVoiceSearch() {
        focusManager.clearFocus()
        keyboardController?.hide()
        voiceError = null
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, telegramSpeechLanguage)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, voicePromptText)
        }
        try {
            telegramVoiceLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            voiceError = voiceUnavailableText
        } catch (_: SecurityException) {
            voiceError = voiceDeniedText
        }
    }

    // Full-bleed screen. The body, not the entire window, scrolls.
    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background,
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    .imePadding().padding(horizontal = if (narrowScreen) 10.dp else 16.dp,
                        vertical = if (narrowScreen) 5.dp else 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Header: bot status, one bot-settings icon, and close.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Send, contentDescription = null,
                        tint = Color(0xFF229ED9),
                        modifier = Modifier.size(if (narrowScreen) 20.dp else 24.dp).rotate(-30f))
                    Spacer(Modifier.size(if (narrowScreen) 5.dp else 8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Telegram Music", color = colors.textPrimary,
                            fontSize = if (narrowScreen) 17.sp else 21.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold)
                        // Connection state belongs in the bot settings page. Keep
                        // the main search header to one clean line.
                    }
                    IconButton(
                        onClick = { showTelegramGuide = true },
                        modifier = Modifier.size(38.dp).semantics { contentDescription = guideHelpText }
                    ) {
                        Icon(Icons.Rounded.HelpOutline, contentDescription = guideHelpText,
                            tint = colors.purpleBright, modifier = Modifier.size(23.dp))
                    }
                    if (tg.authStep == TelegramAuthStep.READY) {
                        IconButton(
                            onClick = {
                                if (!showBotSettings) bot = "@"
                                showBotSettings = !showBotSettings
                            },
                            modifier = Modifier.size(40.dp).semantics {
                                contentDescription = if (showBotSettings) botSettingsBackText else botSettingsOpenText
                            }
                        ) {
                            // Hand-drawn AI ROBOT HEAD + small Settings gear (not SmartToy).
                            // The robot is the primary glyph; the gear denotes bot settings.
                            // No circular background; preserve the existing 40dp touch target.
                            TelegramRobotHeadSettingsIcon(
                                modifier = Modifier.size(36.dp),
                                settingsTint = if (showBotSettings) colors.purpleBright else colors.textSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Rounded.Close, mwString("common.close", "Đóng"), tint = colors.textSecondary)
                    }
                }

                if (tg.authStep != TelegramAuthStep.READY) {
                    // Leave the existing account authentication behavior intact.
                    Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        when (tg.authStep) {
                            TelegramAuthStep.NOT_CONFIGURED -> Text(
                                mwString("telegram.not_configured", "Ứng dụng chưa được cấu hình Telegram. Vui lòng cập nhật phiên bản."),
                                color = colors.textSecondary, fontSize = 12.sp)
                            TelegramAuthStep.STARTING -> {
                                CircularProgressIndicator(modifier = Modifier.size(25.dp))
                                Text(mwString("telegram.initializing", "Đang khởi tạo Telegram…"), color = colors.textSecondary, fontSize = 12.sp)
                            }
                            TelegramAuthStep.PHONE -> {
                                TelegramPhoneEntry(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    onSubmit = { international ->
                                        tg.submitLogin(international)
                                        phone = ""
                                    }
                                )
                            }
                            TelegramAuthStep.REGISTRATION -> Text(
                                mwString("telegram.registration_needed", "Tài khoản chưa đăng ký. Hãy hoàn tất trên Telegram rồi thử lại."),
                                color = colors.textSecondary, fontSize = 12.sp)
                            TelegramAuthStep.QR_CONFIRM -> Text(
                                tg.message ?: mwString("telegram.confirm_other_device", "Xác nhận đăng nhập trên thiết bị Telegram khác."),
                                color = colors.textSecondary, fontSize = 12.sp)
                            TelegramAuthStep.LOGGING_OUT -> Text(mwString("telegram.logging_out", "Đang đăng xuất Telegram…"),
                                color = colors.textSecondary)
                            else -> {
                                val label = when (tg.authStep) {
                                    TelegramAuthStep.CODE -> mwString("telegram.auth_code", "Mã xác nhận Telegram")
                                    TelegramAuthStep.PASSWORD -> mwString("telegram.auth_password", "Mật khẩu xác minh 2 bước")
                                    TelegramAuthStep.EMAIL -> mwString("telegram.auth_email", "Địa chỉ email xác minh")
                                    TelegramAuthStep.EMAIL_CODE -> mwString("telegram.auth_email_code", "Mã xác minh email")
                                    else -> mwString("telegram.auth_info", "Thông tin đăng nhập")
                                }
                                OutlinedTextField(value = authValue, onValueChange = { authValue = it },
                                    label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (tg.authStep == TelegramAuthStep.PASSWORD)
                                        PasswordVisualTransformation()
                                    else androidx.compose.ui.text.input.VisualTransformation.None)
                                Button(onClick = { tg.submitLogin(authValue); authValue = "" },
                                    enabled = authValue.isNotBlank()) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null,
                                        modifier = Modifier.size(18.dp))
                                    Text(mwString("telegram.auth_confirm", "Xác nhận"))
                                }
                            }
                        }
                    }
                } else if (showBotSettings) {
                    // Fixed-height settings page: no verticalScroll. The recent-bot list
                    // gets only the unused room, and shows at most ten compact rows.
                    // On a small screen / with IME open, extra bots stay in history.
                    var settingsAreaPx by remember { mutableStateOf(0) }
                    val shortViewport = settingsAreaPx > 0 && with(LocalDensity.current) {
                        settingsAreaPx.toDp() < 420.dp
                    }
                    val compact = narrowScreen || shortViewport || tg.recentBots.size >= 5
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)
                            .onSizeChanged { size ->
                                if (settingsAreaPx != size.height) settingsAreaPx = size.height
                            }) {
                        Column(modifier = Modifier.fillMaxSize().padding(bottom = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Icon(Icons.Rounded.Settings, contentDescription = null,
                                    tint = colors.purpleBright, modifier = Modifier.size(18.dp))
                                Text(mwString("telegram.bot_settings", "Cài đặt bot"), color = colors.textPrimary,
                                    fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            if (!compact) {
                                Text(mwString("telegram.bot_hint", "Nhập @username hoặc chọn bot đã dùng."),
                                    color = colors.textSecondary, fontSize = 10.sp)
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                OutlinedTextField(
                                    value = bot, onValueChange = { bot = telegramNormalizeBotInput(it) },
                                    modifier = Modifier.weight(1f), singleLine = true,
                                    shape = RoundedCornerShape(13.dp),
                                    placeholder = { Text(mwString("telegram.bot_placeholder", "@tên_bot"), fontSize = 12.sp) },
                                    trailingIcon = {
                                        if (bot.length > 1) {
                                            IconButton(onClick = { bot = "@" }) {
                                                Icon(Icons.Rounded.Close, contentDescription = mwString("telegram.clear_bot", "Xóa tên bot"),
                                                    modifier = Modifier.size(17.dp),
                                                    tint = colors.textSecondary)
                                            }
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.purpleBright,
                                        unfocusedBorderColor = colors.borderSoft)
                                )
                                Button(onClick = {
                                    tg.connectBot(bot)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }, enabled = !tg.busy && !tg.restoringSavedBot && bot.drop(1).isNotBlank(),
                                    shape = RoundedCornerShape(13.dp),
                                    modifier = Modifier.heightIn(min = 52.dp),
                                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp)) {
                                    Icon(Icons.Rounded.Link, contentDescription = null,
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text(mwString("telegram.connect", "Kết nối"), fontSize = 11.sp)
                                }
                            }
                            if (tg.connection.linked) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Icon(Icons.Rounded.CheckCircle, null, tint = colors.green,
                                        modifier = Modifier.size(14.dp))
                                    Text(mwString("telegram.connected", "Đã kết nối Telegram"),
                                        color = colors.green, fontSize = 10.sp,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.History, contentDescription = null,
                                    modifier = Modifier.size(17.dp), tint = colors.textSecondary)
                                Spacer(Modifier.size(6.dp))
                                Text(mwString("telegram.recent_bots", "Bot gần đây ({count})", "count" to tg.recentBots.size),
                                    modifier = Modifier.weight(1f), color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                if (tg.recentBots.isNotEmpty()) {
                                    TextButton(onClick = { showBotHistory = true },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                                        Text(mwString("home.view_all", "Xem tất cả"), color = colors.purpleBright, fontSize = 11.sp)
                                        Icon(Icons.Rounded.ChevronRight, contentDescription = null,
                                            tint = colors.purpleBright, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            if (tg.recentBots.isEmpty()) {
                                Text(mwString("telegram.no_recent", "Chưa có bot nào trong lịch sử."),
                                    color = colors.textSecondary, fontSize = 11.sp)
                            } else {
                                // Unlike a LazyColumn, this preview never scrolls. Non-weighted
                                // settings actions are measured first; only the remaining height
                                // is used for the bot rows, preventing controls from clipping.
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()
                                        .weight(1f, fill = false)) {
                                    val rowHeight = if (compact) 34.dp else 39.dp
                                    val visibleCount = minOf(10, tg.recentBots.size,
                                        ((maxHeight.value - 6f) / (rowHeight.value + 0.5f))
                                            .toInt().coerceAtLeast(0))
                                    if (visibleCount > 0) {
                                        Surface(shape = RoundedCornerShape(13.dp),
                                            color = colors.backgroundSoft,
                                            border = BorderStroke(1.dp, colors.borderSoft)) {
                                            Column(Modifier.fillMaxWidth().padding(
                                                horizontal = 8.dp, vertical = 1.dp)) {
                                                tg.recentBots.take(visibleCount).forEachIndexed { index, recent ->
                                                    val active = tg.connection.linked &&
                                                        tg.connection.botUsername.equals(
                                                            recent.username, ignoreCase = true)
                                                    Row(Modifier.fillMaxWidth().height(rowHeight),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(Icons.Rounded.Send, contentDescription = null,
                                                            tint = Color(0xFF229ED9),
                                                            modifier = Modifier.size(17.dp).rotate(-30f))
                                                        Text("@${recent.username}", color = colors.textPrimary,
                                                            modifier = Modifier.weight(1f),
                                                            fontSize = if (compact) 10.5.sp else 11.sp,
                                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        IconButton(onClick = {
                                                            copyBotUsername(recent.username)
                                                        }, modifier = Modifier.size(31.dp)) {
                                                            Icon(Icons.Rounded.ContentCopy,
                                                                "$copyBotLabel @${recent.username}",
                                                                tint = colors.textSecondary,
                                                                modifier = Modifier.size(17.dp))
                                                        }
                                                        if (active) {
                                                            Icon(Icons.Rounded.CheckCircle,
                                                                mwString("telegram.connected_bot", "Bot đang kết nối"),
                                                                tint = colors.green,
                                                                modifier = Modifier.size(20.dp))
                                                        } else {
                                                            IconButton(onClick = {
                                                                bot = "@"
                                                                tg.connectBot(recent.username)
                                                            }, enabled = !tg.busy,
                                                                modifier = Modifier.size(31.dp)) {
                                                                Icon(Icons.Rounded.Link,
                                                                    mwString("telegram.connect", "Kết nối") + " @${recent.username}",
                                                                    tint = colors.purpleBright,
                                                                    modifier = Modifier.size(19.dp))
                                                            }
                                                        }
                                                    }
                                                    if (index < visibleCount - 1) {
                                                        Divider(color = colors.borderSoft,
                                                            thickness = 0.5.dp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (!shortViewport && tg.connection.linked) {
                                Text(mwString("telegram.bot_controls", "Điều khiển bot"), color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    OutlinedButton(onClick = { tg.sendText("/start") }, enabled = !tg.busy,
                                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 3.dp)) {
                                        Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.size(2.dp)); Text("/start", fontSize = 10.sp)
                                    }
                                    OutlinedButton(onClick = { tg.refresh() }, enabled = !tg.busy,
                                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 3.dp)) {
                                        Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.size(2.dp)); Text(mwString("telegram.refresh", "Làm mới"), fontSize = 10.sp)
                                    }
                                    OutlinedButton(onClick = { tg.openBot() },
                                        modifier = Modifier.weight(1.25f), shape = RoundedCornerShape(11.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 3.dp)) {
                                        Icon(Icons.Rounded.OpenInNew, null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.size(2.dp)); Text("Telegram", fontSize = 10.sp)
                                    }
                                }
                            }
                            if (!shortViewport) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    OutlinedButton(onClick = { tg.disconnect(); bot = "@" },
                                        enabled = tg.connection.linked, modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(11.dp),
                                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 4.dp)) {
                                        Icon(Icons.Rounded.LinkOff, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.size(4.dp))
                                        Text(mwString("telegram.disconnect_bot", "Ngắt bot"), fontSize = 10.sp)
                                    }
                                    OutlinedButton(onClick = { tg.logOut(); bot = "@" },
                                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp),
                                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 4.dp)) {
                                        Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.size(4.dp))
                                        Text(mwString("home.account.google_logout", "Đăng xuất"), fontSize = 10.sp)
                                    }
                                }
                            }
                            if (!compact && !shortViewport) {
                                Text(mwString("telegram.signout_warning", "Đăng xuất sẽ yêu cầu đăng nhập lại Telegram."),
                                    color = colors.textSecondary, fontSize = 9.sp)
                            }
                            // Keep the action next to settings, rather than below a
                            // screen-height empty spacer on tall devices.
                            Spacer(Modifier.height(if (compact) 5.dp else 12.dp))
                            Button(onClick = { showBotSettings = false },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)) {
                                Icon(Icons.Rounded.Done, null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.size(5.dp)); Text(mwString("telegram.done_to_search", "Xong · Về tìm kiếm"), fontSize = 11.sp)
                            }
                        }
                        if (showBotHistory) {
                            Dialog(onDismissRequest = { showBotHistory = false }) {
                                Surface(color = colors.backgroundSoft,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, colors.borderSoft)) {
                                    Column(Modifier.fillMaxWidth().padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.History, null,
                                                tint = colors.purpleBright, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.size(7.dp))
                                            Text(mwString("telegram.bot_history", "Lịch sử bot ({count})", "count" to tg.recentBots.size),
                                                modifier = Modifier.weight(1f),
                                                color = colors.textPrimary, fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold)
                                            IconButton(onClick = { showBotHistory = false },
                                                modifier = Modifier.size(33.dp)) {
                                                Icon(Icons.Rounded.Close, mwString("telegram.close_history", "Đóng lịch sử"), tint = colors.textSecondary)
                                            }
                                        }
                                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 390.dp)) {
                                            items(tg.recentBots.toList(), key = { it.username.lowercase(Locale.ROOT) }) { recent ->
                                                val active = tg.connection.linked &&
                                                        tg.connection.botUsername.equals(recent.username, ignoreCase = true)
                                                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                                    Icon(Icons.Rounded.Send, null, tint = Color(0xFF229ED9),
                                                        modifier = Modifier.size(19.dp).rotate(-30f))
                                                    Column(Modifier.weight(1f)) {
                                                        Text("@${recent.username}", color = colors.textPrimary,
                                                            fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text(if (active) mwString("telegram.connected", "Đã kết nối Telegram") else
                                                            recent.displayName.ifBlank { mwString("telegram.used_bot", "Bot đã sử dụng") },
                                                            color = if (active) colors.green else colors.textSecondary,
                                                            fontSize = 10.sp, maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis)
                                                    }
                                                    IconButton(onClick = {
                                                        copyBotUsername(recent.username)
                                                    }, modifier = Modifier.size(32.dp)) {
                                                        Icon(Icons.Rounded.ContentCopy,
                                                            "$copyBotLabel @${recent.username}",
                                                            tint = colors.textSecondary,
                                                            modifier = Modifier.size(17.dp))
                                                    }
                                                    IconButton(onClick = { tg.removeRecentBot(recent.username) },
                                                        enabled = !tg.busy, modifier = Modifier.size(32.dp)) {
                                                        Icon(Icons.Rounded.DeleteOutline,
                                                            mwString("telegram.remove_recent_bot", "Xóa @{bot} khỏi lịch sử", "bot" to recent.username),
                                                            tint = colors.textSecondary,
                                                            modifier = Modifier.size(18.dp))
                                                    }
                                                    if (active) {
                                                        Icon(Icons.Rounded.CheckCircle, mwString("telegram.connected", "Đã kết nối Telegram"),
                                                            tint = colors.green, modifier = Modifier.size(22.dp))
                                                    } else {
                                                        IconButton(onClick = {
                                                            bot = "@"; tg.connectBot(recent.username)
                                                            showBotHistory = false
                                                        }, enabled = !tg.busy, modifier = Modifier.size(36.dp)) {
                                                            Icon(Icons.Rounded.Link, mwString("telegram.connect", "Kết nối") + " @${recent.username}",
                                                                tint = colors.purpleBright,
                                                                modifier = Modifier.size(20.dp))
                                                        }
                                                    }
                                                }
                                                Divider(color = colors.borderSoft.copy(alpha = 0.8f), thickness = 1.dp)
                                            }
                                        }
                                        TextButton(onClick = {
                                            tg.clearRecentBots()
                                            showBotHistory = false
                                        }, enabled = tg.recentBots.isNotEmpty() && !tg.busy,
                                            modifier = Modifier.fillMaxWidth()) {
                                            Icon(Icons.Rounded.DeleteOutline, null,
                                                modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.size(5.dp))
                                            Text(mwString("telegram.clear_history", "Xóa lịch sử bot"), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (!tg.connection.linked) {
                    if (tg.restoringSavedBot) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp,
                                color = colors.purpleBright)
                            Text(mwString("telegram.restoring_bot", "Đang khôi phục bot Telegram đã liên kết…"),
                                color = colors.textSecondary, fontSize = 12.sp)
                        }
                    } else if (tg.hasSavedBot) {
                        Text(mwString("telegram.restore_failed", "Chưa khôi phục được bot đã lưu. Không cần nhập lại tên bot."),
                            color = colors.textSecondary, fontSize = 12.sp)
                        TextButton(onClick = { tg.retrySavedBotConnection() }) {
                            Text(mwString("telegram.try_connect_again", "Thử kết nối lại"), color = colors.purpleBright)
                        }
                    } else {
                        Text(mwString("telegram.not_linked", "Chưa liên kết bot. Mở Cài đặt để chọn bot Telegram."),
                            color = colors.textSecondary, fontSize = 12.sp)
                    }
                } else {
                    val albumCandidate = openedTelegramEntry
                    val albumDetail = albumCandidate?.let { entry ->
                        tg.selectionKey(entry)?.let { tg.selectedDetails[it] }
                    }
                    val confirmedAlbum = albumCandidate != null &&
                            ((albumDetail?.count ?: 0) > 1 || (albumDetail?.tracks?.size ?: 0) > 1)
                    // Direct bot results and the shared search now render the same
                    // card, album page, download actions and progress rings.
                    if (confirmedAlbum && albumCandidate != null && albumDetail != null) {
                        TelegramSearchAlbumPage(
                            controller = controller,
                            telegram = tg,
                            album = albumCandidate,
                            detail = albumDetail,
                            query = searchRunQuery.ifBlank { tg.dialogSearchKeyword.ifBlank { keyword.trim() } },
                            selectionMode = albumSongSelectMode,
                            selectedKeys = selectedAlbumSongKeys,
                            onToggleSelectionMode = {
                                albumSongSelectMode = !albumSongSelectMode
                                selectedAlbumSongKeys.clear()
                            },
                            onBack = {
                                openedTelegramEntry = null
                                albumSongSelectMode = false
                                selectedAlbumSongKeys.clear()
                            },
                            onClose = onDismiss,
                            onRequestSave = { entries, destination ->
                                controller.saveTelegramSearchSelection(entries,
                                    keyword.trim(), destination)
                            }
                        )
                    } else if (albumCandidate != null) {
                        // Until the bot proves this is an album, do not invent a
                        // file count or display a fake media format.
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { openedTelegramEntry = null }) {
                                Icon(Icons.Rounded.ArrowBack, mwString("telegram.back_results", "Trở về danh sách"), tint = colors.textPrimary)
                            }
                            Text(albumCandidate.title, color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (tg.metadataPreviewRunning || tg.waitingForAudio != null) {
                                CircularProgressIndicator(Modifier.size(16.dp),
                                    strokeWidth = 2.dp, color = colors.purpleBright)
                                Spacer(Modifier.size(8.dp))
                            }
                            Text(mwString("telegram.verifying", "Đang xác minh bài hát hoặc album từ Telegram…"),
                                color = colors.textSecondary, fontSize = 12.sp)
                        }
                        TextButton(onClick = {
                            tg.resolveSearchResultOnTap(albumCandidate, keyword.trim(), controller)
                        }, enabled = !tg.busy && !tg.downloading && !tg.metadataPreviewRunning) {
                            Text(mwString("telegram.recheck", "Kiểm tra lại"), color = colors.purpleBright)
                        }
                        LaunchedEffect(albumCandidate.stableKey, albumDetail?.audio?.messageId) {
                            // resolveSearchResultOnTap starts playback for confirmed single
                            // audio. Close the detail; leave album pages visible.
                            if (albumDetail?.audio != null && (albumDetail.count ?: 0) <= 1) {
                                onDismiss()
                            }
                        }
                    } else {
                        // Use the exact bot search page captured BEFORE any album
                        // probes. Reapply the manager's live per-button confirmation
                        // without adding album submenu buttons as extra results.
                        val rawSearchTracks = searchPageSnapshot.map { original ->
                            val key = tg.selectionKey(original)
                            val detail = key?.let { tg.selectedDetails[it] }
                            val knownCount = key?.let { tg.verifiedEntryFileCounts[it] }
                                ?: detail?.count ?: detail?.tracks?.size?.takeIf { it > 1 }
                            if (original.button != null && original.audio == null && knownCount != null)
                                original.copy(isAlbum = knownCount > 1)
                            else original
                        }
                        // Compare the current BOT SEARCH keyword, not unsubmitted edits
                        // in the input field. Keep the raw snapshot for Next/Previous.
                        val displayedSearchTracks = telegramFilterSearchResults(
                            rawSearchTracks,
                            searchRunQuery.ifBlank { tg.dialogSearchKeyword },
                            tg.isNumberedMusicBot ||
                                rawSearchTracks.any { it.isNumericReplyChoice }
                        )
                        val unrelatedCount = rawSearchTracks.size - displayedSearchTracks.size
                        // Primary view: exactly one search field and the bot's results.
                        OutlinedTextField(
                            value = keyword, onValueChange = { keyword = it.take(200) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.purpleBright,
                                unfocusedBorderColor = colors.borderSoft,
                                focusedContainerColor = colors.backgroundSoft,
                                unfocusedContainerColor = colors.backgroundSoft
                            ),
                            placeholder = { Text(mwString("telegram.search_placeholder", "Tìm bài hát hoặc ca sĩ")) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                if (canSearch) {
                                    selectionMode = false
                                    selectedResultKeys.clear()
                                    runTelegramSearch(keyword)
                                }
                            }),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (keyword.isBlank()) {
                                        IconButton(onClick = { startTelegramVoiceSearch() },
                                            enabled = tg.connection.linked) {
                                            Icon(Icons.Rounded.Mic, contentDescription = mwString("telegram.voice_search", "Tìm bằng giọng nói"),
                                                tint = if (tg.connection.linked) colors.purpleBright else colors.textSecondary)
                                        }
                                    } else {
                                        IconButton(onClick = {
                                            keyword = ""
                                            voiceError = null
                                            // Keep the results visible until the next search.
                                        }) {
                                            Icon(Icons.Rounded.Close, contentDescription = mwString("telegram.clear_search", "Xóa toàn bộ từ khóa"),
                                                tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    if (searchPageLoading) {
                                        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp, color = colors.purpleBright)
                                        }
                                    } else {
                                        IconButton(onClick = {
                                            selectionMode = false
                                            selectedResultKeys.clear()
                                            runTelegramSearch(keyword)
                                        }, enabled = canSearch) {
                                            Icon(Icons.Rounded.Search, contentDescription = mwString("telegram.search", "Tìm nhạc"),
                                                tint = if (canSearch) colors.purpleBright else colors.textSecondary)
                                        }
                                    }
                                }
                            }
                        )
                        voiceError?.let {
                            Text(it, color = Color(0xFFDE655C), fontSize = 11.sp)
                        }
                        Row(Modifier.fillMaxWidth().heightIn(min = 25.dp), verticalAlignment = Alignment.CenterVertically) {
                            val albumCount = displayedSearchTracks.count { it.isAlbum }
                            Text((if (unrelatedCount > 0) {
                                mwString("telegram.filtered_results",
                                    telegramMatchedCountFallback(appLanguage,
                                        displayedSearchTracks.size, rawSearchTracks.size),
                                    "shown" to displayedSearchTracks.size, "total" to rawSearchTracks.size)
                            } else {
                                mwString("telegram.results", "Telegram · {count} kết quả",
                                    "count" to displayedSearchTracks.size)
                            }) + if (albumCount > 0)
                                mwString("telegram.album_label", " · {count} album", "count" to albumCount) else "",
                                modifier = Modifier.weight(1f), color = colors.textPrimary,
                                fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            if (tg.songs.isNotEmpty()) {
                                TextButton(onClick = { tg.openLibrary(controller); onDismiss() },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                                    Text(mwString("telegram.saved_songs", "Nhạc đã lưu"), color = colors.purpleBright, fontSize = 10.5.sp)
                                }
                            }
                        }
                        if (tg.searchSaveMessage.isNotBlank()) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                if (tg.searchSaveRunning) {
                                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp,
                                        color = colors.purpleBright)
                                    Spacer(Modifier.size(7.dp))
                                }
                                Text(telegramUiStatusWithoutBotName(tg.searchSaveMessage),
                                    color = colors.textSecondary, fontSize = 10.5.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        tg.botAccessRequirement?.let {
                            Surface(color = colors.backgroundSoft,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.borderSoft)) {
                                Row(Modifier.fillMaxWidth().padding(9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Text(telegramMusicSubscriptionFallback(appLanguage),
                                        modifier = Modifier.weight(1f),
                                        color = colors.textSecondary, fontSize = 11.sp)
                                    TextButton(onClick = { tg.openBot() }) {
                                        Text(telegramOpenBotFallback(appLanguage),
                                            color = colors.purpleBright, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        tg.waitingForAudio?.let { selected ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text(mwString("telegram.fetching", "Đang lấy: {name}", "name" to selected), modifier = Modifier.weight(1f),
                                    color = colors.textSecondary, fontSize = 11.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                TextButton(onClick = { tg.cancelPlayRequest() }) { Text(mwString("common.cancel", "Hủy")) }
                            }
                        }
                        if (tg.driveUploading) {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                val stageLabel = when (tg.driveUploadStage) {
                                    "telegram" -> mwString("telegram.drive_fetch", "Đang lấy file từ Telegram lên Drive…")
                                    "checking" -> mwString("telegram.drive_check", "Đang kiểm tra các file trên Google Drive…")
                                    "upload" -> mwString("telegram.drive_upload", "Đang tải lên Google Drive…")
                                    else -> mwString("telegram.drive_prepare", "Đang chuẩn bị sao lưu lên Google Drive…")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    if (tg.driveUploadStage != "upload") {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    }
                                    Text(stageLabel, color = colors.textSecondary, fontSize = 11.sp)
                                }
                                val (sentBytes, allBytes) = if (tg.driveUploadStage == "telegram") {
                                    tg.driveTelegramBytesReceived to tg.driveTelegramTotalBytes
                                } else {
                                    tg.driveBytesUploaded to tg.driveTotalBytes
                                }
                                if (tg.driveUploadStage in setOf("telegram", "upload") && allBytes > 0L) {
                                    val ratio = (sentBytes.toFloat() / allBytes.toFloat()).coerceIn(0f, 1f)
                                    LinearProgressIndicator(progress = ratio,
                                        modifier = Modifier.fillMaxWidth(), color = colors.purpleBright)
                                    Text("${(ratio * 100).toInt()}% · " +
                                            "${telegramBytesLabel(sentBytes)} / ${telegramBytesLabel(allBytes)}",
                                        color = colors.textSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                        tg.error?.let { Text(telegramUiStatusWithoutBotName(it), color = Color(0xFFDE655C), fontSize = 11.sp) }
                        // Playback status is already visible in MeloWave's mini-player;
                        // keep this screen focused on compact search results.
                        // NEVER rebuild search rows from tg.results here: probeMetadata()
                        // causes the bot to send or edit detail menus in the same chat.
                        val tracks = displayedSearchTracks
                        // A selection always belongs to the current visible results only.
                        val currentKeys = tracks.map { it.stableKey }.toSet()
                        val chosenTracks = tracks.filter { it.stableKey in selectedResultKeys }
                        val allPlayable = telegramMixedSearchQueue(tg, tracks)
                        val selectedPlayable = telegramMixedSearchQueue(tg, chosenTracks)
                        val allReady = tracks.isNotEmpty() &&
                                tracks.all { telegramSearchEntryReadyForQueue(tg, it) }
                        val selectedReady = chosenTracks.isNotEmpty() &&
                                chosenTracks.all { telegramSearchEntryReadyForQueue(tg, it) }
                        Row(modifier = Modifier.fillMaxWidth().height(32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!selectionMode) {
                                TextButton(modifier = Modifier.height(30.dp), onClick = { selectionMode = true; selectedResultKeys.clear() },
                                    enabled = tracks.isNotEmpty(),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp)) {
                                    Text(mwString("telegram.select_song_album", "Chọn bài / Album"), fontSize = 10.5.sp, color = colors.purpleBright)
                                }
                                TextButton(modifier = Modifier.height(30.dp), onClick = { tg.playSearchPlaylist(allPlayable, controller, keyword.trim()) },
                                    enabled = allReady && allPlayable.isNotEmpty() && tg.canStartSearchPlaylist,
                                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 1.dp)) {
                                    Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(16.dp),
                                        tint = colors.purpleBright)
                                    Text(mwString("telegram.play_all", "Phát tất cả ({count})", "count" to allPlayable.size), fontSize = 10.5.sp,
                                        color = colors.purpleBright)
                                }
                            } else {
                                TextButton(modifier = Modifier.height(30.dp), onClick = {
                                    if (selectedResultKeys.size == currentKeys.size) selectedResultKeys.clear()
                                    else { selectedResultKeys.clear(); selectedResultKeys.addAll(tracks.map { it.stableKey }) }
                                }, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)) {
                                    Text(if (selectedResultKeys.size == currentKeys.size) mwString("library.unselect_all", "Bỏ chọn tất cả")
                                    else mwString("library.select_all", "Chọn tất cả"), fontSize = 10.sp, color = colors.purpleBright)
                                }
                                TextButton(modifier = Modifier.height(30.dp), onClick = {
                                    tg.playSearchPlaylist(selectedPlayable, controller, keyword.trim())
                                    selectionMode = false
                                    selectedResultKeys.clear()
                                }, enabled = selectedReady && selectedPlayable.isNotEmpty() && tg.canStartSearchPlaylist,
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)) {
                                    Text(mwString("telegram.play_selected", "▶ Phát {count} bài", "count" to selectedPlayable.size), fontSize = 10.sp,
                                        color = colors.purpleBright)
                                }
                                TextButton(modifier = Modifier.height(30.dp), onClick = {
                                    selectionMode = false; selectedResultKeys.clear()
                                }, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)) {
                                    Text(mwString("common.cancel", "Hủy"), fontSize = 10.sp, color = colors.textSecondary)
                                }
                            }
                        }
                        // Bot navigation controls appear once, at the bottom of the list.
                        val controlResult = searchPageSnapshot.asReversed()
                            .map { it.result }
                            .firstOrNull { result ->
                                result.buttons.any { button ->
                                    !isTelegramTrackButton(button) &&
                                        !universalIsParsedSongButton(result, button, searchPageSnapshot)
                                }
                            }
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (tracks.isEmpty()) {
                                item {
                                    val botReply = tg.results.lastOrNull {
                                        !it.outgoing && it.text.isNotBlank()
                                    }?.text
                                    // Do not dump an unrelated foreign-language list
                                    // back onto the screen when matching filtered it out.
                                    val emptyLabel = if (rawSearchTracks.isNotEmpty() && unrelatedCount > 0)
                                        mwString("telegram.no_matching_tracks",
                                            telegramNoMatchesFallback(appLanguage, rawSearchTracks.size),
                                            "count" to rawSearchTracks.size)
                                    else telegramUiStatusWithoutBotName(botReply ?: mwString(
                                        "telegram.empty_search", "Nhập từ khóa để tìm nhạc trên bot."))
                                    Text(emptyLabel, color = colors.textSecondary, fontSize = 12.sp,
                                        lineHeight = 18.sp, modifier = Modifier.padding(vertical = 12.dp))
                                }
                            } else {
                                items(tracks, key = { it.stableKey }) { track ->
                                    val entryKey = tg.selectionKey(track)
                                    val detail = entryKey?.let { tg.selectedDetails[it] }
                                    val realAudio = track.audio ?: detail?.audio
                                    val album = track.isAlbum ||
                                            (detail?.count ?: 0) > 1 || (detail?.tracks?.size ?: 0) > 1
                                    val preview = remember(track.stableKey, realAudio?.messageId) {
                                        SongItem(
                                            id = if (realAudio != null)
                                                "tg_search_${realAudio.chatId}_${realAudio.messageId}"
                                            else "tg_choice_${track.stableKey}",
                                            title = track.title, artist = track.artist,
                                            uri = Uri.EMPTY, durationMs = realAudio?.durationMs ?: track.durationMs,
                                            source = SongSource.TELEGRAM, mimeType = realAudio?.mimeType,
                                            sizeBytes = realAudio?.size?.takeIf { it > 0L }
                                        )
                                    }
                                    val downloadable = if (album) {
                                        val childTracks = detail?.tracks.orEmpty()
                                        telegramAlbumQueueTracks(tg, track, childTracks)
                                    } else listOf(track)
                                    fun saveTo(destination: TelegramSearchSaveDestination) {
                                        controller.saveTelegramSearchSelection(downloadable,
                                            keyword.trim(), destination)
                                    }
                                    UnifiedMusicResultRow(
                                        song = preview, controller = controller,
                                        telegramAudio = realAudio, isAlbum = album,
                                        albumCount = detail?.count ?: detail?.tracks?.size,
                                        saveKey = track.stableKey,
                                        technicalOverride = detail?.technicalText?.takeIf { it.isNotBlank() }
                                            ?: tg.knownSearchTechnicalText(track),
                                        selectionMode = selectionMode,
                                        selected = selectedResultKeys.contains(track.stableKey),
                                        // Show progress on the exact item being verified,
                                        // never keep the search-field spinner running.
                                        isChecking = tg.metadataPreviewActiveKey == track.stableKey,
                                        unresolved = tg.metadataPreviewStatuses[track.stableKey] ==
                                                TelegramMetadataCheckStatus.UNKNOWN &&
                                                !tg.metadataPreviewRunning,
                                        onDownload = if (downloadable.isNotEmpty())
                                            ({ saveTo(TelegramSearchSaveDestination.DEVICE) }) else null,
                                        onDrive = if (downloadable.isNotEmpty())
                                            ({ saveTo(TelegramSearchSaveDestination.DRIVE) }) else null
                                    ) {
                                        if (selectionMode) {
                                            if (selectedResultKeys.contains(track.stableKey))
                                                selectedResultKeys.remove(track.stableKey)
                                            else selectedResultKeys.add(track.stableKey)
                                        } else if (album) {
                                            openedTelegramEntry = track
                                            albumSongSelectMode = false
                                            selectedAlbumSongKeys.clear()
                                        } else if (realAudio != null) {
                                            tg.cancelSearchMetadataPrefetch()
                                            tg.playFromBot(realAudio, controller)
                                            onDismiss()
                                        } else if ((track.isNumericReplyChoice || track.isDirectMusicBotChoice) &&
                                            tg.canStartSearchPlaylist) {
                                            // Send the bot's actual song button, wait for its audio.
                                            tg.playSearchResult(track, controller)
                                            // Keep the dialog visible for access/other bot errors.
                                            if (!track.isDirectMusicBotChoice) onDismiss()
                                        } else if ((detail?.count ?: 0) == 1 && tg.canStartSearchPlaylist) {
                                            tg.playSearchPlaylist(listOf(track), controller, keyword.trim())
                                            onDismiss()
                                        } else {
                                            openedTelegramEntry = track
                                            tg.resolveSearchResultOnTap(track, keyword.trim(), controller)
                                        }
                                    }
                                }
                            }
                            if (controlResult != null) {
                                item(key = "telegram_bot_controls") {
                                    val actions = controlResult.buttons.filterNot {
                                        isTelegramTrackButton(it) || TELEGRAM_NUMBERED_TRACK.containsMatchIn(it.label) ||
                                            universalIsParsedSongButton(controlResult, it, searchPageSnapshot)
                                    }
                                    val mooPage = if (tg.isMooMusicBot) controlResult.buttons.firstNotNullOfOrNull { b ->
                                        MOO_MUSIC_PAGE_COUNT.matchEntire(b.label.trim())?.let { match ->
                                            (match.groupValues[1].toIntOrNull() ?: 0) to
                                                (match.groupValues[2].toIntOrNull() ?: 0)
                                        }
                                    } else null
                                    val pager = actions.filter { telegramPagerKind(it) != TelegramPagerKind.OTHER }
                                    val others = actions.filter { telegramPagerKind(it) == TelegramPagerKind.OTHER &&
                                        !(tg.isMooMusicBot && MOO_MUSIC_PAGE_COUNT.matches(it.label.trim())) }
                                    if (pager.isNotEmpty()) {
                                        val current = pager.firstOrNull { telegramPagerKind(it) == TelegramPagerKind.CURRENT }
                                            ?.label?.let(::telegramPagerNumber) ?: mooPage?.first
                                        val last = pager.firstOrNull { telegramPagerKind(it) == TelegramPagerKind.LAST }
                                            ?.label?.let(::telegramPagerNumber) ?: mooPage?.second
                                        val numericPages = pager.filter {
                                            telegramPagerKind(it) == TelegramPagerKind.NUMBER
                                        }
                                        val previous = pager.firstOrNull {
                                            telegramPagerKind(it) == TelegramPagerKind.PREVIOUS
                                        } ?: numericPages.firstOrNull {
                                            current != null && telegramPagerNumber(it.label) == current - 1
                                        } ?: pager.firstOrNull {
                                            current == 2 && telegramPagerKind(it) == TelegramPagerKind.FIRST
                                        }
                                        val next = pager.firstOrNull {
                                            telegramPagerKind(it) == TelegramPagerKind.NEXT
                                        } ?: numericPages.firstOrNull {
                                            current != null && telegramPagerNumber(it.label) == current + 1
                                        } ?: pager.firstOrNull {
                                            current != null && (
                                                (last == current + 1 && telegramPagerKind(it) == TelegramPagerKind.LAST) ||
                                                (tg.isMooMusicBot && last != null && current < last &&
                                                    telegramPagerKind(it) == TelegramPagerKind.LAST))
                                        }
                                        Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(onClick = {
                                                previous?.let { openBotSearchPage(controlResult, it) }
                                            }, enabled = previous != null && !tg.busy,
                                                modifier = Modifier.weight(1f).heightIn(min = 41.dp),
                                                shape = RoundedCornerShape(13.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)) {
                                                Icon(Icons.Rounded.ChevronLeft, contentDescription = null,
                                                    modifier = Modifier.size(19.dp))
                                                Spacer(Modifier.size(3.dp))
                                                Text(mwString("telegram.previous", "Trước"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Text(mwString("telegram.page", "Trang {current}{total}", "current" to (current ?: "–"), "total" to (last?.let { " / $it" }.orEmpty())),
                                                color = colors.textSecondary, fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1.1f))
                                            OutlinedButton(onClick = {
                                                next?.let { openBotSearchPage(controlResult, it) }
                                            }, enabled = next != null && !tg.busy,
                                                modifier = Modifier.weight(1f).heightIn(min = 41.dp),
                                                shape = RoundedCornerShape(13.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)) {
                                                Text(mwString("telegram.next", "Sau"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Spacer(Modifier.size(3.dp))
                                                Icon(Icons.Rounded.ChevronRight, contentDescription = null,
                                                    modifier = Modifier.size(19.dp))
                                            }
                                        }
                                    }
                                    if (others.isNotEmpty()) {
                                        others.chunked(3).forEach { rowActions ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                                rowActions.forEach { button ->
                                                    OutlinedButton(
                                                        onClick = { openBotSearchPage(controlResult, button) },
                                                        enabled = !tg.busy && !tg.downloading && tg.waitingForAudio == null,
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 3.dp)
                                                    ) {
                                                        val actionName = button.label.lowercase(Locale.ROOT)
                                                        val actionIcon = when {
                                                            actionName.contains("download") ||
                                                                    actionName.contains("tải") -> Icons.Rounded.Download
                                                            actionName.contains("back") ||
                                                                    actionName.contains("quay lại") -> Icons.Rounded.ArrowBack
                                                            actionName.contains("search") ||
                                                                    actionName.contains("tìm") -> Icons.Rounded.Search
                                                            else -> Icons.Rounded.ChevronRight
                                                        }
                                                        Icon(actionIcon, contentDescription = null,
                                                            tint = colors.purpleBright,
                                                            modifier = Modifier.size(15.dp))
                                                        Spacer(Modifier.size(3.dp))
                                                        Text(button.label, color = colors.purpleBright,
                                                            fontSize = 10.sp, maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } // normal Telegram search page
                }
                if (showBotSettings || tg.authStep != TelegramAuthStep.READY) {
                    tg.error?.let { Text(telegramUiStatusWithoutBotName(it), color = Color(0xFFDE655C), fontSize = 11.sp) }
                    if (tg.busy) Text(mwString("telegram.processing", "Đang xử lý kết nối Telegram…"), color = colors.textSecondary, fontSize = 11.sp)
                    if (tg.restoringSavedBot) Text(mwString("telegram.restore_processing", "Đang khôi phục bot đã liên kết…"),
                        color = colors.textSecondary, fontSize = 11.sp)
                }
            }
        }
    }
    if (showTelegramGuide) {
        TelegramConnectionGuide(onDismiss = { showTelegramGuide = false })
    }
}

// Inline bot selection + subsequently delivered audio are one logical track.
internal data class TelegramUnifiedTrack(
    val stableKey: String,
    val result: TelegramBotResult,
    val button: TelegramBotAction? = null,
    val audio: TelegramAudioResult? = null,
    val title: String,
    val artist: String,
    val durationMs: Long,
    // A bot's album/folder choice is NOT an audio file: do not show size/bitrate.
    val isAlbum: Boolean = false,
    // Playback of album tracks must reopen the parent menu: old Telegram
    // callback IDs can become invalid after entering another album.
    val albumParent: TelegramUnifiedTrack? = null,
    val albumTrackPosition: Int = -1,
    // @vkmusic_bot: number is a reply-keyboard CHOICE, not an MP3 or a page number.
    // A real audio/document message is still required before Media3 playback.
    val isNumericReplyChoice: Boolean = false,
    // @MooMusicBot / @LyBot: known song button; still must wait for real audio.
    val isDirectMusicBotChoice: Boolean = false
)

/** A search result is a selection, not proof of a folder.
 * Only an explicit per-result count of >= 2 audio files/tracks establishes an album.
 * Never infer an album from a release year, keywords or missing file metadata.
 * A count on the entire search response cannot be attributed to each button.
 */
private fun telegramChoiceIsAlbum(button: TelegramBotAction, durationMs: Long,
                                  verifiedFileCount: Int? = null): Boolean {
    // The only acceptable proof is a callback-linked response with >= 2 files.
    // Even a label with a year, "Album", or a file extension cannot override it.
    return (verifiedFileCount ?: 0) > 1
}

private fun telegramSongMatchKey(raw: String): String {
    val normalized = java.text.Normalizer.normalize(raw.lowercase(Locale.ROOT),
        java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
    return normalized.replace('\u0111', 'd').replace(Regex("[^a-z0-9]+"), "")
}

private fun isMatchingTelegramTrack(a: TelegramUnifiedTrack, b: TelegramUnifiedTrack): Boolean {
    val titleA = telegramSongMatchKey(a.title)
    val titleB = telegramSongMatchKey(b.title)
    if (titleA.isEmpty() || titleA != titleB) return false
    val artistA = telegramSongMatchKey(a.artist).takeUnless {
        it.isEmpty() || it.contains("nghesichuabiet") || it.contains("unknown")
    }
    val artistB = telegramSongMatchKey(b.artist).takeUnless {
        it.isEmpty() || it.contains("nghesichuabiet") || it.contains("unknown")
    }
    if (artistA != null && artistB != null &&
        !artistA.contains(artistB) && !artistB.contains(artistA)) return false
    if (a.durationMs > 0L && b.durationMs > 0L &&
        kotlin.math.abs(a.durationMs - b.durationMs) > 25_000L) return false
    return true
}


/**
 * Validate SEARCH labels from numbered music bots before they reach the UI.
 * A Telegram bot's reply is not guaranteed to be relevant to the typed keyword.
 * This adapter does not change the bot reply, TDLib messages, callbacks or audio.
 * Non-numbered bots retain their legacy behavior, including album browsing.
 */
private fun telegramSearchTokens(value: String): List<String> {
    val plain = java.text.Normalizer.normalize(
        value.lowercase(Locale.ROOT).replace('đ', 'd'),
        java.text.Normalizer.Form.NFD
    ).replace(Regex("\\p{M}+"), "")
    // Keep non-Latin scripts (Korean, Japanese, Khmer, etc.), rather than
    // treating all non-ASCII text as an empty search term.
    return plain.split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.isNotBlank() }
}

internal fun telegramFilterSearchResults(
    entries: List<TelegramUnifiedTrack>,
    keyword: String,
    strictNumberedBot: Boolean
): List<TelegramUnifiedTrack> {
    if (!strictNumberedBot || keyword.isBlank() || entries.isEmpty()) return entries
    val queryTokens = telegramSearchTokens(keyword)
        .filter { it.length >= 2 || it.all(Char::isDigit) }.distinct()
    if (queryTokens.isEmpty()) return entries
    val minimumHits = when (queryTokens.size) {
        1, 2 -> queryTokens.size
        else -> kotlin.math.ceil(queryTokens.size * 0.75).toInt()
    }
    val compactKeyword = telegramSearchTokens(keyword).joinToString("")
    return entries.filter { track ->
        // Do not match the built-in 'Nghệ sĩ chưa biết' placeholder:
        // the word 'chưa' could otherwise produce a false Vietnamese hit.
        val artist = track.artist.takeUnless {
            it.equals("Nghệ sĩ chưa biết", ignoreCase = true) ||
                it.equals("Unknown artist", ignoreCase = true)
        }.orEmpty()
        val titleTokens = telegramSearchTokens(track.title)
        val allTokens = (titleTokens + telegramSearchTokens(artist)).toSet()
        val titleJoined = titleTokens.joinToString("")
        // Also allow spacing variations such as 'Bongbong' vs 'Bong Bong'.
        (compactKeyword.length >= 4 && titleJoined.contains(compactKeyword)) ||
            queryTokens.count { word -> allTokens.contains(word) } >= minimumHits
    }
}


/** Fallback texts for 15 UI languages until these keys are included in the JSON catalog. */
internal fun telegramMusicSubscriptionFallback(language: String): String =
    when (language.lowercase(Locale.ROOT).substringBefore('-')) {
        "en" -> "This bot requires partner-channel subscriptions before sending audio. Complete the steps in Telegram, then search again."
        "zh" -> "此机器人要求先订阅合作频道才能发送音频。请在 Telegram 中完成操作后重新搜索。"
        "ja" -> "音声を受け取るには提携チャンネルへの登録が必要です。Telegramで手続きを完了し、再検索してください。"
        "ko" -> "이 봇은 오디오 전송 전에 제휴 채널 구독을 요구합니다. Telegram에서 완료한 후 다시 검색하세요."
        "es" -> "El bot exige suscribirse a canales asociados antes de enviar audio. Hazlo en Telegram y vuelve a buscar."
        "fr" -> "Ce bot exige l’abonnement à des chaînes partenaires avant l’envoi audio. Terminez dans Telegram, puis relancez la recherche."
        "de" -> "Dieser Bot verlangt vor dem Audio-Versand Partnerkanal-Abos. Schließe sie in Telegram ab und suche erneut."
        "ru" -> "Бот требует подписки на партнёрские каналы перед отправкой аудио. Выполните условия в Telegram и повторите поиск."
        "pt" -> "Este bot exige inscrição em canais parceiros antes de enviar áudio. Conclua no Telegram e pesquise de novo."
        "id" -> "Bot ini mewajibkan langganan kanal mitra sebelum mengirim audio. Selesaikan di Telegram lalu cari lagi."
        "hi" -> "ऑडियो भेजने से पहले इस बॉट को पार्टनर चैनलों की सदस्यता चाहिए। Telegram में पूरा करके फिर खोजें।"
        "ar" -> "يتطلب هذا البوت الاشتراك في قنوات شريكة قبل إرسال الصوت. أكمل الخطوات في Telegram ثم أعد البحث."
        "th" -> "บอตนี้กำหนดให้ติดตามช่องพาร์ทเนอร์ก่อนส่งเสียง โปรดดำเนินการใน Telegram แล้วค้นหาอีกครั้ง"
        "it" -> "Questo bot richiede l'iscrizione a canali partner prima di inviare audio. Completa in Telegram e riprova."
        else -> "Bot yêu cầu đăng ký kênh đối tác trước khi gửi âm thanh. Hãy hoàn tất trong Telegram rồi tìm lại bài."
    }

internal fun telegramOpenBotFallback(language: String): String =
    when (language.lowercase(Locale.ROOT).substringBefore('-')) {
        "en" -> "Open Telegram"; "zh" -> "打开 Telegram"; "ja" -> "Telegram を開く"
        "ko" -> "Telegram 열기"; "es" -> "Abrir Telegram"; "fr" -> "Ouvrir Telegram"
        "de" -> "Telegram öffnen"; "ru" -> "Открыть Telegram"; "pt" -> "Abrir Telegram"
        "id" -> "Buka Telegram"; "hi" -> "Telegram खोलें"; "ar" -> "فتح Telegram"
        "th" -> "เปิด Telegram"; "it" -> "Apri Telegram"; else -> "Mở Telegram"
    }

internal fun telegramMatchedCountFallback(language: String, shown: Int, total: Int): String =
    when (language.lowercase(Locale.ROOT).substringBefore('-')) {
        "en" -> "Telegram · ${shown}/${total} relevant results"
        "zh" -> "Telegram · ${shown}/${total} 条相关结果"
        "ja" -> "Telegram · 関連する結果 ${shown}/${total} 件"
        "ko" -> "Telegram · 관련 결과 ${shown}/${total}개"
        "es" -> "Telegram · ${shown}/${total} resultados relevantes"
        "fr" -> "Telegram · ${shown}/${total} résultats pertinents"
        "de" -> "Telegram · ${shown}/${total} passende Treffer"
        "ru" -> "Telegram · Подходящих: ${shown}/${total}"
        "pt" -> "Telegram · ${shown}/${total} resultados relevantes"
        "id" -> "Telegram · ${shown}/${total} hasil yang cocok"
        "hi" -> "Telegram · ${shown}/${total} संबंधित परिणाम"
        "ar" -> "Telegram · ${shown}/${total} نتائج مطابقة"
        "th" -> "Telegram · ผลลัพธ์ที่ตรง ${shown}/${total} รายการ"
        "it" -> "Telegram · ${shown}/${total} risultati pertinenti"
        else -> "Telegram · ${shown}/${total} kết quả phù hợp"
    }

internal fun telegramNoMatchesFallback(language: String, total: Int): String =
    when (language.lowercase(Locale.ROOT).substringBefore('-')) {
        "en" -> "The bot returned ${total} songs, but none match your search. Try song + artist or another bot."
        "zh" -> "机器人返回了 ${total} 首歌曲，但都不匹配。请尝试歌曲名加歌手，或更换机器人。"
        "ja" -> "ボットは ${total} 曲返しましたが、一致する曲がありません。曲名と歌手名、または別のボットをお試しください。"
        "ko" -> "봇이 ${total}곡을 반환했지만 일치하는 곡이 없습니다. 노래 제목과 가수 이름으로 검색하거나 다른 봇을 사용하세요."
        "es" -> "El bot devolvió ${total} canciones, pero ninguna coincide. Prueba título y artista u otro bot."
        "fr" -> "Le bot a renvoyé ${total} titres, mais aucun ne correspond. Essayez titre et artiste ou un autre bot."
        "de" -> "Der Bot lieferte ${total} Titel, aber keinen passenden. Versuche Titel und Interpret oder einen anderen Bot."
        "ru" -> "Бот вернул ${total} треков, но совпадений нет. Укажите песню и исполнителя или смените бота."
        "pt" -> "O bot retornou ${total} músicas, mas nenhuma corresponde. Tente título e artista ou outro bot."
        "id" -> "Bot mengirim ${total} lagu, tetapi tidak ada yang cocok. Coba judul dan penyanyi atau bot lain."
        "hi" -> "बॉट ने ${total} गाने भेजे, लेकिन कोई मेल नहीं खाता। गीत और कलाकार का नाम या दूसरा बॉट आज़माएँ।"
        "ar" -> "أعاد البوت ${total} أغانٍ دون نتائج مطابقة. جرّب اسم الأغنية والفنان أو بوتًا آخر."
        "th" -> "บอตส่งเพลงมา ${total} เพลง แต่ไม่ตรงคำค้น ลองชื่อเพลงและศิลปินหรือเปลี่ยนบอต"
        "it" -> "Il bot ha trovato ${total} brani, ma nessuno corrisponde. Prova titolo e artista o un altro bot."
        else -> "Bot trả ${total} bài nhưng không khớp từ khóa. Thử tên bài + ca sĩ hoặc chọn bot khác."
    }

internal fun telegramHiddenCountFallback(language: String, count: Int): String =
    when (language.lowercase(Locale.ROOT).substringBefore('-')) {
        "en" -> "Hidden ${count} unrelated results."
        "zh" -> "已隐藏 ${count} 条不相关结果。"
        "ja" -> "関連しない結果を ${count} 件非表示にしました。"
        "ko" -> "관련 없는 결과 ${count}개를 숨겼습니다."
        "es" -> "Se ocultaron ${count} resultados no relacionados."
        "fr" -> "${count} résultats non pertinents masqués."
        "de" -> "${count} unpassende Treffer ausgeblendet."
        "ru" -> "Скрыто неподходящих результатов: ${count}."
        "pt" -> "${count} resultados irrelevantes ocultados."
        "id" -> "${count} hasil yang tidak relevan disembunyikan."
        "hi" -> "${count} असंबंधित परिणाम छिपाए गए।"
        "ar" -> "تم إخفاء ${count} نتائج غير ذات صلة."
        "th" -> "ซ่อนผลลัพธ์ที่ไม่ตรง ${count} รายการ"
        "it" -> "Nascosti ${count} risultati non pertinenti."
        else -> "Đã ẩn ${count} kết quả không phù hợp."
    }

// A VK Music search reply contains "1. Artist – Song 4:42 4.4M 131k" in
// its MESSAGE, while the actual reply-keyboard buttons are just "1" ... "8".
// Do not treat these numbers as page numbers or arbitrary song names.
private val VK_MUSIC_NUMBER = Regex("^[1-8]$")
private val VK_MUSIC_ROW = Regex("""^\s*([1-8])[.)]\s+(.+?)\s*$""")
private val VK_MUSIC_DURATION = Regex("""(?<!\d)(\d{1,3}):([0-5]\d)(?!\d)""")
private val VK_MUSIC_ARTIST_SEPARATOR = Regex("""\s+[-–—]\s+""")

private fun vkMusicNumberButton(button: TelegramBotAction): Int? =
    button.label.trim().takeIf { button.kind == TelegramActionKind.TEXT ||
            button.kind == TelegramActionKind.CALLBACK }
        ?.takeIf { VK_MUSIC_NUMBER.matches(it) }?.toIntOrNull()

/** No automatic bot messages and no file access. Pure, bounded search-page parsing. */
private fun vkMusicNumberedTracks(result: TelegramBotResult): List<TelegramUnifiedTrack> {
    if (result.outgoing || result.text.isBlank()) return emptyList()
    val numberButtons = result.buttons.mapNotNull { button ->
        vkMusicNumberButton(button)?.let { number -> number to button }
    }.toMap()
    if (numberButtons.isEmpty()) return emptyList()

    val found = mutableListOf<TelegramUnifiedTrack>()
    result.text.lineSequence().forEach { line ->
        val row = VK_MUSIC_ROW.matchEntire(line.trim()) ?: return@forEach
        val number = row.groupValues[1].toIntOrNull() ?: return@forEach
        val button = numberButtons[number] ?: return@forEach
        val fullLabel = row.groupValues[2].trim()
        val durationMatch = VK_MUSIC_DURATION.findAll(fullLabel).lastOrNull()
            ?: return@forEach
        val minutes = durationMatch.groupValues[1].toLongOrNull() ?: return@forEach
        val seconds = durationMatch.groupValues[2].toLongOrNull() ?: return@forEach
        val songLabel = fullLabel.substring(0, durationMatch.range.first).trim()
        if (songLabel.length !in 2..180) return@forEach
        val parts = VK_MUSIC_ARTIST_SEPARATOR.split(songLabel, limit = 2)
        val artist = if (parts.size == 2) parts[0].trim() else "Nghệ sĩ chưa biết"
        val title = if (parts.size == 2) parts[1].trim() else songLabel
        if (title.isBlank()) return@forEach
        found.add(TelegramUnifiedTrack(
            stableKey = "vk_number_${result.messageId}_$number", result = result,
            button = button, title = title, artist = artist,
            durationMs = (minutes * 60L + seconds) * 1_000L,
            isNumericReplyChoice = true
        ))
    }
    // Opted-in VK bot only: a numbered title + duration and an exact keyboard
    // button are enough even if the search returns only ONE song.
    return if (found.isNotEmpty()) found.distinctBy { it.stableKey }.sortedBy {
        vkMusicNumberButton(it.button!!) ?: Int.MAX_VALUE
    } else emptyList()
}

// @musgetbot (Musiqa Shazam Tarona): its message has ten numbered title/time
// lines, but the reply-keyboard choices contain ONLY "1".."10". The arrow is
// navigation, not a song. This adapter must NEVER be applied to other bots.
private val MUSGET_NUMBER = Regex("^(?:10|[1-9])$")
private val MUSGET_ROW_START = Regex("""^\s*(10|[1-9])[.)]\s+(.+?)\s*$""")
private val MUSGET_DURATION = Regex("""(?<!\d)(?:(\d{1,2}):)?(\d{1,3}):([0-5]\d)\s*$""")

private fun musgetNumberButton(button: TelegramBotAction): Int? =
    button.label.trim().takeIf { button.kind == TelegramActionKind.TEXT ||
            button.kind == TelegramActionKind.CALLBACK }
        ?.takeIf { MUSGET_NUMBER.matches(it) }?.toIntOrNull()

/** Pure parse; joins multiline titles but only follows numbers the bot actually offers. */
private fun musgetNumberedTracks(result: TelegramBotResult): List<TelegramUnifiedTrack> {
    if (result.outgoing || result.text.isBlank()) return emptyList()
    val numberButtons = result.buttons.mapNotNull { button ->
        musgetNumberButton(button)?.let { number -> number to button }
    }.toMap()
    if (numberButtons.isEmpty()) return emptyList()

    val rawRows = mutableListOf<Pair<Int, String>>()
    var activeNumber: Int? = null
    val activeText = StringBuilder()
    fun flush() {
        activeNumber?.let { number ->
            if (activeText.isNotBlank()) rawRows.add(number to activeText.toString().trim())
        }
        activeNumber = null
        activeText.setLength(0)
    }
    result.text.lineSequence().forEach { sourceLine ->
        val line = sourceLine.trim()
        val next = MUSGET_ROW_START.matchEntire(line)
        if (next != null) {
            flush()
            activeNumber = next.groupValues[1].toIntOrNull()
            activeText.append(next.groupValues[2].trim())
        } else if (activeNumber != null && line.isNotEmpty() && activeText.length < 320 &&
                !line.startsWith("/") && !line.startsWith("🔎") && !line.startsWith("🔍")) {
            // Text may wrap physically as well as visually on a narrow phone.
            activeText.append(' ').append(line)
        }
    }
    flush()

    val found = mutableListOf<TelegramUnifiedTrack>()
    rawRows.forEach { (number, labelWithTime) ->
        val button = numberButtons[number] ?: return@forEach
        val duration = MUSGET_DURATION.find(labelWithTime) ?: return@forEach
        val hours = duration.groupValues[1].toLongOrNull() ?: 0L
        val minutes = duration.groupValues[2].toLongOrNull() ?: return@forEach
        val seconds = duration.groupValues[3].toLongOrNull() ?: return@forEach
        val title = labelWithTime.substring(0, duration.range.first).trim()
        if (title.length !in 2..220) return@forEach
        found.add(TelegramUnifiedTrack(
            stableKey = "musget_number_${result.messageId}_$number",
            result = result, button = button, title = title,
            artist = "Nghệ sĩ chưa biết",
            durationMs = ((hours * 3600L + minutes * 60L + seconds) * 1000L),
            isNumericReplyChoice = true
        ))
    }
    return found.distinctBy { it.stableKey }.sortedBy {
        musgetNumberButton(it.button!!) ?: Int.MAX_VALUE
    }
}


// MooMusic renders the songs as TEXT keyboard buttons, e.g.
// "4. Quang Vinh - Lãng Quên Cuộc Tình (4:30)". Its page counter and
// 'Download all' actions must never be treated as playable tracks.
private val MOO_MUSIC_ROW = Regex("""^\s*(\d{1,3})[.)]\s+(.+?)\s+[-–—]\s+(.+?)\s*$""")
private val MOO_MUSIC_TIME = Regex("""\((?:(\d{1,2}):)?(\d{1,3}):([0-5]\d)\)\s*$""")
private val MOO_MUSIC_PAGE_COUNT = Regex("""^\s*(\d{1,3})\s+из\s+(\d{1,3})\s*$""")
private fun mooMusicSongAction(button: TelegramBotAction): Boolean =
    (button.kind == TelegramActionKind.TEXT || button.kind == TelegramActionKind.CALLBACK) &&
            MOO_MUSIC_ROW.matches(button.label.trim())

private fun mooMusicKeyboardTracks(result: TelegramBotResult): List<TelegramUnifiedTrack> {
    if (result.outgoing) return emptyList()
    return result.buttons.mapIndexedNotNull { index, button ->
        val row = MOO_MUSIC_ROW.matchEntire(button.label.trim()) ?: return@mapIndexedNotNull null
        val artist = row.groupValues[2].trim()
        val right = row.groupValues[3].trim()
        val match = MOO_MUSIC_TIME.find(right)
        val title = (if (match != null) right.substring(0, match.range.first) else right).trim()
        if (artist.length !in 2..100 || title.length !in 2..180) return@mapIndexedNotNull null
        val duration = if (match == null) 0L else {
            val hours = match.groupValues[1].toLongOrNull() ?: 0L
            val minutes = match.groupValues[2].toLongOrNull() ?: 0L
            val seconds = match.groupValues[3].toLongOrNull() ?: 0L
            (hours * 3600L + minutes * 60L + seconds) * 1000L
        }
        TelegramUnifiedTrack("moo_${result.messageId}_$index", result, button = button,
            title = title, artist = artist, durationMs = duration,
            isDirectMusicBotChoice = true)
    }
}

// LyBot renders TITLE – ARTIST as inline CALLBACK buttons (Unicode en dash).
// Ignore the separate "Quang Vinh" artist, "Album", and page-icon buttons.
private val LY_BOT_SONG_ROW = Regex("""^\s*(.+?)\s+[-–—]\s+([^\n]+?)\s*$""")
private fun lyBotSongAction(button: TelegramBotAction): Boolean {
    if (button.kind != TelegramActionKind.TEXT && button.kind != TelegramActionKind.CALLBACK) return false
    if (telegramPagerKind(button) != TelegramPagerKind.OTHER) return false
    val text = button.label.trim()
    if (text.startsWith("👤") || text.startsWith("💿") || text.startsWith("🔍")) return false
    val match = LY_BOT_SONG_ROW.matchEntire(text) ?: return false
    return match.groupValues[1].trim().length >= 2 && match.groupValues[2].trim().length >= 2
}
private fun lyBotKeyboardTracks(result: TelegramBotResult): List<TelegramUnifiedTrack> {
    if (result.outgoing) return emptyList()
    return result.buttons.mapIndexedNotNull { index, button ->
        if (!lyBotSongAction(button)) return@mapIndexedNotNull null
        val row = LY_BOT_SONG_ROW.matchEntire(button.label.trim()) ?: return@mapIndexedNotNull null
        val title = row.groupValues[1].trim()
        val artist = row.groupValues[2].trim()
        TelegramUnifiedTrack("ly_${result.messageId}_$index", result, button = button,
            title = title, artist = artist, durationMs = 0L,
            isDirectMusicBotChoice = true)
    }
}


// ===== Universal bot response adapter (no bot username or special API required) =====
// Only PARSES the bot's existing reply markup and text. It never sends commands,
// probes all results, downloads media or assumes a returned label is playable.
private val UNIVERSAL_BOT_NUMBER = Regex("^(?:[1-9]|[12][0-9]|30)$")
private val UNIVERSAL_BOT_ROW = Regex("""^\s*([1-9]|[12]\d|30)[.)]\s+(.+?)\s*$""")
private val UNIVERSAL_BOT_DURATION = Regex("""(?<!\d)(?:(\d{1,2}):)?(\d{1,3}):([0-5]\d)\s*\)?\s*$""")
private val UNIVERSAL_BOT_SPLIT = Regex("""\s+[-–—]\s+""")
private val UNIVERSAL_BOT_DIRECT_ROW = Regex("""^\s*(?:\d{1,2}[.)]\s+)?(.+?)\s+[-–—]\s+(.+?)\s*$""")

private fun universalChoiceNumber(action: TelegramBotAction): Int? =
    if (action.kind == TelegramActionKind.TEXT || action.kind == TelegramActionKind.CALLBACK)
        action.label.trim().takeIf { UNIVERSAL_BOT_NUMBER.matches(it) }?.toIntOrNull()
    else null

private fun universalSongLabel(value: String, keyword: String): Triple<String, String, Long>? {
    val match = UNIVERSAL_BOT_DURATION.find(value)
    val duration = if (match != null) {
        val hours = match.groupValues[1].toLongOrNull() ?: 0L
        val minutes = match.groupValues[2].toLongOrNull() ?: 0L
        val seconds = match.groupValues[3].toLongOrNull() ?: 0L
        if (hours > 12L || minutes > 180L) return null
        ((hours * 3600L) + (minutes * 60L) + seconds) * 1000L
    } else 0L
    val caption = (if (match != null) value.substring(0, match.range.first) else value)
        .trim().removeSuffix("(").trim().removePrefix("🎵").removePrefix("🎶").trim()
    if (caption.length !in 2..220 || caption.startsWith("/")) return null
    val parts = UNIVERSAL_BOT_SPLIT.split(caption, limit = 2)
    if (parts.size != 2) return Triple("Nghệ sĩ chưa biết", caption, duration)
    val left = parts[0].trim()
    val right = parts[1].trim()
    if (left.length < 2 || right.length < 2) return Triple("Nghệ sĩ chưa biết", caption, duration)
    // The same bot can return both "Song - Artist" and "Artist - Song".
    // Prefer a conservative title over inventing an incorrect artist.
    val q = telegramSearchTokens(keyword).joinToString(" ")
    val l = telegramSearchTokens(left).joinToString(" ")
    val r = telegramSearchTokens(right).joinToString(" ")
    val lw = telegramSearchTokens(left).size
    val rw = telegramSearchTokens(right).size
    return when {
        q.isNotBlank() && r == q -> Triple(left, right, duration)
        q.isNotBlank() && l == q && lw == 1 -> Triple(right, left, duration)
        lw <= 2 && rw >= 3 -> Triple(left, right, duration)
        rw <= 2 && lw >= 3 -> Triple(right, left, duration)
        else -> Triple("Nghệ sĩ chưa biết", caption, duration)
    }
}

private fun universalNumberedRows(text: String): List<Pair<Int, String>> {
    val rows = mutableListOf<Pair<Int, String>>()
    var active: Int? = null
    val label = StringBuilder()
    fun flush() {
        if (active != null && label.isNotBlank()) rows.add(active!! to label.toString().trim())
        active = null
        label.setLength(0)
    }
    for (line in text.lineSequence().take(70)) {
        val trimmed = line.trim()
        val start = UNIVERSAL_BOT_ROW.matchEntire(trimmed)
        if (start != null) {
            flush()
            active = start.groupValues[1].toIntOrNull()
            label.append(start.groupValues[2])
        } else if (active != null && trimmed.isNotBlank() && label.length < 260 &&
            !trimmed.startsWith("/") && !trimmed.startsWith("🔎") &&
            !trimmed.startsWith("🔍") && !trimmed.startsWith("📥") &&
            !trimmed.contains("subscribe", ignoreCase = true) &&
            !trimmed.contains("download page", ignoreCase = true)) {
            label.append(' ').append(trimmed)
        } else if (active != null && (trimmed.startsWith("/") ||
            trimmed.contains("subscribe", ignoreCase = true))) {
            flush()
        }
    }
    flush()
    return rows.take(30).distinctBy { it.first }
}

/** Matches button 1 to song line 1, etc. The bot's real TEXT/CALLBACK action is preserved. */
private fun universalNumberedTracks(page: TelegramBotResult, searchText: String,
                                   keyword: String): List<TelegramUnifiedTrack> {
    if (page.outgoing) return emptyList()
    val choices = page.buttons.mapNotNull { button ->
        universalChoiceNumber(button)?.let { number -> number to button }
    }.toMap()
    if (choices.isEmpty()) return emptyList()
    val rows = universalNumberedRows(searchText)
    val matched = rows.mapNotNull { (number, text) ->
        val button = choices[number] ?: return@mapNotNull null
        val metadata = universalSongLabel(text, keyword) ?: return@mapNotNull null
        if (metadata.third == 0L && !UNIVERSAL_BOT_SPLIT.containsMatchIn(text))
            return@mapNotNull null
        TelegramUnifiedTrack(
            stableKey = "universal_number_${page.messageId}_$number",
            result = page, button = button, title = metadata.second,
            artist = metadata.first, durationMs = metadata.third,
            isNumericReplyChoice = true
        )
    }
    // 1..4 may also be a pagination menu. Require multiple song lines, or one
    // time-stamped song with only one matching keyboard choice.
    if (matched.size < 2 && !(matched.size == 1 && choices.size == 1 &&
            matched.first().durationMs > 0)) return emptyList()
    return matched.sortedBy { universalChoiceNumber(it.button!!) ?: Int.MAX_VALUE }
}

/** Direct song-title actions (callback or reply keyboard); excludes page/advertising controls. */
private fun universalDirectTracks(result: TelegramBotResult,
                                  keyword: String): List<TelegramUnifiedTrack> {
    if (result.outgoing) return emptyList()
    val candidates = result.buttons.mapIndexedNotNull { index, button ->
        if (button.kind != TelegramActionKind.TEXT && button.kind != TelegramActionKind.CALLBACK)
            return@mapIndexedNotNull null
        if (universalChoiceNumber(button) != null ||
            telegramPagerKind(button) != TelegramPagerKind.OTHER ||
            isTelegramDownloadButton(button.label)) return@mapIndexedNotNull null
        val label = button.label.trim()
        val low = label.lowercase(Locale.ROOT)
        if (label.length !in 6..225 || label.startsWith("/") ||
            low.startsWith("subscribe") || low.startsWith("album") ||
            low.startsWith("search") || low.startsWith("back") ||
            low.startsWith("👤") || low.startsWith("💿") ||
            low.startsWith("🔎") || low.startsWith("🔍") ||
            low.startsWith("➡") || low.startsWith("⬅")) return@mapIndexedNotNull null
        // A dash alone does not prove a music row; combine real song-shaped
        // title/artist segments with a group of keyboard song choices.
        val row = UNIVERSAL_BOT_DIRECT_ROW.matchEntire(label) ?: return@mapIndexedNotNull null
        if (row.groupValues[1].trim().length < 2 ||
            row.groupValues[2].trim().length < 2) return@mapIndexedNotNull null
        val metadata = universalSongLabel(label.replace(Regex("""^\d{1,2}[.)]\s+"""), ""),
            keyword) ?: return@mapIndexedNotNull null
        index to TelegramUnifiedTrack(
            stableKey = "universal_direct_${result.messageId}_$index",
            result = result, button = button, title = metadata.second,
            artist = metadata.first, durationMs = metadata.third,
            isDirectMusicBotChoice = true
        )
    }
    if (candidates.size < 2 && candidates.none { it.second.durationMs > 0L })
        return emptyList()
    return candidates.map { it.second }
}

/** Only actual parsed music choices, not numeric page buttons, are hidden from controls. */
private fun universalIsParsedSongButton(page: TelegramBotResult,
                                        action: TelegramBotAction,
                                        tracks: List<TelegramUnifiedTrack>): Boolean =
    tracks.any { track ->
        track.result.messageId == page.messageId && track.button?.let { button ->
            button.kind == action.kind && button.label == action.label &&
                button.callbackData.contentEquals(action.callbackData)
        } == true
    }

internal fun telegramUnifiedTracks(responses: List<TelegramBotResult>,
                                   verifiedFileCounts: Map<String, Int> = emptyMap(),
                                   vkNumberedSearch: Boolean = false,
                                   musgetNumberedSearch: Boolean = false,
                                   mooMusicSearch: Boolean = false,
                                   lyBotSearch: Boolean = false,
                                   searchKeyword: String = ""): List<TelegramUnifiedTrack> {
    val merged = mutableListOf<TelegramUnifiedTrack>()
    responses.forEachIndexed { index, result ->
        val dedicated = when {
            vkNumberedSearch -> vkMusicNumberedTracks(result)
            musgetNumberedSearch -> musgetNumberedTracks(result)
            mooMusicSearch -> mooMusicKeyboardTracks(result)
            lyBotSearch -> lyBotKeyboardTracks(result)
            else -> emptyList()
        }
        val adapted = if (dedicated.isNotEmpty()) dedicated else {
            // Some bots attach their numeric keyboard to a separate reply.
            // Match only nearby text from this same search, never old history.
            val nearbyText = (index downTo maxOf(0, index - 3))
                .map { responses[it].text }
                .firstOrNull { universalNumberedRows(it).isNotEmpty() }
                .orEmpty()
            val numbered = universalNumberedTracks(result, nearbyText, searchKeyword)
            if (numbered.isNotEmpty()) numbered else if (!mooMusicSearch && !lyBotSearch)
                universalDirectTracks(result, searchKeyword) else emptyList()
        }
        if (adapted.isNotEmpty()) {
            adapted.forEach { entry ->
                if (merged.none { it.stableKey == entry.stableKey }) merged.add(entry)
            }
        } else if (!mooMusicSearch && !lyBotSearch) {
            // Do not interpret page icons/artist/album buttons as songs when a
            // dedicated adapter found no music rows in a bot menu.
            result.buttons.filter { isTelegramTrackButton(it) ||
                    TELEGRAM_NUMBERED_TRACK.containsMatchIn(it.label) }
                .forEachIndexed { index, button ->
                    val (artist, title, duration) = songLabelMetadata(button.label)
                    val entry = TelegramUnifiedTrack("button_${result.messageId}_${index}",
                        result = result, button = button, title = title,
                        artist = artist, durationMs = duration,
                        isAlbum = telegramChoiceIsAlbum(button, duration,
                            verifiedFileCounts[telegramBotSelectionKey(result, button)]))
                    if (merged.none { isMatchingTelegramTrack(it, entry) }) merged.add(entry)
                }
        }
        result.audio?.let { audio ->
            val candidate = TelegramUnifiedTrack("audio_${result.messageId}", result = result,
                audio = audio, title = audio.title, artist = audio.artist,
                durationMs = audio.durationMs)
            val previous = merged.indexOfFirst { isMatchingTelegramTrack(it, candidate) }
            if (previous >= 0) {
                // Keep the old row key and position; now show its FLAC/MP3 metadata.
                merged[previous] = candidate.copy(stableKey = merged[previous].stableKey)
            } else merged.add(candidate)
        }
    }
    return merged.takeLast(60)
}

// ===== TelegramTdClient (merged into TelegramMusic.kt) =====
/**
 * A single user-account TDLib session, on this Android device only.
 * This is NOT Telegram Bot API, doesn't use bot tokens, and does not call MeloWave servers.
 * The caller owns one bridge for the lifetime of the MusicController.
 */
internal class TelegramTdClient(
    private val context: Context,
    private val onAuthorization: (TdApi.AuthorizationState) -> Unit,
    private val onMessage: (TdApi.Message) -> Unit,
    private val onMessageChanged: (Long, Long) -> Unit,
    private val onFileUpdated: (TdApi.File) -> Unit,
    private val onError: (String) -> Unit
) : Closeable {
    @Volatile private var client: Client? = null

    @Synchronized
    fun start() {
        if (client != null) return
        try {
            System.loadLibrary("tdjni")
            Client.execute(TdApi.SetLogVerbosityLevel(1))
            client = Client.create(Client.ResultHandler { response ->
                when (response) {
                    is TdApi.UpdateAuthorizationState -> onAuthorization(response.authorizationState)
                    is TdApi.UpdateNewMessage -> onMessage(response.message)
                    is TdApi.UpdateMessageContent -> onMessageChanged(response.chatId, response.messageId)
                    is TdApi.UpdateMessageEdited -> onMessageChanged(response.chatId, response.messageId)
                    is TdApi.UpdateFile -> onFileUpdated(response.file)
                    else -> Unit
                }
            }, null, null)
        } catch (failure: Throwable) {
            client = null
            onError("Không khởi động được TDLib: ${failure.message ?: "thiếu thư viện native"}")
        }
    }

    fun configure(apiId: Int, apiHash: String) {
        if (apiId <= 0 || apiHash.isBlank()) {
            onError("Chưa cấu hình TELEGRAM_API_ID và TELEGRAM_API_HASH khi build ứng dụng.")
            return
        }
        val root = File(context.noBackupFilesDir, "melowave_tdlib").apply { mkdirs() }
        val files = File(context.noBackupFilesDir, "melowave_tdlib_media").apply { mkdirs() }
        val request = TdApi.SetTdlibParameters().apply {
            useTestDc = false
            databaseDirectory = root.absolutePath
            filesDirectory = files.absolutePath
            useFileDatabase = true
            useChatInfoDatabase = true
            useMessageDatabase = true
            useSecretChats = false
            this.apiId = apiId
            this.apiHash = apiHash
            systemLanguageCode = "vi"
            deviceModel = Build.MODEL ?: "Android"
            systemVersion = Build.VERSION.RELEASE ?: "Android"
            applicationVersion = BuildConfig.VERSION_NAME
        }
        send(request)
    }

    fun send(request: TdApi.Function<*>) {
        val target = client ?: run {
            onError("TDLib chưa được khởi động.")
            return
        }
        target.send(request, Client.ResultHandler { result ->
            if (result is TdApi.Error) onError("Telegram: ${result.message} (${result.code})")
        })
    }

    suspend fun request(function: TdApi.Function<*>): TdApi.Object =
        suspendCancellableCoroutine { continuation ->
            val target = client
            if (target == null) {
                continuation.resumeWithException(IllegalStateException("TDLib chưa khởi động."))
                return@suspendCancellableCoroutine
            }
            target.send(function, Client.ResultHandler { result ->
                if (!continuation.isActive) return@ResultHandler
                if (result is TdApi.Error) {
                    continuation.resumeWithException(IllegalStateException("Telegram: ${result.message} (${result.code})"))
                } else {
                    continuation.resume(result)
                }
            })
        }

    override fun close() {
        val target = client ?: return
        client = null
        // TDLib receives an explicit Close; no phone background polling is created.
        target.send(TdApi.Close(), Client.ResultHandler { })
    }
}

// ===== TelegramStreamDataSource (merged into TelegramMusic.kt) =====
/**
 * Read TDLib's on-disk partial download as a seekable source for ExoPlayer.
 * A 512 KiB Telegram download range is requested ONLY when the player needs it.
 * There is no intermediate MP3 copy or HTTP proxy and no full-file download on Play.
 * The TDLib partial-file cache is periodically trimmed by TelegramMusicManager.
 */
internal class TelegramStreamDataSource(
    private val telegram: TelegramMusicManager
) : BaseDataSource(true) {
    private var originalUri: Uri? = null
    private var request: DataSpec? = null
    private var fileId = 0
    private var fileSize = 0L
    private var cursor = 0L
    private var remaining = 0L
    private var bufferedEnd = 0L
    private var reader: RandomAccessFile? = null
    private var opened = false

    override fun open(dataSpec: DataSpec): Long {
        check(!opened) { "Telegram audio source is already open" }
        val uri = dataSpec.uri
        if (uri.scheme != SCHEME || uri.host != "file") throw IOException("Địa chỉ streaming Telegram không hợp lệ")
        val parsedId = uri.lastPathSegment?.toIntOrNull()?.takeIf { it > 0 }
            ?: throw IOException("Không tìm thấy file Telegram")
        transferInitializing(dataSpec)
        telegram.telegramStreamOpened(parsedId)
        try {
            // Pin the file BEFORE any TDLib query: cleanup cannot win the race with open().
            val length = telegram.getTelegramStreamSizeBlocking(parsedId)
            if (length <= 0L) throw IOException("Telegram chưa cung cấp kích thước file nhạc")
            if (dataSpec.position > length) throw IOException("Vị trí tua vượt quá dung lượng nhạc")
            originalUri = uri
            request = dataSpec
            fileId = parsedId
            fileSize = length
            cursor = dataSpec.position
            remaining = if (dataSpec.length == C.LENGTH_UNSET.toLong()) length - cursor
            else minOf(dataSpec.length, length - cursor)
            bufferedEnd = cursor
            opened = true
            transferStarted(dataSpec)
            return remaining
        } catch (failure: Throwable) {
            // Avoid leaking an active-reader count when opening or seeking fails.
            opened = false
            fileId = 0
            originalUri = null
            telegram.telegramStreamClosed(parsedId)
            throw failure
        }
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (!opened) throw IOException("Telegram audio source is closed")
        if (remaining <= 0L) return C.RESULT_END_OF_INPUT
        if (cursor >= bufferedEnd || reader == null) {
            reader?.close()
            reader = null
            val wanted = minOf(CHUNK_BYTES.toLong(), remaining).toInt()
            val segment = telegram.downloadTelegramStreamRangeBlocking(fileId, cursor, wanted)
            val file = File(segment.path)
            if (!file.isFile || segment.availableBytes <= 0L || file.length() < cursor + segment.availableBytes) {
                throw IOException("Telegram chưa tải đủ đoạn nhạc được yêu cầu")
            }
            reader = RandomAccessFile(file, "r").apply { seek(cursor) }
            bufferedEnd = cursor + segment.availableBytes
        }
        val requested = minOf(length.toLong(), remaining, bufferedEnd - cursor).toInt()
        val count = reader?.read(buffer, offset, requested) ?: -1
        if (count <= 0) throw IOException("Không đọc được đoạn nhạc Telegram tại byte $cursor")
        cursor += count
        remaining -= count
        bytesTransferred(count)
        return count
    }

    override fun getUri(): Uri? = originalUri

    override fun close() {
        val wasOpened = opened
        val oldId = fileId
        try { reader?.close() } finally {
            reader = null
            originalUri = null
            request = null
            fileId = 0
            opened = false
            bufferedEnd = 0L
            if (wasOpened) {
                transferEnded()
                telegram.telegramStreamClosed(oldId)
            }
        }
    }

    class Factory(private val telegram: TelegramMusicManager) : DataSource.Factory {
        override fun createDataSource(): DataSource = TelegramStreamDataSource(telegram)
    }

    companion object {
        const val SCHEME = "telegram-stream"
        private const val CHUNK_BYTES = 512 * 1024
        fun uriForFile(fileId: Int): Uri {
            require(fileId > 0)
            return Uri.parse("$SCHEME://file/$fileId")
        }
    }
}

// ===== Phone entry and international E.164 handling (merged) =====
/** Country + national number. Never hard-code +84, and never append a country code twice. */
private data class DialCountry(val region: String, val dialCode: Int)
private data class PhoneUiCopy(
    val instruction: String, val country: String, val number: String,
    val selectCountry: String, val search: String, val invalid: String,
    val preview: String, val continueText: String
)
private val phoneFallback = PhoneUiCopy(
    "Select a country and enter your Telegram phone number.", "Country",
    "Phone number", "Select country", "Search country or dialing code",
    "Check your phone number", "Telegram number", "Continue"
)

private fun phoneEntryCopy(context: Context, language: String): PhoneUiCopy {
    val catalog = MeloWareTranslationCatalog.load(context.applicationContext)
    fun t(key: String, fallback: String) = catalog.text(language, "telegram.phone.$key", fallback)
    return PhoneUiCopy(
        t("instruction", "Chọn quốc gia và nhập số điện thoại Telegram."),
        t("country", "Quốc gia"),
        t("number", "Số điện thoại"),
        t("select", "Chọn quốc gia"),
        t("search", "Tìm quốc gia hoặc mã vùng"),
        t("invalid", "Kiểm tra lại số điện thoại"),
        t("preview", "Số Telegram"),
        t("continue", "Tiếp tục")
    )
}

private fun defaultTelegramRegion(context: Context, phoneUtil: PhoneNumberUtil): String {
    val simRegion = runCatching {
        (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)
            ?.simCountryIso?.uppercase(Locale.ROOT)
    }.getOrNull()
    val localeRegion = Locale.getDefault().country.uppercase(Locale.ROOT)
    return listOfNotNull(simRegion, localeRegion, "VN")
        .firstOrNull { phoneUtil.supportedRegions.contains(it) } ?: "US"
}

private fun dialFlag(region: String): String {
    if (region.length != 2) return "\uD83C\uDF10"
    val a = Character.toChars(0x1F1E6 + region[0].code - 'A'.code)
    val b = Character.toChars(0x1F1E6 + region[1].code - 'A'.code)
    return String(a) + String(b)
}

/**
 * Accepts national number, national trunk prefix, E.164, 00-prefixed international,
 * or a number beginning with the selected dial code (e.g. 84971832146 for VN).
 * Parsing and validation are delegated to Google's numbering metadata.
 */
internal fun telegramNumberE164(raw: String, region: String): String? {
    val util = PhoneNumberUtil.getInstance()
    val pickedRegion = region.uppercase(Locale.ROOT)
    if (pickedRegion !in util.supportedRegions) return null
    val value = buildString {
        raw.trim().forEach { c ->
            when {
                c == '+' && isEmpty() -> append('+')
                Character.digit(c, 10) in 0..9 -> append(Character.digit(c, 10))
            }
        }
    }
    if (value.isBlank() || value == "+") return null
    val dialCode = util.getCountryCodeForRegion(pickedRegion).toString()
    val numbers = when {
        value.startsWith("+") -> listOf(value)
        value.startsWith("00") -> listOf("+" + value.drop(2), value)
        value.startsWith(dialCode) -> listOf("+$value", value)
        else -> listOf(value)
    }
    for (candidate in numbers) {
        val parsed = runCatching { util.parse(candidate, pickedRegion) }.getOrNull() ?: continue
        if (util.isValidNumber(parsed)) {
            return util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        }
    }
    return null
}

@Composable
internal fun TelegramPhoneEntry(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val colors = LocalMusicColors.current
    val phoneUtil = remember { PhoneNumberUtil.getInstance() }
    val copy = remember(context, language) { phoneEntryCopy(context, language) }
    val initialRegion = remember(context) { defaultTelegramRegion(context, phoneUtil) }
    var region by rememberSaveable { mutableStateOf(initialRegion) }
    var countryPickerOpen by remember { mutableStateOf(false) }
    val valid = remember(value, region) { telegramNumberE164(value, region) }
    val countryCode = remember(region) { phoneUtil.getCountryCodeForRegion(region) }

    Text(copy.instruction, color = colors.textSecondary, fontSize = 12.sp)
    Spacer(Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { countryPickerOpen = true }, modifier = Modifier.height(56.dp)) {
            Text("${dialFlag(region)} +$countryCode", maxLines = 1)
            Icon(Icons.Rounded.ArrowDropDown, contentDescription = copy.selectCountry)
        }
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.weight(1f), label = { Text(copy.number) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            isError = value.isNotBlank() && valid == null
        )
    }
    if (valid != null) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.CheckCircle, null, tint = colors.purpleBright,
                modifier = Modifier.size(15.dp))
            Text("${copy.preview}: $valid", color = colors.textSecondary, fontSize = 12.sp)
        }
    } else if (value.isNotBlank()) {
        Text(copy.invalid, color = colors.textSecondary, fontSize = 12.sp)
    }
    Button(onClick = { valid?.let(onSubmit) }, enabled = valid != null) {
        Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(18.dp))
        Text(copy.continueText)
    }

    if (countryPickerOpen) {
        var search by remember { mutableStateOf("") }
        val locale = remember(language) { Locale.forLanguageTag(language.replace('_', '-')) }
        val countries = remember(locale) {
            phoneUtil.supportedRegions.map { DialCountry(it, phoneUtil.getCountryCodeForRegion(it)) }
                .sortedBy { Locale("", it.region).getDisplayCountry(locale).lowercase(locale) }
        }
        val filtered = remember(search, countries) {
            val q = search.trim().lowercase(locale)
            if (q.isBlank()) countries else countries.filter { entry ->
                Locale("", entry.region).getDisplayCountry(locale).lowercase(locale).contains(q) ||
                        entry.region.lowercase(locale).contains(q) ||
                        ("+" + entry.dialCode).contains(q)
            }
        }
        Dialog(onDismissRequest = { countryPickerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(color = colors.backgroundSoft, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(copy.selectCountry, color = colors.textPrimary,
                            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { countryPickerOpen = false }) {
                            Icon(Icons.Rounded.Close, contentDescription = copy.selectCountry)
                        }
                    }
                    OutlinedTextField(value = search, onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text(copy.search) })
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                        items(filtered, key = { it.region }) { item ->
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                region = item.region
                                onValueChange("")
                                countryPickerOpen = false
                            }.padding(vertical = 12.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(dialFlag(item.region))
                                Text(Locale("", item.region).getDisplayCountry(locale),
                                    color = colors.textPrimary, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f))
                                Text("+${item.dialCode}", color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===== Telegram connection guide, one-screen multilingual (merged) =====
private const val TELEGRAM_GUIDE_ASSET_DIR = "tutorial/telegram/"
private val TELEGRAM_GUIDE_ART = listOf(
    "01_phone.webp", "02_telegram_code.webp", "03_enter_code.webp",
    "04_bot_username.webp", "05_connect_bot.webp", "06_ready_to_search.webp"
)

private data class TelegramGuidePage(
    val title: String, val body: String,
    val compactTitle: String = title, val compactBody: String = body
)
private data class TelegramGuideText(
    val title: String, val subtitle: String, val back: String, val next: String,
    val done: String, val close: String, val help: String, val stepLabel: String,
    val safety: String, val hint: String, val pages: List<TelegramGuidePage>
)

private val telegramGuideFallback = TelegramGuideText(
    title = "Telegram Music", subtitle = "6 bước kết nối Telegram",
    back = "Trước", next = "Sau", done = "Bắt đầu tìm kiếm", close = "Đóng hướng dẫn",
    help = "Hướng dẫn kết nối Telegram", stepLabel = "Bước {current}/{total}",
    safety = "Giữ bí mật mã đăng nhập Telegram. Không chia sẻ mã với bất kỳ ai.",
    hint = "Chỉ đăng nhập một lần; phiên sẽ được lưu cho đến khi đăng xuất.",
    pages = listOf(
        TelegramGuidePage("Nhập số điện thoại", "Chọn mã quốc gia, nhập số Telegram rồi nhấn Tiếp tục."),
        TelegramGuidePage("Lấy mã xác nhận", "Mở ứng dụng Telegram chính thức để lấy mã đăng nhập."),
        TelegramGuidePage("Xác nhận đăng nhập", "Nhập mã trong MeloWave và xác nhận. Hoàn tất xác minh 2 bước nếu có."),
        TelegramGuidePage("Nhập tên bot", "Mở Cài đặt bot và nhập @username bot bạn sử dụng."),
        TelegramGuidePage("Kết nối bot", "Nhấn Kết nối. Nếu cần, mở bot trong Telegram và nhấn Start."),
        TelegramGuidePage("Tìm nhạc", "Trở về Telegram Music và tìm bài hát hoặc ca sĩ.")
    )
)

/** Shared app-language catalog; changes immediately with Settings > Language. */
private fun telegramGuideText(context: Context, language: String): TelegramGuideText {
    val catalog = MeloWareTranslationCatalog.load(context.applicationContext)
    fun t(key: String, fallback: String): String = catalog.text(language, "telegram.guide.$key", fallback)
    return telegramGuideFallback.copy(
        subtitle = t("subtitle", telegramGuideFallback.subtitle),
        close = t("close", telegramGuideFallback.close),
        help = t("help", telegramGuideFallback.help),
        safety = t("safety", telegramGuideFallback.safety),
        pages = telegramGuideFallback.pages.mapIndexed { index, page ->
            val prefix = "step${index + 1}"
            val title = t("$prefix.title", page.title)
            val body = t("$prefix.body", page.body)
            TelegramGuidePage(title = title, body = body,
                compactTitle = title, compactBody = body)
        }
    )
}

/** Localized accessibility label for the ? button, including before Telegram sign-in. */
internal fun telegramGuideHelpLabel(context: Context, language: String): String =
    telegramGuideText(context, language).help

/**
 * All six steps fit on one screen (2 columns x 3 rows); NO page navigation
 * and NO vertical scroll. Short localized captions accompany the user's screenshots.
 * Long descriptions remain available to accessibility readers.
 */
@Composable
internal fun TelegramConnectionGuide(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val copy = remember(context, language) { telegramGuideText(context, language) }
    val colors = LocalMusicColors.current
    // Tapping a step opens its illustration in the SAME fullscreen dialog.
    // A Back/Close action returns to the 6-step overview without dismissing it.
    var expandedGuideStep by rememberSaveable { mutableStateOf<Int?>(null) }
    val photos = remember(context) {
        TELEGRAM_GUIDE_ART.map { asset ->
            runCatching {
                context.assets.open(TELEGRAM_GUIDE_ASSET_DIR + asset).use {
                    BitmapFactory.decodeStream(it)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    Dialog(
        onDismissRequest = {
            if (expandedGuideStep != null) expandedGuideStep = null else onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
            val selectedIndex = expandedGuideStep?.takeIf { it in copy.pages.indices }
            if (selectedIndex != null) {
                val page = copy.pages[selectedIndex]
                Column(
                    modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(page.title, color = colors.textPrimary, fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                            Text("${selectedIndex + 1} / ${copy.pages.size} · ${copy.title}",
                                color = colors.textSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = { expandedGuideStep = null }, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Rounded.Close,
                                contentDescription = mwString("common.close", "Đóng"),
                                tint = colors.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.backgroundSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        val screenshot = photos.getOrNull(selectedIndex)
                        if (screenshot != null) {
                            Image(
                                bitmap = screenshot,
                                contentDescription = page.title + ". " + page.body,
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(page.title, color = colors.textSecondary,
                                modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = page.body,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                        color = colors.textPrimary, fontSize = 13.sp, lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(copy.title, color = colors.textPrimary, fontSize = 17.sp,
                                fontWeight = FontWeight.Bold, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text(copy.subtitle, color = colors.textSecondary, fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = copy.close,
                                tint = colors.textSecondary)
                        }
                    }

                    // Measure the real remaining guide space instead of using
                    // BoxWithConstraints (which can trigger IDE scope warnings).
                    var guideAreaSize by remember { mutableStateOf(IntSize.Zero) }
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                            .onSizeChanged { size ->
                                if (guideAreaSize != size) guideAreaSize = size
                            }
                    ) {
                        val density = LocalDensity.current
                        val tight = guideAreaSize == IntSize.Zero || with(density) {
                            guideAreaSize.height.toDp() < 540.dp || guideAreaSize.width.toDp() < 370.dp
                        }
                        val labelFont = if (tight) 10.sp else 11.5.sp
                        val bodyFont = if (tight) 9.sp else 10.5.sp
                        val lineHeight = if (tight) 11.sp else 12.5.sp
                        val gap = if (tight) 4.dp else 6.dp
                        Column(modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(gap)) {
                            repeat(3) { rowIndex ->
                                Row(modifier = Modifier.fillMaxWidth().weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(gap)) {
                                    repeat(2) { colIndex ->
                                        val index = rowIndex * 2 + colIndex
                                        val page = copy.pages[index]
                                        Surface(
                                            modifier = Modifier.weight(1f).fillMaxHeight()
                                                .clickable { expandedGuideStep = index },
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.backgroundSoft,
                                            border = BorderStroke(1.dp, colors.borderSoft)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize().padding(if (tight) 5.dp else 7.dp),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Box(
                                                        modifier = Modifier.size(if (tight) 19.dp else 22.dp)
                                                            .background(colors.purpleBright, RoundedCornerShape(6.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("${index + 1}", color = Color.White,
                                                            fontSize = if (tight) 10.sp else 11.sp,
                                                            fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(page.compactTitle, color = colors.textPrimary,
                                                        fontSize = labelFont, lineHeight = lineHeight,
                                                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.weight(1f))
                                                }
                                                val screenshot = photos.getOrNull(index)
                                                if (screenshot != null) {
                                                    Image(
                                                        bitmap = screenshot,
                                                        contentDescription = page.title + ". " + page.body,
                                                        modifier = Modifier.fillMaxWidth().weight(1f)
                                                            .clip(RoundedCornerShape(7.dp)),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                                Text(page.compactBody, color = colors.textSecondary,
                                                    fontSize = bodyFont, lineHeight = lineHeight,
                                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Rounded.Lock, contentDescription = null,
                            tint = colors.purpleBright, modifier = Modifier.size(13.dp))
                        Text(copy.safety, color = colors.textSecondary, fontSize = 10.sp,
                            lineHeight = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            } // 6-step overview
        }
    }
}