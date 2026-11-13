package com.syedmsidsm.prayerreminder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syedmsidsm.prayerreminder.R

enum class SanctuaryTab { HOME, SETTINGS }

@Composable
fun SanctuaryBottomNavBar(
    activeTab: SanctuaryTab?,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xEB07131A),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SanctuaryNavPill(
                    icon = Icons.Default.Home,
                    label = stringResource(R.string.nav_home),
                    isSelected = activeTab == SanctuaryTab.HOME,
                    onClick = onHomeClick
                )
                SanctuaryNavPill(
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.settings),
                    isSelected = activeTab == SanctuaryTab.SETTINGS,
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun SanctuaryNavPill(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) Color(0xFFFBBF24) else Color.Transparent,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF020617) else Color(0xFFD6D3D1),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isSelected) Color(0xFF020617) else Color(0xFFD6D3D1),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
