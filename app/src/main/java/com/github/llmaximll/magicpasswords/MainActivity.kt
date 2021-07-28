package com.github.llmaximll.magicpasswords

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import com.github.llmaximll.magicpasswords.ui.changepassword.ChangePasswordFragment
import com.github.llmaximll.magicpasswords.databinding.ActivityMainBinding
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.ui.passwords.PasswordsListFragment
import com.github.llmaximll.magicpasswords.states.BottomBarAndFabState
import com.github.llmaximll.magicpasswords.ui.binpasswords.RecycleBinFragment
import com.github.llmaximll.magicpasswords.ui.nav.BottomNavDrawerFragment
import com.github.llmaximll.magicpasswords.ui.settings.SettingsFragment
import com.github.llmaximll.magicpasswords.utils.Animation
import com.github.llmaximll.magicpasswords.utils.Common
import com.github.llmaximll.magicpasswords.utils.Encryption
import com.github.llmaximll.magicpasswords.utils.Storage
import com.google.android.material.transition.platform.MaterialElevationScale
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.lang.reflect.Type

class MainActivity : AppCompatActivity(),
    PasswordsListFragment.Callbacks,
    BottomNavDrawerFragment.Callbacks {

    lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityVM
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    lateinit var createDocumentIntent: Intent
    lateinit var createDocumentResultLauncher: ActivityResultLauncher<Intent>
    lateinit var openDocumentIntent: Intent
    lateinit var openDocumentResultLauncher: ActivityResultLauncher<Intent>

    private val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = Common.initViewModel(this, MainActivityVM::class.java) as MainActivityVM
        val sp = Common.getSharedPreferences(this)
        when (sp.getInt(Common.spThemeApp, 0)) {
            0 -> setTheme(R.style.Theme_MagicPasswords)
            1 -> setTheme(R.style.Theme_MagicPasswordsDay)
            2 -> setTheme(R.style.Theme_MagicPasswordsNight)
        }
        setContentView(binding.root)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setUpBottomNavigationAndFab()
        setUpObservers()

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
                        val file = if (sp.getBoolean(Common.spBackupEncryption, true)) {
                            Encryption.encrypt(gson.toJson(text), this@MainActivity)
                        } else {
                            gson.toJson(text)
                        }
                        if (file != null) {
                            result.data.also { intent ->
                                if (intent != null) {
                                    val path = "/storage/emulated/0/${intent.data?.path?.drop(18)}"
                                    Storage.setTextInStorage(
                                        rootDestination = File(path),
                                        fileName = null,
                                        folderName = null,
                                        text = file
                                    )
                                }
                            }
                        } else {
                            Common.toast(
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
                                Storage.getTextFromStorage(
                                    rootDestination = File(
                                        "/storage/emulated/0/${intent.data?.path?.drop(18)}"),
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
                                val passwordsList: List<PasswordInfo> =
                                    gson.fromJson(Encryption.decrypt(file, this@MainActivity), collectionType)
                                viewModel.showRecoveryPassword(this@MainActivity, passwordsList)
                            } catch (e: JsonParseException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStart() {
        super.onStart()
        binding.run {
            addPasswordFab.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Animation.animateView(v, reverse = false, zChange = false, .8f)
                    }
                    MotionEvent.ACTION_UP -> {
                        Animation.animateView(v, reverse = true, zChange = false, .8f)
                        currentNavigationFragment?.apply {
                            exitTransition = MaterialElevationScale(false).apply {
                                duration = resources.getInteger(
                                    R.integer.material_motion_duration_medium_1).toLong()
                            }
                            reenterTransition = MaterialElevationScale(true).apply {
                                duration = resources.getInteger(
                                    R.integer.material_motion_duration_medium_1).toLong()
                            }
                        }
                        navController.navigate(R.id.addPasswordFragment)
                        v.performClick()
                    }
                }
                true
            }
            clickableImageView.setOnClickListener {
                if (viewModel.bottomBarMenuDataFlow.value) {
                    viewModel.bottomBarMenuDataFlow.value = false
                }
            }
        }
    }

    private fun setUpBottomNavigationAndFab() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.changePasswordFragment -> {
                    viewModel.bottomBarAndFabStateDataFlow.value =
                        BottomBarAndFabState.BottomBarOffFabOff
                }
                R.id.addPasswordFragment -> {
                    viewModel.bottomBarAndFabStateDataFlow.value =
                        BottomBarAndFabState.BottomBarOffFabOff
                }
                R.id.passwordsListFragment -> {
                    viewModel.bottomBarAndFabStateDataFlow.value =
                            BottomBarAndFabState.BottomBarOnFabOn
                    binding.bottomAppBarTitle.text = "Все"
                }
                R.id.settingsFragment -> {
                    viewModel.bottomBarAndFabStateDataFlow.value =
                        BottomBarAndFabState.BottomBarOnFabOff
                    binding.bottomAppBarTitle.text = "Настройки"
                }
                R.id.recycleBinFragment -> {
                    viewModel.bottomBarAndFabStateDataFlow.value =
                        BottomBarAndFabState.BottomBarOnFabOff
                    binding.bottomAppBarTitle.text = "Корзина"
                }
            }
        }

        binding.addPasswordFab.apply {
            setShowMotionSpecResource(R.animator.fab_show)
            setHideMotionSpecResource(R.animator.fab_hide)
        }

        binding.bottomAppBarContentContainer.setOnClickListener {
            viewModel.bottomBarMenuDataFlow.value = !viewModel.bottomBarMenuDataFlow.value
        }
        binding.bottomAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.search -> {
                    Common.toast(this@MainActivity, "search")
                }
            }
            true
        }
    }

    private fun setUpObservers() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.bottomBarAndFabStateDataFlow
                        .collect { state ->
                            when (state) {
                                BottomBarAndFabState.BottomBarOnFabOn -> {
                                    binding.run {
                                        bottomAppBar.performShow()
                                        addPasswordFab.show()
                                    }
                                }
                                BottomBarAndFabState.BottomBarOnFabOff -> {
                                    binding.run {
                                        bottomAppBar.performShow()
                                        addPasswordFab.hide()
                                    }
                                }
                                BottomBarAndFabState.BottomBarOffFabOn -> {
                                    viewModel.bottomBarMenuDataFlow.compareAndSet(
                                        expect = true,
                                        update = false
                                    )
                                    binding.run {
                                        bottomAppBar.performHide()
                                        addPasswordFab.show()
                                    }
                                }
                                BottomBarAndFabState.BottomBarOffFabOff -> {
                                    viewModel.bottomBarMenuDataFlow.compareAndSet(
                                        expect = true,
                                        update = false
                                    )
                                    binding.run {
                                        bottomAppBar.performHide()
                                        addPasswordFab.hide()
                                    }
                                }
                            }
                        }
                }
                launch {
                    viewModel.bottomBarMenuDataFlow
                        .collect { state ->
                            if (state) {
                                binding.run {
                                    Animation.animateMainMenu(
                                        viewModel,
                                        reverse = false,
                                        clickableImageView = clickableImageView,
                                        bottomNavFragment = bottomNavDrawer,
                                        bottomAppBarChevron = bottomAppBarChevron,
                                        bottomAppBarTitle = bottomAppBarTitle
                                    )
                                }
                            } else {
                                binding.run {
                                    Animation.animateMainMenu(
                                        viewModel,
                                        reverse = true,
                                        clickableImageView = clickableImageView,
                                        bottomNavFragment = bottomNavDrawer,
                                        bottomAppBarChevron = bottomAppBarChevron,
                                        bottomAppBarTitle = bottomAppBarTitle
                                    )
                                }
                            }
                        }
                }
            }
        }
    }

    /**
     * Callbacks
     */

    override fun onPasswordsListFragmentChangePassword(idPassword: String, transitionView: View) {
        currentNavigationFragment?.apply {
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            }
        }
        val args = Bundle().apply {
            putString(ChangePasswordFragment.ARG_ID_PASSWORD, idPassword)
            putString(ChangePasswordFragment.ARG_TRANSITION_NAME, transitionView.transitionName)
        }
        val extras: Navigator.Extras = FragmentNavigator.Extras.Builder().addSharedElement(
            transitionView, transitionView.transitionName
        ).build()
        navController.navigate(
            R.id.changePasswordFragment,
            args,
            null,
            extras
        )
    }

    override fun onPasswordsListFragmentSelected(state: Boolean) {
        binding.apply {
            bottomAppBar.menu.run {
                findItem(R.id.delete_passwords).isVisible = state
                findItem(R.id.select_all).isVisible = state
                findItem(R.id.search).isVisible = !state
            }
            if (state) {
                viewModel.bottomBarAndFabStateDataFlow.value =
                    BottomBarAndFabState.BottomBarOnFabOff
            } else {
                viewModel.bottomBarAndFabStateDataFlow.value =
                    BottomBarAndFabState.BottomBarOnFabOn
            }
            bottomAppBarContentContainer.isVisible = !state
        }
    }

    override fun onPasswordsListFragmentOnTouchPassword() {
        if (viewModel.bottomBarMenuDataFlow.value) {
            viewModel.bottomBarMenuDataFlow.value = false
        }
    }

    override fun onMenuItemClicked(button: Int) {
        when (button) {
            BottomNavDrawerFragment.PASSWORDS_ITEM -> {
                currentNavigationFragment?.apply {
                    exitTransition = MaterialFadeThrough().apply {
                        duration = resources.getInteger(
                            R.integer.material_motion_duration_medium_1).toLong()
                    }
                }
                viewModel.bottomBarMenuDataFlow.value = false
                if (currentNavigationFragment !is PasswordsListFragment) {
                    navController.navigate(R.id.action_global_passwordsListFragment)
                }
            }
            BottomNavDrawerFragment.SETTINGS_ITEM -> {
                currentNavigationFragment?.apply {
                    exitTransition = MaterialFadeThrough().apply {
                        duration = resources.getInteger(
                            R.integer.material_motion_duration_medium_1).toLong()
                    }
                }
                viewModel.bottomBarMenuDataFlow.value = false
                if (currentNavigationFragment !is SettingsFragment) {
                    navController.navigate(R.id.action_global_settingsFragment)
                }
            }
            BottomNavDrawerFragment.RECYCLE_BIN_ITEM -> {
                currentNavigationFragment?.apply {
                    exitTransition = MaterialFadeThrough().apply {
                        duration = resources.getInteger(
                            R.integer.material_motion_duration_medium_1).toLong()
                    }
                }
                viewModel.bottomBarMenuDataFlow.value = false
                if (currentNavigationFragment !is RecycleBinFragment) {
                    navController.navigate(R.id.action_global_recycleBinFragment)
                }
            }
        }
    }
}