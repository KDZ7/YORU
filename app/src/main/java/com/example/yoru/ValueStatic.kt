package com.example.yoru

object ValueStatic {
    var host: String = "f6f84dcf008a452b981fb02eb7f7dd35.s1.eu.hivemq.cloud"
    var port: Int = 8883
    var username: String = "hivemq.webclient.1713396490080"
    var password: String = "E38k4eg$5>QIf#HBl&Ns"
    var look: String = "Person"
    var topic: String = "Device/Feature"
    var tmpInfo: String = ""
    fun printValues() {
        println("Host: $host")
        println("Port: $port")
        println("Username: $username")
        println("Password: $password")
        println("Look: $look")
        println("Topic: $topic")
    }
}
