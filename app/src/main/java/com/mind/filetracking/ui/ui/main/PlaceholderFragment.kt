package com.mind.filetracking.ui.ui.main

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.google.zxing.Result
import com.mind.filetracking.R
import com.mind.filetracking.ui.departments
import com.mind.filetracking.ui.ui.main.model.HistoryDTO
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.lang.StringBuilder
import java.util.*


class PlaceholderFragment : Fragment(), ZXingScannerView.ResultHandler {

    companion object {
        private const val ARG_FRAGMENT_TYPE = "fragment_type"
        @JvmStatic
        fun newInstance(fragmentType: String): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FRAGMENT_TYPE, fragmentType)
                }
            }
        }
    }

    private lateinit var pageViewModel: PageViewModel
    private lateinit var mScannerView: ZXingScannerView
    private lateinit var frameLayout: FrameLayout
    private lateinit var button: Button
    private lateinit var etCrn: TextInputEditText
    private lateinit var etDepartment: TextInputEditText
    private lateinit var history: EditText
    private lateinit var llHistory: LinearLayout

    private var isSearchScreen = false
    val historyDTOS = mutableListOf<HistoryDTO>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
            setFragmentType(arguments?.getString(ARG_FRAGMENT_TYPE) ?: "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        button = root.findViewById<Button>(R.id.button)
        etCrn = root.findViewById(R.id.etCrn);
        etDepartment = root.findViewById<TextInputEditText>(R.id.etDepartment)
        frameLayout = root.findViewById(R.id.frameLayout)
        history = root.findViewById(R.id.history)
        llHistory = root.findViewById(R.id.llHistory)
        pageViewModel.text.observe(this, Observer<String> {
            if (it == FragmentType.FileMovement.value) {
                isSearchScreen = false
                button.visibility = View.VISIBLE
                etDepartment.visibility = View.VISIBLE
                llHistory.visibility = View.GONE
            } else {
                isSearchScreen = true
                button.visibility = View.GONE
                etDepartment.visibility = View.GONE
                llHistory.visibility = View.VISIBLE
            }
        })
        etDepartment.setOnClickListener {
            val crn = etCrn.text.toString()
            if (crn.isNotEmpty()) {
                val itemSelected = 0
                AlertDialog.Builder(activity as Context)
                    .setTitle("Select your department")
                    .setSingleChoiceItems(
                        departments.map { it.name }.toTypedArray(),
                        itemSelected
                    ) { di, i ->
                        etDepartment.setText(departments[i].name)
                        di.dismiss()
                    }
                    .setNegativeButton("Close", null)
                    .show()
            } else Toast.makeText(requireActivity(), "Scan CRN first", Toast.LENGTH_SHORT).show()
        }

        mScannerView = ZXingScannerView(activity)
        frameLayout.addView(mScannerView)

        button.setOnClickListener {
//            fdb()
            submitFileStatus()
        }
        return root
    }

    private fun submitFileStatus() {
        val crn = etCrn.text.toString()
        val dpt = etDepartment.text.toString()

        ProgressDialogUtils.show(requireActivity())

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Files")
        databaseReference.child(crn).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(
                        requireActivity(),
                        "CRN does not exists in database",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val hashMap = dataSnapshot.value as HashMap<*, *>
                    val l = departments.filter { it.name == dpt }
                    if (l.isNotEmpty()) {
                        val oldDeptId = hashMap["dpt_id"] ?: 0
                        val newDeptId = l[0].id
                        when {
                            hashMap["in_status"]?.equals("") == true -> {
                                databaseReference.child(crn).child("in_status").setValue(newDeptId)
                            }
                            hashMap["out_status"]?.equals("") == true -> {
                                databaseReference.child(crn).child("out_status").setValue(deptId)
                            }
                            else -> {
                                databaseReference.child(crn).child("dpt_id").setValue(deptId)
                                databaseReference.child(crn).child("in_status").setValue(dpt)
                                databaseReference.child(crn).child("out_status").setValue("")
                            }
                        }
                    }
                }
            }

        })

    }

    private fun fdb() {
        val crn = etCrn.text.toString()
        val dpt = etDepartment.text.toString()

        ProgressDialogUtils.show(requireActivity())

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Files")
        databaseReference.child(crn).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(
                        requireActivity(),
                        "CRN does not exists in database",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val l = departments.filter { it.name == dpt }
                    if (l.isNotEmpty()) {
                        val newDptId = l[0].id
                        val hashMap = dataSnapshot.value as HashMap<*, *>
                        val oldDptId = hashMap["dpt_id"] ?: 0
                        if (newDptId != oldDptId) {
                            databaseReference.child(crn).child("dpt_id").setValue(newDptId)
                            val hid = System.currentTimeMillis()
                            val crnHistoryChild = FirebaseDatabase
                                .getInstance()
                                .reference
                                .child("History")
                                .child(crn)

                            val m = mutableMapOf<String, Any>()
                            m[hid.toString()] = HistoryDTO(newDptId.toLong(), Date().toString())
                            crnHistoryChild.updateChildren(m)

                        }
                    }
                }
                ProgressDialogUtils.hide()
                etCrn.setText("")
                etDepartment.setText("")
            }

            override fun onCancelled(error: DatabaseError) {
                ProgressDialogUtils.hide()
                Toast.makeText(requireActivity(), "Error in moving file", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }

    override fun handleResult(rawResult: Result?) {
        val dialog: Dialog?
        val builder = AlertDialog.Builder(activity as Context)
        builder.setTitle("Scanned Barcode")
        builder.setMessage(rawResult.toString())
        builder.setCancelable(false)
        builder.setPositiveButton("Re Scan") { _, which ->
            val handler = Handler()
            handler.postDelayed({
                mScannerView.setResultHandler(this@PlaceholderFragment)
                mScannerView.resumeCameraPreview(this@PlaceholderFragment)
            }, 2000)
        }
        builder.setNegativeButton("Close") { di, _ ->
            mScannerView.setResultHandler(this@PlaceholderFragment)
            mScannerView.resumeCameraPreview(this@PlaceholderFragment)
            etCrn.setText(rawResult.toString())
            etDepartment.setText("")
            if (isSearchScreen) {
                ProgressDialogUtils.show(requireActivity(), "Searching Please wait...")
                var message = ""
                val databaseReference = FirebaseDatabase.getInstance().reference.child("Files")
                databaseReference.child(etCrn.text.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                Toast.makeText(
                                    requireActivity(),
                                    "CRN does not exists in database",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val hashMap = p0.value as HashMap<*, *>
                                val oldDptId = hashMap["dpt_id"] ?: 0
                                val department = departments.filter { it.id == oldDptId }
                                message = department[0].name
                            }
                        }

                    })
                val dbReference = FirebaseDatabase.getInstance().reference.child("History")
                dbReference.child(etCrn.text.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            ProgressDialogUtils.hide()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                Toast.makeText(
                                    requireActivity(),
                                    "CRN does not exists in database",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                p0.children.forEach {
                                    it.getValue<HistoryDTO>(HistoryDTO::class.java)?.let { it1 ->
                                        historyDTOS.add(
                                            it1
                                        )
                                    }
                                }
                                setHistory(message, historyDTOS)
                            }
                            ProgressDialogUtils.hide()
                        }
                    })
            }
            di.dismiss()
        }
        dialog = builder.create()
        dialog.show()
    }

    fun setHistory(name: String, historyDTO: List<HistoryDTO>) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("File currently in $name department\n")
        stringBuilder.append("Tracking ")
        historyDTO.forEach {
            stringBuilder.append("-> ")
            stringBuilder.append(getDepartmentName(it.departmentId.toString()))
        }
        history.setText(stringBuilder)
    }

    private fun getDepartmentName(id: String): String {
         return departments.filter {
            it.id == id
        }[0].name
    }
}