package com.example.allhome.grocerylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.allhome.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class GroceryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_list_actvity)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            showGroceryListNameInput()
        }
    }


    fun showGroceryListNameInput(){

        val groceryListNameInputDialog = CustomDialog(this)
        groceryListNameInputDialog.setButtonClickListener(View.OnClickListener {
            var groceryListName = groceryListNameInputDialog.groceryListName()

            groceryListNameInputDialog.mAlertDialog.dismiss()
            // open create item activity
            /*val intent = Intent(this,AddGroceryListItemActivity::class.java)
            intent.putExtra(AddGroceryListItemActivity.GROCERY_NAME_EXTRA_DATA_TAG,groceryListName)
            startActivity(intent)*/

            val intent = Intent(this,SingleGroceryListActivity::class.java)
            intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG,groceryListName)
            startActivity(intent)

        })
        groceryListNameInputDialog.createPositiveButton("Continue")
        groceryListNameInputDialog.show()
    }

    /**
     * Custom alertdialog
     */
    class CustomDialog(context: Context) : AlertDialog.Builder(context) {
        companion object{
            var POSITIVE_BUTTON_ID = AlertDialog.BUTTON_POSITIVE
        }
        var mGroceryListNameInput:LinearLayout;
        lateinit var mOnClickListener: View.OnClickListener;
        lateinit var mAlertDialog:AlertDialog;
        init {

            mGroceryListNameInput = LayoutInflater.from(context).inflate( R.layout.grocery_list_name_input, null, false) as LinearLayout
            this.setView(mGroceryListNameInput)
        }

        fun setButtonClickListener(onClickListener: View.OnClickListener) {
            mOnClickListener = onClickListener
        }
        fun createPositiveButton(buttonLabel:String){
            this.setPositiveButton(buttonLabel,null)
        }
        fun groceryListName():String{
            var groceryListNameTextInput:TextInputEditText = mGroceryListNameInput.findViewById(R.id.grocery_list_name_textinputedittext)
            return groceryListNameTextInput.text.toString();

        }

        override fun show(): AlertDialog {

            mAlertDialog = super.show()
            val positiveBtn: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

            if(mOnClickListener != null){
                if(positiveBtn !=null){
                    positiveBtn.id = POSITIVE_BUTTON_ID
                    positiveBtn.setOnClickListener(mOnClickListener)
                }


            }

            return mAlertDialog
        }
    }
}