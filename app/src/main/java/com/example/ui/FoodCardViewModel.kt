package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FoodCardRepository
import com.example.data.FoodMasterProfileEntity
import com.example.data.RestaurantCardEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FoodCardUiState(
    val allCards: List<RestaurantCardEntity> = emptyList(),
    val filteredCards: List<RestaurantCardEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedDistrict: String? = null,
    val showMemorialOnly: Boolean = false,
    val currentTab: Int = 0, // 0 = Binder, 1 = Map Scroll, 2 = Master ID
    val activeCardForDetail: RestaurantCardEntity? = null,
    val isForgeDialogOpen: Boolean = false,
    val duelSelectedCardIds: Set<Long> = emptySet(),
    val isDuelModalOpen: Boolean = false,
    val profile: FoodMasterProfileEntity? = null
)

class FoodCardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodCardRepository

    private val _searchQuery = MutableStateFlow("")
    private val _selectedDistrict = MutableStateFlow<String?>(null)
    private val _showMemorialOnly = MutableStateFlow(false)
    private val _currentTab = MutableStateFlow(0)
    private val _activeCardForDetail = MutableStateFlow<RestaurantCardEntity?>(null)
    private val _isForgeDialogOpen = MutableStateFlow(false)
    private val _duelSelectedCardIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _isDuelModalOpen = MutableStateFlow(false)

    val uiState: StateFlow<FoodCardUiState>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FoodCardRepository(database.foodCardDao())

        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }

        uiState = combine(
            repository.allCards,
            _searchQuery,
            _selectedDistrict,
            _showMemorialOnly,
            _currentTab,
            _activeCardForDetail,
            _isForgeDialogOpen,
            _duelSelectedCardIds,
            _isDuelModalOpen,
            repository.profile
        ) { args: Array<Any?> ->
            @Suppress("UNCHECKED_CAST")
            val cards = args[0] as List<RestaurantCardEntity>
            val query = args[1] as String
            val district = args[2] as String?
            val memorialOnly = args[3] as Boolean
            val tab = args[4] as Int
            val activeCard = args[5] as RestaurantCardEntity?
            val forgeOpen = args[6] as Boolean
            @Suppress("UNCHECKED_CAST")
            val duelIds = args[7] as Set<Long>
            val duelModal = args[8] as Boolean
            val prof = args[9] as FoodMasterProfileEntity?

            val filtered = cards.filter { card ->
                val matchesQuery = query.isBlank() ||
                        card.name.contains(query, ignoreCase = true) ||
                        card.signatureDish.contains(query, ignoreCase = true) ||
                        card.district.contains(query, ignoreCase = true) ||
                        card.flawTrap.contains(query, ignoreCase = true)

                val matchesDistrict = district == null || card.district == district
                val matchesMemorial = !memorialOnly || card.isClosedMemorial

                matchesQuery && matchesDistrict && matchesMemorial
            }

            FoodCardUiState(
                allCards = cards,
                filteredCards = filtered,
                searchQuery = query,
                selectedDistrict = district,
                showMemorialOnly = memorialOnly,
                currentTab = tab,
                activeCardForDetail = activeCard,
                isForgeDialogOpen = forgeOpen,
                duelSelectedCardIds = duelIds,
                isDuelModalOpen = duelModal,
                profile = prof
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoodCardUiState()
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedDistrict(district: String?) {
        _selectedDistrict.value = district
    }

    fun setShowMemorialOnly(show: Boolean) {
        _showMemorialOnly.value = show
    }

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    fun openCardDetail(card: RestaurantCardEntity?) {
        _activeCardForDetail.value = card
    }

    fun openForgeDialog(open: Boolean) {
        _isForgeDialogOpen.value = open
    }

    fun openDuelModal(open: Boolean) {
        _isDuelModalOpen.value = open
    }

    fun toggleCardForDuel(cardId: Long) {
        val current = _duelSelectedCardIds.value.toMutableSet()
        if (current.contains(cardId)) {
            current.remove(cardId)
        } else {
            if (current.size >= 4) {
                // Max 4 for card battle
                Toast.makeText(getApplication(), "最多選擇4張餐廳卡牌決鬥！", Toast.LENGTH_SHORT).show()
                return
            }
            current.add(cardId)
        }
        _duelSelectedCardIds.value = current
    }

    fun clearDuelCards() {
        _duelSelectedCardIds.value = emptySet()
    }

    fun forgeCard(
        name: String,
        district: String,
        signatureDish: String,
        flawTrap: String,
        priceAvg: Int,
        ratingStars: Int,
        tastingNotes: String,
        photoUri: String?,
        isClosedMemorial: Boolean
    ) {
        viewModelScope.launch {
            // RPG Stat calculation:
            val atk = (ratingStars * 18 + (signatureDish.length.coerceAtMost(10) * 1.2)).toInt().coerceIn(60, 99)
            val def = (100 - (flawTrap.length.coerceAtMost(15) * 2)).coerceIn(40, 95)

            val newCard = RestaurantCardEntity(
                name = name,
                district = district,
                signatureDish = signatureDish,
                flawTrap = flawTrap,
                priceAvg = priceAvg,
                ratingStars = ratingStars,
                tastingNotes = tastingNotes,
                photoUri = photoUri,
                isClosedMemorial = isClosedMemorial,
                battleTastePower = atk,
                battleFlawDef = def
            )
            repository.insertCard(newCard)
            _isForgeDialogOpen.value = false
            Toast.makeText(getApplication(), "成功鍛造食卡：$name", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateCard(updatedCard: RestaurantCardEntity) {
        viewModelScope.launch {
            repository.updateCard(updatedCard)
            _activeCardForDetail.value = updatedCard
            Toast.makeText(getApplication(), "已儲存修改：${updatedCard.name}", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateCardPhotos(cardId: Long, photoUris: List<String>) {
        viewModelScope.launch {
            val card = uiState.value.allCards.find { it.id == cardId } ?: return@launch
            val updated = card.copy(photoUri = photoUris.joinToString(","))
            repository.updateCard(updated)
            if (_activeCardForDetail.value?.id == cardId) {
                _activeCardForDetail.value = updated
            }
            Toast.makeText(getApplication(), "已更新相片 (${photoUris.size}張)", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleMemorialStatus(card: RestaurantCardEntity) {
        viewModelScope.launch {
            val updated = card.copy(isClosedMemorial = !card.isClosedMemorial)
            repository.updateCard(updated)
            _activeCardForDetail.value = updated
            val msg = if (updated.isClosedMemorial) "已化為【追憶墓碑卡】(灰階)" else "已恢復為【營業中食卡】"
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteCard(card: RestaurantCardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
            _activeCardForDetail.value = null
            _duelSelectedCardIds.value = _duelSelectedCardIds.value - card.id
            Toast.makeText(getApplication(), "已將 ${card.name} 移出卡冊", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDiningDuelViaWhatsApp(context: Context, cards: List<RestaurantCardEntity>) {
        if (cards.isEmpty()) {
            Toast.makeText(context, "請先選擇至少2張餐廳卡牌進行決鬥！", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.append("[食卡決鬥] 今日食咩大決鬥！\n")
        sb.append("由16-bit食家決鬥機選出以下候選餐廳：\n\n")

        cards.forEachIndexed { index, card ->
            val statusTag = if (card.isClosedMemorial) "[追憶墓碑]" else "[營業中]"
            sb.append("${index + 1}. $statusTag ${card.name}（${card.district}）\n")
            sb.append("   [招牌]：${card.signatureDish}\n")
            sb.append("   [注意]：${card.flawTrap}\n")
            sb.append("   [人均]：HK$${card.priceAvg} | [評分]：${card.ratingStars}/5星\n")
            sb.append("   [戰力]：${card.battleTastePower} ATK\n\n")
        }
        sb.append("[請投邊間一票？即刻回覆！]")

        val message = sb.toString()
        val uri = Uri.parse("whatsapp://send?text=" + Uri.encode(message))
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard share sheet
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "分享開餐決鬥到..."))
        }
    }
}
