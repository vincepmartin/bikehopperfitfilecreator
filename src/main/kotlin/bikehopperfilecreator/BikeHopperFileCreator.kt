package bikehopperfilecreator

import bikehopperclient.RouteData
import com.garmin.fit.*
import java.util.*

class BikeHopperFileCreator(fileName: String, routeData: RouteData) {
    private lateinit var fileEncoder: FileEncoder
    private var startTimeStamp: DateTime = DateTime(Date())
    private var lastTimeStamp: DateTime = DateTime(Date())
    private val PRODUCTID = 0

    init {
        try {
            fileEncoder = FileEncoder(java.io.File(fileName), Fit.ProtocolVersion.V2_0)
        } catch(e: FitRuntimeException) {
            System.err.println("Error opening file $fileName")
        }
    }

    fun writeFileIdMessage() {
        val fileIdMessage = FileIdMesg()
        fileIdMessage.type = File.COURSE
        fileIdMessage.manufacturer = Manufacturer.DEVELOPMENT
        fileIdMessage.product = PRODUCTID
        fileIdMessage.timeCreated = startTimeStamp // Set to now...
        fileIdMessage.serialNumber = 12345L
        fileEncoder.write(fileIdMessage)
    }

    fun writeCourseMessage() {
        val courseMessage = CourseMesg()
        courseMessage.name = "BikeHopper Course" // TODO: Change this to something that makes sense for the route, figure out where to get this data.
        courseMessage.sport = Sport.CYCLING
        fileEncoder.write(courseMessage)
    }

    // Write the positions on our map.
    // TODO: Loop through all points in our leg, add the required time stamp along with speed if actually needed.
    fun writeRecordMessages() {
    }
}