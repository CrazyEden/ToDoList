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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.data.model.Todo
import com.example.todolist.databinding.FragmentTodosBinding
import com.example.todolist.presentation.activity.TAG
import com.example.todolist.presentation.todos.todo.ToDoInfoFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ToDoListFragment : Fragment(), ToDoArgs {

    private lateinit var binding: FragmentTodosBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var targetShowingId:String
    private lateinit var popupMenu:PopupMenu
    private val vModel: ToDoListViewModel by viewModels()
    private var isCurrentUserAdmin = false
    private var isCurrentUserAtHerselfPageOrAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        targetShowingId = savedInstanceState?.getString("id")?:vModel.authId
        super.onCreate(savedInstanceState)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("id",targetShowingId)
        super.onSaveInstanceState(outState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTodosBinding.inflate(inflater, container, false)
        initVars()
        inflateToolbarMenu()
        initViewModelObservers()
        vModel.coldLoad(targetShowingId)
        binding.rcView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY == 0) return@setOnScrollChangeListener binding.buttonAddTodo.show()
            if (scrollY > oldScrollY) binding.buttonAddTodo.hide()
            else binding.buttonAddTodo.show()
        }
        binding.buttonAddTodo.setOnClickListener {
            val args = bundleOf(ToDoInfoFragment.ID_KEY to targetShowingId,)
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
            activity?.title = it?.nickname
        }

    }
    private var mapOfNicknamesAndIds:MutableMap<String,String> = mutableMapOf()

    private fun initVars(){
        adapter = ToDoAdapter(this)
        adapter.setData(vModel.getLocalTodoList())
        binding.rcView.adapter = adapter
        initRcViewSwipe()
        popupMenu = PopupMenu(context,binding.buttonAddTodo)
        popupMenu.setOnMenuItemClickListener {
            val tempStr = it.title.toString()
            val key = mapOfNicknamesAndIds[tempStr]!!
            targetShowingId = key
            activity?.title = tempStr
            Log.i(TAG, "in popup menu was selected item \"$key\"")
            vModel.createToDoObserver(key)
            true
        }
    }
    private fun initRcViewSwipe(){
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val item = adapter.getItem(position)
                if (direction != ItemTouchHelper.LEFT) return
                adapter.removeItem(position)
                Snackbar.make(binding.rcView,"xdd", Snackbar.LENGTH_LONG)
                    .setAction(R.string.cancel){ adapter.addItem(position,item) }
                    .show()
            }
        }).attachToRecyclerView(binding.rcView)
    }

    companion object{
        fun getCurrentTime() = Calendar.getInstance().time.time
    }

    /*ToDoArgs*/
    override fun itemWasUpdated(todo: Todo, position: Int) {
        vModel.updateTodo(todo = todo,id = targetShowingId, position = position)
    }

    override fun openToDoItem(todo: Todo, position: Int) {
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

    override fun listWasUpdated(list: MutableList<Todo>) {
        vModel.updateList(targetShowingId,list)
    }
    /*ToDoArgs*/
}
