package com.lettytrain.notesapp

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.entities.Asyn
import com.lettytrain.notesapp.entities.IdMap
import com.lettytrain.notesapp.entities.Notes
import com.lettytrain.notesapp.entities.User
import com.lettytrain.notesapp.util.NoteBottemSheetFragment
import com.lettytrain.notesapp.util.OKHttpCallback
import com.lettytrain.notesapp.util.SharedPreferenceUtil
import com.lettytrain.notesapp.vo.UserVo
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.android.synthetic.main.fragment_create_note.imgMore
import kotlinx.android.synthetic.main.fragment_notes_bottom_sheet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlinx.android.synthetic.main.fragment_create_note.layoutWebUrl as layoutWebUrl1
import kotlinx.android.synthetic.main.fragment_notes_bottom_sheet.layoutImage as layoutImage1

/**
 *
 * 本地操作，全局同步
 * */
class CreateNoteFragment : BaseFragment(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456
    private var selectedImagePath = ""
    private var noteId = -1
    var currentDate: String? = null
    private var webLink = ""
    private var selectedColor: String? =
        MyApplication.context.resources.getString(R.color.ColorLightBlack.dec())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )
        noteId = requireArguments().getInt("noteId", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_note, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CreateNoteFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = SharedPreferenceUtil.readObject("user", UserVo::class.java)
        val userId = user.userId!!
        Log.d("createonViewCreated", "notesId${noteId}")
        if (noteId != -1) {
            launch {
                context?.let {
                    var notes = withContext(Dispatchers.IO) {
                        NotesDatabase.getDatabase(MyApplication.context).noteDao()
                            .getSpecificNote(noteId)
                    }
                    if (notes.color != null && notes.color !="") {
                        colorView.setBackgroundColor(Color.parseColor(notes.color))
                    } else {
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    etNoteTitle.setText(notes.title)
                    etNoteSubTitle.setText(notes.subTitle)
                    etNoteDesc.setText(notes.noteText)
                    if (notes.imgPath != "") {
                        selectedImagePath = notes.imgPath!!
                        imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                        layoutImage.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        imgDelete.visibility = View.VISIBLE
                    } else {
                        layoutImage.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        imgDelete.visibility = View.GONE
                    }
                    if (notes.webLink != "") {
                        webLink = notes.webLink!!
                        tvWebLink.text = notes.webLink
                        layoutWebUrl.visibility = View.VISIBLE
                        etWebLink.setText(notes.webLink)
                        imgUrlDelete.visibility = View.VISIBLE
                    } else {
                        imgUrlDelete.visibility = View.GONE
                        layoutWebUrl.visibility = View.GONE
                    }
                }
            }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        currentDate = sdf.format(Date())
        colorView.setBackgroundColor(Color.parseColor(selectedColor))
        tvDateTime.text = currentDate
        imgDone.setOnClickListener {
            if (noteId != -1) {
                updateNote()
            } else {
                saveNote(userId)
            }
        }
        imgBack.setOnClickListener {
            val intent = Intent(MyApplication.context, MainActivity::class.java)
            startActivity(intent)
        }
        imgMore.setOnClickListener {
            var noteBottemSheetFragment = NoteBottemSheetFragment.newInstance(noteId)
            noteBottemSheetFragment.show(
                requireActivity().supportFragmentManager,
                "Note Bottom Sheet Fragment"
            )
            //   replaceFragment(NoteBottemSheetFragment())
        }
        imgDelete.setOnClickListener {
            selectedImagePath = ""
            layoutImage.visibility = View.GONE
        }
        btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()) {
                checkWebUrl()
            } else {
                Toast.makeText(requireContext(), "Url is Required", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            if (noteId != -1) {
                tvWebLink.visibility = View.VISIBLE
                layoutWebUrl.visibility = View.GONE
            } else {
                layoutWebUrl.visibility = View.GONE
            }
        }
        imgUrlDelete.setOnClickListener {
            webLink = ""
            tvWebLink.visibility = View.GONE
            imgUrlDelete.visibility = View.GONE
            layoutWebUrl.visibility = View.GONE
        }
        tvWebLink.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            startActivity(intent)
        }
    }

    private fun updateNote() {
        launch {

            var notes = withContext(Dispatchers.IO) {
                NotesDatabase.getDatabase(MyApplication.context).noteDao().getSpecificNote(noteId)
            }
            notes.title = etNoteTitle.text.toString()
            notes.subTitle = etNoteSubTitle.text.toString()
            notes.noteText = etNoteDesc.text.toString()
            notes.updateTime = currentDate
            notes.color = selectedColor
            notes.imgPath = selectedImagePath
            notes.webLink = webLink
            //本地更新
            withContext(Dispatchers.IO) {
                NotesDatabase.getDatabase(MyApplication.context).noteDao().updateNote(notes)
            }
            //更新同步表
            val idMap = withContext(Dispatchers.IO) {
                NotesDatabase.getDatabase(MyApplication.context).idmapDao().selectIdmap(noteId)
            }
            Log.d("update测试","offid${idMap.offlineId},onlineid${idMap.onlineId}")
            if (idMap.onlineId != -1) {
                //note已经add到后端，直接添加update记录
                val user = SharedPreferenceUtil.readObject("user", UserVo::class.java)
                var asyn = Asyn()
                asyn.userId = user.userId
                asyn.offlineId = noteId
                asyn.onlineId = idMap.onlineId
                asyn.operation = "update"
                asyn.time = currentDate
                val count=withContext(Dispatchers.IO) {
                    //存储到本地
                    NotesDatabase.getDatabase(MyApplication.context).asynDao().insertOne(asyn)
                }
                val  asyn_list = withContext(Dispatchers.IO){
                    NotesDatabase.getDatabase(MyApplication.context).asynDao().selectAll(user.userId!!)
                }
                Log.d("update测试","asyn长度 ${asyn_list.size}")
                Log.d("update测试","asyb_id${count}")
            } else {
                //修改asyn的add记录的time
                withContext(Dispatchers.IO) {
                    NotesDatabase.getDatabase(MyApplication.context).asynDao()
                        .updateTime(noteId, currentDate!!)
                }
            }
            val userVo=SharedPreferenceUtil.readObject("user",UserVo::class.java)
            val  asyn_list = withContext(Dispatchers.IO){
                NotesDatabase.getDatabase(MyApplication.context).asynDao().selectAll(userVo.userId!!)
            }
            Log.d("update测试","asyn长度 ${asyn_list.size}")
            etNoteTitle.setText("")
            etNoteSubTitle.setText("")
            etNoteDesc.setText("")
            layoutImage.visibility = View.GONE
            imgNote.visibility = View.GONE
            tvWebLink.visibility = View.GONE
            val intent = Intent(MyApplication.context, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveNote(userId: Int) {
        if (etNoteTitle.text.isNullOrEmpty()) {
            Toast.makeText(context, "Note Title is Requeried", Toast.LENGTH_SHORT).show()
        }
        if (etNoteSubTitle.text.isNullOrEmpty()) {
            Toast.makeText(context, "Note Sub Title is  Requeried", Toast.LENGTH_SHORT).show()
        }
        if (etNoteDesc.text.isNullOrEmpty()) {
            Toast.makeText(context, "Note Description  must not be null", Toast.LENGTH_SHORT).show()
        } else {
            launch(Dispatchers.IO) {

                var notes = Notes()
                notes.title = etNoteTitle.text.toString()
                notes.subTitle = etNoteSubTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.createTime = currentDate
                notes.updateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink
                notes.userId = userId
                val offline_id =
                    NotesDatabase.getDatabase(MyApplication.context).noteDao().insertNotes(notes)
                var idmap = IdMap()
                idmap.userId = userId
                idmap.offlineId = offline_id.toInt()
                idmap.onlineId = -1

                NotesDatabase.getDatabase(MyApplication.context).idmapDao().insertMap(idmap)
                var asyn = Asyn()
                asyn.userId = userId
                asyn.offlineId = offline_id.toInt()
                asyn.onlineId = -1
                asyn.operation = "add"
                asyn.time = currentDate

                //存储到本地
                NotesDatabase.getDatabase(MyApplication.context).asynDao().insertOne(asyn)

                etNoteTitle.setText("")
                etNoteSubTitle.setText("")
                etNoteDesc.setText("")
                layoutImage.visibility = View.GONE
                imgNote.visibility = View.GONE
                tvWebLink.visibility = View.GONE
                val intent = Intent(MyApplication.context, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun checkWebUrl() {
        if (Patterns.WEB_URL.matcher(etWebLink.text.toString()).matches()) {
            layoutWebUrl.visibility = View.GONE
            etWebLink.isEnabled = false
            webLink = etWebLink.text.toString()
            tvWebLink.visibility = View.VISIBLE
            tvWebLink.text = etWebLink.text.toString()
        } else {
            Toast.makeText(requireContext(), "Url is not valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNote(note_id: Int) {

        launch {
            //更新同步表
            val idMap = withContext(Dispatchers.IO) {
                NotesDatabase.getDatabase(MyApplication.context).idmapDao().selectIdmap(note_id)
            }

                Log.d("deletenote", "noteid:${note_id},idmap.onlineId:${idMap.onlineId}")
                val user = SharedPreferenceUtil.readObject("user", UserVo::class.java)
                //如果还没更新到后端，就不需要执行后端同步
                if (idMap.onlineId != -1) {
                    //note已经上传过服务端
                    val count = withContext(Dispatchers.IO) {
                        NotesDatabase.getDatabase(MyApplication.context).asynDao().isExist(note_id)
                    }
                    if (count > 0) {
                        //存在未同步到后端的更新
                        withContext(Dispatchers.IO) {
                            //先删除之前的更新
                            NotesDatabase.getDatabase(MyApplication.context).asynDao()
                                .deleteByOfflineId(note_id)
                        }
                    }
                    //新增delete记录
                    var asyn = Asyn()
                    asyn.userId = user.userId
                    asyn.offlineId = note_id
                    asyn.onlineId = idMap.onlineId
                    asyn.operation = "delete"
                    asyn.time = currentDate
                    withContext(Dispatchers.IO) {
                        //存储到本地
                        NotesDatabase.getDatabase(MyApplication.context).asynDao().insertOne(asyn)
                    }

                    val  asyn_list =NotesDatabase.getDatabase(MyApplication.context).asynDao().selectAll(user.userId!!)
                    for( i in  asyn_list){
                        Log.d("Synworker","idmap offline_id:${i.offlineId}, online_id${i.onlineId}, opration :${i.operation}")
                    }

                } else {
                    //notes未曾上传到服务端
                    //删除之前存在过的add、update记录
                    withContext(Dispatchers.IO) {
                        NotesDatabase.getDatabase(MyApplication.context).asynDao()
                            .deleteByOfflineId(note_id)
                    }
                }

                //删除idmap中相关的键值对
                withContext(Dispatchers.IO) {
                    NotesDatabase.getDatabase(MyApplication.context).idmapDao().delete(idMap)
                }
                withContext(Dispatchers.IO) {
                    NotesDatabase.getDatabase(MyApplication.context).noteDao()
                        .deleteSpecificNote(note_id)
                }
            val intent = Intent(MyApplication.context, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private val BroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var actionColor = intent!!.getStringExtra("action")
            when (actionColor!!) {
                "Blue" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Yellow" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }


                "Purple" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }
                "Green" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }
                "Orange" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }
                "Black" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Image" -> {
                    readStorageTask()
                }
                "WebUrl" -> {
                    layoutWebUrl.visibility = View.VISIBLE
                }
                "DeleteNote" -> {
                    //delete note
                    val speId = intent.getIntExtra("specific_id", -1)
                    if (noteId == speId && noteId != -1) {
                        deleteNote(noteId)
                    }

                }
                else -> {
                    layoutImage.visibility = View.GONE
                    imgNote.visibility = View.GONE
                    layoutWebUrl.visibility = View.GONE
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()

    }

    private fun hasReadStoragePerm(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun readStorageTask() {
        //是否在Manifest中静态申请了权限，如果没有就动态申请权限
        if (hasReadStoragePerm()) {
            // Toast.makeText(requireContext(),"permission Granted",Toast.LENGTH_SHORT).show()
            pickImageFromGallery()
        } else {
            EasyPermissions.requestPermissions(
                requireActivity(), getString(R.string.storage_permission_text),
                READ_STORAGE_PERM, android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms))
            AppSettingsDialog.Builder(requireActivity()).build().show()
    }

    private fun pickImageFromGallery() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_IMAGE)
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {

        var cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            return contentUri.path
        } else {
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            var filepath = cursor.getString(index)
            cursor.close()
            return filepath
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                var selectedImageUrl = data.data
                if (selectedImageUrl != null) {
                    try {
                        var inputStream =
                            requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        var bitmap = BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        layoutImage.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        selectedImagePath = getPathFromUri(selectedImageUrl)!!

                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }


                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            requireActivity()
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.placeNotesFragment, fragment)
        fragmentTransaction.commit()
    }
}
