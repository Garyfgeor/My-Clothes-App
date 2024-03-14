package com.cscorner.myclothes.menu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import com.cscorner.myclothes.loginAndSignUp.MainActivity
import com.cscorner.myclothes.R
import com.cscorner.myclothes.databinding.FragmentSettingsBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

//This class creates the Settings screen where the user can delete his account or change password. This two actions will
// update Firebase too.
class SettingsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        val view: View = inflater.inflate(R.layout.fragment_settings, container, false)


        val changePasswordButton = view.findViewById<Button>(R.id.changePasswordButton)
        val okButton = view.findViewById<Button>(R.id.OKButton)
        val textLayout: RelativeLayout = view.findViewById(R.id.textLayout)
        val deleteAccountButton = view.findViewById<Button>(R.id.deleteAccountButton)

        changePasswordButton.setOnClickListener {
            if(textLayout.visibility == View.VISIBLE){
                textLayout.visibility = View.INVISIBLE
            }
            else{
                textLayout.visibility = View.VISIBLE
            }
        }

        okButton.setOnClickListener {
            changePassword(view, textLayout)
        }

        deleteAccountButton.setOnClickListener {
            showDeleteDialog(auth)
        }
        return view
    }

    //This function checks if the fields for current, new and confirm new password are empty or completed wrong
    // and shows the appropriate error
    private fun changePassword(view: View, textLayout: RelativeLayout){
        val currentPassword: String = view.findViewById<TextInputEditText?>(R.id.currentPasswordText).text.toString()
        val newPassword: String = view.findViewById<TextInputEditText?>(R.id.newPasswordText).text.toString()
        val confirmNewPassword: String = view.findViewById<TextInputEditText?>(R.id.confirmNewPasswordText).text.toString()


        if(currentPassword.isEmpty()){
            val currPass: TextInputLayout = view.findViewById(R.id.currentPassword)
            currPass.isErrorEnabled = true
            currPass.error = "Password cannot be empty"

            currPass.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    currPass.isErrorEnabled = false
                }
                false
            }
        }
        else if(currentPassword.trim().length < 6){
            val currPass: TextInputLayout = view.findViewById(R.id.currentPassword)
            currPass.isErrorEnabled = true
            currPass.error = "Password must contains 6 characters or more."

            currPass.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    currPass.isErrorEnabled = false
                }
                false
            }
        }
        else if(newPassword.isEmpty()){
            val newPass: TextInputLayout = view.findViewById(R.id.newPassword)
            newPass.isErrorEnabled = true
            newPass.error = "New Password cannot be empty"

            newPass.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    newPass.isErrorEnabled = false
                }
                false
            }
        }
        else if(newPassword.trim().length < 6){
            val newPass: TextInputLayout = view.findViewById(R.id.newPassword)
            newPass.isErrorEnabled = true
            newPass.error = "New Password must contains 6 characters or more."

            newPass.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    newPass.isErrorEnabled = false
                }
                false
            }
        }
        else if(confirmNewPassword.isEmpty()){
            val confNewPass: TextInputLayout = view.findViewById(R.id.confirmNewPassword)
            confNewPass.isErrorEnabled = true
            confNewPass.error = "Confirm New Password cannot be empty"

            confNewPass.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    confNewPass.isErrorEnabled = false
                }
                false
            }
        }
        else if(confirmNewPassword.trim().length < 6){
            val confNewPass: TextInputLayout = view.findViewById(R.id.confirmNewPassword)
            confNewPass.isErrorEnabled = true
            confNewPass.error = "Confirm New Password must contains 6 characters or more."

            confNewPass.editText?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    confNewPass.isErrorEnabled = false
                }
                false
            }
        }
        else{
            if(newPassword == confirmNewPassword && currentPassword != newPassword){
                val user = auth.currentUser!!
                val credential = EmailAuthProvider
                    .getCredential(user.email!!, currentPassword)

                showLoading()
                requireActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                user.reauthenticate(credential)
                    .addOnCompleteListener {
                        //If the user is authenticated change his password and update firebase
                        if(it.isSuccessful){
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        hideLoading()
                                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                        Toast.makeText(requireContext(), "Password Changed Successfully!", Toast.LENGTH_SHORT).show()
                                        textLayout.visibility = View.INVISIBLE
                                        val intent = Intent(requireActivity(), MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                    else{
                                        hideLoading()
                                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                        Toast.makeText(requireContext(), "Changing Password Failed!", Toast.LENGTH_SHORT).show()

                                    }
                                }
                        }
                    }
            }
            else{
                Toast.makeText(requireContext(), "Changing Password Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //This function shows a confirm deleting your account dialog when the "Delete Account" button is pressed
    //If the user clicks "Yes" the showDeleteDialog() is called else it closes the dialog.
    private fun showDeleteDialog(auth: FirebaseAuth) {
        val db = FirebaseFirestore.getInstance()
        val builder = AlertDialog.Builder(context, R.style.AlertDialogStyle)
        builder.setTitle("Confirm Deleting your Account")
            .setMessage(("Are you sure you want to delete your account?\n" +
                    "This action cannot be undone and the account will be permanently removed."))
            .setPositiveButton("Yes") {  _, _ ->
                deleteAccount(auth.currentUser!!, auth.currentUser!!.uid, db)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    //This function deletes the user permanently from the Firebase and all the items that he uploaded
    private fun deleteAccount(user: FirebaseUser, userId: String, db: FirebaseFirestore) {
        val categories: List<String> = listOf(
            "Jackets", "TshirtsBlouses", "Trousers",
            "Skirts", "Dresses", "Shorts", "Sportswear", "Accessories"
        )

        showLoading()

        //Delete the items this user uploaded
        for(category in categories){
            db.collection(category)
                .whereEqualTo("donateTo", "Individuals")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection(category)
                            .document(document.id)
                            .delete()
                    }
                }
        }

        //Delete the user
        db.collection("Users")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    db.collection("Users")
                        .document(document.id)
                        .delete()
                        .addOnSuccessListener {
                        }
                }
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Account Deleted Successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        hideLoading()
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
    }

    //Show the loading progress bar
    private fun showLoading(){
        binding.progressBar.visibility = View.VISIBLE
    }

    //Hide the loading progress bar
    private fun hideLoading(){
        binding.progressBar.visibility = View.INVISIBLE
    }
}