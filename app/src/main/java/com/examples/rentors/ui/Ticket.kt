package com.examples.rentors.ui

import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapePath


val ticketShapePathModel = ShapeAppearanceModel
    .Builder()
    .setAllCorners(CornerFamily.ROUNDED, 90f)
    .setLeftEdge(TicketEdgeTreatment(50f))
    .setRightEdge(TicketEdgeTreatment(50f))
    .build()

class TicketDrawable : MaterialShapeDrawable(ticketShapePathModel)

class TicketEdgeTreatment(
    private val size: Float
): EdgeTreatment() {
    override fun getEdgePath(
        length: Float,
        center: Float,
        interpolation: Float,
        shapePath: ShapePath
    ) {
        val circleRadius = size * interpolation
        shapePath.lineTo(center - circleRadius, 0f)
        shapePath.addArc(
            center - circleRadius, -circleRadius,
            center + circleRadius, circleRadius,
            180f,
            -180f
        )
        shapePath.lineTo(length, 0f)
    }
}