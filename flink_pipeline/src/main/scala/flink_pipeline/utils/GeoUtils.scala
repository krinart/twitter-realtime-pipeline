package flink_pipeline.utils

object GeoUtils {

  val cellsNumberX = 1000 // TODO: Deal with this. It was 6666
  val cellsNumberY = cellsNumberX / 2

  val xCoordPerCell: Double = 360.0 / cellsNumberX
  val yCoordPerCell: Double = 180.0 / cellsNumberY

  def coordinatesToCell(longitude: Double, latitude: Double): (Int, Int) = {
    val cellX: Int = ((longitude + 180) / xCoordPerCell).toInt
    val cellY: Int = ((latitude + 90) / yCoordPerCell).toInt
    (cellX, cellY)
  }

  def cellToCoordinates(cell: (Int, Int)): (Double, Double) = {
    val longitude = cell._1 * xCoordPerCell - 180
    val latitude = cell._2 * yCoordPerCell - 90
    (longitude.toFloat, latitude.toFloat)
  }
}
