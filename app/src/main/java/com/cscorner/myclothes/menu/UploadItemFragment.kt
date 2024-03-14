package com.cscorner.myclothes.menu

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.cscorner.myclothes.R
import com.cscorner.myclothes.data.AppUser
import com.cscorner.myclothes.data.Clothes
import com.cscorner.myclothes.databinding.FragmentUploaditemBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.Date

//This class creates the Upload Item screen where the user can upload two images and information
// about his item. Then a "cloth" item is generated and uploaded to firebase.
class UploadItemFragment : Fragment() {

    //Store the request codes for the two camera buttons
    private val REQUEST_CODE_FIRST_IMAGE = 1
    private val REQUEST_CODE_SECOND_IMAGE = 2
    lateinit var binding: FragmentUploaditemBinding
    //Credentials
    private var currentUserId: String = ""
    private var currentUserName: String = ""
    //Firebase
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    //Firebase Firestore
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var storageReference: StorageReference

    private var imageUri1: Uri = Uri.parse("")
    private var imageUri2: Uri = Uri.parse("")

    //Creates the view of the screen with all the fields that the users must complete
    // to create the post. If the user does not complete all the fields an error message will appear.
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploaditemBinding.inflate(inflater, container, false)

        storageReference = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()

        //Field to select in which ("Individuals" or "Charities") the user wants to give his item
        val giveTo = resources.getStringArray(R.array.giveTo)
        val arrayAdapterGiveTo = ArrayAdapter(requireContext(), R.layout.dropdown_item, giveTo)
        binding.postGiveTo.setAdapter(arrayAdapterGiveTo)

        //Field to select the size of the item
        val sizes = resources.getStringArray(R.array.sizes)
        val arrayAdapterSizes = ArrayAdapter(requireContext(), R.layout.dropdown_item, sizes)
        binding.postSize.setAdapter(arrayAdapterSizes)

        //Field to select the category of your item
        val categories = resources.getStringArray(R.array.categories)
        val arrayAdapterCategories = ArrayAdapter(requireContext(),
            R.layout.dropdown_item, categories)
        binding.postCategory.setAdapter(arrayAdapterCategories)

