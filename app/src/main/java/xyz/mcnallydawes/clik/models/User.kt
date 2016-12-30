package xyz.mcnallydawes.clik.models

import java.util.*

class User {
    lateinit var email: String
    lateinit var info: String
    lateinit var career: String
    var lat: Double = 0.0
    var lng: Double = 0.0
    var age: Int = 0
    lateinit var firstName: String
    lateinit var lastName: String
    var lowerAge: Int = 18
    var upperAge: Int = 50
    var shouldGetPush: Boolean = true
    lateinit var pictures: ArrayList<Picture>
    lateinit var show: ArrayList<Gender>
    lateinit var gender: Gender
    lateinit var birthday: String
}
