package com.app.tyst.utility.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.app.tyst.R


/**
 * This class contain alert message helper
 */
object DialogUtil {

    interface IL {
        fun onSuccess()

        fun onCancel(isNeutral: Boolean)
    }

    interface ILOption {
        fun onWhich(position: Int)
    }

    /**
     * This method is used to show alert dialog box for force close application
     *
     * @param context - Object of Context, context from where the activity is going
     * to start.
     * @param msg     - Message String that represents alert box message
     */
    fun alert(context: Context, msg: String) {
        try {
            val alertDialogBuilder = getBuilder(context)
            alertDialogBuilder.setMessage(fromHtml(msg))
            alertDialogBuilder.setPositiveButton(
                    android.R.string.ok
            ) { dialog, _ -> dialog.dismiss() }
            alertDialogBuilder
                    .setOnKeyListener { dialog, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK)
                            dialog.dismiss()
                        false
                    }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    /**
     * This method is used to show alert dialog box for force close application
     *
     * @param context - Object of Context, context from where the activity is going
     * to start.
     * @param msg     - Message String that represents alert box message
     */
    fun alert(
            context: Context,
            title: String = "",
            msg: String = "",
            positiveBtnText: String = context.getString(R.string.yes),
            negativeBtnText: String = context.getString(R.string.no),
            il: IL? = null,
            isCancelable: Boolean = true
    ) {
        try {
            val alertDialogBuilder = getBuilder(context)
            alertDialogBuilder.setTitle(title)
            alertDialogBuilder.setMessage(msg)
            alertDialogBuilder.setCancelable(isCancelable)
            alertDialogBuilder.setPositiveButton(
                    positiveBtnText
            ) { dialog, _ ->
                il?.onSuccess()
                dialog.dismiss()
            }
            alertDialogBuilder.setNegativeButton(
                    negativeBtnText
            ) { dialog, _ ->
                il?.onCancel(false)
                dialog.dismiss()
            }

            alertDialogBuilder
                    .setOnKeyListener { dialog, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            il?.onCancel(true)
                            dialog.dismiss()
                        }
                        false
                    }
            val alertDialog = alertDialogBuilder.create()

            alertDialog.show()
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    /**
     * This method is used to show alert dialog box for force close application
     *
     * @param context - Object of Context, context from where the activity is going
     * to start.
     * @param msg     - Message String that represents alert box message
     */
    fun confirmDialog(context: Context, title: String = "", msg: String = "", positiveBtnText: String = "", negativeBtnText: String = "", neutralBtnText: String = "", il: IL? = null, isCancelable: Boolean = true) {
        try {
            val alertDialogBuilder = getBuilder(context, isCancelable)
            alertDialogBuilder.setMessage(msg)
            alertDialogBuilder.setTitle(title)

            if (positiveBtnText.isNotEmpty()) {
                alertDialogBuilder.setPositiveButton(
                        positiveBtnText
                ) { dialog, _ ->
                    il?.onSuccess()
                    dialog.dismiss()
                }
            }

            if (negativeBtnText.isNotEmpty()) {
                alertDialogBuilder.setNegativeButton(
                        negativeBtnText
                ) { dialog, _ ->
                    il?.onCancel(false)
                    dialog.dismiss()
                }
            }

            if (neutralBtnText.isNotEmpty()) {
                alertDialogBuilder.setNeutralButton(
                        neutralBtnText
                ) { dialog, _ ->
                    il?.onCancel(true)
                    dialog.dismiss()
                }
            }

            alertDialogBuilder
                    .setOnKeyListener { dialog, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            il?.onCancel(false)
                            dialog.dismiss()
                        }
                        false
                    }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    /**
     * This method is used to show alert dialog box for force close application
     *
     * @param context - Object of Context, context from where the activity is going
     * to start.
     */
    fun showAlertDialogOKCancelWithView(
            context: Activity,
            view: View, il: IL, positiveBtnText: String,
            negativeBtnText: String
    ) {
        try {
            val alertDialogBuilder = getBuilder(context)
            alertDialogBuilder.setView(view)
            alertDialogBuilder.setPositiveButton(
                    positiveBtnText
            ) { dialog, _ ->
                il.onSuccess()
                dialog.dismiss()
            }

            alertDialogBuilder.setNegativeButton(
                    negativeBtnText
            ) { dialog, _ ->
                il.onCancel(false)
                dialog.dismiss()
            }

            alertDialogBuilder
                    .setOnKeyListener { dialog, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            il.onCancel(false)
                            dialog.dismiss()
                        }
                        false
                    }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    /**
     * This method is used to show alert dialog box for force close application
     *
     * @param context - Object of Context, context from where the activity is going
     * to start.
     * @param msg     - Message String that represents alert box message
     */
    fun showAlertDialogAction(
            context: Activity, msg: String,
            il: IL, positiveBtnText: String, negativeBtnText: String
    ) {
        try {
            val alertDialogBuilder = getBuilder(context)
            alertDialogBuilder.setMessage(msg)
            alertDialogBuilder.setPositiveButton(
                    positiveBtnText
            ) { dialog, _ ->
                il.onSuccess()
                dialog.dismiss()
            }

            alertDialogBuilder.setNegativeButton(
                    negativeBtnText
            ) { dialog, _ ->
                il.onCancel(false)
                dialog.dismiss()
            }

            alertDialogBuilder
                    .setOnKeyListener { dialog, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            il.onCancel(false)
                            dialog.dismiss()
                        }
                        false
                    }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun getBuilder(context: Context, isCancelable: Boolean = true): AlertDialog.Builder {
        val alertDialogBuilder = AlertDialog.Builder(context,R.style.DatePickerTheme)
        alertDialogBuilder.setCancelable(isCancelable)
        return alertDialogBuilder
    }

    fun ok(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }


    fun showOptionDialog(
            context: Context, title: String,
            opsChars: Array<CharSequence>, il: ILOption
    ) {
        try {
            val alertDialogBuilder = AlertDialog.Builder(
                    context
            )
            alertDialogBuilder.setTitle(title)
            alertDialogBuilder.setItems(
                    opsChars
            ) { dialog, which ->
                il.onWhich(which)
                dialog.dismiss()
            }

            alertDialogBuilder
                    .setOnKeyListener { dialog, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_BACK)
                            dialog.dismiss()
                        false
                    }
            alertDialogBuilder.create().show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}
