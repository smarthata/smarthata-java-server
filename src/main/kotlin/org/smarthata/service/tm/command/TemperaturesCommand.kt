package org.smarthata.service.tm.command

import org.smarthata.repository.MeasureRepository
import org.smarthata.repository.SensorRepository
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import java.util.Date
import java.util.concurrent.TimeUnit

@Service
class TemperaturesCommand(
    private val sensorRepository: SensorRepository,
    private val measureRepository: MeasureRepository,
) : AbstractCommand(TEMPERATURES) {
    override fun answer(request: CommandRequest): BotApiMethod<*> {
        val text = (createTextLineForSensor(13, "Улица")
            + createTextLineForSensor(2000239, "Спальня")
            + createTextLineForSensor(7, "Первый этаж")
            + createTextLineForSensor(83720510, "Гараж")
            + createTextLineForSensor(85948634, "Мастерская"))

        return createTmMessage(request, text)
    }

    private fun createTextLineForSensor(sensorId: Int, name: String): String {
        val sensor = sensorRepository.findByIdOrElseThrow(sensorId)
        val measure = measureRepository.findTopBySensorOrderByDateDesc(sensor)
        return "$name : %.1f°C (%d мин. назад)\n".format(measure.value, createMinutesAgo(measure.date))
    }

    private fun createMinutesAgo(date: Date): Long =
        TimeUnit.MILLISECONDS.toMinutes(Date().time - date.time)

    companion object {
        private const val TEMPERATURES = "temp"
    }
}
