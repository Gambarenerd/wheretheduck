package com.whereduck.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.whereduck.app.data.model.AnimalRegistry
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.ui.theme.StarnazzoHeavy
import com.whereduck.app.ui.theme.StarnazzoLight
import com.whereduck.app.ui.theme.StarnazzoMedium

@Composable
fun StarnazzoLevelSelector(
    selectedLevel: StarnazzoLevel,
    onLevelSelected: (StarnazzoLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StarnazzoLevel.entries.forEach { level ->
            val animalKey = AnimalRegistry.getSelectedAnimal(context, level)
            val animalEmoji = AnimalRegistry.getEmoji(animalKey, level)
            val animalName = AnimalRegistry.findAnimal(animalKey)?.name ?: level.animalName
            val isSelected = level == selectedLevel
            val color = when (level) {
                StarnazzoLevel.LIGHT -> StarnazzoLight
                StarnazzoLevel.MEDIUM -> StarnazzoMedium
                StarnazzoLevel.HEAVY -> StarnazzoHeavy
            }

            Surface(
                onClick = { onLevelSelected(level) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimalEmoji(
                        animalKey = animalKey,
                        emoji = animalEmoji,
                        size = 32.dp,
                        fontSize = 28.sp
                    )
                    Text(
                        text = animalName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
