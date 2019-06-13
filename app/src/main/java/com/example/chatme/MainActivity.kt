package com.example.chatme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.chatme.fragments.MyAccountFragment
import com.example.chatme.fragments.PeopleFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        replaceFragment(PeopleFragment())

        nav_view.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navigation_people -> {
                    replaceFragment(PeopleFragment())
                    true
                }

                R.id.navigation_my_account -> {
                    replaceFragment(MyAccountFragment())
                    true
                }
                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout,fragment)
            .commit()

    }
}
