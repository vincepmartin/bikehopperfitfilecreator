package bikehopperfilecreator

import bikehopperclient.RouteData
import com.garmin.fit.*
import com.garmin.fit.util.SemicirclesConverter
import java.util.*

class BikeHopperFileCreator(private val routeData: RouteData) {
    private val bufferEncoder = BufferEncoder(Fit.ProtocolVersion.V2_0)
    private val recordMessages = arrayListOf<RecordMesg>()
    private var startTimeStamp: DateTime = DateTime(Date())
    private var lastTimeStamp: DateTime = DateTime(Date())
    private val PRODUCTID = 0
    private final val TIME_INCREMENT = 100.0

    fun getBuffer(): ByteArray {
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

    private fun writeFileIdMessage() {
        val fileIdMessage = FileIdMesg()
        fileIdMessage.type = File.COURSE
        fileIdMessage.manufacturer = Manufacturer.GARMIN
        fileIdMessage.product = PRODUCTID
        fileIdMessage.timeCreated = startTimeStamp // Set to now...
        startTimeStamp.add(TIME_INCREMENT)
        fileIdMessage.serialNumber = 12345L
        bufferEncoder.write(fileIdMessage)
    }

    private fun writeCourseMessage() {
        val courseMessage = CourseMesg()
        courseMessage.name = "BikeHopper Course" // TODO: Change this to something that makes sense for the route, figure out where to get this data.
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
        // Adding total distance to lap message
        lapMessage.totalDistance = 1f
        bufferEncoder.write(lapMessage)
    }

    // Create the RecordMessages/positions for our map.
    private fun createRecordMessages() {
        routeData.paths[0].legs[0].geometry.coordinates.forEach{ point ->
            val recordMessage = RecordMesg()
            recordMessage.positionLong = SemicirclesConverter.degreesToSemicircles(point[0])
            recordMessage.positionLat = SemicirclesConverter.degreesToSemicircles(point[1])
            recordMessage.altitude = point[2].toFloat()
            recordMessage.timestamp = lastTimeStamp
            recordMessage.localNum = 5
            recordMessages.add(recordMessage)
            lastTimeStamp.add(TIME_INCREMENT) // Increment time stamp
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
        routeData.paths[0].instructions.forEach{i ->
            val bhCPM = BHCoursePointMessage(i, recordMessages[i.interval[0]])
            val garminCPM = bhCPM.getMessage()
            println("CP Message: ${garminCPM.positionLong} ${garminCPM.positionLat} ${garminCPM.timestamp}")
            println("RecordMessage: ${recordMessages[i.interval[0]].positionLong} ${recordMessages[i.interval[0]].positionLat} ${recordMessages[i.interval[0]].timestamp}")
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
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
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