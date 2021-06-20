package com.github.llmaximll.magicpasswords

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.transition.TransitionInflater
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.fragments.*

class MainActivity : AppCompatActivity(),
    PasswordsListFragment.Callbacks,
    ChangePasswordFragment.Callbacks {

    private lateinit var cf: CommonFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cf = CommonFunctions.get()

        val currentFragment = supportFragmentManager
            .findFragmentById(R.id.container_fragment)
        if (currentFragment == null) {
            val fragment = PasswordsListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_fragment, fragment)
                .commit()
        }
    }

    override fun onPasswordsListFragment(fragment: String, idPassword: String, sharedView: View?) {
        val mFragment = when (fragment) {
            "add" -> {
                ChangePasswordFragment.newInstance(idPassword, sharedView?.transitionName
                    ?: "null")
            }
            "settings" -> {
                SettingsFragment.newInstance()
            }
            "change" -> ChangePasswordFragment.newInstance(idPassword, sharedView?.transitionName
                ?: "null")
            else -> ChangePasswordFragment.newInstance(idPassword, sharedView?.transitionName
                ?: "null")
        }

        //fragment transition
        mFragment.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)
        mFragment.enterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.fade)

        mFragment.sharedElementReturnTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)

        cf.changeFragment(
            supportFragmentManager,
            R.id.container_fragment,
            mFragment,
            backStack = true,
            animation = true,
            transition = true,
            sharedView = sharedView
        )
    }

    override fun onChangePasswordFragment() {
        val fragment = PasswordsListFragment.newInstance()
        cf.changeFragment(
            supportFragmentManager,
            R.id.container_fragment,
            fragment,
            backStack = false,
            animation = true
        )
        // удаление предыдущего фрагмента из backstack
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}