        binding.apply{
            if(AppUser.instance != null){
                currentUserId = AppUser.instance!!.userId.toString()
                currentUserName = AppUser.instance!!.username.toString()
            }

            //Getting 1st image from gallery
            postCameraButton1.setOnClickListener{
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.setType("image/*")
                //call the onActivityResult function
                startActivityForResult(i, REQUEST_CODE_FIRST_IMAGE)
            }

            //Getting 2nd image from gallery
            postCameraButton2.setOnClickListener{
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.setType("image/*")
                //call the onActivityResult function
                startActivityForResult(i, REQUEST_CODE_SECOND_IMAGE)
            }

            //When the "Save" is pressed it checks if all the fields are completed.
            //If this is true it calls the savePost() function else it displays the appropriate error.
            postSaveButton.setOnClickListener{
                val giveToLayout: TextInputLayout = requireView().findViewById(R.id.giveTo)
                val giveToText = giveToLayout.editText?.text.toString()

                val titleLayout: TextInputLayout = requireView().findViewById(R.id.title)
                val titleText = titleLayout.editText?.text.toString()

                val sizeLayout: TextInputLayout = requireView().findViewById(R.id.size)
                val sizeText = sizeLayout.editText?.text.toString()

                val priceLayout: TextInputLayout = requireView().findViewById(R.id.price)
                val priceText = priceLayout.editText?.text.toString()

                val categoryLayout: TextInputLayout = requireView().findViewById(R.id.category)
                val categoryText = categoryLayout.editText?.text.toString()

                val descriptionLayout: TextInputLayout = requireView().findViewById(R.id.description)
                val descriptionText = descriptionLayout.editText?.text.toString()

                //check if the fields are empty or wrong
                if(giveToText.isEmpty()){
                    giveToLayout.isErrorEnabled = true
                    giveToLayout.error = "Choose where you want to give your items."

                    giveToLayout.editText?.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            giveToLayout.isErrorEnabled = false
                        }
                        false
                    }
                }
                else if(titleText.isEmpty()){
                    titleLayout.isErrorEnabled = true
                    titleLayout.error = "Give a title to your item."

                    titleLayout.editText?.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            titleLayout.isErrorEnabled = false
                        }
                        false
                    }
                }
                else if(sizeText.isEmpty()){
                    sizeLayout.isErrorEnabled = true
                    sizeLayout.error = "Select the size of your item."

                    sizeLayout.editText?.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            sizeLayout.isErrorEnabled = false
                        }
                        false
                    }
                }
                else if(priceText.isEmpty()){
                    priceLayout.isErrorEnabled = true
                    priceLayout.error = "Give your item a price."

                    priceLayout.editText?.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            priceLayout.isErrorEnabled = false
                        }
                        false
                    }
                }
                else if(categoryText.isEmpty()){
                    categoryLayout.isErrorEnabled = true
                    categoryLayout.error = "Select the category of your item."

                    categoryLayout.editText?.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            categoryLayout.isErrorEnabled = false
                        }
                        false
                    }
                }
                else if(descriptionText.isEmpty()){
                    descriptionLayout.isErrorEnabled = true
                    descriptionLayout.error = "Give a description to your item."

                    descriptionLayout.editText?.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            descriptionLayout.isErrorEnabled = false
                        }
                        false
                    }
                }
                else if(imageUri1 == Uri.EMPTY || imageUri1 == null){
                    Toast.makeText(
                        context,
                        "You must upload two images of your item.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else if(imageUri2 == Uri.EMPTY || imageUri2 == null){
                    Toast.makeText(
                        context,
                        "You must upload two image of your item.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    showLoading()
                    requireActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    savePost()
                }
            }
        }

        return binding.root
    }

    //Get info from the post and save it to firebase
    private fun savePost() {
        val heading: String = binding.postTitle.text.toString().trim()
        val price: String = binding.postPrice.text.toString().trim()
        val description = binding.postDescription.text.toString().trim()
        val size = binding.postSize.text.toString().trim()
        var category = binding.postCategory.text.toString().trim()
        val userId = auth.currentUser!!.uid
        val donateTo = binding.postGiveTo.text.toString().trim()


        if(category == "T-Shirts/Blouses"){
            category = "TshirtsBlouses"
        }
        //Compress the image before uploading
        val compressedImageBitmap1 = compressImage(imageUri1)
        val compressedImageByteArray1 = convertBitmapToByteArray(compressedImageBitmap1)

        val compressedImageBitmap2 = compressImage(imageUri2)
        val compressedImageByteArray2 = convertBitmapToByteArray(compressedImageBitmap2)

        //Saving the path of images in storage
        val filePath1: StorageReference = storageReference
            .child("clothes_images")
            .child("my_image_" + Timestamp.now().seconds)

        //Uploading the compressed images
        filePath1.putBytes(compressedImageByteArray1)
            .addOnSuccessListener {
                //Upload the first image
                filePath1.downloadUrl.addOnSuccessListener { uri1 ->
                    val imageUri1: String = uri1.toString()
                    var imageUri2: String
                    val timeStamp = Timestamp(Date())
                    val clothId = ""

                    //Upload the second image
                    val filePath2: StorageReference = storageReference
                        .child("clothes_images")
                        .child("my_image_" + Timestamp.now().seconds)
                    filePath2.putBytes(compressedImageByteArray2)
                        .addOnSuccessListener {
                            filePath2.downloadUrl.addOnSuccessListener { uri2 ->
                                imageUri2 = uri2.toString()

                                //Creating the object of Cloth
                                val cloth = Clothes(
                                    donateTo, imageUri1, imageUri2, heading, description, price,
                                    size, category, userId, clothId, timeStamp
                                )

                                //Adding the new cloth to the right category
                                val collectionReference: CollectionReference = db.collection(category)
                                collectionReference.add(cloth)
                                    .addOnSuccessListener { documentReference ->
                                        val documentId = documentReference.id
                                        val clothReference = collectionReference.document(documentId)

                                        clothReference.update(
                                            "clothId", documentId
                                        )
                                        hideLoading()
                                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                        Toast.makeText(context, "Post Added", Toast.LENGTH_LONG).show()
                                        val transaction: FragmentTransaction =
                                            requireActivity().supportFragmentManager.beginTransaction()
                                        transaction.replace(R.id.fragment_container, HomeIndividualsFragment())
                                        transaction.addToBackStack(null)
                                        requireActivity().supportFragmentManager.popBackStack()
                                        transaction.commit()
                                    }
                                }
                            }
                }
            }
            .addOnFailureListener {
                hideLoading()
                Toast.makeText(context, "Post Uploading Failed", Toast.LENGTH_LONG).show()
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Log.i("UPLOAD", "FAILED")
            }
    }

    //Show the loading progress bar
    private fun showLoading(){
        binding.progressBar.visibility = View.VISIBLE
    }

    //Hide the loading progress bar
    private fun hideLoading(){
        binding.progressBar.visibility = View.INVISIBLE
    }

    //Function that compresses the bitmap image
    private fun compressImage(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
    }

    //Function that converts the bitmap to a byte array
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    //Get the image from the gallery
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Check which button was clicked and handle the result accordingly
        when (requestCode) {
            REQUEST_CODE_FIRST_IMAGE -> {
                if (resultCode == RESULT_OK && data != null) {
                    //Handle the first image URI
                    imageUri1 = data.data!!
                    binding.postImage1.setImageURI(imageUri1)//showing the image
                    binding.postImageHint1.visibility = View.GONE
                }
            }
            REQUEST_CODE_SECOND_IMAGE -> {
                if (resultCode == RESULT_OK && data != null) {
                    //Handle the second image URI
                    imageUri2 = data.data!!
                    binding.postImage2.setImageURI(imageUri2)//showing the image
                    binding.postImageHint2.visibility = View.GONE
                }
            }
        }
    }

    //Authenticate the user on Start
    override fun onStart() {
        super.onStart()
        user = auth.currentUser!!
    }
}