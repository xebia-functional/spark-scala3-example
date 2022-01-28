package tsp

import scala.math._

object Haversine:
  val R = 6371.0 // mean radius of Earth in km

  /**
   * Calculate the straight-line distance in km between two coordinates using
   * the Haversine formula. This does not give an entirely accurate result,
   * because it assumes the Earth is a perfect sphere, but it's good enough
   * for our purposes.
   *
   * Of course, the travelling distance between two cities would be longer than this,
   * as you'd have to travel by road or air.
   */
  def distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double =
    val dLat = (lat2 - lat1).toRadians
    val dLon = (lon2 - lon1).toRadians

    val a = (sin(dLat / 2) * sin(dLat / 2)) +
            (cos(lat1.toRadians) * cos(lat2.toRadians) * sin(dLon / 2) * sin(dLon / 2))
    val c = 2 * atan2(sqrt(a), sqrt(1.0 - a))
    R * c
