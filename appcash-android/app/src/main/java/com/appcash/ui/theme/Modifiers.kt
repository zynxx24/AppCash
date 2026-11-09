package com.appcash.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Gradasi kartu oranye sesuai desain: terang di atas, gelap di bawah. */
val OrangeCardBrush = Brush.verticalGradient(listOf(Color(0xFFFFB347), OrangeDark))

/**
 * Kartu oranye standar AppCash: bayangan lembut + border tipis + sudut bulat.
 * Urutan modifier penting: shadow -> border -> clip -> background,
 * supaya bayangan tidak terpotong dan border mengikuti sudut kartu.
 */
fun Modifier.orangeCard(radius: Dp = 16.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .shadow(elevation = 6.dp, shape = shape, ambientColor = OrangeDark, spotColor = OrangeDark)
        .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
        .clip(shape)
        .background(OrangeCardBrush)
}
