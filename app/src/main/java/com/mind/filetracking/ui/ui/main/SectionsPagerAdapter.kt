package com.mind.filetracking.ui.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mind.filetracking.R

private val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        if (position == 0)
            return PlaceholderFragment.newInstance(FragmentType.FileMovement.value)
        else
            return PlaceholderFragment.newInstance(FragmentType.SearchTracking.value)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 2
    }
}

enum class FragmentType (val value: String){
    FileMovement("FileMovement"), SearchTracking("SearchTracking")
}