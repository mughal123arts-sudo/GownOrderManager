package com.mughalarts.gownordermanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mughalarts.gownordermanager.GownApplication
import com.mughalarts.gownordermanager.data.Order
import com.mughalarts.gownordermanager.util.PaymentStatus
import com.mughalarts.gownordermanager.util.formatOrderNumber
import com.mughalarts.gownordermanager.util.todayMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GownColor(val label: String) {
    Blue("Blue"),
    Green("Green"),
    Mehroon("Mehroon"),
    Black("Black")
}

data class GownColorState(
    val selected: Boolean = false,
    val quantity: String = ""
) {
    /** Unchecked colors always count as zero. */
    val quantityValue: Int
        get() = if (selected) (quantity.toIntOrNull() ?: 0) else 0
}

data class NewOrderUiState(
    val customerName: String = "",
    val schoolName: String = "",
    val contactNumber: String = "",
    val address: String = "",
    val deliveryDate: Long? = null,
    val colors: Map<GownColor, GownColorState> =
        GownColor.entries.associateWith { GownColorState() },
    val totalAmount: String = "",
    val advanceAmount: String = "",
    val specialInstructions: String = "",
    val errorMessage: String? = null
) {
    val totalGowns: Int
        get() = colors.values.sumOf { it.quantityValue }

    val totalAmountValue: Long
        get() = totalAmount.toLongOrNull() ?: 0L

    val advanceValue: Long
        get() = advanceAmount.toLongOrNull() ?: 0L

    val advanceTooHigh: Boolean
        get() = advanceValue > totalAmountValue

    val remainingBalance: Long
        get() = if (advanceTooHigh) 0L else totalAmountValue - advanceValue

    /** Only "Pending" or "Paid". */
    val paymentStatus: String
        get() = PaymentStatus.from(totalAmountValue, advanceValue)

    /** True when at least one field has something in it. */
    val hasAnyData: Boolean
        get() = customerName.isNotBlank() ||
            schoolName.isNotBlank() ||
            contactNumber.isNotBlank() ||
            address.isNotBlank() ||
            deliveryDate != null ||
            colors.values.any { it.selected } ||
            totalAmount.isNotBlank() ||
            advanceAmount.isNotBlank() ||
            specialInstructions.isNotBlank()
}

private fun Order.toUiState(): NewOrderUiState {
    fun colorState(selected: Boolean, quantity: Int) = GownColorState(
        selected = selected,
        quantity = if (selected && quantity > 0) quantity.toString() else ""
    )
    return NewOrderUiState(
        customerName = customerName,
        schoolName = schoolName,
        contactNumber = contactNumber,
        address = address,
        deliveryDate = if (deliveryDate > 0L) deliveryDate else null,
        colors = mapOf(
            GownColor.Blue to colorState(blueSelected, blueQuantity),
            GownColor.Green to colorState(greenSelected, greenQuantity),
            GownColor.Mehroon to colorState(mehroonSelected, mehroonQuantity),
            GownColor.Black to colorState(blackSelected, blackQuantity)
        ),
        totalAmount = if (totalAmount > 0L) totalAmount.toString() else "",
        advanceAmount = if (advanceAmount > 0L) advanceAmount.toString() else "",
        specialInstructions = specialInstructions
    )
}

class NewOrderViewModel(application: Application) : AndroidViewModel(application) {

    private val gownApp = getApplication<GownApplication>()
    private val repository = gownApp.repository
    private val preferences = gownApp.preferences

    private val todayDate = todayMillis()
    private var saving = false
    private var loadedOrderId: Long? = null

    private val _uiState = MutableStateFlow(NewOrderUiState())
    val uiState: StateFlow<NewOrderUiState> = _uiState.asStateFlow()

    /** The order being edited, or null when creating a new order. */
    private val editing = MutableStateFlow<Order?>(null)

