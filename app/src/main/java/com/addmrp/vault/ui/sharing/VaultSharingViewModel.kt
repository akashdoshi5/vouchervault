package com.addmrp.vault.ui.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.addmrp.vault.data.remote.VaultGroupDataSource
import com.addmrp.vault.domain.model.GroupMember
import com.addmrp.vault.domain.model.VaultGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultSharingUiState(
    val group: VaultGroup? = null,
    val members: List<GroupMember> = emptyList(),
    val isOwner: Boolean = false,
    val inviteEmail: String = "",
    val isCreatingGroup: Boolean = false,
    val isInviting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class VaultSharingViewModel @Inject constructor(
    private val groupDataSource: VaultGroupDataSource
) : ViewModel() {

    private val _inviteEmail = MutableStateFlow("")
    private val _loadingState = MutableStateFlow(false)
    private val _message = MutableStateFlow<Pair<String?, String?>>(null to null) // success, error

    val uiState: StateFlow<VaultSharingUiState> = combine(
        groupDataSource.observeMyGroup(),
        _inviteEmail,
        _loadingState,
        _message
    ) { group, email, loading, (success, error) ->
        VaultSharingUiState(
            group = group,
            members = group?.members ?: emptyList(),
            isOwner = group?.ownerUid == (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""),
            inviteEmail = email,
            isInviting = loading,
            successMessage = success,
            errorMessage = error,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        VaultSharingUiState()
    )

    fun updateInviteEmail(email: String) {
        _inviteEmail.value = email
        _message.value = null to null
    }

    fun createGroup() {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                groupDataSource.createGroup("My Household")
                _message.value = "🎉 Household vault created!" to null
            } catch (e: Exception) {
                _message.value = null to (e.message ?: "Failed to create group")
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun inviteMember() {
        val email = _inviteEmail.value.trim()
        if (email.isBlank() || !email.contains("@")) {
            _message.value = null to "Enter a valid email address"
            return
        }

        val group = uiState.value.group ?: return
        if (!group.canAddMember()) {
            _message.value = null to "Group is full (max ${VaultGroup.MAX_MEMBERS} members)"
            return
        }

        viewModelScope.launch {
            _loadingState.value = true
            try {
                // Note: In production, this would look up the user by email
                // For MVP, we use the email as both UID placeholder and display
                groupDataSource.inviteMember(
                    groupId = group.id,
                    email = email,
                    displayName = email.substringBefore("@"),
                    uid = email.replace(".", "_").replace("@", "_")
                )
                _inviteEmail.value = ""
                _message.value = "✅ Invitation sent to $email" to null
            } catch (e: Exception) {
                _message.value = null to (e.message ?: "Failed to invite")
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun removeMember(memberUid: String) {
        val group = uiState.value.group ?: return
        viewModelScope.launch {
            try {
                groupDataSource.removeMember(group.id, memberUid)
                _message.value = "Member removed" to null
            } catch (e: Exception) {
                _message.value = null to (e.message ?: "Failed to remove")
            }
        }
    }

    fun clearMessages() {
        _message.value = null to null
    }
}
