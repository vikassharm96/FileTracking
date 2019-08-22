package com.mind.filetracking.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mind.filetracking.R
import com.mind.filetracking.ui.ui.main.FragmentType
import com.mind.filetracking.ui.ui.main.PlaceholderFragment
import com.mind.filetracking.ui.ui.main.ProgressDialogUtils
import com.mind.filetracking.ui.ui.main.model.DepartmentDTO
import com.mind.filetracking.ui.ui.main.model.HistoryDTO
import kotlinx.android.synthetic.main.activity_main.*

val departments = mutableListOf<DepartmentDTO>()

class MainActivity : AppCompatActivity() {

    var iTag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        btnFile.setOnClickListener { open(PlaceholderFragment.newInstance(FragmentType.FileMovement.value), 1) }
        btnSearch.setOnClickListener { open(PlaceholderFragment.newInstance(FragmentType.SearchTracking.value), 2) }

        getDepartments()
    }

    private fun getDepartments() {
        ProgressDialogUtils.show(this, "Fetching Departments...")
        val databaseReference = FirebaseDatabase.getInstance().reference.child("Departments")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    departments.add(DepartmentDTO(it.key as String, it.value as String))
                }
                ProgressDialogUtils.hide()
            }

            override fun onCancelled(error: DatabaseError) {
                ProgressDialogUtils.hide()
            }
        })
    }

    private fun open(f: Fragment, tag: Int) {
        if (departments.isEmpty()) {
            Toast.makeText(this, "Departments not available", Toast.LENGTH_SHORT).show()
            getDepartments()
            return
        }
        iTag = tag
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.flContainer, f, tag.toString())
                .commit()
        llBtns.visibility = View.GONE
        flContainer.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (llBtns.visibility == View.GONE) {
            llBtns.visibility = View.VISIBLE
            flContainer.visibility = View.GONE
            supportFragmentManager.findFragmentByTag(iTag.toString())?.let {
                supportFragmentManager.beginTransaction().remove(it).commit()
            }
        } else {
            super.onBackPressed()
        }
    }
}