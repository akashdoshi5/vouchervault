package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.Voucher
import javax.inject.Inject

/**
 * Pure function use case (Rule 5: Testability First).
 * Calculates the total monetary value of non-expired, non-redeemed vouchers.
 */
class CalculateTotalAssetsUseCase @Inject constructor() {

    operator fun invoke(vouchers: List<Voucher>): Double {
        return vouchers
            .filter { !it.isExpired && !it.isRedeemed }
            .sumOf { it.value }
    }
}
