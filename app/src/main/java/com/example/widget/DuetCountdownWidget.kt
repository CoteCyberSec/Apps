package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R
import com.example.data.LanguagePreferences
import com.example.model.CoupleAestheticCatalog
import com.example.model.WidgetCoupleSnapshot
import com.example.model.WidgetCountdownDisplayData
import com.example.model.WidgetSummaryFormatter
import com.example.ui.theme.resolveThemePalette

class DuetCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DuetCountdownWidget()
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            DuetCountdownWidget().updateAll(context)
        } catch (e: Exception) {
            Log.w("DuetWidget", "Failed refreshing widget", e)
        }
    }
}

enum class PinWidgetOutcome {
    REQUESTED_SYSTEM_PROMPT,
    UNSUPPORTED_BY_LAUNCHER
}

object WidgetUpdateHelper {
    private const val TAG = "WidgetUpdateHelper"

    suspend fun syncSnapshotAndUpdateWidgets(
        context: Context,
        preferences: LanguagePreferences,
        snapshot: WidgetCoupleSnapshot
    ) {
        try {
            val changed = preferences.saveWidgetSnapshotIfChanged(snapshot)
            if (changed) {
                DuetCountdownWidget().updateAll(context)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to sync widget snapshot", e)
        }
    }

    fun requestPinCountdownWidget(context: Context): PinWidgetOutcome {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                val provider = ComponentName(context, DuetCountdownWidgetReceiver::class.java)
                val requested = appWidgetManager.requestPinAppWidget(provider, null, null)
                if (requested) {
                    return PinWidgetOutcome.REQUESTED_SYSTEM_PROMPT
                }
            }
        }
        return PinWidgetOutcome.UNSUPPORTED_BY_LAUNCHER
    }
}

class DuetCountdownWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(COMPACT_SIZE, MEDIUM_SIZE, EXPANDED_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = LanguagePreferences(context)
        val initialSnapshot = preferences.readWidgetSnapshot()

        provideContent {
            val snapshot by preferences.widgetSnapshotFlow.collectAsState(initial = initialSnapshot)
            val safeId = snapshot.aestheticThemeId.ifBlank { CoupleAestheticCatalog.DEFAULT_ID }
            val (lightScheme, _) = resolveThemePalette(safeId, darkTheme = false)
            val (darkScheme, _) = resolveThemePalette(safeId, darkTheme = true)
            val display = WidgetSummaryFormatter.buildDisplayData(snapshot)

            GlanceTheme(colors = ColorProviders(light = lightScheme, dark = darkScheme)) {
                DuetWidgetContent(display = display)
            }
        }
    }

    companion object {
        private val COMPACT_SIZE = DpSize(130.dp, 96.dp)
        private val MEDIUM_SIZE = DpSize(240.dp, 100.dp)
        private val EXPANDED_SIZE = DpSize(260.dp, 165.dp)
    }
}

@Composable
private fun DuetWidgetContent(display: WidgetCountdownDisplayData) {
    val size = LocalSize.current
    val isCompact = size.width < 200.dp
    val isExpanded = size.height >= 150.dp && !isCompact

    val rootModifier = GlanceModifier
        .fillMaxSize()
        .background(GlanceTheme.colors.surface)
        .appWidgetBackground()
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
            } else {
                GlanceModifier.cornerRadius(22.dp)
            }
        )
        .clickable(actionStartActivity<MainActivity>())
        .padding(if (isCompact) 10.dp else 12.dp)

    Column(modifier = rootModifier) {
        if (!isCompact) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_heart),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                    modifier = GlanceModifier.size(14.dp)
                )
                Spacer(modifier = GlanceModifier.width(5.dp))
                Text(
                    text = display.headerTitle,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )
                Box(
                    modifier = GlanceModifier
                        .size(28.dp)
                        .cornerRadius(14.dp)
                        .clickable(actionRunCallback<RefreshWidgetAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_refresh),
                        contentDescription = "Refresh",
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                        modifier = GlanceModifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = GlanceModifier.height(6.dp))
        }

        if (isCompact) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactMetricRow(
                    iconRes = R.drawable.ic_widget_heart,
                    tint = GlanceTheme.colors.primary,
                    tileColor = GlanceTheme.colors.primaryContainer,
                    eyebrow = display.togetherEyebrow,
                    headline = listOf(display.togetherValue, display.togetherUnit)
                        .filter { it.isNotBlank() }
                        .joinToString(" "),
                    textColor = GlanceTheme.colors.onSurface,
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                CompactMetricRow(
                    iconRes = R.drawable.ic_widget_plane,
                    tint = GlanceTheme.colors.tertiary,
                    tileColor = GlanceTheme.colors.tertiaryContainer,
                    eyebrow = display.reunionEyebrow,
                    headline = listOf(display.reunionValue, display.reunionUnit)
                        .filter { it.isNotBlank() }
                        .joinToString(" "),
                    textColor = GlanceTheme.colors.onSurface,
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                )
            }
        } else {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricTile(
                    iconRes = R.drawable.ic_widget_heart,
                    accentColor = GlanceTheme.colors.primary,
                    tileColor = GlanceTheme.colors.primaryContainer,
                    onSurface = GlanceTheme.colors.onSurface,
                    onSurfaceVariant = GlanceTheme.colors.onSurfaceVariant,
                    eyebrow = display.togetherEyebrow,
                    value = display.togetherValue,
                    unit = display.togetherUnit,
                    subtitle = display.togetherSubtitle,
                    showSubtitle = isExpanded || !display.hasTogetherDate,
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                MetricTile(
                    iconRes = R.drawable.ic_widget_plane,
                    accentColor = GlanceTheme.colors.tertiary,
                    tileColor = GlanceTheme.colors.tertiaryContainer,
                    onSurface = GlanceTheme.colors.onSurface,
                    onSurfaceVariant = GlanceTheme.colors.onSurfaceVariant,
                    eyebrow = display.reunionEyebrow,
                    value = display.reunionValue,
                    unit = display.reunionUnit,
                    subtitle = display.reunionSubtitle,
                    showSubtitle = true,
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun CompactMetricRow(
    iconRes: Int,
    tint: ColorProvider,
    tileColor: ColorProvider,
    eyebrow: String,
    headline: String,
    textColor: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier
            .background(tileColor)
            .cornerRadius(14.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = eyebrow,
            colorFilter = ColorFilter.tint(tint),
            modifier = GlanceModifier.size(16.dp)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column {
            Text(
                text = eyebrow,
                style = TextStyle(
                    color = tint,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                text = headline,
                style = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MetricTile(
    iconRes: Int,
    accentColor: ColorProvider,
    tileColor: ColorProvider,
    onSurface: ColorProvider,
    onSurfaceVariant: ColorProvider,
    eyebrow: String,
    value: String,
    unit: String,
    subtitle: String,
    showSubtitle: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(tileColor)
            .cornerRadius(18.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = eyebrow,
                colorFilter = ColorFilter.tint(accentColor),
                modifier = GlanceModifier.size(14.dp)
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = eyebrow,
                style = TextStyle(
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = TextStyle(
                    color = onSurface,
                    fontSize = if (value.length <= 5) 22.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            if (unit.isNotBlank()) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = unit,
                    style = TextStyle(
                        color = onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }

        if (showSubtitle && subtitle.isNotBlank()) {
            Spacer(modifier = GlanceModifier.height(3.dp))
            Text(
                text = subtitle,
                style = TextStyle(
                    color = onSurfaceVariant,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}
