package com.whereduck.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.whereduck.app.data.model.AnimalRegistry

@Composable
fun AnimalEmoji(
    animalKey: String,
    emoji: String,
    size: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val animal = AnimalRegistry.findAnimal(animalKey)
    val drawableRes = animal?.drawableRes
    if (drawableRes != null) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = animalKey,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = modifier
                .fillMaxSize()
                .padding(top = animal.topPadding)
        )
    } else {
        Text(
            text = emoji,
            fontSize = fontSize,
            modifier = modifier
        )
    }
}
