package bikehopperfilecreator

import bikehopperclient.RouteData
import com.garmin.fit.*
import com.garmin.fit.util.SemicirclesConverter
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class BikeHopperFileCreator(private val routeData: RouteData, private val path: Int)  {
    private val bufferEncoder = BufferEncoder(Fit.ProtocolVersion.V2_0)
    private val recordMessages = arrayListOf<RecordMesg>()
    private var startTimeStamp: DateTime = DateTime(Date())
    private var lastTimeStamp: DateTime = DateTime(Date())
    private val productId = 0
    private val timeIncrement = 100.0

    fun getBuffer(): ByteArray {
        try {
            writeFileIdMessage()
            writeCourseMessage()
            createRecordMessages()
            calculateDistanceToPriorPointInMeters()
            writeLapMessage()
            writeTimerStartMessage()
            writeRecordMessages()
            writeCoursePoints()
            writeTimerStopMessage()
            return bufferEncoder.close()
        }
        catch (e: FitRuntimeException) {
            println("Error creating FIT file: ${e.message}")
            throw BikeHopperFileCreatorException()
        }
    }

    private fun writeFileIdMessage() {
        val fileIdMessage = FileIdMesg()
        fileIdMessage.type = File.COURSE
        fileIdMessage.manufacturer = Manufacturer.GARMIN
        fileIdMessage.product = productId
        fileIdMessage.timeCreated = startTimeStamp // Set to now...
        startTimeStamp.add(timeIncrement)
        fileIdMessage.serialNumber = 12345L
        bufferEncoder.write(fileIdMessage)
    }

    private fun writeCourseMessage() {
        val courseMessage = CourseMesg()
        courseMessage.name = "BikeHopper Course"
        courseMessage.sport = Sport.CYCLING
        courseMessage.localNum = 1
        bufferEncoder.write(courseMessage)
    }

    private fun writeLapMessage() {
        val lapMessage = LapMesg()
        lapMessage.startTime = startTimeStamp
        lapMessage.timestamp = startTimeStamp
        lapMessage.totalElapsedTime =  (lastTimeStamp.timestamp - startTimeStamp.timestamp).toFloat()
        lapMessage.totalTimerTime =  (lastTimeStamp.timestamp - startTimeStamp.timestamp).toFloat()
        lapMessage.startPositionLong = recordMessages[0].positionLong
        lapMessage.startPositionLat = recordMessages[0].positionLat
        lapMessage.endPositionLong = recordMessages[recordMessages.size - 1].positionLong
        lapMessage.endPositionLat = recordMessages[recordMessages.size - 1].positionLat
        lapMessage.localNum = 2
        bufferEncoder.write(lapMessage)
    }

    // Create the RecordMessages/positions for our map.
    private fun createRecordMessages() {
        routeData.paths[path].legs[0].geometry.coordinates.forEach{ point ->
            val recordMessage = RecordMesg()
            recordMessage.positionLong = SemicirclesConverter.degreesToSemicircles(point[0])
            recordMessage.positionLat = SemicirclesConverter.degreesToSemicircles(point[1])
            recordMessage.altitude = point[2].toFloat()
            recordMessage.timestamp = lastTimeStamp
            recordMessage.localNum = 5
            recordMessages.add(recordMessage)
            lastTimeStamp.add(timeIncrement) // Increment time stamp between each point
        }
    }

    private fun writeTimerStartMessage() {
        val eventStartMessage = EventMesg()
        eventStartMessage.timestamp = startTimeStamp
        eventStartMessage.event = Event.TIMER
        eventStartMessage.eventType = EventType.START
        eventStartMessage.localNum = 3
        bufferEncoder.write(eventStartMessage)
    }

    // Generate the turn by turn directions
    private fun writeCoursePoints() {
        routeData.paths[path].instructions.forEach{i ->
            val bhCPM = BHCoursePointMessage(i, recordMessages[i.interval[0]])
            val garminCPM = bhCPM.getMessage()
            bufferEncoder.write(garminCPM)
        }
    }

    private fun writeRecordMessages() {
        recordMessages.forEach { r -> bufferEncoder.write(r) }
    }

    private fun findDistanceInMetersBetweenTwoGPSPoints(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 //meters
        val dLat = Math.toRadians(lat2-lat1)
        val dLng = Math.toRadians(lon2-lon1)
        val a = sin(dLat/2) * sin(dLat/2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng/2) * sin(dLng/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        return earthRadius * c
    }

    private fun calculateDistanceToPriorPointInMeters() {
        var distance = 0.0
        recordMessages.forEachIndexed { index, recordMessage ->
            if (index > 0) {
                val priorRecordMessage = recordMessages[index - 1]
                distance += findDistanceInMetersBetweenTwoGPSPoints(
                        SemicirclesConverter.semicirclesToDegrees(priorRecordMessage.positionLat),
                        SemicirclesConverter.semicirclesToDegrees(priorRecordMessage.positionLong),
                        SemicirclesConverter.semicirclesToDegrees(recordMessage.positionLat),
                        SemicirclesConverter.semicirclesToDegrees(recordMessage.positionLong)
                )
            }
            recordMessage.distance = distance.toFloat()
        }
    }

    private fun writeTimerStopMessage() {
        val eventStopMessage = EventMesg()
        eventStopMessage.timestamp = lastTimeStamp
        eventStopMessage.event = Event.TIMER
        eventStopMessage.eventType = EventType.STOP_ALL
        eventStopMessage.localNum = 3
        bufferEncoder.write(eventStopMessage)
    }
}

class BikeHopperFileCreatorException: FitRuntimeException("Error creating fit file.")