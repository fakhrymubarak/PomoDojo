package com.fakhry.pomodojo.features.preferences.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fakhry.pomodojo.core.designsystem.theme.PomoDojoTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs

@Composable
fun WheelNumbers(
    modifier: Modifier = Modifier.fillMaxWidth(),
    start: Int = 1,
    end: Int = 10,
    selectedValue: Int = start,
    onValueChange: (Int) -> Unit = {},
    onValueChangeDebounceMillis: Long = 120,
) {
    val numbers = remember(start, end) {
        when {
            start <= end -> (start..end).toList()
            else -> (start downTo end).toList()
        }
    }
    if (numbers.isEmpty()) return

    val lowerBound = minOf(start, end)
    val upperBound = maxOf(start, end)
    val clampedSelected = selectedValue.coerceIn(lowerBound, upperBound)
    val selected = if (numbers.contains(clampedSelected)) clampedSelected else numbers.first()
    val selectedIndexInNumbers = numbers.indexOf(selected).takeIf { it >= 0 } ?: 0

    val itemWidth = 44.dp
    val indicatorHeight = 56.dp
    val baseIndex = remember(numbers) {
        val midpoint = Int.MAX_VALUE / 2
        val modulo = numbers.size
        if (modulo == 0) 0 else midpoint - (midpoint % modulo)
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = baseIndex + selectedIndexInNumbers,
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val hapticFeedback = LocalHapticFeedback.current

    val currentValue by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val itemsInfo = layoutInfo.visibleItemsInfo
            if (itemsInfo.isEmpty()) {
                return@derivedStateOf numbers.first()
            }
            val viewportCenter =
                layoutInfo.viewportStartOffset +
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            val centredIndex = itemsInfo.minByOrNull { info ->
                val itemCenter = info.offset + info.size / 2
                abs(itemCenter - viewportCenter)
            }?.index ?: baseIndex

            val safeIndex = ((centredIndex % numbers.size) + numbers.size) % numbers.size
            numbers[safeIndex]
        }
    }
    var hasInitialSync by remember { mutableStateOf(false) }
    var lastEmittedValue by remember { mutableStateOf(selected) }
    LaunchedEffect(selected) {
        lastEmittedValue = selected
    }
    var hasSettled by remember { mutableStateOf(false) }
    LaunchedEffect(numbers, selected) {
        if (numbers.isEmpty()) return@LaunchedEffect
        if (currentValue == selected) {
            if (!hasInitialSync) {
                hasInitialSync = true
            }
            return@LaunchedEffect
        }
        val modulo = numbers.size
        val selectedIndex = selectedIndexInNumbers
        val currentIndex = listState.firstVisibleItemIndex
        val candidateIndices = if (modulo == 0) {
            emptyList()
        } else {
            val currentCycle = currentIndex / modulo
            listOf(currentCycle - 1, currentCycle, currentCycle + 1).mapNotNull { cycle ->
                val candidate = cycle * modulo + selectedIndex
                candidate.takeIf { it >= 0 }
            }
        }
        val defaultTarget = baseIndex + selectedIndex
        val targetIndex = candidateIndices.minByOrNull { abs(it - currentIndex) } ?: defaultTarget
        if (!hasInitialSync) {
            listState.scrollToItem(targetIndex)
        } else if (currentIndex != targetIndex) {
            listState.animateScrollToItem(targetIndex)
        }
        if (!hasInitialSync) {
            hasInitialSync = true
        }
    }
    LaunchedEffect(currentValue) {
        if (currentValue == lastEmittedValue) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        delay(onValueChangeDebounceMillis)
        if (currentValue == lastEmittedValue) return@LaunchedEffect
        lastEmittedValue = currentValue
        onValueChange(currentValue)
        if (!hasSettled) {
            hasSettled = true
        }
    }

    BoxWithConstraints(
        modifier = modifier.height(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        val horizontalPadding = remember(maxWidth, itemWidth) {
            val padding = (maxWidth - itemWidth) / 2
            if (padding > 0.dp) padding else 0.dp
        }

        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(count = Int.MAX_VALUE) { index ->
                val safeIndex = index % numbers.size
                val number = numbers[safeIndex]
                WheelNumberItem(
                    number = number,
                    isSelected = number == currentValue,
                    width = itemWidth,
                )
            }
        }

        SelectionOverlay(
            modifier = Modifier.align(Alignment.Center),
            width = itemWidth,
            height = indicatorHeight,
            shape = MaterialTheme.shapes.medium,
        )
    }
}

@Composable
private fun WheelNumberItem(number: Int, isSelected: Boolean, width: Dp) {
    val scale by animateFloatAsState(if (isSelected) 1f else 0.85f)
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.4f)
    val density = LocalDensity.current

    Box(
        modifier = Modifier.width(width).height(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = if (isSelected) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleMedium
            },
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.cameraDistance = 12f * density.density
            },
        )
    }
}

@Composable
private fun SelectionOverlay(modifier: Modifier = Modifier, width: Dp, height: Dp, shape: Shape) {
    Box(
        modifier = modifier.width(width).height(height).background(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            shape = shape,
        ),
    )
}

@Preview(showBackground = false)
@Composable
fun InfiniteNumberListPreview() {
    PomoDojoTheme {
        WheelNumbers()
    }
}
