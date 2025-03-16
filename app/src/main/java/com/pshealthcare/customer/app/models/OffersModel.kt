package com.pshealthcare.customer.app.models

data class OffersModel(

    val offer_img_url_1: String,
    val offer_img_url_2: String,
    val offer_img_url_3: String
){
    constructor() : this("", "", "")
}
