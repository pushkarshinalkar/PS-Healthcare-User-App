package com.pshealthcare.customer.app.models

data class OrdersModel(

    val orderid: String,
    val userid: String,
    val name: String,
    val relation: String,
    val age: String,
    val address: String,
    val city: String,
    val pincode: String,
    val mobile: String,
    val total_price: String,
    val total_discount: String,
    var date: String,
    val status: String,
    val result_link: String,
    val invoice_link: String,
    val pay_mode: String,
    val dr_name: String,
    val dr_spec: String,
    val dr_regno: String,
    val dr_mobileno: String,
    val tests: List<TestsModel>
){
    constructor() : this("", "", "","","","","","","","","", "","","","","","","","","",emptyList())
}