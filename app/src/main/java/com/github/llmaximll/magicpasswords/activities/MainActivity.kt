package com.github.llmaximll.magicpasswords.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionInflater
import com.github.llmaximll.magicpasswords.Encryption
import com.github.llmaximll.magicpasswords.OnBackPressedListener
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.ActivityMainBinding
import com.github.llmaximll.magicpasswords.fragments.ChangePasswordFragment
import com.github.llmaximll.magicpasswords.fragments.PasswordsListFragment
import com.github.llmaximll.magicpasswords.fragments.RecycleBinFragment
import com.github.llmaximll.magicpasswords.fragments.SettingsFragment
import com.github.llmaximll.magicpasswords.utils.StorageUtils
import com.github.llmaximll.magicpasswords.vm.MainActivityVM
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.hypot

private const val KEY_CHANGE_VALUE = "key_change_value"

class MainActivity : AppCompatActivity(),
    PasswordsListFragment.Callbacks,
    ChangePasswordFragment.Callbacks {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cf: CommonFunctions
    private lateinit var viewModel: MainActivityVM
    private lateinit var passwordsFragment: Fragment
    lateinit var createDocumentIntent: Intent
    lateinit var createDocumentResultLauncher: ActivityResultLauncher<Intent>
    /**
     * В зависимости от [changeValue] будет показан либо список паролей, либо создание пароля
     */
    private var changeValue = false
    private var passwordFormat = ChangePasswordFragment.PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS
    private var difficultPassword = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        cf = CommonFunctions.get()
        viewModel = cf.initViewModel(this, MainActivityVM::class.java) as MainActivityVM
        val sp = cf.getSharedPreferences(this)
        when (sp.getInt(cf.spThemeApp, 0)) {
            0 -> setTheme(R.style.Theme_MagicPasswords)
            1 -> setTheme(R.style.Theme_MagicPasswordsDay)
            2 -> setTheme(R.style.Theme_MagicPasswordsNight)
        }
        setContentView(binding.root)

        val currentFragment = supportFragmentManager
            .findFragmentById(R.id.container_fragment)
        if (currentFragment == null) {
            passwordsFragment = PasswordsListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container_fragment, passwordsFragment)
                .commit()
        }
        changeValue = savedInstanceState?.getBoolean(KEY_CHANGE_VALUE) ?: false
        binding.secondContainer.visibility = if (changeValue)
            View.VISIBLE else
            View.GONE
        setButtons()
        binding.withoutRadioButton.isChecked = true
        // Регистрация activity
        createDocumentIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "backup.txt")
        }
        createDocumentResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val text = viewModel.getAllPasswords()
                        val gson = GsonBuilder().create()
                        val en = Encryption()
                        val file = if (sp.getBoolean(cf.spBackupEncryption, true)) {
                            en.encrypt(gson.toJson(text), this@MainActivity)
                        } else {
                            gson.toJson(text)
                        }
                        if (file != null) {
                            result.data.also { intent ->
                                if (intent != null) {
                                    val path = "/storage/emulated/0/${intent.data?.path?.drop(18)}"
                                    StorageUtils.setTextInStorage(
                                        rootDestination = File(path),
                                        context = this@MainActivity,
                                        fileName = null,
                                        folderName = null,
                                        text = file
                                    )
                                }
                            }
                        } else {
                            cf.toast(
                                this@MainActivity,
                                "Ошибка при создании резервной копии"
                            )
                        }
                    }
                }
            }
    }

    override fun onPasswordsListFragment(fragment: String, idPassword: String, sharedView: View?) {
        val mFragment = when (fragment) {
            "add" -> ChangePasswordFragment.newInstance(idPassword, sharedView?.transitionName ?: "null")
            "settings" -> SettingsFragment.newInstance()
            "recycle bin" -> RecycleBinFragment.newInstance()
            "change" -> ChangePasswordFragment.newInstance(idPassword, sharedView?.transitionName ?: "null")
            else -> ChangePasswordFragment.newInstance(idPassword, sharedView?.transitionName ?: "null")
        }

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
            animation = true,
            transition = true,
            sharedView = sharedView
        )
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        var backPressedListener: OnBackPressedListener? = null
        for (fragment in fm.fragments) {
            if (fragment is OnBackPressedListener) {
                backPressedListener = fragment
                break
            }
        }
        if (backPressedListener?.onBackPressed() == true) {
            passwordsFragment = PasswordsListFragment.newInstance()
            cf.changeFragment(
                fm,
                R.id.container_fragment,
                passwordsFragment,
                backStack = false,
                animation = true
            )
        }
        if (backPressedListener?.onBackPressed() == null) {
            if (!changeValue) {
                super.onBackPressed()
            } else {
                val name = binding.nameEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                val password2 = binding.passwordEditText2.text.toString()
                val description = binding.descriptionEditText.text.toString()
                val address = binding.addressEditText.text.toString()
                if (name != "" || password != "" || password2 != "" || description != "" || address != "") {
                    cf.toast(this, "Черновик сохранён")
                }
                replaceMainFragments(REPLACE_ON_PASSWORDS_LIST_FRAGMENT)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_CHANGE_VALUE, changeValue)
    }

    private fun setButtons() {
        binding.okButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val password2 = binding.passwordEditText2.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val address = binding.addressEditText.text.toString()
            if (viewModel.checkFields(this, name, password, password2)) {
                viewModel.addPassword(
                    PasswordInfo(
                        name = name,
                        password = password,
                        description = description,
                        address = address,
                        messagePassword = binding.messageRadioButton.isChecked
                    )
                )
                binding.nameEditText.setText("")
                binding.passwordEditText.setText("")
                binding.passwordEditText2.setText("")
                binding.descriptionEditText.setText("")
                binding.addressEditText.setText("")
                binding.passwordToggleCheckBox.isChecked = false
                binding.withoutRadioButton.isChecked = true
                binding.messageEditText2.setText("")
                difficultPassword = 15
                binding.countSymbolsTextView.hint = "Количество знаков: $difficultPassword"
                binding.difficultSeekBar.progress = difficultPassword
                val startRadius = hypot(
                    binding.containerFragment.width.toDouble(),
                    binding.containerFragment.height.toDouble()
                ).toFloat()
                val animatorCircular = ViewAnimationUtils.createCircularReveal(
                    binding.secondContainer,
                    binding.okButton.x.toInt() + 150,
                    binding.okButton.y.toInt() + 80,
                    startRadius,
                    0f
                )
                animatorCircular.apply {
                    doOnEnd {
                        binding.secondContainer.visibility = View.GONE
                        changeValue = false
                    }
                    animatorCircular.duration = 800L
                    animatorCircular.start()
                    (passwordsFragment as? PasswordsListFragment)?.getAllPasswords()
                }
            }
        }
        binding.passwordToggleCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.passwordEditText.inputType = 129
                binding.passwordEditText2.inputType = 129
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordEditText2.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        binding.generateButton.setOnClickListener {
            if (!binding.messageRadioButton.isChecked) {
                val password = viewModel.generatePassword(difficultPassword, passwordFormat)
                binding.passwordEditText.setText(password)
                binding.passwordEditText2.setText(password)
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    val enc = Encryption()
                    val newPassword = enc.encrypt(binding.messageEditText2.text.toString(), this@MainActivity)
                    withContext(Dispatchers.Main) {
                        binding.passwordEditText.setText("$newPassword")
                        binding.passwordEditText2.setText("$newPassword")
                        difficultPassword = newPassword?.length ?: 0
                        binding.countSymbolsTextView.hint = "Количество знаков: ${newPassword?.length}"
                        binding.difficultSeekBar.progress = difficultPassword
                    }
                }
            }
        }
        binding.difficultSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress != 0) {
                    difficultPassword = progress
                    val newPassword = viewModel.generatePassword(difficultPassword, passwordFormat)
                    binding.countSymbolsTextView.hint = "Количество знаков: $progress"
                    binding.passwordEditText.setText(newPassword)
                    binding.passwordEditText2.setText(newPassword)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.without_radioButton -> {
                    passwordFormat = ChangePasswordFragment.PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS
                    binding.messageInputLayout.isEnabled = false
                    binding.difficultSeekBar.isEnabled = true
                    binding.countSymbolsTextView.hint = "Количество знаков: $difficultPassword"
                    binding.passwordEditText.isEnabled = true
                    binding.passwordEditText2.isEnabled = true
                }
                R.id.with_radioButton2 -> {
                    passwordFormat = ChangePasswordFragment.PASSWORD_FORMAT_WITH_SPEC_SYMBOLS
                    binding.messageInputLayout.isEnabled = false
                    binding.difficultSeekBar.isEnabled = true
                    binding.countSymbolsTextView.hint = "Количество знаков: $difficultPassword"
                    binding.passwordEditText.isEnabled = true
                    binding.passwordEditText2.isEnabled = true
                }
                R.id.message_radioButton -> {
                    binding.messageInputLayout.isEnabled = true
                    binding.difficultSeekBar.isEnabled = false
                    binding.passwordEditText.isEnabled = false
                    binding.passwordEditText2.isEnabled = false
                }
            }
        }
    }

    fun replaceMainFragments(mode: Int) {
        when (mode) {
            REPLACE_ON_ADD_FRAGMENT -> {
                val endRadius = hypot(
                    binding.containerFragment.width.toDouble(),
                    binding.containerFragment.height.toDouble()
                ).toFloat()
                val animatorCircular = ViewAnimationUtils.createCircularReveal(
                    binding.secondContainer,
                    binding.fabView.x.toInt() + 75,
                    binding.fabView.y.toInt() + 75,
                    0f,
                    endRadius
                )
                animatorCircular.duration = 800L
                binding.secondContainer.visibility = View.VISIBLE
                changeValue = true
                animatorCircular.start()
            }
            REPLACE_ON_PASSWORDS_LIST_FRAGMENT -> {
                val startRadius = hypot(
                    binding.containerFragment.width.toDouble(),
                    binding.containerFragment.height.toDouble()
                ).toFloat()
                val animatorCircular = ViewAnimationUtils.createCircularReveal(
                    binding.secondContainer,
                    binding.fabView.x.toInt() + 75,
                    binding.fabView.y.toInt() + 75,
                    startRadius,
                    0f
                )
                animatorCircular.apply {
                    doOnEnd {
                        binding.secondContainer.visibility = View.GONE
                        changeValue = false
                    }
                    animatorCircular.duration = 800L
                    animatorCircular.start()
                }
            }
        }
    }

    override fun onChangePasswordFragment() {
        val fragment = PasswordsListFragment.newInstance()
        cf.changeFragment(
            supportFragmentManager,
            R.id.container_fragment,
            fragment,
            animation = true
        )
    }

    companion object {
        const val REPLACE_ON_ADD_FRAGMENT = 0
        const val REPLACE_ON_PASSWORDS_LIST_FRAGMENT = 1
    }
}