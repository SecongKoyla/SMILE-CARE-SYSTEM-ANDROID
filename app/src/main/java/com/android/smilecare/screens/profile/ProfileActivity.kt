package com.android.smilecare.screens.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.User
import com.android.smilecare.utils.disableEdgeToEdge
import com.android.smilecare.utils.toast

class ProfileActivity : AppCompatActivity(), ProfileContract.View {

    private lateinit var presenter: ProfileContract.Presenter

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
                // Some providers don't support persistable permissions; best-effort.
            }

            // Ensure the app can actually read this URI before persisting it.
            val canRead = try {
                contentResolver.openInputStream(uri)?.use { true } ?: false
            } catch (_: Exception) {
                false
            }

            if (!canRead) {
                toast("Unable to access selected photo. Please choose another image.")
                return@registerForActivityResult
            }

            presenter.onSetPhoto(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        presenter = ProfilePresenter(this, ProfileModel(application as CustomApp))

        findViewById<TextView>(R.id.textClose).setOnClickListener { finish() }

        findViewById<Button>(R.id.buttonSaveChanges).setOnClickListener {
            presenter.onSavePersonalInfo(
                findViewById<EditText>(R.id.editFirstName).text.toString(),
                findViewById<EditText>(R.id.editLastName).text.toString()
            )
        }

        val changePasswordBody = findViewById<LinearLayout>(R.id.layoutChangePasswordBody)
        findViewById<TextView>(R.id.textChangePasswordHeader).setOnClickListener {
            changePasswordBody.visibility = if (changePasswordBody.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        findViewById<Button>(R.id.buttonUpdatePassword).setOnClickListener {
            presenter.onUpdatePassword(
                findViewById<EditText>(R.id.editCurrentPassword).text.toString(),
                findViewById<EditText>(R.id.editNewPassword).text.toString(),
                findViewById<EditText>(R.id.editConfirmPassword).text.toString()
            )
        }

        findViewById<View>(R.id.cardProfilePhoto).setOnClickListener { showPhotoOptions() }
        findViewById<TextView>(R.id.textProfilePhotoHeader).setOnClickListener { showPhotoOptions() }

        presenter.load()

        // Optional navigation from menu: expand desired section.
        when (intent.getStringExtra(EXTRA_OPEN_SECTION)) {
            SECTION_CHANGE_PASSWORD -> changePasswordBody.visibility = View.VISIBLE
            SECTION_PROFILE_PHOTO -> showPhotoOptions()
        }
    }

    private fun showPhotoOptions() {
        AlertDialog.Builder(this)
            .setTitle("Profile Photo")
            .setItems(arrayOf("Upload Photo", "Remove Photo", "Cancel")) { dialog, which ->
                when (which) {
                    0 -> pickPhoto.launch(arrayOf("image/*"))
                    1 -> presenter.onRemovePhoto()
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    override fun showUser(user: User) {
        findViewById<TextView>(R.id.textFullName).text = "${user.firstName} ${user.lastName}".trim()
        findViewById<TextView>(R.id.textEmail).text = user.email

        findViewById<EditText>(R.id.editFirstName).setText(user.firstName)
        findViewById<EditText>(R.id.editLastName).setText(user.lastName)
        findViewById<EditText>(R.id.editEmail).setText(user.email)

        val image = findViewById<ImageView>(R.id.imageProfilePhoto)
        if (user.photoUri.isNotBlank()) {
            try {
                image.setImageURI(Uri.parse(user.photoUri))
            } catch (_: Exception) {
                image.setImageResource(R.drawable.profile)
                presenter.onRemovePhoto()
                toast("Saved profile photo can't be accessed. Please upload again.")
            }
        } else {
            image.setImageResource(R.drawable.profile)
        }
    }

    override fun showMessage(message: String) = toast(message)

    override fun closeScreen() = finish()

    override fun showChangePasswordExpanded(expanded: Boolean) {
        findViewById<View>(R.id.layoutChangePasswordBody).visibility = if (expanded) View.VISIBLE else View.GONE
    }

    override fun showPersonalInfoExpanded(expanded: Boolean) {
        // Personal info is always visible in this screen.
    }

    companion object {
        const val EXTRA_OPEN_SECTION = "open_section"
        const val SECTION_VIEW_PROFILE = "view_profile"
        const val SECTION_CHANGE_PASSWORD = "change_password"
        const val SECTION_PROFILE_PHOTO = "profile_photo"
    }
}
