package com.github.llmaximll.magicpasswords.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.github.llmaximll.magicpasswords.MainActivity
import com.github.llmaximll.magicpasswords.R
import com.github.llmaximll.magicpasswords.adaptersholders.PasswordsListAdapter
import com.github.llmaximll.magicpasswords.adaptersholders.SimpleItemTouchHelperCallback
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.databinding.FragmentPasswordsListBinding
import com.github.llmaximll.magicpasswords.vm.PasswordsListVM
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


private const val TAG = "PasswordsListFragment"
private const val KEY_RECYCLER_VIEW = "key_recycler_view"

class PasswordsListFragment : Fragment() {

    interface Callbacks {
        fun onPasswordsListFragment(fragment: String, idPassword: String, sharedView: View?)
    }

    private lateinit var binding: FragmentPasswordsListBinding
    private lateinit var cf: CommonFunctions
    private lateinit var viewModel: PasswordsListVM
    private lateinit var adapter: PasswordsListAdapter
    private var recyclerViewState: Parcelable? = null
    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cf = CommonFunctions.get()
        cf.log(TAG, "onCreate()")
        //fragment transition
        exitTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.fade)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPasswordsListBinding.inflate(inflater, container, false)

        setToolBar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = cf.initViewModel(this, PasswordsListVM::class.java) as PasswordsListVM
        //Другое
        recyclerViewState = viewModel.getRecyclerViewState()

        getAllPasswords()
        //transition
        postponeEnterTransition()
        binding.coordinatorLayout.doOnPreDraw { startPostponedEnterTransition() }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStart() {
        super.onStart()
        binding.addPasswordFab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    cf.animateView(v, reverse = false, zChange = false, .8f)
                }
                MotionEvent.ACTION_UP -> {
                    cf.animateView(v, reverse = true, zChange = false, .8f)
                    (activity as? MainActivity)?.replaceMainFragments(MainActivity.REPLACE_ON_ADD_FRAGMENT)
                    v.performClick()
                }
            }
            true
        }
//        binding.addPasswordFab.setOnClickListener {
//            val fabImageView = binding.fabImageView
//            val fab = binding.addPasswordFab
//            val animatorX = ObjectAnimator.ofFloat(fabImageView, "scaleX", 30f)
//            val animatorY = ObjectAnimator.ofFloat(fabImageView, "scaleY", 30f)
//            val animatorXFab = ObjectAnimator.ofFloat(fab, "scaleX", 0f)
//            val animatorYFab = ObjectAnimator.ofFloat(fab, "scaleY", 0f)
//            val transitionDrawable = TransitionDrawable(arrayOf(ContextCompat.getDrawable(requireContext(), R.drawable.circle_blue), ContextCompat.getDrawable(requireContext(), R.drawable.circle_white)))
//            fabImageView.background = transitionDrawable
//            transitionDrawable.startTransition(400)
//            fabImageView.animate().apply {
//                translationZ(20f)
//            }
//            AnimatorSet().apply {
//                playTogether(animatorX, animatorY, animatorXFab, animatorYFab)
//                duration = 400L
//                start()
//            }.doOnEnd {
//
//            }
//        }
    }



    override fun onPause() {
        super.onPause()
        recyclerViewState = binding.passwordsRecyclerView.layoutManager?.onSaveInstanceState()
        viewModel.saveRecyclerViewState(recyclerViewState as LinearLayoutManager.SavedState?)
    }

//    private fun writeFile() {
//        val bos = ByteArrayOutputStream()
//        val out = ObjectOutputStream(bos)
//        try {
//            val mF = f(recyclerViewState as LinearLayoutManager.SavedState?)
//            out.writeObject(object : Serializable { val state = recyclerViewState })
//            requireContext().openFileOutput("RecyclerViewState", MODE_PRIVATE).use { output ->
//                output.write(bos.toByteArray())
//                output.close()
//                cf.log(TAG, "Запись успешна")
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            out.close()
//            bos.close()
//        }
//    }

//    private fun readFile(): Serializable? {
//        var out: ObjectInputStream? = null
//        try {
//            cf.log(TAG, "readFile()")
//            requireContext().openFileInput("RecyclerViewState").use { stream ->
//                out = ObjectInputStream(stream)
//                cf.log(TAG, "Чтение успешно: recyclerViewState=$recyclerViewState")
//                return out?.readObject() as Serializable?
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            out?.close()
//        }
//        return null
//    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    fun getAllPasswords() {
        viewModel.getAllPasswords(0)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.passwordsListFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { passwordsList ->
                    if (passwordsList != null) {
                        setRecyclerView(passwordsList)
                    }
                }
        }
    }

    private fun setToolBar(toolBar: Toolbar = binding.toolBar) {
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    callbacks?.onPasswordsListFragment("settings", "null", null)
                }
                R.id.recycle_bin -> {
                    callbacks?.onPasswordsListFragment("recycle bin", "null", null)
                }
            }
            true
        }
    }

    private fun setRecyclerView(passwordsList: List<PasswordInfo>) {
        val mutPasswordsList = mutableListOf<PasswordInfo>()
        mutPasswordsList.addAll(passwordsList)
        if (passwordsList.isNotEmpty()) {
            val rV = binding.passwordsRecyclerView
            rV.layoutManager = LinearLayoutManager(requireContext())
            //Восстановление состояния RecyclerView
            binding.passwordsRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)

            adapter = PasswordsListAdapter(callbacks, mutPasswordsList, viewModel, requireContext(), binding.coordinatorLayout)
            rV.adapter = adapter
            val callback = SimpleItemTouchHelperCallback(adapter)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(rV)
        }
    }

    companion object {
        fun newInstance(): PasswordsListFragment {
            return PasswordsListFragment()
        }
    }
}