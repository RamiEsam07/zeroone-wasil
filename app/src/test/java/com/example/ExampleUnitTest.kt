package com.example

import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testOrderStatusStateTransitions() {
        // Pending can transition to Accepted or Rejected
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.ACCEPTED))
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.REJECTED))
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.READY))
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.COMPLETED))

        // Accepted can transition to Preparing
        assertTrue(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.PREPARING))
        assertFalse(OrderStatus.ACCEPTED.canTransitionTo(OrderStatus.PENDING))

        // Preparing can transition to Ready
        assertTrue(OrderStatus.PREPARING.canTransitionTo(OrderStatus.READY))
        assertFalse(OrderStatus.PREPARING.canTransitionTo(OrderStatus.PENDING))

        // Ready can transition to Completed
        assertTrue(OrderStatus.READY.canTransitionTo(OrderStatus.COMPLETED))
        assertFalse(OrderStatus.READY.canTransitionTo(OrderStatus.PREPARING))

        // Completed is terminal
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.READY))
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PENDING))
    }

    @Test
    fun testUserRolePermissions() {
        assertTrue(UserRole.STORE_OWNER.canManageSettings())
        assertFalse(UserRole.STORE_STAFF.canManageSettings())
        assertTrue(UserRole.STORE_MANAGER.canManageProducts())
    }
}
