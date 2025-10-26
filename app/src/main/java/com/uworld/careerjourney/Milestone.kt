package com.uworld.careerjourney

import androidx.compose.ui.geometry.Offset
import kotlin.math.pow

// Data class representing a milestone on the path
data class Milestone(
    val name: String, // Milestone name
    var t: Float, // Parameter along the path (0 to 1)
    val phase: Float = 0f, // Optional phase for animation purposes
    val description: String = "" // Milestone description
)

class CandyRoadPath(val segments: List<CubicSegment>) {
    // Represents a cubic Bezier curve segment
    data class CubicSegment(
        val p0: Offset, // start point
        val p1: Offset, // control point 1
        val p2: Offset, // control point 2
        val p3: Offset // end point
    )

    fun pointAt(t: Float): Offset {
        if (segments.isEmpty()) return Offset.Zero
        val segmentIndex = ((segments.size - 1) * t).coerceIn(0f, segments.size - 1f).toInt()
        val localT = ((t * segments.size) - segmentIndex).coerceIn(0f, 1f)
        val s = segments[segmentIndex]
        val u = 1 - localT
        return (s.p0 * u.pow(3)) +
                (s.p1 * 3f * u.pow(2) * localT) +
                (s.p2 * 3f * u * localT.pow(2)) +
                (s.p3 * localT.pow(3))
    }

    fun tangentAt(t: Float): Offset {
        if (segments.isEmpty()) return Offset.Zero
        val segmentIndex = ((segments.size - 1) * t).coerceIn(0f, segments.size - 1f).toInt()
        val localT = ((t * segments.size) - segmentIndex).coerceIn(0f, 1f)
        val s = segments[segmentIndex]
        val u = 1 - localT
        // derivative of cubic bezier
        return (s.p1 - s.p0) * 3f * u.pow(2) +
                (s.p2 - s.p1) * 6f * u * localT +
                (s.p3 - s.p2) * 3f * localT.pow(2)
    }

    fun bounds(): Pair<Offset, Offset> {
        val points = (0..100).map { pointAt(it / 100f) }
        val minX = points.minOf { it.x }
        val minY = points.minOf { it.y }
        val maxX = points.maxOf { it.x }
        val maxY = points.maxOf { it.y }
        return Offset(minX, minY) to Offset(maxX, maxY)
    }

    // Find the nearest point on the path to a given point and return its t parameter and coordinates
    fun nearestPointTo(point: Offset, steps: Int = 200): Pair<Float, Offset> {
        var nearestT = 0f
        var nearestPoint = Offset.Zero
        var minDist = Float.MAX_VALUE

        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val p = pointAt(t)
            val dist = (p - point).getDistance()
            if (dist < minDist) {
                minDist = dist
                nearestT = t
                nearestPoint = p
            }
        }

        return nearestT to nearestPoint
    }

    fun nearestTTo(point: Offset, steps: Int = 500): Float {
        var nearestT = 0f
        var minDist = Float.MAX_VALUE
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val p = pointAt(t)
            val dist = (p - point).getDistance()
            if (dist < minDist) {
                minDist = dist
                nearestT = t
            }
        }
        return nearestT
    }
}

private operator fun Offset.times(scalar: Float) = Offset(x * scalar, y * scalar)
private operator fun Offset.plus(other: Offset) = Offset(x + other.x, y + other.y)
private operator fun Offset.minus(other: Offset) = Offset(x - other.x, y - other.y)