package com.whereduck.app.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whereduck.app.ui.theme.DuckTheme

@Composable
fun DashboardTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats carousel placeholder
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Le tue stats",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DuckTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DuckTheme.colors.cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Starnazzi questa settimana",
                        fontSize = 14.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                    Text(
                        text = "0 inviati · 0 ricevuti",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuckTheme.colors.textPrimary
                    )
                }
            }
        }

        // VIP users placeholder
        item {
            Text(
                text = "VIP",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DuckTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DuckTheme.colors.cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Aggiungi utenti preferiti per starnazzarli al volo",
                        fontSize = 14.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                }
            }
        }

        // Favorite groups placeholder
        item {
            Text(
                text = "Gruppi preferiti",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DuckTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DuckTheme.colors.cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Segna gruppi come preferiti per vederli qui",
                        fontSize = 14.sp,
                        color = DuckTheme.colors.textSecondary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
