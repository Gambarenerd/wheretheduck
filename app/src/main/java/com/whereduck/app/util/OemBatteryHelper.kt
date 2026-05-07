package com.whereduck.app.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

data class OemBatteryInstruction(
    val manufacturer: String,
    val title: String,
    val steps: List<String>,
    val settingsIntent: Intent?
)

object OemBatteryHelper {

    fun getInstruction(): OemBatteryInstruction? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> OemBatteryInstruction(
                manufacturer = "Xiaomi",
                title = "Disabilita ottimizzazione batteria",
                steps = listOf(
                    "Vai in Impostazioni → App → WhereTheDuck",
                    "Tocca \"Risparmio energetico\" → seleziona \"Nessuna restrizione\"",
                    "Torna indietro → abilita \"Avvio automatico\""
                ),
                settingsIntent = Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
            )
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> OemBatteryInstruction(
                manufacturer = "Huawei",
                title = "Disabilita ottimizzazione batteria",
                steps = listOf(
                    "Vai in Impostazioni → Batteria → Avvio app",
                    "Trova WhereTheDuck → disabilita \"Gestione automatica\"",
                    "Abilita manualmente: Avvio automatico, Avvio secondario, Esecuzione in background"
                ),
                settingsIntent = Intent().apply {
                    component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                }
            )
            manufacturer.contains("samsung") -> OemBatteryInstruction(
                manufacturer = "Samsung",
                title = "Disabilita ottimizzazione batteria",
                steps = listOf(
                    "Vai in Impostazioni → Cura dispositivo → Batteria",
                    "Tocca \"Limiti utilizzo in background\"",
                    "Rimuovi WhereTheDuck dalla lista delle app limitate",
                    "Aggiungi WhereTheDuck alle \"App senza limitazioni\""
                ),
                settingsIntent = Intent().apply {
                    component = ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.battery.ui.BatteryActivity"
                    )
                }
            )
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> OemBatteryInstruction(
                manufacturer = "OPPO/Realme",
                title = "Disabilita ottimizzazione batteria",
                steps = listOf(
                    "Vai in Impostazioni → Batteria → Risparmio energetico",
                    "Trova WhereTheDuck → seleziona \"Non limitare\""
                ),
                settingsIntent = null
            )
            manufacturer.contains("vivo") -> OemBatteryInstruction(
                manufacturer = "Vivo",
                title = "Disabilita ottimizzazione batteria",
                steps = listOf(
                    "Vai in Impostazioni → Batteria → Gestione consumo in background",
                    "Trova WhereTheDuck → seleziona \"Non limitare\""
                ),
                settingsIntent = null
            )
            else -> null
        }
    }

    fun tryOpenSettings(context: Context, instruction: OemBatteryInstruction): Boolean {
        return try {
            instruction.settingsIntent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
