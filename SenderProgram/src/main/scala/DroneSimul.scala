package scala


import org.apache.spark.util.random
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsValue, Json}

import scala.util.Random

class DroneSimul(val id: Int) {

  val cities: List[String] = List("NY", "NJ", "CT","PA","NE","MD","MA","99","RI","OL","HF","VF","NP","BF","AS","CH","JK","LS","MP","UK","VP")
  val typeIdentifiable: List[Boolean] = List(true, false)
  val plateIds: List[String] = List("GBB9093", "62416MB", "78755JZ","63009MA","91648MC","T60DAR","GCR2838","XZ764G","GBH9379","MCL78B","M367CN","GAR6813","GEN8674","GAC2703","40793JY","GAD1485","GFC5338","815M342","GJA3452","YZY6476","WBJ819")
  val violationCodes: List[Int] = List(46,14,75,86,95,87,55,78,12,45,43,66,99,23,24,19,53,34,82,26,84,25,66,85,75,91)
  val summonsNumbers: List[String] = List("1283294138", "1283294138", "1283294163","1283294175","1283294187","1283294217","1283294229","1283983620","1286248000","1286282330","1286282342","1286246416","1286246398","1286123550","1286036800","1283983825","1283983771","1283983734","1283983679","1283983667","1283983631")

  var IDdrone: Int = id
  var niveauBatterie: Int = 100
  var droneAltitude: Int = 0
  var temperature: Int = 0
  var speed : Int = 0
  var disc_space: Int = 5000
  var temps: Long = DateTime.now(DateTimeZone.UTC).getMillis
  var city = getRandomElements(cities)
  var identifiable = getRandomElements(typeIdentifiable)
  var plaque = getRandomElements(plateIds)
  var violationCode = getRandomElements(violationCodes)
  var summonsNumber = getRandomElements(summonsNumbers)
  var latitude: Double = 57.3648
  var longitude: Double = 8.5364

  def infoDrone(): Unit = {
    println ("Drone id: " + IDdrone);
    //println ("Position Latitude : " + latitude);
    //println ("Position Longitude : " + longitude);
    //println ("City : " + city);
  }

  def messageJSON(): String = {
    val messageDroneJSON: JsValue = Json.parse(
      s"""
         |{
         |  "IdDrone" : ${IDdrone},
         |  "NiveauBatterie" : ${niveauBatterie},
         |  "DroneAltitude" : ${droneAltitude},
         |  "Speed" : ${speed},
         |  "DiscSpace" : ${disc_space},
         |  "RegistrationState" : "${city}",
         |  "Temperature" : ${temperature},
         |  "ViolationTime" : ${temps},
         |  "Identifiable" : ${identifiable},
         |  "PlateID" : "${plaque}",
         |  "ViolationCode" : ${violationCode},
         |  "SummonsNumber" : "${summonsNumber}",
         |  "Latitude" : ${latitude},
         |  "Longitude" : ${longitude},
         |  "PlateType" : "-1",
         |  "IssueDate" : "-1",
         |  "ViolationLocation" : "-1",
         |  "VehicleBodyType": "-1",
         |  "VehicleMake" : "-1",
         |  "IssuingAgency" : "-1",
         |  "StreetCode1" : "-1",
         |  "StreetCode2" : "-1",
         |  "StreetCode3" : "-1",
         |  "VehicleExpirationDate": "-1",
         |  "ViolationPrecinct": "-1",
         |  "IssuerPrecinct": "-1",
         |  "IssuerCode" : "-1",
         |  "IssuerCommand": "-1",
         |  "IssuerSquad" : "-1"
         |}
         |""".stripMargin)

    Json.stringify(messageDroneJSON)
  }

  def simulDrone(): Unit ={
    val random = new Random()
    temperature = 37 + 1
    niveauBatterie = niveauBatterie - 2
    droneAltitude = droneAltitude + 1 + random.nextInt(4)
    speed = 20 + random.nextInt(60)
    disc_space = disc_space - random.nextInt(30 + random.nextInt(20))
    latitude = latitude + random.nextDouble()
    longitude = longitude + random.nextDouble()
    summonsNumber = getRandomElements(summonsNumbers)
    identifiable = getRandomElements(typeIdentifiable)
    violationCode = getRandomElements(violationCodes)
    plaque = getRandomElements(plateIds)
    city = getRandomElements(cities)
    temps = temps + 300028

  }

  def getRandomElements(list: List[Any]): Any = {
    Random.shuffle(list).head
  }


}
