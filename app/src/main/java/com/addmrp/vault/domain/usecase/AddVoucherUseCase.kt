package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.Voucher
import com.addmrp.vault.domain.repository.VoucherRepository
import javax.inject.Inject

class AddVoucherUseCase @Inject constructor(
    private val repository: VoucherRepository,
    private val vaultGroupDataSource: com.addmrp.vault.data.remote.VaultGroupDataSource,
    private val auth: com.google.firebase.auth.FirebaseAuth
) {
    suspend operator fun invoke(voucher: Voucher) {
        val uid = auth.currentUser?.uid
        val finalVoucher = if (uid != null) {
            val groupIds = vaultGroupDataSource.getGroupMemberUids()
            voucher.copy(
                ownerId = voucher.ownerId.ifEmpty { uid },
                sharedWith = groupIds
            )
        } else {
            voucher
        }
        repository.addVoucher(finalVoucher)
    }
}
