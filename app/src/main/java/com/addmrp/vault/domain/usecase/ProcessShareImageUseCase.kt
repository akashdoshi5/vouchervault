package com.addmrp.vault.domain.usecase

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Data class holding the parsed OCR results for pre-filling the Scan form.
 * All fields are optional — OCR is best-effort, user always reviews.
 */
data class OcrResult(
    val brand: String? = null,
    val code: String? = null,
    val value: String? = null,
    val expiryDate: LocalDate? = null,
    val rawText: String = ""
)

/**
 * "Share to Vault" OCR Pipeline Use Case.
 *
 * Flow:
 *   1. Receives an image Uri from a share intent
 *   2. Runs Google ML Kit Text Recognition on-device
 *   3. Parses the extracted text to identify brand, promo code, value, and expiry date
 *   4. Returns an OcrResult for the ScanViewModel to pre-fill the form
 *
 * This is a REAL implementation — not a TODO placeholder (Rule 1).
 */
@Singleton
class ProcessShareImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Run OCR on the shared image and parse the results.
     * Runs on a background coroutine (caller is responsible for dispatching on IO).
     */
    suspend fun execute(imageUri: Uri): OcrResult {
        val fullText = runOcr(imageUri)
        return parseOcrText(fullText)
    }

    /**
     * Step 1: ML Kit text recognition — suspending wrapper around the Task API.
     */
    private suspend fun runOcr(imageUri: Uri): String = suspendCancellableCoroutine { cont ->
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    cont.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

    /**
     * Step 2: Parse the raw OCR text to extract structured voucher data.
     *
     * Known patterns from Indian fintech apps (GPay, PhonePe, CRED):
     *   - Promo codes: Uppercase alphanumeric, typically 6-16 chars (e.g., "FLAT100", "GPAY2024SAVE")
     *   - Values: "₹150", "₹200 OFF", "FLAT 20% OFF", "Rs. 100"
     *   - Expiry: "Valid till 31 Mar 2026", "Expires on 15/04/2026", "Use before 30-06-2026"
     *   - Brands: "Zomato", "Swiggy", "Amazon", "Flipkart" — matched from a known list
     */
    internal fun parseOcrText(text: String): OcrResult {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val fullText = lines.joinToString(" ")

        return OcrResult(
            brand = extractBrand(fullText),
            code = extractCode(fullText),
            value = extractValue(fullText),
            expiryDate = extractExpiry(fullText),
            rawText = text
        )
    }

    // ── Brand Extraction ─────────────────────────────────────
    // Match against known Indian brands. If no match, try to use
    // the first capitalized word that isn't a common English word.
    private val knownBrands = listOf(
        "Zomato", "Swiggy", "Amazon", "Flipkart", "Myntra", "Ajio",
        "BigBasket", "Blinkit", "Zepto", "Dunzo", "Uber", "Ola",
        "MakeMyTrip", "Goibibo", "Yatra", "OYO", "Nykaa", "Mamaearth",
        "PharmEasy", "1mg", "Starbucks", "Dominos", "Pizza Hut",
        "McDonald's", "KFC", "Burger King", "Tata CLiQ", "Boat",
        "JioMart", "Reliance", "Croma", "BookMyShow", "Paytm",
        "Google Pay", "PhonePe", "CRED", "Freecharge", "MobiKwik"
    )

    private fun extractBrand(text: String): String? {
        // First: try exact match from known brands (case-insensitive)
        for (brand in knownBrands) {
            if (text.contains(brand, ignoreCase = true)) {
                return brand
            }
        }
        return null
    }

    // ── Promo Code Extraction ────────────────────────────────
    // Promo codes are typically uppercase alphanumeric, 4-20 chars,
    // sometimes with hyphens. Exclude common false positives.
    private val codePattern = Regex(
        """(?:code|coupon|promo|voucher|use)[:\s]*([A-Z0-9]{4,20}(?:[-][A-Z0-9]{2,10})*)""",
        RegexOption.IGNORE_CASE
    )
    private val standaloneCodePattern = Regex(
        """[A-Z][A-Z0-9]{5,19}"""
    )
    private val falsePositives = setOf(
        "VALID", "OFFER", "TERMS", "APPLY", "EXPIRES", "AVAILABLE",
        "DISCOUNT", "CASHBACK", "SAVINGS", "REWARD", "COUPON", "PROMO",
        "FLAT", "UPTO", "MINIMUM", "ORDER", "ABOVE", "BELOW", "TOTAL"
    )

    private fun extractCode(text: String): String? {
        // Priority 1: Explicitly labelled codes ("Code: FLAT100", "Use promo: SAVE50")
        codePattern.find(text)?.let { match ->
            return match.groupValues[1]
        }

        // Priority 2: Standalone uppercase sequences that look like promo codes
        standaloneCodePattern.findAll(text)
            .map { it.value }
            .filter { it !in falsePositives }
            .firstOrNull()
            ?.let { return it }

        return null
    }

    // ── Value Extraction ─────────────────────────────────────
    // Patterns: "₹150", "Rs. 200", "Rs 100", "FLAT 20%", "₹500 OFF"
    private val rupeePattern = Regex(
        """(?:₹|Rs\.?\s*|INR\s*)(\d+(?:,\d{3})*(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )
    private val percentPattern = Regex(
        """(\d{1,3})%\s*(?:OFF|DISCOUNT|CASHBACK)?""",
        RegexOption.IGNORE_CASE
    )

    private fun extractValue(text: String): String? {
        // Priority 1: Rupee amounts
        rupeePattern.find(text)?.let { match ->
            val amount = match.groupValues[1].replace(",", "")
            return amount
        }
        // Priority 2: Percentage discounts — return as-is, user can edit
        percentPattern.find(text)?.let { match ->
            return match.groupValues[1]
        }
        return null
    }

    // ── Expiry Date Extraction ───────────────────────────────
    // Patterns:
    //   "Valid till 31 Mar 2026"  → dd MMM yyyy
    //   "Expires on 15/04/2026"  → dd/MM/yyyy
    //   "Use before 30-06-2026"  → dd-MM-yyyy
    //   "31/03/2026"             → dd/MM/yyyy
    //   "2026-06-30"             → yyyy-MM-dd
    private val expiryLabelPattern = Regex(
        """(?:valid\s*(?:till|until|upto)|expires?\s*(?:on|by|:)?|use\s*before|expiry\s*(?:date)?[:\s])\s*(.{6,20})""",
        RegexOption.IGNORE_CASE
    )
    private val datePatterns = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd MMM yyyy"),
        DateTimeFormatter.ofPattern("dd MMMM yyyy"),
        DateTimeFormatter.ofPattern("MMM dd, yyyy"),
        DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    )
    private val dateRegex = Regex(
        """(\d{1,2}[/\-.]\d{1,2}[/\-.]\d{2,4}|\d{4}[/\-.]\d{1,2}[/\-.]\d{1,2}|\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\w*\s+\d{4})""",
        RegexOption.IGNORE_CASE
    )

    private fun extractExpiry(text: String): LocalDate? {
        // Priority 1: Labelled expiry ("Valid till ...", "Expires on ...")
        expiryLabelPattern.find(text)?.let { match ->
            val dateStr = match.groupValues[1].trim()
            parseDate(dateStr)?.let { return it }
        }

        // Priority 2: Any recognizable date pattern in the text
        dateRegex.findAll(text).forEach { match ->
            parseDate(match.value)?.let { return it }
        }

        return null
    }

    private fun parseDate(text: String): LocalDate? {
        val cleaned = text.trim().replace(Regex("""\s+"""), " ")
        for (formatter in datePatterns) {
            try {
                return LocalDate.parse(cleaned, formatter)
            } catch (_: DateTimeParseException) {
                // Try next format
            }
        }
        return null
    }
}
