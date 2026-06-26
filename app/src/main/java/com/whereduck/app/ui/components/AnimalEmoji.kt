package com.whereduck.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.whereduck.app.R

@Composable
fun AnimalEmoji(
    animalKey: String,
    emoji: String,
    size: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    if (animalKey == "duck") {
        Image(
            painter = painterResource(R.drawable.duck_icon),
            contentDescription = "Anatra",
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = modifier.defaultMinSize(minWidth = size, minHeight = size)
        )
    } else {
        Text(
            text = emoji,
            fontSize = fontSize,
            modifier = modifier
        )
    }
}
