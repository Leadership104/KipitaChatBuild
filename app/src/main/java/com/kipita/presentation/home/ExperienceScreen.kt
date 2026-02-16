package com.kipita.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun ExperienceScreen(paddingValues: PaddingValues) {
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text("Kipita Experience", style = MaterialTheme.typography.headlineMedium)
        Text("Experience travel like never before", style = MaterialTheme.typography.bodyLarge)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
            itemsIndexed(KipitaFeatures.officialComponents) { index, feature ->
                val expanded = expandedIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (expanded) 1f else 0.97f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "feature-card-scale"
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clickable { expandedIndex = if (expanded) -1 else index }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(feature.title, style = MaterialTheme.typography.titleLarge)
                        AnimatedVisibility(visible = expanded) {
                            Text(feature.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
