package com.github.llmaximll.magicpasswords.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "LoginFragment"
private const val ARG_FIRST_LAUNCH = "arg_first_launch"
private const val ARG_SECOND_PASSWORD = "arg_second_password"

class LoginFragment : Fragment() {

    interface Callbacks {
        fun onLoginFragment(password: String?)
    }

    private lateinit var binding: FragmentLoginBinding
    private lateinit var cf: CommonFunctions
    private lateinit var sp: SharedPreferences
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var callbacks: Callbacks? = null
    private var firstLaunch = false
    private var secondPassword = ""
    private var password = ""
    private var fingerprint: Boolean? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cf = CommonFunctions.get()
        sp = cf.getSharedPreferences(requireContext())
        fingerprint = sp.getBoolean(cf.spFingerPrint, false)
        //arguments
        firstLaunch = arguments?.getBoolean(ARG_FIRST_LAUNCH, false) ?: false
        secondPassword = arguments?.getString(ARG_SECOND_PASSWORD, "") ?: ""
        cf.log(TAG, "arguments | firstLaunch=$firstLaunch")
        //auth
        if (sp.getBoolean(cf.spFingerPrint, false)) {
            showFingerprint(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!firstLaunch) {
            firstLaunch()
        } else {
            binding.infoTextView.text = "Введите пароль"
        }
        if (fingerprint == false) {
            binding.fingerprintButton.visibility = View.GONE
            binding.backButton.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        password = ""
        setButtonsListeners()
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun showFingerprint(mode: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            biometricPrompt = BiometricPrompt(this@LoginFragment,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)

                        if (!mode) {
                            setColorCircles(1)
                            setColorCircles(2)
                            setColorCircles(3)
                            setColorCircles(4)
                            binding.circle1ImageView.setBackgroundResource(R.drawable.circle_password_green)
                            binding.circle2ImageView.setBackgroundResource(R.drawable.circle_password_green)
                            binding.circle3ImageView.setBackgroundResource(R.drawable.circle_password_green)
                            binding.circle4ImageView.setBackgroundResource(R.drawable.circle_password_green)
                            callbacks?.onLoginFragment(null)
                        } else {
                            val editor = sp.edit()
                            editor.putBoolean(cf.spFingerPrint, true)
                            editor.putBoolean(cf.spFirstLaunch, true)
                            editor.putString(cf.spPassword, password)
                            editor.apply()
                            binding.circle1ImageView.setBackgroundResource(R.drawable.circle_password_green)
                            binding.circle2ImageView.setBackgroundResource(R.drawable.circle_password_green)
                            binding.circle3ImageView.setBackgroundResource(R.drawable.circle_password_green)
                            binding.circle4ImageView.setBackgroundResource(R.drawable.circle_password_green)
                            callbacks?.onLoginFragment(null)
                            cf.toast(requireContext(), "Настройки сохранены")
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        if (!mode) {
                            cf.toast(requireContext(), "Вход не удался")
                        } else {
                            val editor = sp.edit()
                            editor.putBoolean(cf.spFirstLaunch, true)
                            editor.putString(cf.spPassword, password)
                            editor.apply()
                            callbacks?.onLoginFragment(null)
                            cf.toast(requireContext(), "Настройки сохранены")
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)

                        if (mode) {
                            val editor = sp.edit()
                            editor.putBoolean(cf.spFirstLaunch, true)
                            editor.putString(cf.spPassword, password)
                            editor.apply()
                            callbacks?.onLoginFragment(null)
                            cf.toast(requireContext(), "Настройки сохранены")
                        }
                    }
                })

                promptInfo = if (!mode) {
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Вход в MagicPasswords")
                        .setNegativeButtonText("Отмена")
                        .build()
                } else {
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Отпечаток пальца")
                        .setSubtitle("Добавить биометрическую аутентификацию?")
                        .setNegativeButtonText("Отмена")
                        .build()
                }

                biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun setColorCircles(count: Int) {
        when (count) {
            1 -> {
                binding.circle1ImageView.setBackgroundResource(R.drawable.circle_password_on)
                scaleCircle(1)
            }
            2 -> {
                binding.circle2ImageView.setBackgroundResource(R.drawable.circle_password_on)
                scaleCircle(2)
            }
            3 -> {
                binding.circle3ImageView.setBackgroundResource(R.drawable.circle_password_on)
                scaleCircle(3)
            }
            4 -> {
                binding.circle4ImageView.setBackgroundResource(R.drawable.circle_password_on)
                scaleCircle(4)
            }
        }
    }

    private fun scaleCircle(count: Int) {
        val view = when (count) {
            1 -> binding.circle1ImageView
            2 -> binding.circle2ImageView
            3 -> binding.circle3ImageView
            4 -> binding.circle4ImageView
            else -> binding.circle1ImageView
        }
        val animatorX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f)
        val animatorY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f)
        AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = 150
            start()
        }
        val animatorXR = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
        val animatorYR = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
        AnimatorSet().apply {
            playTogether(animatorXR, animatorYR)
            duration = 150
            startDelay = 150L
            start()
        }
    }

    private fun checkPassword() {
        if (password.length >= 4) {
            if (!firstLaunch) {
                if (secondPassword.isNotEmpty()) {
                    if (password == secondPassword) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) &&
                                !sp.getBoolean(cf.spFingerPrint, false)) {
                                showFingerprint(true)
                            }
                        } else {
                            val editor = sp.edit()
                            editor.putBoolean(cf.spFirstLaunch, true)
                            editor.putString(cf.spPassword, password)
                            editor.apply()
                            callbacks?.onLoginFragment(null)
                            cf.toast(requireContext(), "Настройки сохранены")
                        }
                    } else {
                        cf.toast(requireContext(), "Пароли не совпадают")
                    }
                } else {
                    callbacks?.onLoginFragment(password)
                }
            } else {
                sp = cf.getSharedPreferences(requireContext())
                val correctPassword = sp.getString(cf.spPassword, "")
                if (password == correctPassword) {
                    binding.circle1ImageView.setBackgroundResource(R.drawable.circle_password_green)
                    binding.circle2ImageView.setBackgroundResource(R.drawable.circle_password_green)
                    binding.circle3ImageView.setBackgroundResource(R.drawable.circle_password_green)
                    binding.circle4ImageView.setBackgroundResource(R.drawable.circle_password_green)
                    callbacks?.onLoginFragment(null)
                } else {
                    cf.toast(requireContext(), "Неверный пароль")
                }
            }
        }
        if (password.isNotEmpty()) {
            binding.fingerprintButton.visibility = View.GONE
            binding.backButton.visibility = View.VISIBLE
        } else {
            binding.fingerprintButton.visibility = View.VISIBLE
            binding.backButton.visibility = View.GONE
        }
    }

    private fun setButtonsListeners() {
        binding.number0Button.setOnClickListener {
            if (password.length < 4) {
                password += 0
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number1Button.setOnClickListener {
            if (password.length < 4) {
                password += 1
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number2Button.setOnClickListener {
            if (password.length < 4) {
                password += 2
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number3Button.setOnClickListener {
            if (password.length < 4) {
                password += 3
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number4Button.setOnClickListener {
            if (password.length < 4) {
                password += 4
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number5Button.setOnClickListener {
            if (password.length < 4) {
                password += 5
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number6Button.setOnClickListener {
            if (password.length < 4) {
                password += 6
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number7Button.setOnClickListener {
            if (password.length < 4) {
                password += 7
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number8Button.setOnClickListener {
            if (password.length < 4) {
                password += 8
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.number9Button.setOnClickListener {
            if (password.length < 4) {
                password += 9
                setColorCircles(password.length)
                checkPassword()
            }
        }
        binding.backButton.setOnClickListener {
            if (password.isNotEmpty()) {
                password = password.substring(0, password.length - 1)
                backPressedButton()
            }
        }
        binding.fingerprintButton.setOnClickListener {
            showFingerprint(false)
        }
    }

    private fun firstLaunch() {
        if (secondPassword.isEmpty()) {
            binding.infoTextView.text = "Создайте пароль"
        } else {
            binding.infoTextView.text = "Повторите пароль"
        }
    }

    private fun backPressedButton() {
        binding.circle1ImageView.setBackgroundResource(R.drawable.circle_password_off)
        binding.circle2ImageView.setBackgroundResource(R.drawable.circle_password_off)
        binding.circle3ImageView.setBackgroundResource(R.drawable.circle_password_off)
        binding.circle4ImageView.setBackgroundResource(R.drawable.circle_password_off)
        when (password.length) {
            1 -> setColorCircles(1)
            2 -> {
                setColorCircles(1)
                setColorCircles(2)
            }
            3 -> {
                setColorCircles(1)
                setColorCircles(2)
                setColorCircles(3)
            }
            4 -> {
                setColorCircles(1)
                setColorCircles(2)
                setColorCircles(3)
                setColorCircles(4)
            }
        }
        if (password.isNotEmpty()) {
            binding.fingerprintButton.visibility = View.GONE
            binding.backButton.visibility = View.VISIBLE
        } else {
            if (fingerprint == true) {
                binding.fingerprintButton.visibility = View.VISIBLE
                binding.backButton.visibility = View.GONE
            }
        }
    }

    companion object {
        fun newInstance(firstLaunch: Boolean, secondPassword: String): LoginFragment {
            val args = Bundle().apply {
                putBoolean(ARG_FIRST_LAUNCH, firstLaunch)
                putString(ARG_SECOND_PASSWORD, secondPassword)
            }
            return LoginFragment().apply {
                arguments = args
            }
        }
    }
}