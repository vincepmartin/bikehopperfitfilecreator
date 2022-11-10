package bikehopperfilecreator

import bikehopperclient.Instructions
import com.garmin.fit.*

class BHCoursePointMessage(private val instruction: Instructions, private val correspondingRecordMessage: RecordMesg) {
    /*
        Translate GraphHopper sign messages to Garmin CoursePoint types
        https://docs.graphhopper.com/#operation/getRoute/responses/200/json/paths/instructions/sign
     */
    private fun getGarminSignFromGraphHopper(sign: Int): CoursePoint {
        return when (sign) {
            -98 -> CoursePoint.U_TURN  // A U-turn without the knowledge if it is a right or left U-turn
            -8 -> CoursePoint.U_TURN // a left U-turn
            -7 -> CoursePoint.SLIGHT_LEFT // keep left
            -6 -> CoursePoint.INFO // not yet used: leave roundabout
            -3 -> CoursePoint.SHARP_LEFT // turn sharp left
            -2 -> CoursePoint.LEFT  // turn left
            -1 -> CoursePoint.SLIGHT_LEFT // turn slight left
            0 -> CoursePoint.STRAIGHT  // continue on street
            1 -> CoursePoint.SLIGHT_LEFT // turn slight right
            2 -> CoursePoint.RIGHT // turn right
            3 -> CoursePoint.SHARP_RIGHT // turn sharp right
            4 -> CoursePoint.INFO // the finish instruction before the last point
            5 -> CoursePoint.INFO // the instruction before a via point
            6 -> CoursePoint.INFO // the instruction before entering a roundabout
            7 -> CoursePoint.SLIGHT_RIGHT // keep right
            8 -> CoursePoint.U_TURN // a right U-turn
            else -> CoursePoint.GENERIC // Nothing found
        }
    }

    fun getMessage(): CoursePointMesg {
        val cpm = CoursePointMesg()
        cpm.timestamp = correspondingRecordMessage.timestamp
        cpm.name = instruction.text
        cpm.positionLong = correspondingRecordMessage.positionLong
        cpm.positionLat = correspondingRecordMessage.positionLat
        cpm.distance = correspondingRecordMessage.distance
        cpm.type = getGarminSignFromGraphHopper(instruction.sign)
        cpm.localNum = 4
        return cpm
    }
}