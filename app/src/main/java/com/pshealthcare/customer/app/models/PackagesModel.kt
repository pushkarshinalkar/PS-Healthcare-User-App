package com.pshealthcare.customer.app.models

data class PackagesModel(
    val package_name: String = "",
    val img_url: String = "",
    val package_details: String = "",
    val tests: Map<String, TestsModel> = emptyMap()
){
    constructor() : this("", "","", emptyMap())
}

