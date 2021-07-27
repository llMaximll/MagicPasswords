package com.github.llmaximll.magicpasswords.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.MainActivity
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(),
    LoginFragment.Callbacks {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sp: SharedPreferences
    private var firstLaunch: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sp = CommonFunctions.getSharedPreferences(this)
        when (sp.getInt(CommonFunctions.spThemeApp, 0)) {
            0 -> setTheme(R.style.Theme_MagicPasswords)
            1 -> setTheme(R.style.Theme_MagicPasswordsDay)
            2 -> setTheme(R.style.Theme_MagicPasswordsNight)
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firstLaunch = sp.getBoolean(CommonFunctions.spFirstLaunch, false)

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
            CommonFunctions.changeFragment(
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