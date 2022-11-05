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

    fun getBuffer(): ByteArray {
        writeFileIdMessage()
        writeCourseMessage()
        createRecordMessages()
        writeLapMessage()
        writeTimerStartMessage()
        writeRecordMessages()
        writeCoursePoints()
        writeTimerStopMessage()
        return bufferEncoder.close()
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

    private fun writeFileIdMessage() {
        val fileIdMessage = FileIdMesg()
        fileIdMessage.type = File.COURSE
        fileIdMessage.manufacturer = Manufacturer.DEVELOPMENT
        fileIdMessage.product = PRODUCTID
        fileIdMessage.timeCreated = startTimeStamp // Set to now...
        fileIdMessage.serialNumber = 12345L
        bufferEncoder.write(fileIdMessage)
    }

    private fun writeCourseMessage() {
        val courseMessage = CourseMesg()
        courseMessage.name = "BikeHopper Course" // TODO: Change this to something that makes sense for the route, figure out where to get this data.
        courseMessage.sport = Sport.CYCLING
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
        bufferEncoder.write(lapMessage)
    }

    // Write the positions on our map.
    private fun createRecordMessages() {
        routeData.paths[0].legs[0].geometry.coordinates.forEach{ point ->
            val recordMessage = RecordMesg()
            recordMessage.positionLong = SemicirclesConverter.degreesToSemicircles(point[0])
            recordMessage.positionLat = SemicirclesConverter.degreesToSemicircles(point[1])
            recordMessage.altitude = point[2].toFloat()
            recordMessage.timestamp = lastTimeStamp
            recordMessages.add(recordMessage)
            lastTimeStamp.add(1) // Increment time stamp
        }
    }

    private fun writeTimerStartMessage() {
        val eventStartMessage = EventMesg()
        eventStartMessage.timestamp = startTimeStamp
        eventStartMessage.event = Event.TIMER
        eventStartMessage.eventType = EventType.START
        bufferEncoder.write(eventStartMessage)
    }

    private fun writeTimerStopMessage() {
        val eventStopMessage = EventMesg()
        eventStopMessage.timestamp = lastTimeStamp
        eventStopMessage.event = Event.TIMER
        eventStopMessage.eventType = EventType.STOP_ALL
        bufferEncoder.write(eventStopMessage)
    }
}