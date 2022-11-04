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

    fun getFile(): java.io.File {
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
        writeRecordMessages()
        writeCoursePoints()
        writeTimerStopMessage()

        // Close file.
        try {
            fileEncoder.close()
        } catch (e: FitRuntimeException) {
            System.err.println("Error closing file $fileName")
            e.printStackTrace()
        }

        return java.io.File("nachos.fit")
        println("Encoded FIT file $fileName")
    }

    // Generate the turn by turn directions
    private fun writeCoursePoints() {

    }

    private fun writeRecordMessages() {
        recordMessages.forEach { r -> fileEncoder.write(r) }
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
        val lapMessage = LapMesg()
        lapMessage.startTime = startTimeStamp
        lapMessage.timestamp = startTimeStamp
        lapMessage.totalElapsedTime =  (lastTimeStamp.timestamp - startTimeStamp.timestamp).toFloat()
        lapMessage.totalTimerTime =  (lastTimeStamp.timestamp - startTimeStamp.timestamp).toFloat()
        lapMessage.startPositionLong = recordMessages[0].positionLong
        lapMessage.startPositionLat = recordMessages[0].positionLat
        lapMessage.endPositionLong = recordMessages[recordMessages.size - 1].positionLong
        lapMessage.endPositionLat = recordMessages[recordMessages.size - 1].positionLat
        fileEncoder.write(lapMessage)
    }

    // Write the positions on our map.
    private fun createRecordMessages() {
        routeData.paths[1].legs[0].geometry.coordinates.forEach{ point ->
            val recordMessage = RecordMesg()
            recordMessage.positionLong = SemicirclesConverter.degreesToSemicircles(point[0])
            recordMessage.positionLat = SemicirclesConverter.degreesToSemicircles(point[1])
            recordMessage.altitude = point[2].toFloat()
            recordMessage.timestamp = lastTimeStamp
            recordMessages.add(recordMessage)
            lastTimeStamp.add(1) // Increment time stamp
            printRecordMessage(recordMessage)
        }
    }

    private fun writeTimerStartMessage() {
        val eventStartMessage = EventMesg()
        eventStartMessage.timestamp = startTimeStamp
        eventStartMessage.event = Event.TIMER
        eventStartMessage.eventType = EventType.START
        fileEncoder.write(eventStartMessage)
    }

    private fun writeTimerStopMessage() {
        val eventStopMessage = EventMesg()
        eventStopMessage.timestamp = lastTimeStamp
        eventStopMessage.event = Event.TIMER
        eventStopMessage.eventType = EventType.STOP_ALL
        fileEncoder.write(eventStopMessage)
    }

    private fun printRecordMessage(rm: RecordMesg) {
       println("${rm.positionLong}, ${rm.positionLat}, ${rm.altitude}, ${rm.timestamp}")
    }
}