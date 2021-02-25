package com.example.allhome.data

import androidx.lifecycle.ViewModel
import java.math.BigDecimal

class GroceryItem(var name: String? = null,
                  val quantity:BigDecimal = BigDecimal.ZERO,
                  val unit: String? = null,
                  val pricerPerUnit:BigDecimal = BigDecimal.ZERO,
                  val notes: String? = null,
                  val imagePath: String? = null) : ViewModel() {



    /*fun setName(nameParameter:String){
        name = nameParameter
    }*/



}