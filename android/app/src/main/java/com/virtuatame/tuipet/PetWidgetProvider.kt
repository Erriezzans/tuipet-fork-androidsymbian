package com.virtuatame.tuipet

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class PetWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val FEED = "com.virtuatame.tuipet.FEED"
        private const val PLAY = "com.virtuatame.tuipet.PLAY"

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val prefs = context.getSharedPreferences("pet", Context.MODE_PRIVATE)
            val hunger = prefs.getInt("hunger", 50)
            val happiness = prefs.getInt("happiness", 50)

            val views = RemoteViews(context.packageName, R.layout.pet_widget)
            views.setTextViewText(R.id.pet_level, "Level 1")
            views.setTextViewText(
                R.id.pet_stats,
                "Hunger: $hunger%  Happiness: $happiness%"
            )

            fun actionIntent(action: String, request: Int): PendingIntent {
                val intent = Intent(context, PetWidgetProvider::class.java).apply {
                    this.action = action
                }
                return PendingIntent.getBroadcast(
                    context, request, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            views.setOnClickPendingIntent(R.id.feed_button, actionIntent(FEED, id * 10 + 1))
            views.setOnClickPendingIntent(R.id.play_button, actionIntent(PLAY, id * 10 + 2))

            val launch = PendingIntent.getActivity(
                context, id,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.pet_image, launch)

            manager.updateAppWidget(id, views)
        }

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, PetWidgetProvider::class.java)
            )
            ids.forEach { update(context, manager, it) }
        }
    }

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray
    ) {
        ids.forEach { update(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val prefs = context.getSharedPreferences("pet", Context.MODE_PRIVATE)
        val hunger = prefs.getInt("hunger", 50)
        val happiness = prefs.getInt("happiness", 50)

        when (intent.action) {
            FEED -> prefs.edit()
                .putInt("hunger", (hunger + 20).coerceAtMost(100))
                .putInt("happiness", (happiness + 2).coerceAtMost(100))
                .apply()

            PLAY -> prefs.edit()
                .putInt("hunger", (hunger - 5).coerceAtLeast(0))
                .putInt("happiness", (happiness + 15).coerceAtMost(100))
                .apply()

            else -> return
        }

        refresh(context)
    }
}
