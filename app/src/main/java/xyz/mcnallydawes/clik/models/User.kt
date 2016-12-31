package xyz.mcnallydawes.clik.models

import java.util.*

class User {
    var email = String()
    var info = String()
    var career = String()
    var lat: Double = 0.0
    var lng: Double = 0.0
    var age: Int = 0
    var firstName = String()
    var lastName = String()
    var lowerAge: Int = 18
    var upperAge: Int = 50
    var shouldGetPush: Boolean = true
    var pictures = ArrayList<Picture>()
    var show = ArrayList<Gender>()
    lateinit var gender: Gender
    var birthday = String()
}
