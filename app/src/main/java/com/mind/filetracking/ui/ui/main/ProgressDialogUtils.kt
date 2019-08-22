package com.mind.filetracking.ui.ui.main

import android.app.ProgressDialog
import android.content.Context

object ProgressDialogUtils {

    private var progressDialog: ProgressDialog? = null

    fun show(ctx: Context, msg:String = "Please Wait...") {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(ctx)
        }
        progressDialog?.apply {
            setMessage(msg)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(false)
            show()
        }
    }

    fun hide() {
        progressDialog?.let {
            it.dismiss()
            progressDialog = null
        }
    }
}
