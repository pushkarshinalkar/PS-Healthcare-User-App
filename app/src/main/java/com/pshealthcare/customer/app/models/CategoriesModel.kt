package com.pshealthcare.customer.app.models

data class CategoriesModel(

    var name : String,
    var image : String
){
    constructor() : this("", "")
}
