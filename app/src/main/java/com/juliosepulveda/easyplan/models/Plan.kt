package com.juliosepulveda.easyplan.models

import java.util.*

data class Plan(
        var user: String,
        var title: String,
        var desc: String,
        var fechaDesde: Date,
        var fechaHasta: Date,
        var persons: Int,
        var image: String,
        var location: String,
        var group: String)
