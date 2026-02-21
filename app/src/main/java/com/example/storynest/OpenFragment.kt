package com.example.storynest

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun AppCompatActivity.openFragment(fragment: Fragment, tag: String) {

    val manager = supportFragmentManager
    val transaction = manager.beginTransaction()

    transaction.setReorderingAllowed(true)

    transaction.setCustomAnimations(
        R.anim.enter_from_right,
        R.anim.exit_to_left,
        R.anim.enter_from_left,
        R.anim.exit_to_right
    )

    val current = manager.primaryNavigationFragment
    current?.let { transaction.hide(it) }

    var existing = manager.findFragmentByTag(tag)

    if (existing == null) {
        existing = fragment
        transaction.add(R.id.nav_host, existing, tag)
    } else {
        transaction.show(existing)
    }

    transaction.setPrimaryNavigationFragment(existing)
    transaction.addToBackStack(tag)
    transaction.commit()
}