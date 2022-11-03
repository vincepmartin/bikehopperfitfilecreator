package bikehopperfilecreator

import bikehopperclient.RouteData
import com.garmin.fit.*
import com.garmin.fit.util.SemicirclesConverter
import java.util.*

class BikeHopperFileCreator(val fileName: String, val routeData: RouteData) {
    private lateinit var fileEncoder: FileEncoder
    private val recordMessages = arrayListOf<RecordMesg>()
    private var startTimeStamp: DateTime = DateTime(Date())
    private var lastTimeStamp: DateTime = DateTime(Date())
    private val PRODUCTID = 0

    fun getFile() {
        // Open file
        try {
            fileEncoder = FileEncoder(java.io.File(fileName), Fit.ProtocolVersion.V2_0)
        } catch(e: FitRuntimeException) {
            System.err.println("Error opening file $fileName")
        }

        writeFileIdMessage()
        writeCourseMessage()
        createRecordMessages()
        writeLapMessage()
        writeTimerStartMessage()
        recordMessages.forEach { r -> fileEncoder.write(r) }
        writeTimerStopMessage()

        // Close file.
        try {
            fileEncoder.close()
        } catch (e: FitRuntimeException) {
            System.err.println("Error closing file $fileName")
            e.printStackTrace()
        }

        println("Encoded FIT file $fileName")
    }

    private fun writeFileIdMessage() {
        val fileIdMessage = FileIdMesg()
        fileIdMessage.type = File.COURSE
        fileIdMessage.manufacturer = Manufacturer.DEVELOPMENT
        fileIdMessage.product = PRODUCTID
        fileIdMessage.timeCreated = startTimeStamp // Set to now...
        fileIdMessage.serialNumber = 12345L
        fileEncoder.write(fileIdMessage)
    }

    private fun writeCourseMessage() {
        val courseMessage = CourseMesg()
        courseMessage.name = "BikeHopper Course" // TODO: Change this to something that makes sense for the route, figure out where to get this data.
        courseMessage.sport = Sport.CYCLING
        fileEncoder.write(courseMessage)
    }

    private fun writeLapMessage() {
        val lapMesg = LapMesg()
        lapMesg.startTime = startTimeStamp
        lapMesg.timestamp = startTimeStamp
        lapMesg.totalElapsedTime =  (lastTimeStamp.timestamp - startTimeStamp.timestamp).toFloat()
        lapMesg.totalTimerTime =  (lastTimeStamp.timestamp - startTimeStamp.timestamp).toFloat()
        lapMesg.startPositionLong = recordMessages[0].positionLong
        lapMesg.startPositionLat = recordMessages[0].positionLat
        lapMesg.endPositionLong = recordMessages[recordMessages.size - 1].positionLong
        lapMesg.endPositionLat = recordMessages[recordMessages.size - 1].positionLat
        fileEncoder.write(lapMesg)
    }

    // Write the positions on our map.
    // TODO: Loop through all points in our leg, add the required time stamp along with speed if actually needed.
    private fun createRecordMessages() {
        routeData.paths[1].legs[0].geometry.coordinates.forEach{ point ->
            val recordMesg = RecordMesg()
            recordMesg.positionLong = SemicirclesConverter.degreesToSemicircles(point[0])
            recordMesg.positionLat = SemicirclesConverter.degreesToSemicircles(point[1])
            recordMesg.altitude = point[2].toFloat()
            recordMesg.timestamp = lastTimeStamp
            recordMessages.add(recordMesg)
            lastTimeStamp.add(1)
            printRecordMessage(recordMesg)
        }
    }

    fun writeTimerStartMessage() {
        val eventStartMessage = EventMesg()
        eventStartMessage.timestamp = startTimeStamp
        eventStartMessage.event = Event.TIMER
        eventStartMessage.eventType = EventType.START
        fileEncoder.write(eventStartMessage)
    }

    fun writeTimerStopMessage() {
        val eventStopMessage = EventMesg()
        eventStopMessage.timestamp = lastTimeStamp
        eventStopMessage.event = Event.TIMER
        eventStopMessage.eventType = EventType.STOP_ALL
        fileEncoder.write(eventStopMessage)
    }

    fun printRecordMessage(rm: RecordMesg) {
       println("${rm.positionLong}, ${rm.positionLat}, ${rm.altitude}, ${rm.timestamp}")
    }
}