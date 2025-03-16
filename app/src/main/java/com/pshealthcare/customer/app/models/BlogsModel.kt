package com.pshealthcare.customer.app.models

data class BlogsModel(
    val blogTitle: String,
    val article: String,
    val imageUrl: String,
    val videoUrl: String,
){
    constructor() : this("", "","","")
}

