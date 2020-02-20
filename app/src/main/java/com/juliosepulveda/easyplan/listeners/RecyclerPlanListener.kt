package com.juliosepulveda.easyplan.listeners

import com.juliosepulveda.easyplan.models.Plan

interface RecyclerPlanListener {
    fun onClick (plan: Plan, position: Int)
}