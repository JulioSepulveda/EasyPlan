package com.juliosepulveda.easyplan.activities.newPlan

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.juliosepulveda.easyplan.R
import com.juliosepulveda.easyplan.utils.*
import com.juliosepulveda.easyplan.utils.toast
import kotlinx.android.synthetic.main.activity_calendar.*
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private var desde: Date? = null
    private var fechasCorrectas = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView.minDate = calendarView.date

        getExtras()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth -> getDate(dayOfMonth, month, year) }
    }

    private fun getExtras() {
        if (intent.getIntExtra("tipo", 0) == ACTIVITY_SELECT_DATE_DESDE) {
            tvTitle.text = getString(R.string.calendar_desde)

        }  else if (intent.getIntExtra("tipo", 0) == ACTIVITY_SELECT_DATE_HASTA) {
            tvTitle.text = getString(R.string.calendar_hasta)
            desde = Date(intent.getLongExtra("desde", 0))

            calendarView.date = desde!!.time
        }
    }

    private fun getDate(dia: Int, mes: Int, anno: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, anno)
        cal.set(Calendar.MONTH, mes)
        cal.set(Calendar.DAY_OF_MONTH, dia)

        val fecha = cal.time

        if (intent.getIntExtra("tipo", 0) == ACTIVITY_SELECT_DATE_HASTA) {
            if (fecha < desde)
                toast(R.string.seleccion_fecha_hasta)
            else
                fechasCorrectas = true
        }
        else
            fechasCorrectas = true

        if (fechasCorrectas){
            val intent = intent
            intent.putExtra("fecha", fecha.time)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}

