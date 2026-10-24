package com.nerdsyntax.juntalucas.core.navigation

object Routes {
    const val ACTIVITY_SELECTION = "activity_selection"
    const val ADD_EXPENSE = "add_expense"
    const val ADD_SALE = "add_movement"
    const val ADD_PRODUCT = "add_product"
    const val EDIT_PRODUCT = "add_product/{productId}"
    const val BUSINESS_INFO = "business_info"
    const val SESSION_CHECK = "session_check"
    const val SPLASH = "splash"
    const val STARTING_POINT = "starting_point"
    const val WELCOME = "welcome"

    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val VERIFY_EMAIL = "verify_email"

    // Menú inferior
    const val DASHBOARD = "dashboard"
    const val MOVEMENTS = "movements"
    const val BUSINESS = "business"
    const val AI = "ai"
    const val PROFILE = "profile"

    // Cuenta
    const val ACCOUNT = "account"

    // Inteligencia artificial
    const val AI_CHAT = "ai_chat"
    const val AI_MONTHLY = "ai_monthly"

    // Se conserva hasta actualizar el flujo anterior.
    const val AI_LOADING = "ai_loading"
}