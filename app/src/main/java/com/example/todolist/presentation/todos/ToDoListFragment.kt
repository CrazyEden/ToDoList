package com.example.todolist.presentation.todos

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.example.todolist.R
import com.example.todolist.databinding.FragmentMainBinding
import com.example.todolist.presentation.activity.TAG
import com.example.todolist.presentation.todos.todo.ToDoInfoFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ToDoListFragment : Fragment(){

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var targetShowingId:String
    private lateinit var targetShowingNick:String
    private lateinit var popupMenu:PopupMenu
    private val vModel: ToDoListViewModel by viewModels()
    private var isCurrentUserAdmin = false
    private var isCurrentUserAtHerselfPageOrAdmin = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        initVars()
        inflateToolbarMenu()
        initViewModelObservers()
        binding.rcView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY == 0) return@setOnScrollChangeListener binding.buttonAddTodo.show()
            if (scrollY > oldScrollY) binding.buttonAddTodo.hide()
            else binding.buttonAddTodo.show()
        }
        binding.buttonAddTodo.setOnClickListener {
            val args = bundleOf(
                ToDoInfoFragment.ID_KEY to targetShowingId,
            )
            parentFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, ToDoInfoFragment::class.java,args)
                .commit()
        }
        return binding.root
    }

    private fun inflateToolbarMenu() {
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
                menuInflater.inflate(R.menu.main_menu, menu)

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.showListUsers){
                    popupMenu.show()
                    return true
                }
                return false
            }
        }
        requireActivity().addMenuProvider(menuProvider,viewLifecycleOwner,Lifecycle.State.RESUMED)
    }

    private fun initViewModelObservers() {

        vModel.adminIdLiveData.observe(viewLifecycleOwner){
            if (it==null) return@observe

            isCurrentUserAdmin =  vModel.authId == it
            Log.i(TAG,"adminIdLiveData observe string \"$it\" | your id \"${vModel.authId}\" \n" +
                    "isShowHidedTodo \"$isCurrentUserAtHerselfPageOrAdmin\" | isAdmin \"$isCurrentUserAdmin\"")
        }
        vModel.dataInFirebaseLiveData.observe(viewLifecycleOwner){
            val list = it?.listTodo
            isCurrentUserAtHerselfPageOrAdmin = (isCurrentUserAdmin || (vModel.authId == targetShowingId))
            adapter.setPermission(isCurrentUserAtHerselfPageOrAdmin)

            if (adapter.getRawList() != list)
                adapter.setData(list)
        }
        vModel.listCurrentUsers.observe(viewLifecycleOwner){ it ->
            if (it.isEmpty()) return@observe
            popupMenu.menu.clear()
            mapOfNicknamesAndIds.clear()
            it.forEach {
                if (it?.nickname == null) it?.nickname = it?.userId
                if(it?.userId==null) return@observe
                mapOfNicknamesAndIds[it.nickname!!] = it.userId!!
                popupMenu.menu.add(it.nickname)
            }
        }
        vModel.userDataLiveData.observe(viewLifecycleOwner){
            targetShowingNick = it?.nickname.toString()
            activity?.title = it?.nickname
        }
        vModel.coldLoad(targetShowingId)
    }
    private var mapOfNicknamesAndIds:MutableMap<String,String> = mutableMapOf()

    private fun initVars(){
        adapter = ToDoAdapter(){ todo,position->
            val args = bundleOf(
                ToDoInfoFragment.ID_KEY to targetShowingId,
                ToDoInfoFragment.TODO_POSITION_KEY to position,
                ToDoInfoFragment.TODO_KEY to todo
            )
            parentFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, ToDoInfoFragment::class.java,args)
                .commit()
        }
        binding.rcView.adapter = adapter
        targetShowingId = vModel.authId

        popupMenu = PopupMenu(context,binding.buttonAddTodo)
        popupMenu.setOnMenuItemClickListener {
            val tempStr = it.title.toString()
            val key = mapOfNicknamesAndIds[tempStr]!!
            targetShowingId = key
            targetShowingNick = tempStr
            activity?.title = tempStr
            Log.i(TAG, "in popup menu was selected item \"$key\"")
            vModel.createToDoObserver(key)
            true
        }
    }

    companion object{
        fun getCurrentTime() = Calendar.getInstance().time.time
    }
}
