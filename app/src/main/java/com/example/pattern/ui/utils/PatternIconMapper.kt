package com.example.pattern.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object PatternIconMapper {
    fun getIcon(name: String): ImageVector {
        return when (name) {
            // General
            "home" -> Icons.Rounded.Home
            "star" -> Icons.Rounded.Star
            "heart" -> Icons.Rounded.Favorite
            "check" -> Icons.Rounded.CheckCircle
            "light" -> Icons.Rounded.Lightbulb
            "search" -> Icons.Rounded.Search
            "settings" -> Icons.Rounded.Settings
            "person" -> Icons.Rounded.Person
            "group" -> Icons.Rounded.Group
            "time" -> Icons.Rounded.Schedule
            "calendar" -> Icons.Rounded.CalendarMonth
            "edit" -> Icons.Rounded.Edit
            "photo" -> Icons.Rounded.CameraAlt
            "celebration" -> Icons.Rounded.Celebration
            "chat" -> Icons.Rounded.Chat

            // Fitness & Health
            "fitness" -> Icons.Rounded.FitnessCenter
            "water" -> Icons.Rounded.WaterDrop
            "run" -> Icons.Rounded.DirectionsRun
            "bike" -> Icons.Rounded.DirectionsBike
            "walk" -> Icons.Rounded.DirectionsWalk
            "meditation" -> Icons.Rounded.SelfImprovement
            "gym" -> Icons.Rounded.FitnessCenter
            "pool" -> Icons.Rounded.Pool
            "sports_soccer" -> Icons.Rounded.SportsSoccer
            "sports_basketball" -> Icons.Rounded.SportsBasketball
            "sports_tennis" -> Icons.Rounded.SportsTennis
            "spa" -> Icons.Rounded.Spa
            "psychology" -> Icons.Rounded.Psychology
            "bathtub" -> Icons.Rounded.Bathtub

            // Food & Nutrition
            "food" -> Icons.Rounded.Restaurant
            "coffee" -> Icons.Rounded.Coffee
            "fastfood" -> Icons.Rounded.Fastfood
            "nutrition" -> Icons.Rounded.RestaurantMenu
            "local_drink" -> Icons.Rounded.LocalDrink

            // Work & Productivity
            "work" -> Icons.Rounded.Work
            "book" -> Icons.AutoMirrored.Rounded.MenuBook
            "code" -> Icons.Rounded.Code
            "art" -> Icons.Rounded.Palette
            "school" -> Icons.Rounded.School
            "brush" -> Icons.Rounded.Brush
            "cleaning" -> Icons.Rounded.CleaningServices
            "phone" -> Icons.Rounded.Phone
            "email" -> Icons.Rounded.Email
            "computer" -> Icons.Rounded.Computer
            "laptop" -> Icons.Rounded.Laptop
            "storage" -> Icons.Rounded.Storage

            // Finance
            "money" -> Icons.Rounded.Payments
            "shopping" -> Icons.Rounded.ShoppingCart
            "savings" -> Icons.Rounded.Savings
            "account_balance" -> Icons.Rounded.AccountBalance
            "wallet" -> Icons.Rounded.Wallet
            "trending_up" -> Icons.Rounded.TrendingUp

            // Home & Lifestyle
            "house" -> Icons.Rounded.House
            "pets" -> Icons.Rounded.Pets
            "soap" -> Icons.Rounded.Soap
            "movie" -> Icons.Rounded.Movie
            "game" -> Icons.Rounded.Gamepad
            "smoking" -> Icons.Rounded.SmokingRooms
            "no_smoking" -> Icons.Rounded.SmokeFree
            "local_laundry" -> Icons.Rounded.LocalLaundryService
            "bed" -> Icons.Rounded.Bed

            else -> Icons.AutoMirrored.Rounded.Label
        }
    }
}
