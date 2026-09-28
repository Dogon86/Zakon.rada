package com.example.data.network

import com.example.data.local.LawEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class RadaWebScraper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchLaw(queryOrUrl: String, selectedCategory: String = ""): Result<LawEntity> = withContext(Dispatchers.IO) {
        try {
            val url = buildTargetUrl(queryOrUrl)
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Помилка завантаження з rada.gov.ua (Код ${response.code})"))
            }

            val html = response.body?.string() ?: return@withContext Result.failure(Exception("Отримано порожню відповідь від сервера"))
            val document = Jsoup.parse(html, url)

            val parsedLaw = parseDocument(document, url, queryOrUrl, selectedCategory)
            Result.success(parsedLaw)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildTargetUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        // If it looks like a rada id: e.g. "254к/96-вр" or "435-15"
        val encodedId = trimmed.replace(" ", "")
        return "https://zakon.rada.gov.ua/laws/show/$encodedId"
    }

    private fun parseDocument(doc: Document, fullUrl: String, originalInput: String, userCategory: String): LawEntity {
        // Extract Title
        var title = doc.select("h1.rv-head, h1.rv-title, h1.title, h1").text().trim()
        if (title.isBlank()) {
            title = doc.title().replace(" - Верховна Рада України", "").replace(" - Законодавство України", "").trim()
        }
        if (title.isBlank()) {
            title = "Закон України ($originalInput)"
        }

        // Extract radaId from URL or document
        val radaId = extractRadaId(fullUrl, originalInput)

        // Extract metadata (adoption date, status, etc.)
        val docInfo = doc.select(".rv-doc-info, .doc-info, .rv-card-info").text().trim()
        var dateAdopted = ""
        val dateRegex = Regex("""(\d{2}\.\d{2}\.\d{4})""")
        val match = dateRegex.find(docInfo)
        if (match != null) {
            dateAdopted = match.value
        }

        val status = if (doc.text().contains("Втратив чинність", ignoreCase = true)) {
            "Втратив чинність"
        } else {
            "Чинний"
        }

        // Clean text content
        val contentElement = doc.selectFirst(".rv-text, #article, .rv-content, .rv-card-body, #content") ?: doc.body()

        // Remove script, style, comments, header nav elements
        contentElement.select("script, style, noscript, nav, header, footer, .rv-nav, .share, .rv-print").remove()

        val textBuilder = StringBuilder()
        val paragraphs = contentElement.select("p, h2, h3, h4, div.article, div.rv-article, div.paragraph")

        if (paragraphs.isNotEmpty()) {
            for (p in paragraphs) {
                val cleanLine = p.text().trim()
                if (cleanLine.isNotBlank() && cleanLine.length > 2) {
                    textBuilder.append(cleanLine).append("\n\n")
                }
            }
        } else {
            // Fallback to all text nodes
            val raw = contentElement.text()
            val lines = raw.split("\n")
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    textBuilder.append(trimmed).append("\n\n")
                }
            }
        }

        val finalFullText = textBuilder.toString().trim()

        // Automatically determine legal category if not provided
        val category = if (userCategory.isNotBlank()) {
            userCategory
        } else {
            detectCategory(title, finalFullText)
        }

        val summary = if (docInfo.isNotBlank()) docInfo else "Документ з бази zakon.rada.gov.ua (ID: $radaId)"

        return LawEntity(
            radaId = radaId,
            title = title,
            shortTitle = generateShortTitle(title),
            radaUrl = fullUrl,
            category = category,
            fullText = finalFullText,
            summary = summary,
            dateAdopted = dateAdopted,
            status = status,
            dateSaved = System.currentTimeMillis()
        )
    }

    private fun extractRadaId(url: String, fallback: String): String {
        val showPrefix = "/laws/show/"
        if (url.contains(showPrefix)) {
            val after = url.substringAfter(showPrefix).substringBefore("#").substringBefore("?")
            if (after.isNotBlank()) return after
        }
        return fallback.trim()
    }

    private fun generateShortTitle(title: String): String {
        return when {
            title.contains("Конституція", ignoreCase = true) -> "КУ"
            title.contains("Цивільний кодекс", ignoreCase = true) -> "ЦКУ"
            title.contains("Кримінальний кодекс", ignoreCase = true) -> "ККУ"
            title.contains("Податковий кодекс", ignoreCase = true) -> "ПКУ"
            title.contains("Кодекс законів про працю", ignoreCase = true) -> "КЗпП"
            title.contains("Господарський кодекс", ignoreCase = true) -> "ГКУ"
            title.contains("адміністративного судочинства", ignoreCase = true) -> "КАСУ"
            title.contains("Цивільний процесуальний", ignoreCase = true) -> "ЦПК"
            title.contains("Кримінальний процесуальний", ignoreCase = true) -> "КПК"
            title.contains("про адміністративні правопорушення", ignoreCase = true) -> "КУпАП"
            else -> ""
        }
    }

    private fun detectCategory(title: String, textSample: String): String {
        val lowerTitle = title.lowercase()
        val lowerSample = textSample.take(500).lowercase()

        return when {
            lowerTitle.contains("конституц") || lowerTitle.contains("державний лад") -> "Конституційне право"
            lowerTitle.contains("цивільн") || lowerTitle.contains("договір") || lowerTitle.contains("зобов'язанн") -> "Цивільне право"
            lowerTitle.contains("кримінальн") || lowerTitle.contains("злочин") || lowerTitle.contains("покаранн") -> "Кримінальне право"
            lowerTitle.contains("адміністративн") || lowerTitle.contains("поліці") || lowerTitle.contains("управлінн") -> "Адміністративне право"
            lowerTitle.contains("господарськ") || lowerTitle.contains("підприємниц") || lowerTitle.contains("комерц") -> "Господарське право"
            lowerTitle.contains("прац") || lowerTitle.contains("відпуст") || lowerTitle.contains("заробітн") -> "Трудове право"
            lowerTitle.contains("податк") || lowerTitle.contains("фінанс") || lowerTitle.contains("мито") || lowerTitle.contains("бюджет") -> "Податкове та фінансове право"
            lowerTitle.contains("процесуальн") || lowerTitle.contains("судочинств") || lowerTitle.contains("суд") || lowerTitle.contains("виконавче") -> "Процесуальне право"
            lowerTitle.contains("військ") || lowerTitle.contains("оборон") || lowerTitle.contains("мобілізац") || lowerTitle.contains("безпек") -> "Військове право"
            lowerSample.contains("кримінальн") -> "Кримінальне право"
            lowerSample.contains("цивільн") -> "Цивільне право"
            else -> "Інше"
        }
    }
}
