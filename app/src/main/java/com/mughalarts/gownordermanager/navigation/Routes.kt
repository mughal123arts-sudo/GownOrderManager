package com.mughalarts.gownordermanager.navigation

object Routes {
    const val ORDER_ID = "orderId"
    const val ORDER_DETAILS = "order_details/{orderId}"
    const val EDIT_ORDER = "edit_order/{orderId}"

    fun orderDetails(id: Long): String = "order_details/$id"
    fun editOrder(id: Long): String = "edit_order/$id"
}
