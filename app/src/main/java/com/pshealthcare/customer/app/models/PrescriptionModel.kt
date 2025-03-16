package com.pshealthcare.customer.app.models

data class PrescriptionModel(
    val pres_link: String,
    val mailid: String
){
    constructor() : this("", "")
}

