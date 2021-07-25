package com.github.llmaximll.magicpasswords.activities

import android.app.Activity
import android.content.Intent
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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.github.llmaximll.magicpasswords.Encryption
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.ActivityMainBinding
import com.github.llmaximll.magicpasswords.fragments.*
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.utils.StorageUtils
import com.github.llmaximll.magicpasswords.vm.MainActivityVM
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type
import kotlin.math.hypot

private const val TAG = "MainActivity"
private const val KEY_CHANGE_VALUE = "key_change_value"

class MainActivity : AppCompatActivity(),
    PasswordsListFragment.Callbacks {

    lateinit var binding: ActivityMainBinding
    private lateinit var cf: CommonFunctions
    private lateinit var viewModel: MainActivityVM
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    lateinit var createDocumentIntent: Intent
    lateinit var createDocumentResultLauncher: ActivityResultLauncher<Intent>
    lateinit var openDocumentIntent: Intent
    lateinit var openDocumentResultLauncher: ActivityResultLauncher<Intent>
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

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.navigationView, navController)

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
            putExtra(Intent.EXTRA_TITLE, "MagicPasswordsBackup.txt")
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
        openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "MagicPasswordsBackup.txt")
        }
        openDocumentResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data.also { intent ->
                        if (intent != null) {
                            val file =
                                StorageUtils.getTextFromStorage(
                                    rootDestination = File(
                                        "/storage/emulated/0/${intent.data?.path?.drop(18)}"),
                                    context = this@MainActivity,
                                    fileName = null,
                                    folderName = null
                                )
                            val gson = GsonBuilder().create()
                            val collectionType: Type =
                                object : TypeToken<List<PasswordInfo>>() {}.type
                            try {
                                val passwordsList: List<PasswordInfo> = gson.fromJson(file, collectionType)
                                viewModel.showRecoveryPassword(this@MainActivity, passwordsList)
                            } catch (e: JsonSyntaxException) {
                                val en = Encryption()
                                val passwordsList: List<PasswordInfo> =
                                    gson.fromJson(en.decrypt(file, this@MainActivity), collectionType)
                                viewModel.showRecoveryPassword(this@MainActivity, passwordsList)
                            } catch (e: JsonParseException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
    }

    override fun onPasswordsListFragmentChangePassword(idPassword: String) {
        val args = Bundle().apply {
            putString(ChangePasswordFragment.ARG_ID_PASSWORD, idPassword)
        }
        navController.navigate(R.id.action_passwordsListFragment_to_changePasswordFragment, args)
    }

    override fun onPasswordsListFragmentAddPassword() {
        replaceMainFragments(REPLACE_ON_ADD_FRAGMENT)
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
                    binding.navHostFragment.width.toDouble(),
                    binding.navHostFragment.height.toDouble()
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
                    (navHostFragment.childFragmentManager.fragments[0] as? PasswordsListFragment)
                        ?.viewModel?.getAllPasswords(0)
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

    private fun replaceMainFragments(mode: Int) {
        when (mode) {
            REPLACE_ON_ADD_FRAGMENT -> {
                val endRadius = hypot(
                    binding.navHostFragment.width.toDouble(),
                    binding.navHostFragment.height.toDouble()
                ).toFloat()
                val animatorCircular = ViewAnimationUtils.createCircularReveal(
                    binding.secondContainer,
                    binding.fabView.x.toInt() - 158,
                    binding.fabView.y.toInt() + 70,
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
                    binding.navHostFragment.width.toDouble(),
                    binding.navHostFragment.height.toDouble()
                ).toFloat()
                val animatorCircular = ViewAnimationUtils.createCircularReveal(
                    binding.secondContainer,
                    binding.fabView.x.toInt() - 158,
                    binding.fabView.y.toInt() + 70,
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

    override fun onBackPressed() {
        if (changeValue) {
            replaceMainFragments(REPLACE_ON_PASSWORDS_LIST_FRAGMENT)
            changeValue = false
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val REPLACE_ON_ADD_FRAGMENT = 0
        const val REPLACE_ON_PASSWORDS_LIST_FRAGMENT = 1
    }
}