    val orderNumber: StateFlow<String> =
        combine(preferences.orderCounter, editing) { counter, order ->
            order?.orderNumber ?: formatOrderNumber(counter + 1)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), formatOrderNumber(1))

    val orderDate: StateFlow<Long> =
        editing
            .map { it?.orderDate ?: todayDate }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), todayDate)

    /** Call with an order id to edit it; call with null (or do nothing) for a new order. */
    fun loadOrder(orderId: Long?) {
        if (orderId == null || loadedOrderId == orderId) return
        loadedOrderId = orderId
        viewModelScope.launch {
            val order = repository.getOrder(orderId) ?: return@launch
            editing.value = order
            _uiState.value = order.toUiState()
        }
    }

    private fun change(transform: (NewOrderUiState) -> NewOrderUiState) {
        _uiState.update { transform(it).copy(errorMessage = null) }
    }

    fun onCustomerName(value: String) = change { it.copy(customerName = value) }
    fun onSchoolName(value: String) = change { it.copy(schoolName = value) }
    fun onContactNumber(value: String) = change { it.copy(contactNumber = value) }
    fun onAddress(value: String) = change { it.copy(address = value) }
    fun onSpecialInstructions(value: String) = change { it.copy(specialInstructions = value) }

    fun onDeliveryDate(millis: Long?) {
        if (millis != null) change { it.copy(deliveryDate = millis) }
    }

    fun onColorSelected(color: GownColor, selected: Boolean) = change { state ->
        val newColorState =
            if (selected) state.colors.getValue(color).copy(selected = true) else GownColorState()
        state.copy(colors = state.colors + (color to newColorState))
    }

    fun onColorQuantity(color: GownColor, text: String) = change { state ->
        val digits = text.filter { it.isDigit() }.take(4)
        val newColorState = state.colors.getValue(color).copy(quantity = digits)
        state.copy(colors = state.colors + (color to newColorState))
    }

    fun onTotalAmount(text: String) =
        change { it.copy(totalAmount = text.filter { c -> c.isDigit() }.take(9)) }

    fun onAdvanceAmount(text: String) =
        change { it.copy(advanceAmount = text.filter { c -> c.isDigit() }.take(9)) }

    /** Every field is optional. Only two rules: not completely empty, and advance <= total. */
    private fun validate(state: NewOrderUiState): String? {
        return when {
            !state.hasAnyData -> "Please fill at least one field."
            state.advanceTooHigh -> "Advance payment cannot be more than the total amount."
            else -> null
        }
    }

    private fun buildOrder(
        s: NewOrderUiState,
        id: Long,
        orderNumber: String,
        orderDate: Long,
        createdAt: Long,
        updatedAt: Long
    ): Order {
        val blue = s.colors.getValue(GownColor.Blue)
        val green = s.colors.getValue(GownColor.Green)
        val mehroon = s.colors.getValue(GownColor.Mehroon)
        val black = s.colors.getValue(GownColor.Black)
        return Order(
            id = id,
            orderNumber = orderNumber,
            customerName = s.customerName.trim(),
            schoolName = s.schoolName.trim(),
            contactNumber = s.contactNumber.trim(),
            address = s.address.trim(),
            orderDate = orderDate,
            deliveryDate = s.deliveryDate ?: 0L,
            blueSelected = blue.selected,
            blueQuantity = blue.quantityValue,
            greenSelected = green.selected,
            greenQuantity = green.quantityValue,
            mehroonSelected = mehroon.selected,
            mehroonQuantity = mehroon.quantityValue,
            blackSelected = black.selected,
            blackQuantity = black.quantityValue,
            totalGowns = s.totalGowns,
            totalAmount = s.totalAmountValue,
            advanceAmount = s.advanceValue,
            remainingBalance = s.remainingBalance,
            paymentStatus = s.paymentStatus,
            specialInstructions = s.specialInstructions.trim(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /** Creates a new order, or updates the one being edited. [onSaved] gets the order id. */
    fun saveOrder(onSaved: (Long) -> Unit) {
        if (saving) return
        val s = _uiState.value
        val error = validate(s)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        saving = true
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val existing = editing.value
                val savedId: Long
                if (existing == null) {
                    val number = preferences.nextOrderNumber()
                    val order = buildOrder(
                        s = s,
                        id = 0L,
                        orderNumber = formatOrderNumber(number),
                        orderDate = todayDate,
                        createdAt = now,
                        updatedAt = now
                    )
                    savedId = repository.insert(order)
                    _uiState.value = NewOrderUiState()
                } else {
                    val order = buildOrder(
                        s = s,
                        id = existing.id,
                        orderNumber = existing.orderNumber,
                        orderDate = existing.orderDate,
                        createdAt = existing.createdAt,
                        updatedAt = now
                    )
                    repository.update(order)
                    savedId = existing.id
                }
                onSaved(savedId)
            } finally {
                saving = false
            }
        }
    }
}
