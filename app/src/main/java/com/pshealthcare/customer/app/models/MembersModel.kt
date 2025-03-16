package com.pshealthcare.customer.app.models

data class MembersModel(

    val userid: String,
    val name: String,
    val relation: String,
    val birthdate: String,
    val address: String,
    val city: String,
    val pincode: String,
    val mobile: String
){
    constructor() : this("", "", "","","","","","")
}
