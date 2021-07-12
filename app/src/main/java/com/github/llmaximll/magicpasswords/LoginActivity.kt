package com.github.llmaximll.magicpasswords

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.databinding.ActivityLoginBinding
import com.github.llmaximll.magicpasswords.fragments.LoginFragment

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity(),
    LoginFragment.Callbacks {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var cf: CommonFunctions
    private lateinit var sp: SharedPreferences
    private var firstLaunch: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cf = CommonFunctions.get()
        sp = cf.getSharedPreferences(this)
        when (sp.getInt(cf.spThemeApp, 0)) {
            0 -> setTheme(R.style.Theme_MagicPasswords)
            1 -> setTheme(R.style.Theme_MagicPasswordsDay)
            2 -> setTheme(R.style.Theme_MagicPasswordsNight)
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firstLaunch = sp.getBoolean(cf.spFirstLaunch, false)

        val currentFragment = supportFragmentManager
            .findFragmentById(R.id.container_fragment)
        if (currentFragment == null) {
            val fragment = LoginFragment.newInstance(firstLaunch!!, "")
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container_fragment, fragment)
                .commit()
        }
    }

    override fun onLoginFragment(password: String?) {
        if (password != null) {
            val fragment = LoginFragment.newInstance(firstLaunch!!, password)
            cf.changeFragment(
                supportFragmentManager,
                R.id.container_fragment,
                fragment,
                backStack = true,
                animation = true
            )
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}