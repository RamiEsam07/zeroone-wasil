package com.example.data.model

enum class OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    COMPLETED,
    REJECTED,
    CANCELLED;

    fun canTransitionTo(target: OrderStatus): Boolean {
        return when (this) {
            PENDING -> target == ACCEPTED || target == REJECTED
            ACCEPTED -> target == PREPARING || target == CANCELLED
            PREPARING -> target == READY || target == CANCELLED
            READY -> target == COMPLETED || target == CANCELLED
            COMPLETED, REJECTED, CANCELLED -> false
        }
    }

    val isTerminal: Boolean
        get() = this == COMPLETED || this == REJECTED || this == CANCELLED

    val isActive: Boolean
        get() = this == ACCEPTED || this == PREPARING || this == READY

    fun getDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                PENDING -> "جديد"
                ACCEPTED -> "تم القبول"
                PREPARING -> "قيد التحضير"
                READY -> "جاهز للتسليم"
                COMPLETED -> "مكتمل"
                REJECTED -> "مرفوض"
                CANCELLED -> "ملغي"
            }
        } else {
            when (this) {
                PENDING -> "New Order"
                ACCEPTED -> "Accepted"
                PREPARING -> "Preparing"
                READY -> "Ready for Pickup"
                COMPLETED -> "Completed"
                REJECTED -> "Rejected"
                CANCELLED -> "Cancelled"
            }
        }
    }
}

enum class StoreStatus {
    OPEN,
    BUSY,
    CLOSED;

    fun getDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                OPEN -> "مفتوح"
                BUSY -> "مشغول"
                CLOSED -> "مغلق"
            }
        } else {
            when (this) {
                OPEN -> "OPEN"
                BUSY -> "BUSY"
                CLOSED -> "CLOSED"
            }
        }
    }
}

enum class UserRole {
    STORE_OWNER,
    STORE_MANAGER,
    STORE_STAFF,
    CUSTOMER,
    DRIVER,
    ADMIN;

    fun canManageSettings(): Boolean = this == STORE_OWNER || this == ADMIN
    fun canManageProducts(): Boolean = this == STORE_OWNER || this == STORE_MANAGER || this == ADMIN
    fun canProcessOrders(): Boolean = true
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    CREDIT_CARD,
    APPLE_PAY,
    WALLET;

    fun getDisplayName(isArabic: Boolean): String {
        return if (isArabic) {
            when (this) {
                CASH_ON_DELIVERY -> "الدفع عند الاستلام"
                CREDIT_CARD -> "بطاقة ائتمانية"
                APPLE_PAY -> "Apple Pay"
                WALLET -> "المحفظة"
            }
        } else {
            when (this) {
                CASH_ON_DELIVERY -> "Cash on Delivery"
                CREDIT_CARD -> "Credit Card"
                APPLE_PAY -> "Apple Pay"
                WALLET -> "In-App Wallet"
            }
        }
    }
}

enum class PaymentStatus {
    PAID,
    PENDING_CASH,
    REFUNDED
}
