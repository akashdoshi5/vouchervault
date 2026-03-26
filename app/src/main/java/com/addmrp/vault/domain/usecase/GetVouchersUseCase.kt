package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.Voucher
import com.addmrp.vault.domain.repository.VoucherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVouchersUseCase @Inject constructor(
    private val repository: VoucherRepository
) {
    operator fun invoke(): Flow<List<Voucher>> = repository.observeVouchers()
}
